package cleaning

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.io.{LongWritable, Text}
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat
import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by cscarion on 19/11/2015.
  */
object ActorCleanerJob {

  def main(args: Array[String]) {
    val hadoopConf = new Configuration
    val conf = new SparkConf().setAppName("Spark Movie Grouper")
    val sc = new SparkContext(conf)

    hadoopConf.set("textinputformat.record.delimiter", "\n\n")
    val cleaned = sc.newAPIHadoopFile("hdfs://node1/actors.list", classOf[TextInputFormat], classOf[LongWritable], classOf[Text], hadoopConf)
    cleaned.map { tuple: (LongWritable, Text) =>
      val singleLine = tuple._2.toString.replace("\n\t\t\t", ";;")
      val keyValue = singleLine.toString.split("\t+")
      if(keyValue.size > 1) {
        val movies = keyValue(1).split(";;").map { (movie: String) =>
          movie.substring(0, movie.indexOf('('))
        }.mkString(";;")
       s"${keyValue(0)}:::${movies}"
      }else{
       "no_actor"
      }
    }.filter(!_.contains("no_actor")).coalesce(1).saveAsTextFile("hdfs://node1/actors_cleaned")
  }
}
