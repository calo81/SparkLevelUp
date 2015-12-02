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
* ssh into node2 and execute `/usr/local/spark/sbin/start-slave spark://node1:7077`
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

#### Grouping. Now the following is for the actual level up



