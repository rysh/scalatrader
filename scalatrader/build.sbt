import sbt.CrossVersion

name := """scalatrader"""
organization := "scalatrader"

version := "0.0.7-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.3"

libraryDependencies += guice
libraryDependencies ++= Seq(
  "mysql" % "mysql-connector-java" % "5.1.34",
  "org.scalikejdbc" %% "scalikejdbc" % "3.1.0",
  "org.scalikejdbc" %% "scalikejdbc-config" % "3.1.0",
  "org.scalikejdbc" %% "scalikejdbc-test" % "3.1.0" % "test",
  "org.scalikejdbc" %% "scalikejdbc-play-initializer" % "2.6.0-scalikejdbc-3.1",
  "org.skinny-framework" %% "skinny-http-client" % "2.3.7",
  "org.scala-lang" % "scala-reflect" % scalaVersion.value,
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "org.scalaz" % "scalaz_2.12" % "7.3.0-M14",
  "com.amazonaws" % "aws-java-sdk" % "1.11.158",
  "com.pubnub" % "pubnub-gson" % "4.6.5",
  "com.github.pathikrit" % "better-files_2.12" % "3.0.0",
  "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test
)

val circeVersion = "0.8.0"
libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)

addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "scalatrader.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "scalatrader.binders._"

maintainer in Docker := "Ryuhei Ishibashi <rysh.cact@gmail.com>"

dockerExposedPorts in Docker := Seq(9000, 9443)
