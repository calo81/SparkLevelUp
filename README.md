# SparkLevelUp
Run a cluster, process data, make decisions

Using a Movie domain

What we are going to do:

- What and How of Spark
- Run a cluster
- Submit the raw files to HDFS, (Actors, Genres, Movies, summaries)
- Submit a job to execute some enrichment and grouping, (Couples in each movie?)
- Submit a ML clustering job for similar movies
- Tell me what movie you like, and we recommend you another, interactive queryin


```
Information courtesy of
IMDb
(http://www.imdb.com).
Used with permission.
```

##Spark quick introduction

###What is it?

Apache Spark is an open source clustering framework for batch and streaming (microbatching really) processing.

###Main selling points


- Fast (Memory based).
- Operations well beyond map-reduce.
- Data Scientist friendly. (Python and R)
- Interactive.
- Same processes can run in stream or batch mode.
- Built in tools for analysis. (Machine Learning, SparkSQL)
- Distributed abstraction that feels like working locally (RDD).
- Integration with the Hadoop ecosystem. In particular HDFS

###Creating the cluster in AWS

The power of Spark comes from the use of multiple machines to perform taks that naturally fit a distributed model. Many tasks do fit a model where it can be subdivided into smaller tasks and then optionally recombined for a result. Sometimes we just have to think of them that way.

####Let's install Spark

We will be using Spark 1.5.0 compiled against Hadoop 2.

So go to a directory of your choice (I will assume this directory is `~/Programs` from now on and call it **SPARK_HOME**) and:

```
wget http://www.apache.org/dyn/closer.lua/spark/spark-1.5.0/spark-1.5.0.tgz
tar zxvf spark-1.5.0.tgz
cd spark-1.5.0
mvn -Phadoop-2.3 -Dhadoop.version=2.6.0 -DskipTests clean package
``` 

####AWS EC2 simple cluster creation

Spark comes with a nice script that takes all the pain away from creating a Spark cluster in AWS by hand. 

You need the following set up previously to using it:

