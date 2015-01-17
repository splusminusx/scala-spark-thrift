import java.io._
import com.twitter.scrooge.ThriftStruct
import livetex.io.thrift.{ MessageCodec => codec }
import livetex.io.stream.MessageStream
import org.apache.thrift.protocol.TMessage
import service.example.Example.{getState$args, getState$result, notify$args, notify$result}


/**
 * Десериализация вызовов Thrift методов.
 */
object MethodDeserialization {
  def main(args: Array[String]): Unit = {
    codec.buildMethodDecoder("getState", getState$args, getState$result)
    codec.buildMethodDecoder("notify", notify$args, notify$result)

    val filename = "/home/stx/thrift_method_call.bin"
    val stream = new DataInputStream(new BufferedInputStream(new FileInputStream(filename)))
    var messages = List[(TMessage, ThriftStruct)]()

    while (stream.available() != 0) {
      val data = MessageStream.read(stream)
      if (data.length != 0) {
        messages ::= codec.decode(data)
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

    println(messages.length.toString)
  }
}
