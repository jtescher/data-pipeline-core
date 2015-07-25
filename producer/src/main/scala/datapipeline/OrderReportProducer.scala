package datapipeline

import java.util
import java.util.UUID
import com.typesafe.config.ConfigFactory
import org.apache.kafka.clients.producer.{ProducerConfig, ProducerRecord, KafkaProducer}
import play.api.libs.json.Json

import scala.util.Random

object OrderReportProducer {
  def main(args: Array[String]): Unit = {
    val config = ConfigFactory.load()

    val kafkaZookeeperBrokerUrl = config.getString("kafka.zookeeper.broker.url")
    val kafkaTopic = config.getString("kafka.topic")
    val messagesPerSec = config.getInt("messages-per-second")

    implicit val lineItemFormat = Json.format[LineItem]
    implicit val orderFormat = Json.format[Order]

    // Zookeeper connection properties
    val props = new util.HashMap[String, Object]()
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaZookeeperBrokerUrl)
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")

    val producer = new KafkaProducer[String, String](props)

    // Send some messages
    while(true) {
      (1 to messagesPerSec).foreach { messageNum =>
        val orderId = UUID.randomUUID
        val lineItems = (1 to Random.nextInt(10)).map { _ =>
          LineItem(
            id = UUID.randomUUID,
            orderId = orderId,
            amount = Random.nextInt(100).toFloat.toString,
            currencyCode = "USD"
          )
        }
        val order = Order(id = orderId, lineItems = lineItems.toList)

        val message = new ProducerRecord[String, String](kafkaTopic, null, Json.toJson(order).toString())
        producer.send(message)
      }

      Thread.sleep(1000)
    }
  }
}

// Model classes
case class Order(id: UUID, lineItems: List[LineItem])
case class LineItem(id: UUID, orderId: UUID, amount: String, currencyCode: String)
