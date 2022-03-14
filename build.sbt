Global / onChangedBuildSource := ReloadOnSourceChanges

lazy val akkaHttpVersion = "10.2.9"
lazy val akkaVersion = "2.6.18"
lazy val slickVersion = "3.3.3"
lazy val upickleVersion = "1.5.0"
lazy val http4sVersion = "1.0.0-M31"
lazy val circeVersion = "0.14.1"
lazy val doobieVersion = "1.0.0-RC1"

lazy val root = (project in file("."))
  .settings(
    inThisBuild(List(organization := "org.alexeyn", scalaVersion := "2.13.8")),
    name := "akka-crud-service",
    libraryDependencies ++= Seq(
      "org.tpolecat" %% "doobie-core" % doobieVersion,
      "org.tpolecat" %% "doobie-postgres" % doobieVersion,
      "org.http4s" %% "http4s-blaze-server" % http4sVersion,
      "org.http4s" %% "http4s-circe" % http4sVersion,
      "org.http4s" %% "http4s-dsl" % http4sVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "io.circe" %% "circe-core" % circeVersion,
      "com.typesafe.scala-logging" %% "scala-logging" % "3.9.4",
      "ch.qos.logback" % "logback-classic" % "1.2.11",
      "org.postgresql" % "postgresql" % "42.3.3",
      "com.github.pureconfig" %% "pureconfig" % "0.17.1",
      "eu.timepit" %% "refined-pureconfig" % "0.9.28",
      "com.typesafe.slick" %% "slick" % slickVersion,
      "com.typesafe.slick" %% "slick-hikaricp" % slickVersion,
      "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-stream" % akkaVersion,
      "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test,
      "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
      "de.heikoseeberger" %% "akka-http-upickle" % "1.39.2",
      "com.lihaoyi" %% "upickle" % upickleVersion,
      "com.lihaoyi" %% "ujson" % upickleVersion,
      "com.softwaremill.macwire" %% "macros" % "2.5.6",
      "org.scalatest" %% "scalatest" % "3.2.11" % Test,
      "com.dimafeng" %% "testcontainers-scala" % "0.40.2" % Test,
      "org.testcontainers" % "postgresql" % "1.16.3" % Test,
      "com.storm-enroute" %% "scalameter-core" % "0.21" % Test
    ),
    dockerBaseImage := "openjdk:8-jre-alpine",
    Test / fork := true,
    addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.10.3")
  )
  .enablePlugins(JavaAppPackaging, AshScriptPlugin)

scalacOptions ++= Seq("-deprecation", "-encoding", "UTF-8", "-language:higherKinds", "-language:postfixOps", "-feature")
