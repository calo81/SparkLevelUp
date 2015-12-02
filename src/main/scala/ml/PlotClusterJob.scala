package ml

import com.github.aztek.porterstemmer.PorterStemmer
import org.apache.spark.mllib.clustering.LDA
import org.apache.spark.ml.feature.{IDF, HashingTF}
import org.apache.spark.{SparkContext, SparkConf}

/**
  * Created by cscarion on 20/11/2015.
  */
object PlotClusterJob {

  def notStopWord(word: String): Boolean = {
    !List("a", "about", "above", "after", "again", "against",
      "all", "am", "an", "and", "any", "are", "aren't", "as", "at",
      "be", "because", "been", "before", "being", "below", "between", "both", "but", "by", "can't", "cannot", "could", "couldn't", "did", "didn't", "do", "does", "doesn't", "doing", "don't", "down", "during", "each", "few", "for", "from", "further", "had", "hadn't", "has", "hasn't", "have", "haven't", "having", "he", "he'd", "he'll", "he's", "her", "here", "here's", "hers", "herself", "him", "himself", "his", "how", "how's", "i", "i'd", "i'll", "i'm", "i've", "if", "in", "into", "is", "isn't", "it", "it's", "its", "itself", "let's", "me", "more", "most", "mustn't", "my", "myself", "no", "nor", "not", "of", "off", "on", "once", "only", "or", "other", "ought", "our", "ours").contains(word)
  }

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("Spark Movie Grouper").setMaster("local[6]")
    val sc = new SparkContext(conf)
    val sqlContext = new org.apache.spark.sql.SQLContext(sc)
    import sqlContext.implicits._

    val movies = sc.textFile("hdfs://localhost:9000/plots_cleaned")

    val plotsWords = movies.map { (movie: String) =>
      (movie.split(":::")(0), movie.split(":::")(1).split(" ").toList.filter(notStopWord).map(PorterStemmer.stem(_)))
    }.toDF("movie", "plot")

    val tf = new HashingTF("what-goes-here")
    tf.setNumFeatures(10000)
    tf.setInputCol("plot")
    tf.setOutputCol("vector")

    val tfVectors = tf.transform(plotsWords)

    val idf = new IDF()

    idf.setInputCol("vector")

    idf.setOutputCol("last_vector")

    val idfModel = idf.fit(tfVectors)

    val tfIdfVectors = idfModel.transform(tfVectors)

    tfIdfVectors.coalesce(1).write.parquet("hdfs://localhost:9000/vectorized_terms")


  }
}
