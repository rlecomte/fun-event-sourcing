import Dependencies._


lazy val commonSettings = Seq(
  name := "Functional Event Sourcing",
  organization := "io.es",
  version := "0.1.0-SNAPSHOT",
  scalaVersion := "2.12.6",
  addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.6")
)

lazy val root = (project in file("."))
  .settings(inThisBuild(commonSettings))
  .aggregate(infra, testkit, `domain-example`)

lazy val infra = (project in file("infra"))
  .settings(
    inThisBuild(commonSettings),
    libraryDependencies += scalaTest % Test,
    libraryDependencies += fs2,
    libraryDependencies ++= circe,
    libraryDependencies += catsMtl
  )

lazy val testkit = (project in file("testkit"))

lazy val `domain-example` = (project in file("domain-example"))
  .settings(inThisBuild(commonSettings))
  .dependsOn(infra)
  .dependsOn(testkit)