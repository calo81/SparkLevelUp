name := "SparkLevelUp"

version := "1.0"

scalaVersion := "2.10.6"

libraryDependencies += "org.apache.spark" %% "spark-core" % "1.5.2"
libraryDependencies += "org.apache.spark" %% "spark-sql" % "1.5.2"
libraryDependencies += "org.apache.spark" %% "spark-mllib" % "1.5.2"
libraryDependencies += "org.slf4j" % "slf4j-simple" % "1.7.13"
libraryDependencies += "org.scalanlp" % "breeze_2.10" % "0.11.2"
libraryDependencies += "org.apache.hadoop" % "hadoop-aws" % "2.6.0"

resolvers += Resolver.mavenLocal

// This statement includes the assembly plug-in capabilities

// Configure JAR used with the assembly plug-in
jarName in assembly := "SparkLevelUp.jar"

// A special option to exclude Scala itself from our assembly JAR, since Spark
// already bundles Scala.
assemblyOption in assembly :=
  (assemblyOption in assembly).value.copy(includeScala = false)

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case _ => MergeStrategy.first
}
