import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf
import java.io._
import org.apache.thrift.transport.TMemoryInputTransport
import org.apache.thrift.protocol._
import scala.collection.mutable
import stats.{ThriftMessage, ThriftMessageHandler, NullMessage}
import example._


/**
 * Десериализация вызовов Thrift методов.
 */
object SparkDeserialization {
  protected val protocolFactory = new TBinaryProtocol.Factory(true, true)
  protected val handlerMap = new mutable.HashMap[String, ThriftMessageHandler]

  def decode(request: Array[Byte]): ThriftMessage = {
    val inputTransport = new TMemoryInputTransport(request)
    val iprot = protocolFactory.getProtocol(inputTransport)

    val msg = iprot.readMessageBegin()
    val func = handlerMap.get(msg.name)
    func match {
      case Some(fn) =>
        fn(msg, iprot)
      case _ => NullMessage
    }
  }

  def main(args: Array[String]) {
    handlerMap.put("getState", new ExampleGetStateHandler())
    val dis = new DataInputStream(new FileInputStream(args(0)))
    val data = Stream.continually({
      val packetLength = dis.readInt()
      val buf = new Array[Byte](packetLength)
      dis.read(buf, 0, packetLength)
      buf
    }).takeWhile(_ => dis.available() != 0).toList

    val conf = new SparkConf().setAppName("Simple Application")
    val sc = new SparkContext(conf)
    val logData = sc.makeRDD[Array[Byte]](data)

    /*
    val logLine = logData.map(decode).map(m => (m.seqId, m)).reduceByKey((a, b) => {
      (a, b) match {
        case (ExampleGetStateCall(seqId, id), ExampleGetStateResult(_, success)) => ExampleGetState(seqId, id, success)
        case (ExampleGetStateResult(_, success), ExampleGetStateCall(seqId, id)) => ExampleGetState(seqId, id, success)
        case (ExampleGetState(seqId, id, success), _) => ExampleGetState(seqId, id, success)
        case (_, ExampleGetState(seqId, id, success)) => ExampleGetState(seqId, id, success)
      }
    })
    logLine.foreach {
      case (seqId, ExampleGetState(_,id, Some(success))) => println("SeqId=" + seqId + " : id=" + id.toString + " success=" + success.toString)
      case (seqId, _) => println("SeqId=" + seqId + " : without result")
    }
    */
  }
}
