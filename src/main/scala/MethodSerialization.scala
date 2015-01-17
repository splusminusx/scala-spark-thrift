import java.io._
import livetex.io.thrift.{MessageCodec => codec}
import livetex.io.stream.MessageStream
import service.example.Example.{getState$args, getState$result, getUnsafeState$args, getUnsafeState$result, notify$args, notify$result}


/**
 * Сериализация вызовов Thrift методов.
 */
object MethodSerialization {
  def main(args: Array[String]): Unit = {
    val filename = "/home/stx/thrift_method_call.bin"
    val initialState = false
    val finalState = true
    val stream = new DataOutputStream(new FileOutputStream(filename))

    for (id <- Iterator.range(0, 200)) {
      val buf = codec.encodeCall("getState", id, getState$args(id.toString))
      MessageStream.write(stream, buf)
    }

    for (id <- Iterator.range(0, 100)) {
      val buf = codec.encodeReplay("getState", id, getState$result(success = Some(true)))
      MessageStream.write(stream, buf)
    }

    for (id <- Iterator.range(0, 10)) {
      val buf = codec.encodeCall("notify", id, notify$args(initialState, id.toString))
      MessageStream.write(stream, buf)
    }

    for (id <- Iterator.range(0, 10)) {
      val buf = codec.encodeCall("notify", id + 100, notify$args(finalState, id.toString))
      MessageStream.write(stream, buf)
    }

    for (id <- Iterator.range(0, 100)) {
      val buf = codec.encodeReplay("notify", id, notify$result())
      MessageStream.write(stream, buf)
    }
  }
}
