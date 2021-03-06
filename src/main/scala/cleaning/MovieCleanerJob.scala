package cleaning

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.io.{LongWritable, Text}
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat
import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by cscarion on 19/11/2015.
  */
object MovieCleanerJob {

  def main(args: Array[String]) {
    val conf = new SparkConf().setAppName("Spark Movie Grouper")
    val sc = new SparkContext(conf)
    val movies = sc.textFile("hdfs://node1/movies.list")
    movies.map { (movie: String) =>
      if (movie.contains("(")) {
        movie.substring(0, movie.indexOf('('))
      } else {
        movie
      }
    }.distinct().coalesce(1).saveAsTextFile("hdfs://node1/movies_cleaned")
  }
}
