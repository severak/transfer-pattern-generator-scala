name := "tpg"

version := "1.0"

scalaVersion := "2.12.0"

libraryDependencies += "org.scalactic" %% "scalactic" % "3.0.0"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.0" % "test"

libraryDependencies ++= Seq(
  "org.scalikejdbc" %% "scalikejdbc"          % "2.5.0",
  "org.scalikejdbc" %% "scalikejdbc-config"   % "2.5.0",
  "ch.qos.logback"  %  "logback-classic"      % "1.1.7",
  "mysql"           %  "mysql-connector-java" % "5.1.40",
  "com.zaxxer"      %  "HikariCP"             % "2.5.1"
)

lazy val commonSettings = Seq(
  version := "0.1-SNAPSHOT",
  organization := "ai.rail",
  scalaVersion := "2.12.0",
  test in assembly := {}
)

lazy val app = (project in file("app")).
  settings(commonSettings: _*).
  settings(
    mainClass in assembly := Some("opentrack.tpg.Main")
  )
