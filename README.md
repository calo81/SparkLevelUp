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



