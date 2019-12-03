import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.spark.{ SparkConf, SparkContext }
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.Seconds
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.streaming.kafka010._
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe

object StreamingConsumer extends App {
  val kafkaParams = Map[String, Object](
    "bootstrap.servers" -> "localhost:9092",
    "key.deserializer" -> classOf[StringDeserializer],
    "value.deserializer" -> classOf[StringDeserializer],
    "group.id" -> "use_a_separate_group_id_for_each_stream",
    "auto.offset.reset" -> "latest",
    "enable.auto.commit" -> (false: java.lang.Boolean)
  )

  val sparkConf = new SparkConf().setMaster("local[2]").setAppName("StreamingConsumer")
  val ssc = new StreamingContext(sparkConf, Seconds(2))
  val sc = ssc.sparkContext
  sc.setLogLevel("ERROR")
  val topics = Array("SensorData")
  val stream = KafkaUtils.createDirectStream[String, String](
    ssc,
    PreferConsistent,
    Subscribe[String, String](topics, kafkaParams)
  )
  stream.map(record=>(record.value().toString)).print
  ssc.start()
  ssc.awaitTermination()
}


