import com.twitter.scrooge.ThriftStruct
import livetex.io.stream.MessageStream
import livetex.io.thrift.{MessageCodec => codec}
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf
import java.io._

import org.apache.spark.rdd.RDD
import org.apache.thrift.protocol.TMessage
import service.example.Example.{notify$result, notify$args, getState$result, getState$args}


/**
 * Десериализация вызовов Thrift методов.
 */
object SparkDeserialization {
  def main(args: Array[String]) {
    codec.buildMethodDecoder("getState", getState$args, getState$result)
    codec.buildMethodDecoder("notify", notify$args, notify$result)

    val filename = "/home/stx/thrift_method_call.bin"
    val stream = new DataInputStream(new BufferedInputStream(new FileInputStream(filename)))
    var messages = List[Array[Byte]]()

    while (stream.available() != 0) {
      val data = MessageStream.read(stream)
      if (data.length != 0) {
        messages ::= data
      }
    }

    val conf = new SparkConf().setAppName("Simple Application")
    val sc = new SparkContext(conf)
    val logs = sc.makeRDD[Array[Byte]](messages)

    val result = logs
      .map(codec.decode)
      .filter(x => x._1.name == "notify")
      .map[(String, (Int, Boolean))]({
        case (m: TMessage, notify$args(state, id)) => (id, (m.seqid, state))
        case _ => ("", (0, false))
      }).reduceByKey((a, b) => if (a._1 > b._1) a else b)

    result.foreach {
      case (id, (_, state)) => println("notify: id=" + id + " state=" + state)
      case _ =>
    }
  }
}
