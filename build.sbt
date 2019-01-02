lazy val akkaHttpVersion = "10.1.6"
lazy val akkaVersion = "2.5.19"
lazy val slickVersion = "3.2.3"
lazy val upickleVersion = "0.6.7"

lazy val root = (project in file("."))
  .settings(
    inThisBuild(List(organization := "org.alexeyn", scalaVersion := "2.12.8")),
    name := "akka-crud-service",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-core" % "1.4.0",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.9.0",
      "ch.qos.logback" % "logback-classic" % "1.2.3",
      "org.postgresql" % "postgresql" % "9.4-1203-jdbc4",
      "com.github.pureconfig" %% "pureconfig" % "0.10.1",
      "com.typesafe.slick" %% "slick" % slickVersion,
      "com.typesafe.slick" %% "slick-hikaricp" % slickVersion,
      "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-stream" % akkaVersion,
      "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test,
      "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
      "de.heikoseeberger" %% "akka-http-upickle" % "1.23.0",
      "com.lihaoyi" %% "upickle" % upickleVersion,
      "com.lihaoyi" %% "ujson" % upickleVersion,
      "com.softwaremill.macwire" %% "macros" % "2.3.1",
      "org.scalatest" %% "scalatest" % "3.0.5" % Test,
      "com.dimafeng" %% "testcontainers-scala" % "0.20.0" % Test,
      "org.testcontainers" % "postgresql" % "1.9.1" % Test,
      "com.storm-enroute" %% "scalameter-core" % "0.10.1" % Test
    ),
    dockerBaseImage := "openjdk:8-jre-alpine",
    Test / fork := true
  )
  .enablePlugins(JavaAppPackaging, AshScriptPlugin)
