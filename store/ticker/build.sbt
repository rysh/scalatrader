import Dependencies._

libraryDependencies += "com.pubnub" % "pubnub-gson" % "4.6.5"
libraryDependencies += "org.slf4j" % "slf4j-simple" % "1.6.4"

lazy val root = (project in file("."))
	.enablePlugins(JavaAppPackaging, AshScriptPlugin, sbtdocker.DockerPlugin)
	.settings(
    organization := "scalatrader",
    scalaVersion := "2.12.1",
    name := "store-ticker",
    mainClass in (Compile, run) := Some("Main"),
    version      := "0.0.0-SNAPSHOT",
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

    