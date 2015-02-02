import hdfslogger.HDFSLogger
import livetex.test.SparkTestUtils
import org.scalatest.Matchers
import ru.livetex.io.codec.PacketType._
import ru.livetex.io.thrift.CallReplayCodec
import ru.livetex.spark._
import ru.livetex.service.id.Identification.{getId$result, getId$args}
import ru.livetex.stats.id.{Identification, Person}
import scala.util.Random
import org.elasticsearch.hadoop.cfg.ConfigurationOptions
import org.apache.spark.sql._
import org.elasticsearch.spark.sql._


class ReadSequenceFileTest extends SparkTestUtils with Matchers {
  conf.set(ConfigurationOptions.ES_NODES, "localhost")

  def createGetIdLog(logger: HDFSLogger , messageCount: Int,
                     idPrefix: String, idWithTwoRequests: String) {
    val methodName = "getId"
    val codec = new CallReplayCodec
    codec.buildMethodDecoder(methodName, getId$args, getId$result)

    for (id <- Iterator.range(0, messageCount)) {
      logger.log(Some((BINARY_TYPE,
        codec.encode(methodName, id, getId$args(),
          getId$result(Some(idPrefix + id.toString), None)).toSeq)))
    }
    logger.log(Some((BINARY_TYPE,
      codec.encode(methodName, messageCount + 1, getId$args(),
        getId$result(Some(idWithTwoRequests), None)).toSeq)))

    logger.close()
  }

  sparkTest("It should read data from HDFS") {
    val sqlCtx = new SQLContext(sc)
    val hadoopUrl = "hdfs://hadoop:8020"
    val path = "/spark/" + Random.alphanumeric.take(10).mkString
    val logger = new HDFSLogger(hadoopUrl, path, 3600)

    val messageCount = 100000
    val idPrefix = "person:"
    val idWithTwoRequests = idPrefix + 1

    createGetIdLog(logger, messageCount, idPrefix, idWithTwoRequests)

    val data = Identification.countGetIdCallById(readMessages(sc, hadoopUrl + path))
    sqlCtx.createSchemaRDD(data).saveToEs("test/ReadSequenceFileTest")

    val result = data.collect()
    println(result.toList)

    result should have length messageCount
    result should contain (new Person(idWithTwoRequests, 2))
  }
}
