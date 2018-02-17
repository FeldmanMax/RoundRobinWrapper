import sbt.Keys.libraryDependencies

name := "RoundRobinWrapper"

version := "0.1"

scalaVersion := "2.12.4"

unmanagedBase := baseDirectory.value / "lib"

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.0.0" % "test",
  "com.typesafe" % "config" % "1.3.1",
  "org.scalaj" %% "scalaj-http" % "2.3.0",
  "net.codingwell" %% "scala-guice" % "4.1.1",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.7.2",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "io.circe" %% "circe-core" % "0.8.0",
  "io.circe" %% "circe-generic"% "0.8.0",
  "io.circe" %% "circe-parser" % "0.8.0")

unmanagedJars in Compile += file(baseDirectory.value + "/lib/dockerwrapper.jar")
unmanagedJars in Compile += file(baseDirectory.value + "/lib/roundrobin.jar")