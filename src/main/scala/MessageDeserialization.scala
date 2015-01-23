import java.io._
import com.twitter.scrooge.ThriftStruct
import livetex.io.thrift.MessageCodec
import livetex.io.stream.MessageInputStreamReader
import org.apache.thrift.protocol.TMessage
import service.example.Example.{getState$args, getState$result, notify$args, notify$result}


/**
 * Десериализация вызовов Thrift методов.
 */
object MessageDeserialization {
  def main(args: Array[String]): Unit = {
    val codec = new MessageCodec
    codec.buildMethodDecoder("getState", getState$args, getState$result)
    codec.buildMethodDecoder("notify", notify$args, notify$result)

    val filename = if(args.length > 0) args(0) else "/home/stx/thrift_method_call.bin"
    val stream = new DataInputStream(new BufferedInputStream(new FileInputStream(filename)))
    val reader = new MessageInputStreamReader(stream)
    var messages = List[(TMessage, ThriftStruct)]()

    while (stream.available() != 0) {
      reader.readMessage() match {
        case Some(data) => messages ::= codec.decode(data)
        case _ =>
      }
    }

    messages.reverse.foreach {
      case (null, null) =>
      case (m: TMessage, s: ThriftStruct) => println(m.seqid)
        s match {
          case getState$args(id) => println("Method \"getState\" was called with id = " + id)
          case getState$result(success) => println("Method \"getState\" returns " + success)
          case notify$args(state, id) => println("Method \"notify\" was called with id = " + id + " and state = " + state)
          case _ =>
        }
    }
  }
}
