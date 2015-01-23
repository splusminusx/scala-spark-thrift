import livetex.io.stream.MessageInputStreamReader
import livetex.io.thrift.MessageCodec
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf
import java.io._
import org.apache.thrift.protocol.TMessage
import service.example.Example.{notify$result, notify$args, getState$result, getState$args}


/**
 * Десериализация вызовов Thrift методов.
 */
object SparkDeserialization {
  def main(args: Array[String]) {
    val filename = if(args.length > 0) args(0) else "/vagrant/thrift_method_call.bin"
    val stream = new DataInputStream(new BufferedInputStream(new FileInputStream(filename)))
    val reader = new MessageInputStreamReader(stream)
    var messages = List[Array[Byte]]()

    while (stream.available() != 0) {
      reader.readMessage() match {
        case Some(data) => messages ::= data
        case None =>
      }
    }

    val conf = new SparkConf().setAppName("Simple Application")
    val sc = new SparkContext(conf)
    val logs = sc.makeRDD[Array[Byte]](messages)

    val result = logs
      .mapPartitions(valueIterator => {
        val codec = new MessageCodec
        codec.buildMethodDecoder("getState", getState$args, getState$result)
        codec.buildMethodDecoder("notify", notify$args, notify$result)
        val transformedIterator = valueIterator.map(codec.decode)
        transformedIterator
      })
      .filter(x => x._1.name == "notify")
      .map[(String, (Int, Boolean))]({
        case (m: TMessage, notify$args(state, id)) => (id, (m.seqid, state))
        case _ => ("", (0, false))
      }).reduceByKey((a, b) => if (a._1 > b._1) a else b)
      .collect()

    result.foreach {
      case (id, (_, state)) => println("notify: id=" + id + " state=" + state)
      case _ =>
    }
  }
}
