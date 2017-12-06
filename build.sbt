import Dependencies._

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "com.example",
      scalaVersion := "2.12.4",
      version      := "0.1.0-SNAPSHOT"
    )),
    name := "Functional Event Sourcing",
    libraryDependencies += "org.typelevel" %% "cats-core" % "1.0.0-RC1",
    libraryDependencies += scalaTest % Test
  )
