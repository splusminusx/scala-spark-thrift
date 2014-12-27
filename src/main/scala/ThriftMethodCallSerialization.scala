import java.io._
import com.twitter.scrooge.ThriftStruct
import org.apache.thrift.transport.{TTransport, TMemoryBuffer}
import org.apache.thrift.protocol.{TMessageType, TMessage, TBinaryProtocol}
import service.example.Example.{getState$args, getState$result}


/**
 * Сериализация вызовов Thrift методов.
 */
object ThriftMethodCallSerialization {
  protected val protocolFactory = new TBinaryProtocol.Factory(true, true)

  def encodeCall(name: String, args: ThriftStruct, buf: TTransport, seqId: Int): Unit = {
    encodeMessage(name, new TMessage(name, TMessageType.CALL, seqId), args, buf)
  }

  def encodeReplay(name: String, args: ThriftStruct, buf: TTransport, seqId: Int): Unit = {
    encodeMessage(name, new TMessage(name, TMessageType.REPLY, seqId), args, buf)
  }

  def encodeMessage(name: String, message: TMessage, args: ThriftStruct, buf: TTransport): Unit = {
   val oprot = protocolFactory.getProtocol(buf)

    oprot.writeMessageBegin(message)
    args.write(oprot)
    oprot.writeMessageEnd()
  }

  def main(args: Array[String]): Unit = {
    val filename = "/home/stx/projects/scala-spark-thrift/thrift_method_call.bin"
    val stream = new DataOutputStream(new FileOutputStream(filename))

    // Thrift Method name
    val name = "getState"

    for (id <- Iterator.range(0, 2000)) {
      // encode data
      val buf = new TMemoryBuffer(512)
      val args = getState$args(id.toString)
      encodeCall(name, args, buf, id)
      stream.writeInt(buf.length)
      stream.write(buf.getArray, 0, buf.length)
    }

    for (id <- Iterator.range(0, 1000)) {
      // encode data
      val buf = new TMemoryBuffer(512)
      val args = getState$result(success = Some(true))
      encodeReplay(name, args, buf, id)
      stream.writeInt(buf.length)
      stream.write(buf.getArray, 0, buf.length)
    }
  }
}
