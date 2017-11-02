import Dependencies._

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "com.rysh.scalatrader",
      scalaVersion := "2.12.2",
      version      := "0.1.0-SNAPSHOT"
    )),
    name := "open-info",
    libraryDependencies += scalaTest % Test
  )

val circeVersion = "0.8.0"
libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)

libraryDependencies += "org.skinny-framework" %% "skinny-http-client" % "2.3.7"
libraryDependencies += "org.scalaz" % "scalaz_2.12" % "7.3.0-M14"
libraryDependencies += "org.slf4j" % "slf4j-nop" % "1.7.25"

addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)