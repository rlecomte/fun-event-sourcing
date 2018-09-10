import sbt._

object Dependencies {
  val scalaTestVersion = "3.0.3"
  val circeVersion = "0.9.3"
  val fs2Version = "0.10.4"
  val catsMtlVersion = "0.2.1"

  lazy val scalaTest = "org.scalatest" %% "scalatest" % scalaTestVersion
  lazy val fs2 = "co.fs2" %% "fs2-core" % fs2Version

  lazy val circe = Seq(
    "io.circe" %% "circe-core",
    "io.circe" %% "circe-generic",
    "io.circe" %% "circe-parser"
  ).map(_ % circeVersion)

  lazy val catsMtl = "org.typelevel" %% "cats-mtl-core" % catsMtlVersion
}
