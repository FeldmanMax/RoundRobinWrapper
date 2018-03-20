import sbt.Keys.libraryDependencies

autoScalaLibrary := true
managedScalaInstance := false

lazy val commonSettings = Seq(
  organization := "max.feldman",
  version := "0.1.0-SNAPSHOT",
  scalaVersion := "2.12.4"
)

lazy val root = (project in file("."))
  .settings(
    commonSettings,
    name := "RoundRobinWrapper",
    libraryDependencies ++= Seq("com.typesafe" % "config" % "1.3.1",
      "org.scalatest" %% "scalatest" % "3.0.1" % "test",
      "feldman.max" % "inframodule" % "1.0.0" from "file:///Users/maksik1/IdeaProjects/RoundRobinWrapper/lib/inframodule.jar",
      "feldman.max" % "roundrobin" % "1.0.0" from "file:///Users/maksik1/IdeaProjects/RoundRobinWrapper/lib/roundrobin.jar"
        exclude("org.slf4j", "slf4j-api"))

  )

exportJars := true