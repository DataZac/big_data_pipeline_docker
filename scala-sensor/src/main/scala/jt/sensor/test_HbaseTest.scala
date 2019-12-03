import org.apache.hadoop.hbase.{ HBaseConfiguration, HTableDescriptor, HColumnDescriptor }
import org.apache.hadoop.hbase.util.Bytes
import org.apache.hadoop.hbase.mapreduce.{ TableInputFormat, TableOutputFormat }
import org.apache.hadoop.hbase.client.{ HBaseAdmin, Put, HTable }
import org.apache.hadoop.hbase.client.Scan
import org.apache.hadoop.hbase.client._
import org.apache.hadoop.hbase.util.Bytes
import org.apache.hadoop.hbase.{CellUtil, HBaseConfiguration, TableName}
import org.apache.hadoop.conf.Configuration
import scala.collection.JavaConverters._

object ScalaHBaseExample extends App{

  def printRow(result : Result) = {
    val cells = result.rawCells();
    print( Bytes.toString(result.getRow) + " : " )
    for(cell <- cells){
      val col_name = Bytes.toString(CellUtil.cloneQualifier(cell))
      val col_value = Bytes.toString(CellUtil.cloneValue(cell))
      print("(%s,%s) ".format(col_name, col_value))
    }
    println()
  }

  val conf : Configuration = HBaseConfiguration.create()
  /*
  From http://hbase.apache.org/0.94/book/zookeeper.html
  A distributed Apache HBase (TM) installation depends on a running ZooKeeper cluster. All participating nodes and clients
  need to be able to access the running ZooKeeper ensemble. Apache HBase by default manages a ZooKeeper "cluster" for you.
  It will start and stop the ZooKeeper ensemble as part of the HBase start/stop process. You can also manage the ZooKeeper
  ensemble independent of HBase and just point HBase at the cluster it should use. To toggle HBase management of ZooKeeper,
  use the HBASE_MANAGES_ZK variable in conf/hbase-env.sh. This variable, which defaults to true, tells HBase whether to
  start/stop the ZooKeeper ensemble servers as part of HBase start/stop.
  */

  conf.set("hbase.zookeeper.quorum", "localhost");
  conf.set("hbase.zookeeper.property.clientPort", "2182");
  conf.set("hbase.master", "localhost:16010");

  val connection = ConnectionFactory.createConnection(conf)
  val table = connection.getTable(TableName.valueOf( Bytes.toBytes("test1") ) )
/*
  // Put example
  var put = new Put(Bytes.toBytes("row1"))
  put.addColumn(Bytes.toBytes("d"), Bytes.toBytes("test_column_name"), Bytes.toBytes("test_value"))
  put.addColumn(Bytes.toBytes("d"), Bytes.toBytes("test_column_name2"), Bytes.toBytes("test_value2"))
  table.put(put)

  // Get example
  println("Get Example:")
  var get = new Get(Bytes.toBytes("row1"))
  var result = table.get(get)
  printRow(result)
*/
  //Scan example
  println("\nScan Example:")
  var scan = table.getScanner(new Scan())
  scan.asScala.foreach(result => {
    printRow(result)
  })

  table.close()
  connection.close()
}
/*
object SparkStreamingKafkaDemo extends App {

def toHBase(row: (_, _)) {
  val hConf = new HBaseConfiguration()
  hConf.set("hbase.zookeeper.quorum", "localhost:2182")
  val tableName = "Streaming_wordcount"
  val hTable = new HTable(hConf, tableName)
  val tableDescription = new HTableDescriptor(tableName)
  //tableDescription.addFamily(new HColumnDescriptor("Details".getBytes()))
  val thePut = new Put(Bytes.toBytes(row._1.toString()))
  thePut.add(Bytes.toBytes("Word_count"), Bytes.toBytes("Occurances"), Bytes.toBytes(row._2.toString))
  hTable.put(thePut)
}
val Hbase_inset = cnt.foreachRDD(rdd => if (!rdd.isEmpty()) rdd.foreach(toHBase(_)))

}
*/