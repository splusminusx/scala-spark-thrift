import java.io._
import livetex.io.thrift.MessageCodec
import livetex.io.stream.MessageOutputStreamWriter
import service.example.Example.{getState$args, getState$result, notify$args, notify$result}


/**
 * Сериализация вызовов Thrift методов.
 */
object MessageSerialization {
  def main(args: Array[String]): Unit = {
    val codec = new MessageCodec
    val filename = if(args.length > 0) args(0) else "/home/stx/thrift_method_call.bin"
    val initialState = false
    val finalState = true
    val stream = new DataOutputStream(new FileOutputStream(filename))
    val writer = new MessageOutputStreamWriter(stream)

    for (id <- Iterator.range(0, 100)) {
      writer(codec.encodeCall("getState", id, getState$args(id.toString)))
    }

    for (id <- Iterator.range(0, 100)) {
      writer(codec.encodeReplay("getState", id, getState$result(success = Some(true))))
    }

    for (id <- Iterator.range(0, 10)) {
      writer(codec.encodeCall("notify", id, notify$args(initialState, id.toString)))
    }

    for (id <- Iterator.range(0, 10)) {
      writer(codec.encodeCall("notify", id + 100, notify$args(finalState, id.toString)))
    }

    for (id <- Iterator.range(0, 100)) {
      writer(codec.encodeReplay("notify", id, notify$result()))
    }
  }
}
