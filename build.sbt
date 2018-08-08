import Dependencies._

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "com.example",
      scalaVersion := "2.12.4",
      version      := "0.1.0-SNAPSHOT"
    )),
    name := "Functional Event Sourcing",
    libraryDependencies += scalaTest % Test,
    libraryDependencies += fs2,
    libraryDependencies ++= circe,
    libraryDependencies += catsFree,
    addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.6")
  )
