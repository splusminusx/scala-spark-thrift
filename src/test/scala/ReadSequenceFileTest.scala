import hdfslogger.HDFSLogger
import livetex.test.SparkTestUtils
import org.scalatest.Matchers
import ru.livetex.io.codec.PacketType._
import ru.livetex.io.thrift.CallReplayCodec
import ru.livetex.spark._
import ru.livetex.service.id.Identification.{getId$result, getId$args}
import ru.livetex.stats.id.Identification
import scala.util.Random


class ReadSequenceFileTest extends SparkTestUtils with Matchers {
  def createGetIdLog(logger: HDFSLogger , messageCount: Int, idPrefix: String, idWithTwoRequests: String) {
    val methodName = "getId"
    val codec = new CallReplayCodec
    codec.buildMethodDecoder(methodName, getId$args, getId$result)

    for (id <- Iterator.range(0, messageCount)) {
      logger.log(Some((BINARY_TYPE,
        codec.encode(methodName, id, getId$args(), getId$result(Some(idPrefix + id.toString), None)).toSeq)))
    }
    logger.log(Some((BINARY_TYPE,
      codec.encode(methodName, messageCount + 1, getId$args(), getId$result(Some(idWithTwoRequests), None)).toSeq)))

    logger.close()
  }

  sparkTest("It should read data from HDFS") {
    val hadoopUrl = "hdfs://hadoop:8020"
    val path = "/spark/" + Random.alphanumeric.take(10).mkString
    val logger = new HDFSLogger(hadoopUrl, path, 3600)

    val messageCount = 10
    val idPrefix = "person:"
    val idWithTwoRequests = idPrefix + 1

    createGetIdLog(logger, messageCount, idPrefix, idWithTwoRequests)

    val data = Identification.countGetIdCallById(readMessages(sc, hadoopUrl + path)).collect()

    println(data.toList)

    data should have length messageCount
    data should contain (idWithTwoRequests, 2)
  }
}
