name := """forex-server"""

version := "1.0"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "3.3.7"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "7.0.2" % Test
libraryDependencies += "org.scalatestplus" %% "mockito-5-18" % "3.2.19.0" % "test"
libraryDependencies += "com.softwaremill.sttp.client4" %% "core" % "4.0.12"
libraryDependencies += "com.softwaremill.sttp.client4" %% "pekko-http-backend" % "4.0.12"

scalafmtOnCompile := true
