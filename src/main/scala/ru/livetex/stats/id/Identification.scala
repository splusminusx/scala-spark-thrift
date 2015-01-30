package ru.livetex.stats.id


import org.apache.spark.rdd.RDD
import org.apache.spark.SparkContext._
import ru.livetex.io.thrift._
import ru.livetex.service.id.Identification.{getId$args, getId$result}
import ru.livetex.types.Error
import ru.livetex.spark._


object Identification {
  val DUMMY_KEY = ""
  val methodName = "getId"

  private def filterMethod(message: CallReplay): Boolean =
    message._2 == methodName

  private def decodeGetId(original: Iterator[RawCallReplay]): Iterator[CallReplay] = {
    val codec = new IteratorCodec
    codec.buildMethodDecoder(methodName, getId$args, getId$result)
    codec.decodeCallReplay(original)
  }

  private def extractGetIdData(messages: CallReplay): (Option[String], Option[Error]) =
    messages match {
      case (timestamp, name, getId$args(), getId$result(id, error)) => (id, error)
      case _ => (None, None)
    }

  def countGetIdCallById(rdd: RDD[RawCallReplay]): RDD[(String, Int)] = {
    rdd
      .mapPartitions(decodeGetId)
      .filter(filterMethod)
      .map(extractGetIdData)
      .filter(x => x._1.isDefined)
      .map(x => (x._1.getOrElse(DUMMY_KEY), 1))
      .reduceByKey((a, b) => a + b)
  }
}
