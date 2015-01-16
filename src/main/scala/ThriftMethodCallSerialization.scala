import java.io._
import com.twitter.scrooge.ThriftStruct
import org.apache.thrift.transport.TMemoryBuffer
import org.apache.thrift.protocol.{TMessageType, TMessage, TBinaryProtocol}
import service.example.Example.{getState$args, getState$result}
import livetex.service


/**
 * Сериализация вызовов Thrift методов.
 */
object ThriftMethodCallSerialization {
  val defaultFilename = "/home/stx/work/projects/Offline-Service-Statistics-Example/thrift_method_call.bin"
  val protocolFactory = new TBinaryProtocol.Factory(true, true)

  def encodeCall(name: String, seqId: Int, data: ThriftStruct): TMemoryBuffer = {
    encodeMessage(new TMessage(name, TMessageType.CALL, seqId), data)
  }

  def encodeReplay(name: String, seqId: Int, data: ThriftStruct): TMemoryBuffer = {
    encodeMessage(new TMessage(name, TMessageType.REPLY, seqId), data)
  }

  def encodeMessage(message: TMessage, args: ThriftStruct): TMemoryBuffer = {
    val buf = new TMemoryBuffer(512)
    val oprot = protocolFactory.getProtocol(buf)

    oprot.writeMessageBegin(message)
    args.write(oprot)
    oprot.writeMessageEnd()
    buf
  }

  def writeLog(buf: TMemoryBuffer)(implicit stream: DataOutputStream): Unit = {
    stream.writeInt(buf.length)
    stream.write(buf.getArray, 0, buf.length)
  }

  def main(args: Array[String]): Unit = {
    val filename = args.length match {
      case 0 => defaultFilename
      case _ => args(0)
    }
    implicit val stream = new DataOutputStream(new FileOutputStream(filename))
    val name = "getState"

    for (id <- Iterator.range(0, 2000)) {
      writeLog(encodeCall(name, id, getState$args(id.toString)))
    }

    for (id <- Iterator.range(0, 1000)) {
      writeLog(encodeReplay(name, id, getState$result(success = Some(true))))
    }
  }
}
