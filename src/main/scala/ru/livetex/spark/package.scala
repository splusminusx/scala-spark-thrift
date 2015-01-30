package ru.livetex

import com.twitter.scrooge.ThriftStruct
import hdfslogger.io.BytesArrayWritable
import org.apache.hadoop.io.{BytesWritable, LongWritable}
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import ru.livetex.io.thrift._



package object spark {
  type CallReplay = (Timestamp, String, ThriftStruct, ThriftStruct)
  type RawCallReplay = (Timestamp, Array[RawMessage])

  def readMessages(sc: SparkContext, fullPath: String): RDD[RawCallReplay] = {
    sc.sequenceFile(fullPath, classOf[LongWritable], classOf[BytesArrayWritable])
    .map { case (x, y) => (x.get(), y.toArray.asInstanceOf[Array[BytesWritable]].map(_.getBytes)) }
  }
}
