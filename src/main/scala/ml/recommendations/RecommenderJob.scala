package ml.recommendations

import org.apache.spark.{SparkContext, SparkConf}
import org.apache.spark.mllib.recommendation.ALS
import org.apache.spark.mllib.recommendation.MatrixFactorizationModel
import org.apache.spark.mllib.recommendation.Rating

/**
  * Created by cscarion on 25/11/2015.
  */
object RecommenderJob {

  def main(args: Array[String]): Unit = {

    val conf = new SparkConf().setAppName("Spark Movie Grouper").setMaster("local[6]")
    val sc = new SparkContext(conf)
    // Load and parse the data
    val data = sc.textFile("s3n://sb-level-ups/spark/ratings.csv")
    val ratings = data.filter(!_.startsWith("u")).map { (line: String) =>
      val splitted = line.split(",")
      Rating(splitted(0).toInt, splitted(1).toInt, splitted(2).toDouble)
    }

    // Build the recommendation model using ALS
    val rank = 10
    val numIterations = 10
    val model = ALS.train(ratings, rank, numIterations, 0.01)
    model.save(sc, "s3n://sb-level-ups/spark/recommendationModel")



    // Now for the interactive
    // have to run spark-shell like this:
    // ~/Programs/spark-1.5.0/bin/spark-shell --master local[6] --driver-memory 4G --executor-memory 4G --executor-cores 6

    import org.apache.spark.{SparkContext, SparkConf}
    import org.apache.spark.mllib.recommendation.ALS
    import org.apache.spark.mllib.recommendation.MatrixFactorizationModel
    import org.apache.spark.mllib.recommendation.Rating
    // Batman Lover
    val newRatings = sc.parallelize(List(Rating(0, 79132, 5), Rating(0, 130219, 5), Rating(0, 91529, 5), Rating(0, 48780, 5), Rating(0, 33794, 5), Rating(0, 1889, 5), Rating(0, 4226, 5)))


    val dataAgain = sc.textFile("s3n://sb-level-ups/spark/ratings.csv")
    val ratingsAgain = dataAgain.filter(!_.startsWith("u")).map { (line: String) =>
      val splitted = line.split(",")
      Rating(splitted(0).toInt, splitted(1).toInt, splitted(2).toDouble)
    }

    ALS.train(ratingsAgain.union(newRatings), 10, 10, 0.01).save(sc, "s3n://sb-level-ups/spark/newRecommendationModel")

    val loadedModel = MatrixFactorizationModel.load(sc, "s3n://sb-level-ups/spark/newRecommendationModel")

    val recommendations = sc.parallelize(loadedModel.recommendProducts(0, 5))

    val movies = sc.textFile("s3n://sb-level-ups/spark/movies.csv")

    val keyedMovies = movies.filter(!_.startsWith("mov")).map { (line: String) =>
      val splitted = line.split(",")
      (splitted(0).toInt, splitted(1))
    }

    recommendations.map { (r: Rating) =>
      (r.product, r.rating)
    }.join(keyedMovies).map(_._2).sortByKey(false).map(_._2).collect()

  }
}
