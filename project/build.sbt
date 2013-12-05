resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

addSbtPlugin("com.github.scct" % "sbt-scct" % "0.3-SNAPSHOT")

//addSbtPlugin("com.github.theon" %% "xsbt-coveralls-plugin" % "0.0.5-SNAPSHOT")
// See theon/xsbt-coveralls-plugin#18, currently using a snapshot build of the pull request (9c65aca24e875ff3f705c49843b6315cd3bf54a5)
// Therefore, we need to manually pull in the plugin's dependencies manually...
libraryDependencies ++= Seq (
  "org.scala-lang" % "scala-reflect" % scalaVersion.value,
  "com.fasterxml.jackson.core" % "jackson-core" % "2.2.3",
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.2.3",
  "org.scalaj" %% "scalaj-http" % "0.3.6",
  "org.scalatest" %% "scalatest" % "1.9.1" % "test",
  "org.mockito" % "mockito-core" % "1.9.5"
)