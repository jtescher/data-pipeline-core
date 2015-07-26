package datapipeline.config

import com.typesafe.config.ConfigFactory
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.kafka.KafkaUtils

object KafkaConfig {

  private val config = ConfigFactory.load()

  private val zookeeperUrl = config.getString("kafka.zookeeper.url")
  private val groupId = config.getString("kafka.groupId")

  def createStream(streamingContext: StreamingContext, topics: Map[String, Int]): DStream[String] = {
    KafkaUtils.createStream(streamingContext, zookeeperUrl, groupId, topics).map(_._2)
  }

}
