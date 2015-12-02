package ml

import org.apache.spark.ml.feature.{IDF, HashingTF}
import org.apache.spark.mllib.linalg
import org.apache.spark.sql.Row
import org.apache.spark.{SparkContext, SparkConf}
import breeze.linalg._

import scala.collection.mutable

/**
  * Created by cscarion on 20/11/2015.
  */
object FindMyMovieInteractive {

  val conf = new SparkConf().setAppName("Spark Movie Grouper")
  val sc = new SparkContext(conf)
  val sqlContext = new org.apache.spark.sql.SQLContext(sc)

  import sqlContext.implicits._

  def createVectorForMovie(plot: String): SparseVector[Double] = {
    val tf = new HashingTF("what-goes-here")
    tf.setNumFeatures(10000)
    tf.setInputCol("plot")
    tf.setOutputCol("vector")


    val terms = plot.split(" ").toList
    val tfVectors = tf.transform(sc.parallelize(List(("movie_x", terms))).toDF("movie", "plot"))

    val idf = new IDF()

    idf.setInputCol("vector")

    idf.setOutputCol("last_vector")

    val idfModel = idf.fit(tfVectors)

    val tfIdfVectors = idfModel.transform(tfVectors)

    val vector = tfIdfVectors.first().getAs[linalg.Vector]("last_vector").asInstanceOf[linalg.SparseVector]

    new SparseVector(vector.indices, Array.fill[Double](vector.size)(1.0 / vector.size), vector.size)
  }

  def main(args: Array[String]): Unit = {

    val records = sqlContext.read.parquet("hdfs://node1/vectorized_terms")

    val similarity = 0.0

    val vectorForMovie = createVectorForMovie("stars war light force jedi empire rebels")

    records.rdd.map { (row: Row) =>
      val vector = row.getAs[linalg.Vector]("last_vector").asInstanceOf[linalg.SparseVector]
      val breeze1 = new SparseVector(vector.indices, vector.values, vector.size)
      val cosineSim = breeze1.dot(vectorForMovie)
      (row, cosineSim)
    }.filter { (tuple: (Row, Double)) =>
      tuple._2 > similarity
    }.map { (tuple: (Row, Double)) =>
      (tuple._2, tuple._1.getAs[String]("movie"))
    }.sortByKey(false).distinct().top(20).foreach { (tuple: (Double, String)) =>
      println(tuple._2 + " "+ tuple._1)
    }
  }
}
