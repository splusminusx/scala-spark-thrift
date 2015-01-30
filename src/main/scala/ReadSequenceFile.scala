import org.apache.spark._
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.hadoop.io.{BytesWritable, LongWritable}
import hdfslogger.io.BytesArrayWritable


object ReadSequenceFile {
  def main(args: Array[String]): Unit = {
    val sc = new SparkContext("local[4]", "ReadSequenceFile")
    val inFile = "hdfs://hadoop:8020/spark"

    val data = sc.sequenceFile(inFile, classOf[LongWritable], classOf[BytesArrayWritable]).map{case (x, y) =>
      (x.get(), y.toArray.asInstanceOf[Array[BytesWritable]].length)}
    println(data.collect().toList)
  }
}
