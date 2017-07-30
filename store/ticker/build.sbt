import Dependencies._

libraryDependencies += "com.pubnub" % "pubnub-gson" % "4.6.5"
libraryDependencies += "org.slf4j" % "slf4j-simple" % "1.6.4"
libraryDependencies += "com.amazonaws" % "aws-java-sdk" % "1.11.158"
libraryDependencies += "com.github.pathikrit" % "better-files_2.12" % "3.0.0"


val circeVersion = "0.8.0"
libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-generic-extras",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)
libraryDependencies += "org.scalamacros" % "paradise_2.12.2" % "2.1.0"
addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)

lazy val root = (project in file("."))
	.enablePlugins(JavaAppPackaging, AshScriptPlugin, sbtdocker.DockerPlugin)
	.settings(
    organization := "scalatrader",
    scalaVersion := "2.12.1",
    name := "store-ticker",
    mainClass in (Compile, run) := Some("application.Main"),
    version      := "0.0.1-SNAPSHOT",
    dockerfile in docker := {
      val stageDir: File = stage.value
      val targetDir = "/opt/docker"

      new Dockerfile {
        from("java:8-jdk-alpine")
        copy(stageDir, targetDir)
        entryPoint(s"$targetDir/bin/${executableScriptName.value}")
      }
    },
    imageNames in docker := Seq(
      ImageName(s"${organization.value}/${name.value}:latest"),
      ImageName(
        namespace = Some(organization.value),
        repository = name.value,
        tag = Some("v" + version.value)
      )
    ),
    libraryDependencies += scalaTest % Test
  )
