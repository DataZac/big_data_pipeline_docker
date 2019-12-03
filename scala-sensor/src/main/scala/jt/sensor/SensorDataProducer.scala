import java.util.Properties
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
object SensorDataProducerApp extends App {

  val props:Properties = new Properties()
  props.put("bootstrap.servers","localhost:9092")
  props.put("key.serializer",
    "org.apache.kafka.common.serialization.StringSerializer")
  props.put("value.serializer",
    "org.apache.kafka.common.serialization.StringSerializer")
  props.put("acks","all")

  class SensorMessageProducer() {
    val r = scala.util.Random
    def getMessage(Device: String): String = {
      "deviceId: " + Device + ", temperature: " + r.nextInt(200) + ", latitude: "  + r.nextFloat() +", longitude: " + r.nextFloat()+ ", timestamp: " + r.nextInt(99999999)
    }
  }

  val producer = new KafkaProducer[String, String](props)
  val messageProducer = new SensorMessageProducer()
  val topicA = "SensorData"

  try {
    for (i <- 0 to 50) {
      Thread.sleep(5000)
      val record = new ProducerRecord[String, String](topicA, i.toString, messageProducer.getMessage("A"))
      val metadata = producer.send(record)
      val record2 = new ProducerRecord[String, String](topicA, i.toString, messageProducer.getMessage("B"))
      val metadata2 = producer.send(record2)
      val record3 = new ProducerRecord[String, String](topicA, i.toString, messageProducer.getMessage("C"))
      val metadata3 = producer.send(record3)
      println(record, record2, record3)

    }
  }catch{
    case e:Exception => e.printStackTrace()
  }finally {
    producer.close()
  }
}