import org.apache.spark.{SparkConf, SparkContext}
import org.elasticsearch.spark._
import org.elasticsearch.hadoop.cfg.ConfigurationOptions


object IdentificationIndex {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf()
    conf.set(ConfigurationOptions.ES_HOST, "localhost")
    val sc = new SparkContext(conf)

    val numbers = Map("id" -> "person:111", "attribute" -> "test")
    val airports = Map("OTP" -> "Otopeni", "SFO" -> "San Fran")

    sc.makeRDD(Seq(numbers, airports)).saveToEs("test/docs")
  }
}
