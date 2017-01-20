organization := "de.dfki.cps"
name := "stools"
scalaVersion := "2.11.8"
version := "1.1.0"
licenses += ("MIT", url("http://opensource.org/licenses/MIT"))
bintrayOrganization := Some("dfki-cps")

scalacOptions := Seq("-deprecation")

crossScalaVersions := Seq("2.11.8","2.12.1")

libraryDependencies += "org.antlr" % "antlr-runtime" % "3.2"
libraryDependencies += "commons-logging" % "commons-logging" % "1.2"