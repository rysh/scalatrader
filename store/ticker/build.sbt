import Dependencies._

libraryDependencies += "com.pubnub" % "pubnub-gson" % "4.6.5"
libraryDependencies += "org.slf4j" % "slf4j-simple" % "1.6.4"
libraryDependencies += "com.amazonaws" % "aws-java-sdk" % "1.11.158"
libraryDependencies += "com.github.pathikrit" % "better-files_2.12" % "3.0.0"

lazy val root = (project in file("."))
	.enablePlugins(JavaAppPackaging, AshScriptPlugin, sbtdocker.DockerPlugin)
	.settings(
    organization := "scalatrader",
    scalaVersion := "2.12.1",
    name := "store-ticker",
    mainClass in (Compile, run) := Some("application.Main"),
    version      := "0.0.6-SNAPSHOT",
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
