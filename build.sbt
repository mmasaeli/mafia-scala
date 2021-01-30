ThisBuild / scalaVersion := "2.12.12"
ThisBuild / organization := "com.example"

lazy val mafia = (project in file("."))
  .settings(
    name := "Mafia"
  )

libraryDependencies += "com.bot4s" %% "telegram-core" % "4.4.0-RC2"
libraryDependencies += "commons-lang" % "commons-lang" % "2.6"

libraryDependencies += "org.springframework.boot" % "spring-boot-starter-web" % "2.4.2"
libraryDependencies += "org.springframework.boot" % "spring-boot-configuration-processor" % "2.4.2"
//libraryDependencies += "org.springframework.boot" % "spring-boot-starter-data-redis" % "2.4.2"
libraryDependencies += "org.springframework.data" % "spring-data-redis" % "2.4.2"
libraryDependencies += "redis.clients" % "jedis" % "3.5.1"
//libraryDependencies += "io.lettuce" % "lettuce-core" % "6.0.2.RELEASE"

mainClass in Compile := Some("org.masood.mafia.MyServiceApplication")
