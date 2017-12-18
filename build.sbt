
lazy val root = (project in file("."))
  .settings(
    name := "ScalatraderRoot",
    organization := "com.scalatrader",
    scalaVersion := "2.12.3",
    version      := "0.0.1-SNAPSHOT"
  )

lazy val scalatrader = project
lazy val ticker = (project in file("store/ticker"))