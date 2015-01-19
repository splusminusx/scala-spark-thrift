name := "Offline-Service-Statistics-Example"

version := "1.0"

organization := "livetex"

scalaVersion := "2.10.4"

com.twitter.scrooge.ScroogeSBT.newSettings

scalaVersion := "2.10.4"

resolvers += "Sonatype Nexus Repository Manager" at
  "http://sonatype-nexus.livetex.ru/nexus/content/groups/public"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "1.1.1" % "provided",
  "org.apache.thrift" % "libthrift" % "0.8.0",
  "com.twitter" %% "scrooge-core" % "3.16.3",
  "com.twitter" %% "finagle-thrift" % "6.5.0",
  "livetex" %% "livetex-interfaces" % "1.0-SNAPSHOT" withSources(),
  "org.scalatest" % "scalatest_2.10" % "2.0" % "test"
)
