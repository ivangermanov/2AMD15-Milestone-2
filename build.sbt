ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.12.15"

lazy val root = (project in file("."))
  .settings(
    name := "Big Data Management Milestone 2 SBT"
  )


libraryDependencies ++= Seq(
  ("org.apache.spark" %% "spark-sql" % "3.2.0"),
  ("org.apache.spark" %% "spark-streaming" % "3.2.0"),
  "io.netty" % "netty-all" % "4.1.75.Final"
)

Compile/mainClass := Some("myPackage.Main")
// include the 'provided' Spark dependency on the classpath for `sbt run`
//Compile / run := Defaults.runTask(Compile / fullClasspath, Compile / run / mainClass, Compile / run / runner).evaluated