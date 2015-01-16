import com.twitter.scrooge.{ThriftStructCodec3, ThriftStruct}
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf
import java.io._
import org.apache.thrift.TException
import org.apache.thrift.transport.TMemoryInputTransport
import org.apache.thrift.protocol._
import service.example.Example.{getState$result, getState$args}
import scala.collection.mutable


/**
 * Десериализация вызовов Thrift методов.
 */
object SparkDeserialization {
  protected val defaultFilename = "/home/stx/work/projects/Offline-Service-Statistics-Example/thrift_method_call.bin"
  protected val protocolFactory = new TBinaryProtocol.Factory(true, true)
  protected val handlerMap = new mutable.HashMap[String, (TMessage, TProtocol) => ThriftStruct]

  def decode(request: Array[Byte]): (TMessage, ThriftStruct) = {
    val inputTransport = new TMemoryInputTransport(request)
    val iprot = protocolFactory.getProtocol(inputTransport)
    try {
      val msg = iprot.readMessageBegin()
      handlerMap.get(msg.name) match {
        case Some(fn) =>
          (msg, fn(msg, iprot))
        case _ =>
          (msg, null)
      }
    } catch {
      case e: TException => (null, null)
    }
  }

  def buildDecoder[A <: ThriftStruct, R <: ThriftStruct](args: ThriftStructCodec3[A], result: ThriftStructCodec3[R]): (TMessage, TProtocol) => ThriftStruct =
    (message, protocol) => {
      message.`type` match {
        case TMessageType.CALL => args.decode(protocol)
        case TMessageType.REPLY => result.decode(protocol)
      }
    }

  def buildCallReducer[A,R](): ((Option[ThriftStruct], Option[ThriftStruct]), (Option[ThriftStruct], Option[ThriftStruct])) => (Option[A], Option[R]) = {
    case ((Some(x), None), (Some(y), None)) => (x, y) match {
      case (a: A, r: R) => (Some(a), Some(r))
      case (r: R, a: A) => (Some(a), Some(r))
      case _ => (None: Option[A], None: Option[R])
    }
    case ((Some(a: A), Some(r: R)), _) => (Some(a), Some(r))
    case (_, (Some(a: A), Some(r: R))) => (Some(a), Some(r))
    case _ => (None: Option[A], None: Option[R])
  }

  def main(args: Array[String]) {
    val methodName = "getState"
    val filename = args.length match {
      case 0 => defaultFilename
      case _ => args(0)
    }
    handlerMap.put(methodName, buildDecoder(getState$args, getState$result))
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

    val calls = logData
      .map(decode)
      .filter(x => x._1.name == methodName)
      .map(x => (x._1.seqid, (Some(x._2): Option[ThriftStruct], None: Option[ThriftStruct])))
      .reduceByKey(buildCallReducer[getState$args, getState$result]())

    calls.foreach({
      case (seqId, (Some(a: getState$args), Some(r: getState$result))) => println("getState: id=" + a.id + ", r=" + r.success)
      case _ =>
    })
  }
}
