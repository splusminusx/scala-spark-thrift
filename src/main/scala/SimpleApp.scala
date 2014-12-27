import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf
import java.io._
import org.apache.thrift.transport.TMemoryInputTransport
import org.apache.thrift.protocol._
import scala.collection.mutable
import scala.collection.mutable.HashMap
import stats.ThriftMessageHandler
import example._
import scala.collection.mutable.MutableList


/**
 * Десериализация вызовов Thrift методов.
 */
object SparkDeserialization {
  protected val protocolFactory = new TBinaryProtocol.Factory(true, true)
  protected val handlerMap = new HashMap[String, ThriftMessageHandler]
  handlerMap.put("getState", new ExampleGetStateHandler())

  def decode(request: Array[Byte]): (Int, String) = {
    val inputTransport = new TMemoryInputTransport(request)
    val iprot = protocolFactory.getProtocol(inputTransport)

    val msg = iprot.readMessageBegin()
    val func = handlerMap.get(msg.name)
    func match {
      case Some(fn) =>
        fn(msg.`type`, iprot, msg.seqid)
      case _ => (0, "")
    }
  }
}


object SimpleApp {
  def main(args: Array[String]) {
    val filename = "/home/stx/projects/scala-spark-thrift/thrift_method_call.bin"
    val bis = new BufferedInputStream(new FileInputStream(filename))
    val dis = new DataInputStream(bis)
    var data: mutable.MutableList[Array[Byte]] = new mutable.MutableList[Array[Byte]]()

    while (dis.available() != 0) {
      val packetLength = dis.readInt()
      if (dis.available() >= packetLength) {
        val buf = new Array[Byte](512)
        dis.read(buf, 0, packetLength)
        data = data :+ buf
      }
    }


    val conf = new SparkConf().setAppName("Simple Application").setMaster("local[4]")
    val sc = new SparkContext(conf)
    val logData = sc.makeRDD[Array[Byte]](data)

    /*
    val countMap = logData
      .map(line => line.split(":"))
      .map(lines => (lines(0).trim(), lines(1).trim()))
      .filter(pair => pair._1 == "ConversationCreation")
      .flatMap(pair => pair._2.split(","))
      .map(line => line.split("="))
      .map(lines => (lines(0).trim(), lines(1).trim()))
      .filter(pair => pair._1 == "visitor")
      .map(pair => (pair._2, 1))
      .reduceByKey((a, b) => a + b)

    countMap.foreach {
      case (k, v) => println("visitor: %s, conversation count: %s".format(k, v))
    }
    */

    val logLine = logData.map(SparkDeserialization.decode).reduceByKey((a, b) => a + "; " + b)
    logLine.foreach {
      case (seqId: Int, log: String) => println("SeqId=" + seqId + " : " + log)
    }
  }
}
