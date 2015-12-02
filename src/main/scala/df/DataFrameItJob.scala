package df

import org.apache.spark.{SparkContext, SparkConf}

/**
  * Created by cscarion on 19/11/2015.
  */
object DataFrameItJob {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("Spark Movie Dataframer").setMaster("local")
    val sc = new SparkContext(conf)
    val sqlContext = new org.apache.spark.sql.SQLContext(sc)
    import sqlContext.implicits._

    type Trifecta = Tuple3[Seq[String], Seq[String], Seq[String]]

    val grouped = sc.objectFile[(String, Trifecta)]("hdfs://node1:9000/grouped_info")

    val records = grouped.map((tuple: (String, Trifecta)) => Record(tuple._1, tuple._2._1.mkString(";;"), tuple._2._2.mkString(";;"), tuple._2._3.mkString(";;"))).toDF

    records.registerTempTable("records")

    records.printSchema()

    records.write.parquet("hdfs://node1:9000/records_parquet")

  }
}
