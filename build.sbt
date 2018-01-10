import sbt.Keys.libraryDependencies

name := "RoundRobinWrapper"

version := "0.1"

scalaVersion := "2.12.4"

libraryDependencies ++= Seq("org.scalatest" %% "scalatest" % "3.0.0" % "test")