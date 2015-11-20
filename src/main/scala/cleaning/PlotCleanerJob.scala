package cleaning

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.io.{Text, LongWritable}
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat
import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by cscarion on 20/11/2015.
  */
object PlotCleanerJob {
  def main(args: Array[String]): Unit = {
    val hadoopConf = new Configuration
    val conf = new SparkConf().setAppName("Spark Movie Grouper").setMaster("local")

    val sc = new SparkContext(conf)

    hadoopConf.set("textinputformat.record.delimiter", "-------------------------------------------------------------------------------")
    val cleaned = sc.newAPIHadoopFile("hdfs://localhost:9000/plot.list", classOf[TextInputFormat], classOf[LongWritable], classOf[Text], hadoopConf)

    val keyedMovies = cleaned.map { (tuple: (LongWritable, Text)) =>
      val singleLine = tuple._2.toString.replace("\n", " ")
      val almostKey = singleLine.substring(0, singleLine.indexOf("PL:")).replace("MV:", "")
      val key = almostKey.substring(0, almostKey.indexOf("(")).replace("\"","").trim
      val review = singleLine.substring(singleLine.indexOf("PL:"), singleLine.size - 1).replace("PL:", "").toLowerCase
      s"${key}:::${review}"
    }
    keyedMovies.coalesce(1).saveAsTextFile("hdfs://localhost:9000/plots_cleaned")
  }
}
