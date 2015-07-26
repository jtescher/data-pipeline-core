package datapipeline.config

import com.typesafe.config.ConfigFactory
import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}

object SparkConfig {

  val config = ConfigFactory.load()

  val sparkConf = new SparkConf()
    .set("spark.cassandra.connection.host", config.getString("cassandra.host"))
    .setMaster(config.getString("spark.master-url"))
    .setAppName(config.getString("spark.app-name"))

  val streamingContext = new StreamingContext(conf = sparkConf, batchDuration = Seconds(2))

}
