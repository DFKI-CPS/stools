organization := "de.dfki.cps"
name := "stools"
scalaVersion := "2.12.1"
version := "1.0.0"

scalacOptions := Seq("-deprecation")

crossScalaVersions := Seq("2.11.8","2.12.1")

libraryDependencies += "org.antlr" % "antlr-runtime" % "3.+"
libraryDependencies += "commons-logging" % "commons-logging" % "1.2"