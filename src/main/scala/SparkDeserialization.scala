import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf
import java.io._
import org.apache.thrift.transport.TMemoryInputTransport
import org.apache.thrift.protocol._
import scala.collection.mutable
import stats.ThriftMessageHandler
import example._


/**
 * Десериализация вызовов Thrift методов.
 */
object SparkDeserialization {
  protected val protocolFactory = new TBinaryProtocol.Factory(true, true)
  protected val handlerMap = new mutable.HashMap[String, ThriftMessageHandler]

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

  def main(args: Array[String]) {
    handlerMap.put("getState", new ExampleGetStateHandler())
    val filename = "/home/stx/projects/scala-spark-thrift/thrift_method_call.bin"
    val dis = new DataInputStream(new FileInputStream(filename))
    val data = Stream.continually({
      val packetLength = dis.readInt()
      val buf = new Array[Byte](packetLength)
      dis.read(buf, 0, packetLength)
      buf
    }).takeWhile(_ => dis.available() != 0).toList

    val conf = new SparkConf().setAppName("Simple Application")
    val sc = new SparkContext(conf)
    val logData = sc.makeRDD[Array[Byte]](data)

    val logLine = logData.map(decode).reduceByKey((a, b) => a + "; " + b)
    logLine.foreach {
      case (seqId: Int, log: String) => println("SeqId=" + seqId + " : " + log)
    }
  }
}
