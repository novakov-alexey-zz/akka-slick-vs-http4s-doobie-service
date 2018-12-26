lazy val akkaHttpVersion = "10.1.5"
lazy val akkaVersion = "2.5.17"
lazy val slickVersion = "3.2.3"

lazy val root = (project in file(".")).settings(
  inThisBuild(List(organization := "org.alexeyn", scalaVersion := "2.12.7")),
  name := "akka-crud-service",
  libraryDependencies ++= Seq(
    "com.github.pureconfig" %% "pureconfig" % "0.9.1",
    "org.typelevel" %% "cats-core" % "1.4.0",
    "com.typesafe.scala-logging" %% "scala-logging" % "3.9.0",
    "ch.qos.logback" % "logback-classic" % "1.2.3",
    "org.postgresql" % "postgresql" % "9.4-1203-jdbc4",
    "com.github.pureconfig" %% "pureconfig" % "0.9.1",
    "com.typesafe.slick" %% "slick" % slickVersion,
    "com.typesafe.slick" %% "slick-hikaricp" % slickVersion,
    "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-stream" % akkaVersion,
    "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test,
    "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
    "org.scalatest" %% "scalatest" % "3.0.5" % Test,
    "com.dimafeng" %% "testcontainers-scala" % "0.20.0" % Test,
    "org.testcontainers" % "postgresql" % "1.9.1" % Test
  ),
  dockerBaseImage := "openjdk:8-jre-alpine"
).enablePlugins(JavaAppPackaging, AshScriptPlugin)
