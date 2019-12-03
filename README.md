Description of a big data pipeline setup and testing, using:


- base [kafka docker-image from wurstmeister](https://github.com/wurstmeister/kafka-docker)
- [hbase docker-image from big-data-europe](https://github.com/big-data-europe/docker-hbase)
- `Spark 2.11`

## Kafka

Edit the `docker-compose.yml`:
```
version: '2'
services:
  zookeeper:
    image: wurstmeister/zookeeper:3.4.6
    ports:
     - "2181:2181"
  kafka:
    build: .
    ports:
     - "9092:9092"
    expose:
     - "9093"
    environment:
      KAFKA_ADVERTISED_LISTENERS: INSIDE://kafka:9093,OUTSIDE://localhost:9092
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: INSIDE:PLAINTEXT,OUTSIDE:PLAINTEXT
      KAFKA_LISTENERS: INSIDE://0.0.0.0:9093,OUTSIDE://0.0.0.0:9092
      KAFKA_INTER_BROKER_LISTENER_NAME: INSIDE
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
    volumes:
     - /var/run/docker.sock:/var/run/docker.sock
```
To expose the internal port 9093 of kafka brokers to make it availble from outside the docker container on 9092. ZK will be available on 2181 form the outside.

- Start docker-image with `docker-compose up -d`

- To test kafka with zookeeper from inside the docker container, get the internal ip of the kafka broker with `docker inspect kafka-docker-master-kafka_1`. In This case `172.18.0.2:9092`

- To test kafka, go inside the kafka shell / the kafka container:  
`
docker exec -it kafka-docker-master_kafka_1  /bin/bash
`

- Create topic and view:  
```
$KAFKA_HOME/bin/kafka-topics.sh --create --bootstrap-server 172.18.0.2:9092 --replication-factor 1 --partit
ions 1 --topic SensorData
```  
```
$KAFKA_HOME/bin/kafka-topics.sh --list --zookeeper zookeeper:2181
``` 

- Start producer  
`
$KAFKA_HOME/bin/kafka-console-producer.sh --topic SensorData --broker-list 172.18.0.2:9092
`

- Start consumer  
`
$KAFKA_HOME/bin/kafka-console-consumer.sh --bootstrap-server 172.18.0.2:9092  --topic SensorData --from-beg
inning
`

## Spark producer and streaming consumer 

- To connect to the broker with the spark streaming kafka api, use localhost on the exposed port 9092.

- Dependencies / `pom.xml`  
```
    <dependency>
      <groupId>org.scala-lang</groupId>
      <artifactId>scala-library</artifactId>
      <version>${scala.version}</version>
    </dependency>
    <dependency>
      <groupId>junit</groupId>
      <artifactId>junit</artifactId>
      <version>4.11</version>
      <scope>test</scope>
    </dependency>
    <dependency>
      <groupId>org.slf4j</groupId>
      <artifactId>slf4j-api</artifactId>
      <version>1.7.5</version>
    </dependency>
    <dependency>
      <groupId>org.slf4j</groupId>
      <artifactId>slf4j-log4j12</artifactId>
      <version>1.7.5</version>
    </dependency>
    <dependency>
      <groupId>org.specs2</groupId>
      <artifactId>specs2-junit_${scala.compat.version}</artifactId>
      <version>2.4.16</version>
      <scope>test</scope>
    </dependency>
    <dependency>
      <groupId>org.apache.kafka</groupId>
      <artifactId>kafka-clients</artifactId>
      <version>2.1.0</version>
    </dependency>
      <dependency>
          <groupId>org.apache.spark</groupId>
          <artifactId>spark-streaming_2.11</artifactId>
          <version>2.1.0</version>
      </dependency>
      <dependency>
          <groupId>org.apache.spark</groupId>
          <artifactId>spark-core_2.11</artifactId>
          <version>2.1.0</version>
      </dependency>
    <dependency>
        <groupId>org.apache.spark</groupId>
        <artifactId>spark-streaming-kafka-0-10_2.11</artifactId>
        <version>2.2.0</version>
    </dependency>
    <dependency>
      <groupId>org.specs2</groupId>
      <artifactId>specs2-core_${scala.compat.version}</artifactId>
      <version>2.4.16</version>
      <scope>test</scope>
    </dependency>
    <dependency>
      <groupId>org.scalatest</groupId>
      <artifactId>scalatest_${scala.compat.version}</artifactId>
      <version>2.2.4</version>
      <scope>test</scope>
    </dependency>
      <dependency>
          <groupId>org.apache.hbase</groupId>
          <artifactId>hbase-client</artifactId>
          <version>1.3.1</version>
      </dependency>
      <dependency>
          <groupId>org.apache.hbase</groupId>
          <artifactId>hbase-common</artifactId>
          <version>1.3.1</version>
      </dependency>
      <dependency>
          <groupId>org.apache.hbase</groupId>
          <artifactId>hbase-protocol</artifactId>
          <version>1.3.1</version>
      </dependency>
      <dependency>
          <groupId>org.apache.hbase</groupId>
          <artifactId>hbase-hadoop2-compat</artifactId>
          <version>1.3.1</version>
      </dependency>
      <dependency>
          <groupId>org.apache.hbase</groupId>
          <artifactId>hbase-annotations</artifactId>
          <version>1.3.1</version>
      </dependency>
      <dependency>
          <groupId>org.apache.hbase</groupId>
          <artifactId>hbase-server</artifactId>
          <version>1.3.1</version>
      </dependency>
```      
- Start a producer which mocks 3 sensors that send data every 5 seconds to the kafka topic:  
```
import java.util.Properties
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
object SensorDataProducer extends App {
  
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
      val sensors = Array("A", "B", "C")
      for (s <- sensors) {
        Thread.sleep(1000)
        val record = new ProducerRecord[String, String](topicA, i.toString, messageProducer.getMessage(s))
        val metadata = producer.send(record)
      }
    }
  }catch{
    case e:Exception => e.printStackTrace()
  }finally {
    producer.close()
  }
}
```
- Spark streaming consumer to read messages from the topic:  
```
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
```
- Test output:  
![](/producer_consumer_demo.png?raw=true)


