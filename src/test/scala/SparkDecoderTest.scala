import livetex.io.thrift.MessageCodec
import livetex.test.SparkTestUtils
import org.apache.spark.SparkContext._
import org.apache.thrift.protocol.TMessage
import service.example.Example.{notify$args, notify$result}


class SparkDecoderTest extends SparkTestUtils {
  sparkTest("Spark decoder should decode Thrift messages") {
    // parameters
    val codec = new MessageCodec
    val name = "notify"
    val initialState = false
    val finalState = true
    val messageCount = 10

    // build decoder
    codec.buildMethodDecoder(name, notify$args, notify$result)

    // generate messages
    var messages = List[Array[Byte]]()
    for (id <- Iterator.range(0, messageCount)) {
      messages ::= codec.encodeCall(name, id, notify$args(initialState, id.toString))
      messages ::= codec.encodeCall(name, id + 100, notify$args(finalState, id.toString))
    }

    // aggregate result
    val result = sc.makeRDD[Array[Byte]](messages)
      .mapPartitions(valueIterator => {
        val codec = new MessageCodec
        codec.buildMethodDecoder(name, notify$args, notify$result)
        val transformedIterator = valueIterator.map(codec.decode)
        transformedIterator
      })
      .filter(x => x._1.name == name)
      .map[(String, (Int, Boolean))]({
        case (m: TMessage, notify$args(state, id)) => (id, (m.seqid, state))
        case _ => ("", (0, false))
      })
      .reduceByKey((a, b) => if (a._1 > b._1) a else b)
      .collect()

    // check result
    result.foreach {
      case (id, (_, state)) => assert(state == finalState, "Final state should be " + finalState)
    }
    assert(result.length == messageCount, "Spark must collect " + messageCount + " messages")
  }
}