- An AWS account.
- A VPC and a Subnet in your AWS account (This are optional and if you don't have them you can remove those options from the following script).
- A KeyPair in your AWS account and the corresponding file in your local filesystem (I'll call it *spark-key* now).
- The proper AWS environment variables set in your local machine for authenticating against your AWS account.

Then just execute the following to create a 10 workers node (You will need to replace all the values with the ones that make sense to you of course).


```
~/Programs/spark-1.5.0/ec2/spark-ec2 --key-pair=spark-key --identity-file=/Users/cscarion/.ssh/spark-key.pem --region=eu-west-1 -s 10 --hadoop-major-version=yarn --instance-type=r3.large --vpc-id=vpc-1cc01879 --subnet-id=subnet-5e4cdf3b --private-ips --zone=eu-west-1a --additional-tags="Application:SparkLevelUp,Stage:Integration"  launch spark-cluster-carlo
```

You can visit the master URL at `http://master-ip:8080/`, there you will have links to the workers as well.


####Upload the files to HDFS


```
bin/hdfs dfs -put ~/ossources/SparkLevelUp/data/movies.list.gz hdfs://master-ip:9000/movies.gz

```

Do the same for all the files.

Navigate to `http://master-ip:50070/` to inspect the distributed filesystem.


####Data cleaning/preparing

A very important step in Data Science and analysis is cleaning the data to get it into a good state to allow it to be analysed.

The files downloaded from IMDB have some issues that dont allow them to be readily processable by Spark.

Manually I removed the headers of all the files, then I used spark itself to clean the data further.

The code for each cleaning job can be found on the "cleaning" package in the source code.

####Creating a program to group the data

Currently we have 5 data files.

* actors_cleaned
* actresses_cleaned
* genres_cleaned
* movies_cleaned
* plot_cleaned

The first thing we want to do is to group some of them so that we have just 1 record per movie, actor, actress and genre, we then assume that any actress-actor combination is a couple.

- So create and submit a job that does what I just mentioned. In Scala

- Interactively, Based on the new records return all the genres a given actor (let's say Schwarzenegger) has done. In R.
- - Interactively, Based on the new records return the plot for all the movies a given actor (let's say Schwarzenegger) has done. In Python.


#####First in Scala

```
// Excute to open the spark shell: (Replace the paths appropiately)

~/Programs/spark-1.5.0/bin/spark-shell --jars /Users/cscarion/ossources/SparkLevelUp/hadoop-aws-2.6.0.jar,/Users/cscarion/ossources/SparkLevelUp/lib/aws-java-sdk-1.10.34.jar,/Users/cscarion/ossources/SparkLevelUp/lib/guava-14.0.1.jar

```


```
 type Trifecta = Tuple3[Seq[String], Seq[String], Seq[String]]

 val grouped = sc.objectFile[(String, Trifecta)]("hdfs://localhost:9000/grouped_info")

 val horrorMovies = grouped.filter(_._2._1.contains("Horror"))
 
 
 val horrorMoviesWithCertainActor = grouped.filter(_._2._1.contains("Horror")).filter(_._2._2.contains("Nicholson, Jack (I)"))
 
 // print the movies
 
 horrorMoviesWithCertainActor.foreach((tuple: (String, Trifecta)) => println(tuple._1))

 
 val allGenresAnActorHasBeenIn = grouped.filter(_._2._2.contains("Nicholson, Jack (I)")).flatMap(_._2._1).distinct

 allGenresAnActorHasBeenIn.foreach((genre: String) => println(genre))
 
 val moviesForACouple = grouped.filter(_._2._2.contains("Willis, Bruce")).filter(_._2._3.contains("Jovovich, Milla")).filter(_._2._1.contains("Sci-Fi"))


```

#####In R

First we have to make the data fit to a model supported by R. So we create a Parquet file executing the job: `DataFrameItJob.scala` in the project.

**Note:** Ideally I wouldn't convert the `genres`, `actors` and `actresses` collections to CSV strings, but didn't manage to make the collections work in R dataframes.

The open your `RStudio` and let's work.

```
Sys.setenv(SPARK_HOME="/Users/cscarion/Programs/spark-1.5.0")
 .libPaths(c(file.path(Sys.getenv("SPARK_HOME"), "R", "lib"), .libPaths()))

 library(SparkR)
 
 sc <- sparkR.init(master="local")
 
 data <- read.df(path = "hdfs://localhost:9000/records_parquet")
 
 horrorMovies <- filter(data, contains(data$genres, "Horror"))
 
 horrorMoviesWithCertainActor <- filter(horrorMovies, contains(horrorMovies$actors, "Nicholson, Jack"))
 
 // print the movies
 
 head(horrorMoviesWithCertainActor[,1], n=13)

 
```

#####A bit of SparkSQL

When we created the Parquet file to use in R, we also created a Spark Dataframe. A Spark Dataframe is like an RDD (in that it is distributed) but with a schema associated to it (column names). This creates the possibility of querying it in more traditional ways.

```
val sqlContext = new org.apache.spark.sql.SQLContext(sc)
import sqlContext.implicits._
val records = sqlContext.read.parquet("hdfs://localhost:9000/records_parquet")
records.registerTempTable("records")
val horrorMoviesWithJack = sqlContext.sql("SELECT name from records where genres like '%Horror%' and actors like '%Nicholson, Jack%'")

//Show the movies

horrorMoviesWithJack.show

```



**Note:** You don't necesarily need to store the new records back into the filesystem as we did here, and sometimes you don't want to. This is just to see how to submit a batch job.

#### Creating a program to cluster the data

We want now to cluster (in the ML sense) the data, based on the plot of each movie.

- Create a Spark ML task that does that.

- Interactively say which kind of movie you like "I like elves and mages", and get a list back of recommended movies.








