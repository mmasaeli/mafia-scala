ThisBuild / scalaVersion := "2.12.12"
ThisBuild / organization := "com.example"

val AkkaVersion = "2.6.11"

lazy val mafia = (project in file("."))
  .settings(
    name := "Mafia"
  )

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % AkkaVersion
libraryDependencies += "com.bot4s" %% "telegram-core" % "4.4.0-RC2"
libraryDependencies += "commons-lang" % "commons-lang" % "2.6"
//libraryDependencies += "org.telegram" % "telegrambots" % "5.0.1"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % Test
libraryDependencies += "com.typesafe.akka" %% "akka-testkit" % AkkaVersion % Test
