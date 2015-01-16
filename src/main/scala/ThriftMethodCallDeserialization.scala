import java.io._
import org.apache.thrift.TException
import org.apache.thrift.transport.TMemoryInputTransport
import org.apache.thrift.protocol._
import scala.collection.mutable
import com.twitter.scrooge.{ThriftStructCodec3, ThriftStruct}
import service.example.Example.{getState$args, getState$result}


/**
 * Десериализация вызовов Thrift методов.
 */
object ThriftMethodCallDeserialization {
  val defaultFilename = "/home/stx/work/projects/Offline-Service-Statistics-Example/thrift_method_call.bin"
  val protocolFactory = new TBinaryProtocol.Factory(true, true)
  val handlerMap = new mutable.HashMap[String, (TMessage, TProtocol) => ThriftStruct]

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

  def readMessage(dis: DataInputStream): Option[Array[Byte]] = {
    val packetLength = dis.readInt()
    if (dis.available() >= packetLength) {
      val buf = new Array[Byte](packetLength)
      dis.read(buf, 0, packetLength)
      Some(buf)
    } else{
      None
    }
  }

  def buildDecoder[A <: ThriftStruct, R <: ThriftStruct](args: ThriftStructCodec3[A],
                                                         result: ThriftStructCodec3[R]): (TMessage, TProtocol) => ThriftStruct =
    (message, protocol) => {
      message.`type` match {
        case TMessageType.CALL => args.decode(protocol)
        case TMessageType.REPLY => result.decode(protocol)
      }
    }

  def main(args: Array[String]): Unit = {
    val filename = args.length match {
      case 0 => defaultFilename
      case _ => args(0)
    }

    handlerMap.put("getState", buildDecoder(getState$args, getState$result))

    val dis = new DataInputStream(new FileInputStream(filename))
    val data = Stream.continually({
      val packetLength = dis.readInt()
      val buf = new Array[Byte](packetLength)
      dis.read(buf, 0, packetLength)
      buf
    }).takeWhile(_ => dis.available() != 0).toList

    data
      .map(decode)
      .filter(x => x._1.name == "getState")
      .map(x => (x._1.seqid, x))
  }
}
