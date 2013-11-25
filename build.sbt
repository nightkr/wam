val scalatestVersion = "2.0"

scalaVersion := "2.10.3"

scalacOptions += "-feature"

libraryDependencies += "org.scalatest" %% "scalatest" % scalatestVersion % "test"

libraryDependencies += "org.scalautils" %% "scalautils" % scalatestVersion

libraryDependencies += "com.jsuereth" %% "scala-arm" % "1.3"

ScctPlugin.instrumentSettings

CoverallsPlugin.coverallsSettings

