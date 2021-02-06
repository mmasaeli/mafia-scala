ThisBuild / scalaVersion := "2.12.12"
ThisBuild / organization := "com.example"

lazy val mafia = (project in file("."))
  .settings(
    name := "Mafia"
  )

libraryDependencies += "info.mukel" %% "telegrambot4s" % "3.0.14"
libraryDependencies += "commons-lang" % "commons-lang" % "2.6"

libraryDependencies += "org.springframework.boot" % "spring-boot-starter" % "2.4.2"
libraryDependencies += "org.springframework.boot" % "spring-boot-configuration-processor" % "2.4.2"
libraryDependencies += "org.springframework.data" % "spring-data-redis" % "2.4.2"
libraryDependencies += "redis.clients" % "jedis" % "3.5.1"

mainClass in Compile := Some("org.masood.mafia.MafiaApplication")
