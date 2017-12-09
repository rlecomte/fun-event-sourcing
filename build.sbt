import Dependencies._

val circeVersion = "0.9.0-M2"
val doobieVersion = "0.5.0-M9"
val h2Version= "1.3.170"
val flywayVersion = "5.0.2"

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "io.es",
      scalaVersion := "2.12.4",
      version      := "0.1.0-SNAPSHOT"
    )),
    name := "Functional Event Sourcing",
    libraryDependencies += scalaTest % Test,
    libraryDependencies += "org.flywaydb" % "flyway-core" % flywayVersion,
    libraryDependencies += "com.h2database" % "h2" % h2Version,
    libraryDependencies ++= Seq(
      "io.circe" %% "circe-core",
      "io.circe" %% "circe-generic",
      "io.circe" %% "circe-parser"
    ).map(_ % circeVersion),
    libraryDependencies ++= Seq(
      "org.tpolecat" %% "doobie-core",
      "org.tpolecat" %% "doobie-hikari",
      "org.tpolecat" %% "doobie-postgres"
    ).map(_ % doobieVersion)
  )
