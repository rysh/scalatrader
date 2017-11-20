name := """scalatrader"""
organization := "scalatrader"

version := "0.0.1-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.3"

libraryDependencies += guice
libraryDependencies ++= Seq(
  "mysql"           %  "mysql-connector-java"         % "5.1.34",
  "org.scalikejdbc" %% "scalikejdbc"                  % "3.1.0",
  "org.scalikejdbc" %% "scalikejdbc-config"           % "3.1.0",
  "org.scalikejdbc" %% "scalikejdbc-test"             % "3.1.0"   % "test",
  "org.scalikejdbc" %% "scalikejdbc-play-initializer" % "2.6.0-scalikejdbc-3.1"
)
libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value
libraryDependencies += "ch.qos.logback" %  "logback-classic"   % "1.2.3"
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "scalatrader.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "scalatrader.binders._"

maintainer in Docker := "Ryuhei Ishibashi <rysh.cact@gmail.com>"

dockerExposedPorts in Docker := Seq(9000, 9443)