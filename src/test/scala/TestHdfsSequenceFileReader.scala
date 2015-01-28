import livetex.test.SparkTestUtils
import org.apache.hadoop.io.{LongWritable, Text}

class TestHdfsSequenceFileReader extends SparkTestUtils {
  sparkTest("read from sequence file") {
    val data = sc.sequenceFile("hdfs://172.17.0.4:9000/spark/test", classOf[LongWritable], classOf[Text])
    val results = data.map(x => x._1).collect()

    results.foreach(println(_))
  }
}
