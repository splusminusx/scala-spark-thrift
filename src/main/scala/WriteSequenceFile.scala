import org.apache.hadoop.io.{LongWritable, Text}
import org.apache.hadoop.mapred.SequenceFileOutputFormat
import org.apache.spark._
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._


object WriteSequenceFile {
  def main(args: Array[String]): Unit = {
    val sc = new SparkContext("local[4]", "WriteSequenceFile")
    val outFile = "hdfs://hadoop:8020/spark/test"
    val messages = Array("1232323", "23345", "73232890", "23345").map(m => (m.length, m))

    val output = sc.makeRDD[(Int, String)](messages)
    output.saveAsSequenceFile(outFile)
  }
}
