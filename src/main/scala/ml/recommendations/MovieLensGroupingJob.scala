package ml.recommendations

import org.apache.spark.{SparkContext, SparkConf}

/**
  * Created by cscarion on 24/11/2015.
  */
object MovieLensGroupingJob {
  def main(args: Array[String]) {
    val conf = new SparkConf().setAppName("Spark Movie Lens Grouper").setMaster("local")
    val sc = new SparkContext(conf)
    val movies = sc.textFile("hdfs://localhost:9000/movies.csv")
    val ratings = sc.textFile("hdfs://localhost:9000/ratings.csv")

    val keyedMovies = movies.map { (line: String) =>
      val splitted = line.split(",")
      val movieName = if (splitted(1).contains("(")) {
        splitted(1).substring(0, splitted(1).indexOf("(")).trim
      } else {
        splitted(1).trim
      }.replace("\"","")
      (splitted(0), movieName)
    }

    val keyedRatings = ratings.map { (line: String) =>
      val splitted = line.split(",")
      (splitted(1), (splitted(0), splitted(2)))
    }

    val moviesAndRatings = keyedMovies.join(keyedRatings)

    val triple = moviesAndRatings.map { (tuple: (String, (String, (String, String)))) =>
      s"${tuple._2._1},${tuple._2._2._1},${tuple._2._2._2}"
    }

    triple.coalesce(1).saveAsTextFile("hdfs://localhost:9000/groupedRatings.csv")
  }
}
