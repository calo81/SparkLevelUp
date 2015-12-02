All tasks in the nodes will be run as ROOT

* `brew cask install vagrant`
* `git clone https://github.com/calo81/Spark-on-Vagrant.git`
* `git checkout hadoop-2.6.0-spark-1.5.2`
* cd Spark-on-Vagrant
* vagrant up 
* In your local machine add to hosts the line `10.211.55.101 node1`
* vagrant ssh node1
* `sudo su -`
* yum install git
* `git clone https://github.com/calo81/SparkLevelUp.git`
* cd SparkLevelUp
* sbt compile
* sbt assembly
* Edit `/usr/local/hadoop/etc/hadoop/slaves` adding a single line with the content `node2`
* `/usr/local/hadoop/sbin/stop-dfs.sh`
* `/usr/local/hadoop/sbin/start-dfs.sh`
* ssh into node2 and execute `/usr/local/spark/sbin/start-slave.sh spark://node1:7077`
* ssh into node1 and Execute example job `$SPARK_HOME/bin/spark-submit --class org.apache.spark.examples.SparkPi --master spark://node1:7077 $SPARK_HOME/lib/spark-examples*.jar`
* Visit node1 URL `http://10.211.55.101:8080/` and see the job running
* You also can see your cluster's HDFS here `http://node1:50070/dfshealth.html#tab-overview`
* Now from `node1` `python get-pip.py`
* `pip install awscli`
* `aws configure` and set your AWS credentials
* `aws s3 cp --recursive s3://sb-level-ups/spark .`
* `/usr/local/hadoop/bin/hdfs dfs -put actors.list hdfs://node1/actors.list`
* `/usr/local/hadoop/bin/hdfs dfs -put actresses.list hdfs://node1/actresses.list`
* `/usr/local/hadoop/bin/hdfs dfs -put genres.list hdfs://node1/genres.list`
* `/usr/local/hadoop/bin/hdfs dfs -put movies.list hdfs://node1/movies.list`
* `/usr/local/hadoop/bin/hdfs dfs -put plots.list hdfs://node1/plots.list`
* `/usr/local/hadoop/bin/hdfs dfs -put movies.csv hdfs://node1/movies.csv`
* `/usr/local/hadoop/bin/hdfs dfs -put ratings.csv hdfs://node1/ratings.csv`
* 

###Running the actual jobs

####Clean the data

Still in node1 as root, in SparkLevelUp root directory:

* ` /usr/local/spark/bin/spark-submit --master spark://node1:7077 --class cleaning.MovieCleanerJob /root/SparkLevelUp/target/scala-2.10/SparkLevelUp.jar`
* ` /usr/local/spark/bin/spark-submit --master spark://node1:7077 --class cleaning.PlotCleanerJob /root/SparkLevelUp/target/scala-2.10/SparkLevelUp.jar`
* ` /usr/local/spark/bin/spark-submit --master spark://node1:7077 --class cleaning.ActorCleanerJob /root/SparkLevelUp/target/scala-2.10/SparkLevelUp.jar`
* ` /usr/local/spark/bin/spark-submit --master spark://node1:7077 --class cleaning.ActressCleanerJob /root/SparkLevelUp/target/scala-2.10/SparkLevelUp.jar`
* ` /usr/local/spark/bin/spark-submit --master spark://node1:7077 --class cleaning.GenresCleanerJob /root/SparkLevelUp/target/scala-2.10/SparkLevelUp.jar`

#### Grouping. Processing the data.

* ` /usr/local/spark/bin/spark-submit --master spark://node1:7077 --class GroupingJob /root/SparkLevelUp/target/scala-2.10/SparkLevelUp.jar`
* Remember you can see your running Job here: `http://node1:8080/`
* Now let's get a dataframe `/usr/local/spark/bin/spark-submit --master spark://node1:7077 --class df.DataFrameItJob /root/SparkLevelUp/target/scala-2.10/SparkLevelUp.jar`
#### Interactively Analysing the data
* `/usr/local/spark/bin/spark-shell --master spark://node1:7077 --jars /root/SparkLevelUp/lib/hadoop-aws-2.6.0.jar,/root/SparkLevelUp/lib/aws-java-sdk-1.10.34.jar,/root/SparkLevelUp/lib/guava-14.0.1.jar`
* Then:

```
type Trifecta = Tuple3[Seq[String], Seq[String], Seq[String]]

 val grouped = sc.objectFile[(String, Trifecta)]("hdfs://node1/grouped_info")

 val horrorMovies = grouped.filter(_._2._1.contains("Horror"))


 val horrorMoviesWithCertainActor = grouped.filter(_._2._1.contains("Horror")).filter(_._2._2.contains("Nicholson, Jack (I)"))

 // print the movies

 horrorMoviesWithCertainActor.foreach((tuple: (String, Trifecta)) => println(tuple._1))


 val allGenresAnActorHasBeenIn = grouped.filter(_._2._2.contains("Nicholson, Jack (I)")).flatMap(_._2._1).distinct

 allGenresAnActorHasBeenIn.foreach((genre: String) => println(genre))

 val moviesForACouple = grouped.filter(_._2._2.contains("Willis, Bruce")).filter(_._2._3.contains("Jovovich, Milla")).filter(_._2._1.contains("Sci-Fi"))
```



