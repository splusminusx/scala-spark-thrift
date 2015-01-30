name := "Offline-Service-Statistics-Example"

version := "1.0"

organization := "livetex"

scalaVersion := "2.10.4"

resolvers += "Hortonworks Releases" at "http://repo.hortonworks.com/content/repositories/releases/"

resolvers += "Hortonworks Jetty" at "http://repo.hortonworks.com/content/repositories/jetty-hadoop/"

resolvers += "Sonatype Nexus Repository Manager" at
  "http://sonatype-nexus.livetex.ru/nexus/content/groups/public"

libraryDependencies ++= Seq(
  ("livetex" %% "hdfs-logger" % "0.0.4-SNAPSHOT")
    .exclude("org.apache.hadoop", "hadoop-client"),
  "org.apache.spark" %% "spark-core" % "1.2.0.2.2.0.0-82" % "provided",
  "livetex" %% "livetex-interfaces" % "0.0.1-SNAPSHOT" withSources(),
  "livetex" %% "message-codec" % "1.0.4-SNAPSHOT" withSources(),
  "org.scalatest" % "scalatest_2.10" % "2.0" % "test"
)
