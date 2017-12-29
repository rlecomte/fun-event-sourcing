import Dependencies._

val circeVersion = "0.9.0-M2"
val doobieVersion = "0.5.0-M9"
val h2Version= "1.3.170"
val flywayVersion = "5.0.2"

scalaOrganization := "org.typelevel"
scalaVersion := "2.12.4-bin-typelevel-4"

scalacOptions ++= Seq(
  "-deprecation", // Warn when deprecated API are used
  "-feature", // Warn for usages of features that should be importer explicitly
  "-unchecked", // Warn when generated code depends on assumptions
  "-Ywarn-dead-code", // Warn when dead code is identified
  "-Ywarn-numeric-widen", // Warn when numeric are widened
  "-Xlint", // Additional warnings (see scalac -Xlint:help)
  "-Ywarn-adapted-args", // Warn if an argument list is modified to match the receive
  "-Yliteral-types",
  "-language:postfixOps",
  "-language:implicitConversions",
  "-language:reflectiveCalls",
  "-language:existentials",
  "-language:higherKinds",
  "-language:experimental.macros",
  "-Xstrict-patmat-analysis",
  "-Xlint:strict-unsealed-patmat",
  "-Yinduction-heuristics"
)

lazy val root = (project in file(".")).
  settings(

    name := "Functional Event Sourcing",

    inThisBuild(List(
      organization := "io.es",
      scalaVersion := "2.12.4",
      version      := "0.1.0-SNAPSHOT"
    )),

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
