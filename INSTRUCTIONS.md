* `brew cask install vagrant`
* `git clone https://github.com/calo81/Spark-on-Vagrant.git`
* `git checkout hadoop-2.6.0-spark-1.5.2`
* cd Spark-on-Vagrant
* vagrant up 
* In your local machine add to hosts the line `10.211.55.101 node1`
* vagrant ssh node1
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
