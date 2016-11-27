scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  "org.scalactic"       %% "scalactic"            % "3.0.0",
  "org.scalatest"       %% "scalatest"            % "3.0.0" % "test",
  "ch.qos.logback"      %  "logback-classic"      % "1.1.7",
  "com.github.mauricio" %% "mysql-async"          % "0.2.20"
)

lazy val commonSettings = Seq(
  name := "tpg",
  version := "0.1-SNAPSHOT",
  organization := "ai.rail",
  scalaVersion := "2.11.8",
  test in assembly := {}
)

lazy val app = (project in file("app")).
  settings(commonSettings: _*).
  settings(
    mainClass in assembly := Some("opentrack.tpg.Main")
  )
