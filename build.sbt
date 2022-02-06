import Settings.module

ThisBuild / version := "0.1"
ThisBuild / organization := "io.github.oybek"
ThisBuild / scalaVersion := "3.1.1"
ThisBuild / scalacOptions ++= Seq(
 "-encoding", "utf8", // Option and arguments on same line
 "-Xfatal-warnings",  // New lines for each options
 "-deprecation",
 "-unchecked",
 "-language:implicitConversions",
 "-language:higherKinds",
 "-language:existentials",
 "-language:postfixOps"
)

lazy val common = module(name = "common", file = file("common"))
lazy val cstrike = module("cstrike", file("cstrike"))
lazy val scenario = module("scenario", file("scenario"), playcs)
lazy val database = module("database", file("database"))
lazy val playcs = module("playcs", file("."), cstrike, common, database)

// Custom tasks
lazy val testAll = taskKey[Unit]("Run all tests")
testAll := {
  (common / assembly / test).value
  (cstrike / assembly / test).value
  (database / assembly / test).value
  (scenario / assembly / test).value
  (assembly / test).value
}
