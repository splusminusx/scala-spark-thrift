package livetex.test

import org.apache.log4j.Level
import org.apache.spark.{SparkConf, SparkContext}
import org.scalatest.FunSuite


trait SparkTestUtils extends FunSuite {
  val conf = new SparkConf()
  var sc: SparkContext = _

  /**
   * Метод для описания тестов с использованием Spark. 
   *
   * @param name - имя теста.
   * @param silenceSpark - отключить логирование в стапрк.
   */
  def sparkTest(name: String, silenceSpark : Boolean = true)(body: => Unit) {
    test(name){
      val origLogLevels: Map[String, Level] = if (silenceSpark) SparkUtil.silenceSpark() else Map[String, Level]()
      sc = new SparkContext("local[4]", name, conf)

      try {
        body
      }
      finally {
        sc.stop()
        sc = null
        System.clearProperty("spark.master.port")
        if (silenceSpark) SparkUtil.restoreLogLevels(origLogLevels)
      }
    }
  }
}
