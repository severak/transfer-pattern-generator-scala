name := "tpg"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies += "org.scalactic" %% "scalactic" % "3.0.0"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.0" % "test"

libraryDependencies ++= Seq(
  "org.scalikejdbc" %% "scalikejdbc"         % "2.4.2",
  "com.h2database"  %  "h2"                  % "1.4.192",
  "ch.qos.logback"  %  "logback-classic"     % "1.1.7",
  "mysql"           % "mysql-connector-java" % "5.1.40"
)