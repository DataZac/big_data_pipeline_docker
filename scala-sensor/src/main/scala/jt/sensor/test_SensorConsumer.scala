import java.util.{Collections, Properties}
import java.util.regex.Pattern
import org.apache.kafka.clients.consumer.KafkaConsumer
import scala.collection.JavaConverters._
object KafkaConsumerSubscribeApp extends App {

  val props:Properties = new Properties()
  props.put("group.id", "test")
  props.put("bootstrap.servers","localhost:9092")
  props.put("key.deserializer",
    "org.apache.kafka.common.serialization.StringDeserializer")
  props.put("value.deserializer",
    "org.apache.kafka.common.serialization.StringDeserializer")
  props.put("enable.auto.commit", "true")
  props.put("auto.commit.interval.ms", "1000")
  val consumer = new KafkaConsumer(props)
  val topics = List("SensorData")
  try {
    consumer.subscribe(topics.asJava)
    while (true) {
      val records = consumer.poll(10)
      for (record <- records.asScala) {
        println("Topic: " + record.topic() +
          ",Key: " + record.key() +
          ",Value: " + record.value() +
          ", Offset: " + record.offset() +
          ", Partition: " + record.partition())
      }
    }
  }catch{
    case e:Exception => e.printStackTrace()
  }finally {
    consumer.close()
  }
}

/*

 {  “data”: {   “deviceId”: “11c1310e-c0c2-461b-a4eb-f6bf8da2d23c“,   “temperature”: 12,   “location”: {    “latitude”: “52.14691120000001”,    “longitude”: “11.658838699999933”   },   “time”: “1509793231”  } }
 */