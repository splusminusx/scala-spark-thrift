name := "Offline-Service-Statistics-Example"

version := "1.0"

scalaVersion := "2.10.4"

libraryDependencies += "org.apache.spark" %% "spark-core" % "1.1.1" % "provided"

com.twitter.scrooge.ScroogeSBT.newSettings

scalaVersion := "2.10.4"

libraryDependencies ++= Seq(
  "org.apache.thrift" % "libthrift" % "0.8.0",
  "com.twitter" %% "scrooge-core" % "3.16.3",
  "com.twitter" %% "finagle-thrift" % "6.5.0"
)
