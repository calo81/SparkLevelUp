import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf
import org.apache.hadoop.conf.Configuration


object GroupingJob {
  def main(args: Array[String]) {
    val conf = new SparkConf().setAppName("Spark Movie Grouper").setMaster("local")
    val sc = new SparkContext(conf)
    val actresses = sc.textFile("hdfs://localhost:9000/actresses_cleaned")
    val actors = sc.textFile("hdfs://localhost:9000/actors_cleaned")
    val genres = sc.textFile("hdfs://localhost:9000/genres_cleaned")

    val keyedGenres = genres.map { (movieGenre: String) =>
      val splitted = movieGenre.split(":::")
      (splitted(0).replace("\"", "").trim, splitted(1))
    }

    val keyedActors = actors.flatMap { (actorLine: String) =>
      val splitted = actorLine.split(":::")
      if (splitted.size > 1) {
        val actor = splitted(0)
        val movies = splitted(1)
        movies.split(";;").distinct.map { (movie: String) =>
          (movie.replace("\"", "").trim, actor)
        }
      } else {
        List(("no_movie", "no_actor"))
      }
    }

    val keyedActresses = actresses.flatMap { (actorLine: String) =>
      val splitted = actorLine.split(":::")
      if (splitted.size > 1) {
        val actor = splitted(0)
        val movies = splitted(1)
        movies.split(";;").distinct.map { (movie: String) =>
          (movie.replace("\"", "").trim, actor)
        }
      } else {
        List(("no_movie", "no_actor"))
      }
    }

    keyedGenres.cogroup(keyedActors, keyedActresses).coalesce(1).saveAsObjectFile("hdfs://localhost:9000/grouped_info")
  }
}
