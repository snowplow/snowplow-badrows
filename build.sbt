lazy val root = project.in(file("."))
  .settings(
    name := "snowplowbadrows",
    version := "0.1.0-M1",
    organization := "com.snowplowanalytics",
    scalaVersion := "2.11.12",
    initialCommands := "import com.snowplowanalytics.snowplowbadrows._"
  )
  .settings(BuildSettings.buildSettings)
  .settings(
    resolvers ++= Seq(
      "Sonatype OSS Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"
    ),
    libraryDependencies ++= Seq(

      Dependencies.igluCoreCirce,
      Dependencies.specs2
    )
  )
  .settings(BuildSettings.helpersSettings)
