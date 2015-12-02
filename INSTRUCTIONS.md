* `git clone https://github.com/calo81/Spark-on-Vagrant.git`
* cd Spark-on-Vagrant
* vagrant up 
* vagrant ssh node1
* yum install git
* `git clone https://github.com/calo81/SparkLevelUp.git`
* cd SparkLevelUp
* sbt compile
* sbt assembly
* Edit `/usr/local/spark/hadoop/etc/hadoop/slaves` adding a single line with the content `node2`
* `/usr/local/spark/hadoop/sbin/stop-dfs.sh`
* * `/usr/local/spark/hadoop/sbin/start-dfs.sh`
