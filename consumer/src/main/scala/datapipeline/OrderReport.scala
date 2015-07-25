package datapipeline

import java.util.UUID

import com.typesafe.config.ConfigFactory
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.kafka.KafkaUtils
import com.datastax.spark.connector.streaming._
import org.apache.spark.SparkConf
import play.api.libs.json.Json

object OrderReport {

  // Model classes
  case class Order(id: UUID, eventId: Int, lineItems: List[LineItem])
  case class LineItem(id: UUID, orderId: UUID, amount: String, currencyCode: String)

  // Model serialization
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

    val ordersJson = KafkaUtils.createStream(ssc, kafkaZookeeperUrl, kafkaGroupId, Map("order-topic" -> 1)).map(_._2)
    val orders = ordersJson.flatMap(Json.parse(_).asOpt[Order])

    // Save all line items as they stream in
    val lineItems = orders.flatMap(_.lineItems)
    lineItems.saveToCassandra("reports", "line_items")

    // Save per event totals as they stream in
    val eventTotals = orders.map(order => (order.eventId, order.lineItems.map(_.amount.toFloat).sum)).reduceByKey(_ + _)
    eventTotals.saveToCassandra("reports", "event_totals")

    ssc.start()
    ssc.awaitTermination()
  }
}

