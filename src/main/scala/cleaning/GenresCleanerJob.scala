package cleaning

import org.apache.spark.{SparkContext, SparkConf}

/**
  * Created by cscarion on 19/11/2015.
  */
object GenresCleanerJob {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("Spark Movie Grouper").setMaster("local")
    val sc = new SparkContext(conf)
    val movies = sc.textFile("hdfs://localhost:9000/genres.list")
    movies.map { (movie: String) =>
      val movieGenre = movie.split("\t+")
      var movieName = movieGenre(0)
      movieName = if (movieName.contains("(")) {
        movieName.substring(0, movie.indexOf('('))
      } else {
        movieName
      }
      s"${movieName}:::${movieGenre(1)}"
    }.coalesce(1).saveAsTextFile("hdfs://localhost:9000/genres_cleaned")
  }
}
