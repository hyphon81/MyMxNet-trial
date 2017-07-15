name := """MyMxNet-trial"""

version := "0.1"

scalaVersion := "2.11.7"

classpathTypes += "maven-plugin"

val dl4jVersion = "0.8.0"

// Change this to another test framework if you prefer
libraryDependencies ++= Seq(
  //"com.typesafe.akka" %% "akka-actor" % "2.3.11",
  "org.scalatest" %% "scalatest" % "2.2.4" % "test",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0",
  "ch.qos.logback" % "logback-classic" % "1.2.3"
)

mainClass in assembly := Some("net.hyphon81.nn.xor.Xor")

assemblyMergeStrategy in assembly := {
  case PathList("javax", "servlet", xs @ _*)         => MergeStrategy.first
  case PathList(ps @ _*) if ps.last endsWith ".properties" => MergeStrategy.first
  case PathList(ps @ _*) if ps.last endsWith ".xml" => MergeStrategy.first
  case PathList(ps @ _*) if ps.last endsWith ".types" => MergeStrategy.first
  case PathList(ps @ _*) if ps.last endsWith ".class" => MergeStrategy.first
  case "application.conf"                            => MergeStrategy.concat
  case "unwanted.txt"                                => MergeStrategy.discard
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}

