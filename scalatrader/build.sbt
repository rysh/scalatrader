name := """scalatrader"""
organization := "scalatrader"

version := "0.0.1-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.3"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "scalatrader.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "scalatrader.binders._"

maintainer in Docker := "Ryuhei Ishibashi <rysh.cact@gmail.com>"

dockerExposedPorts in Docker := Seq(9000, 9443)