addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.6.7")

libraryDependencies += "mysql" % "mysql-connector-java"  % "5.1.33"

addSbtPlugin("org.scalikejdbc" %% "scalikejdbc-mapper-generator" % "3.1.0")

addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "0.9.4")