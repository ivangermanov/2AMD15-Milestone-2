ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.1.1"

lazy val root = (project in file("."))
  .settings(
    name := "Big Data Management Milestone 2 SBT"
  )


libraryDependencies ++= Seq(
  ("org.apache.spark" %% "spark-sql" % "3.2.0" % "provided").cross(CrossVersion.for3Use2_13),
  ("org.apache.spark" %% "spark-streaming" % "3.2.0" % "provided").cross(CrossVersion.for3Use2_13),
)

// include the 'provided' Spark dependency on the classpath for `sbt run`
//Compile / run := Defaults.runTask(Compile / fullClasspath, Compile / run / mainClass, Compile / run / runner).evaluated