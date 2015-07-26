package datapipeline

import java.util.UUID
import com.datastax.spark.connector.streaming._
import datapipeline.config.{SparkConfig, KafkaConfig}
import play.api.libs.json.Json
import SparkConfig.streamingContext

object OrderReport {

  // Model classes
  case class Order(id: UUID, eventId: Int, lineItems: List[LineItem])
  case class LineItem(id: UUID, orderId: UUID, amount: String, currencyCode: String)

  // Model serialization
  implicit val lineItemFormat = Json.format[LineItem]
  implicit val orderFormat = Json.format[Order]

  def main(args: Array[String]): Unit = {

    // Checkpoint
    streamingContext.checkpoint("checkpoint")

    // Create order stream from kafka topic
    val ordersJson = KafkaConfig.createStream(streamingContext, topics = Map("order-topic" -> 1))
    val orders = ordersJson.flatMap(Json.parse(_).asOpt[Order])

    // Save all line items as they stream in
    val lineItems = orders.flatMap(_.lineItems)
    lineItems.saveToCassandra("reports", "line_items")

    // Save per event totals as they stream in
    val eventTotals = orders.map(order => (order.eventId, order.lineItems.map(_.amount.toFloat).sum)).reduceByKey(_ + _)
    eventTotals.saveToCassandra("reports", "event_totals")

    // Start streaming
    streamingContext.start()
    streamingContext.awaitTermination()
  }

}
