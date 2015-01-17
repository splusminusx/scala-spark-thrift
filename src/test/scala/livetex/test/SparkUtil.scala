package livetex.test

import org.apache.log4j.{Level, Logger}


/**
 * Утилиты для работы со Spark.
 */
object SparkUtil {
   /**
    * Отключить логирование в Spark
    */
   def silenceSpark(): Map[String, Level] = {
     setLogLevels(Level.WARN, Seq("spark", "org.eclipse.jetty", "akka"))
   }

   /**
    * Установить уровень логирования для логгеров.
    *
    * @param level - уровень логирования.
    * @param loggers - именя логгеров.
    */
   def setLogLevels(level: Level, loggers: TraversableOnce[String]): Map[String, Level] = {
     loggers.map{
       loggerName =>
         val logger = Logger.getLogger(loggerName)
         val prevLevel = logger.getLevel
         logger.setLevel(level)
         loggerName -> prevLevel
     }.toMap
   }

   /**
    * Восставносить первоначальный уровень логирования для логгеров.
    *
    * @param origLogLevels - первоначальный уровень логирования.
    */
   def restoreLogLevels(origLogLevels: Map[String, Level]): Unit = {
     origLogLevels.foreach {
       case (loggerName, level) =>
         Logger.getLogger(loggerName).setLevel(level)
       case _ =>
     }
   }
 }
