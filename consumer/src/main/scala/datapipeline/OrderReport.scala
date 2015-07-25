package datapipeline

import java.util.UUID

import com.typesafe.config.ConfigFactory
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.SparkConf
import play.api.libs.json.Json

object OrderReport {

  // Model classes
  case class Order(id: UUID, lineItems: List[LineItem])
  case class LineItem(id: UUID, orderId: UUID, amount: String, currencyCode: String)
  implicit val lineItemFormat = Json.format[LineItem]
  implicit val orderFormat = Json.format[Order]

  // Consumer job
  def main(args: Array[String]): Unit = {
    val config = ConfigFactory.load()
    val kafkaZookeeperUrl = config.getString("kafka.zookeeper.url")
    val kafkaGroupId = config.getString("kafka.groupId")
    val cassandraHost = config.getString("cassandra.host")
    val sparkConf = new SparkConf(true)
      .setMaster(config.getString("spark.master.url"))
      .set("spark.cassandra.connection.host", cassandraHost)
      .setAppName("OrderReport")
    val ssc = new StreamingContext(sparkConf, Seconds(2))
    ssc.checkpoint("checkpoint")

    val orderStream = KafkaUtils.createStream(ssc, kafkaZookeeperUrl, kafkaGroupId, Map("order-topic" -> 1)).map(_._2)
    val lineItems = orderStream.flatMap(Json.parse(_).asOpt[Order]).flatMap(_.lineItems)
    // Store line items
    val lineItemTotals = lineItems.map(lineItem => (lineItem.orderId, lineItem.amount.toFloat))
      .reduceByKeyAndWindow(_ + _, _ - _, Seconds(30), Seconds(10))
    // Store Window

    lineItemTotals.print()
    ssc.start()
    ssc.awaitTermination()
  }
}

