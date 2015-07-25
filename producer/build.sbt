name := """data-pipeline-producer"""

version := "1.0"

scalaVersion := "2.11.7"

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

// Change this to another test framework if you prefer
libraryDependencies ++= Seq(
  "org.apache.kafka" %% "kafka" % "0.8.2.1",
  "org.apache.spark" %% "spark-core" % "1.4.1",
  "org.apache.spark" %% "spark-streaming" % "1.4.1",
  "org.apache.spark" %% "spark-streaming-kafka" % "1.4.1",
  "com.typesafe.play" %% "play-json" % "2.4.2",
  "com.typesafe" % "config" % "1.3.0",
  "org.scalatest" %% "scalatest" % "2.2.4" % "test"
)