lazy val root = project.in(file("."))
  .settings(
    name := "snowplowbadrows",
    version := "0.1.0-M1",
    organization := "com.snowplowanalytics",
    scalaVersion := "2.12.8",
    initialCommands := "import com.snowplowanalytics.snowplowbadrows._"
  )
  .settings(
    resolvers ++= Seq(
      "Sonatype OSS Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"
    ),
    libraryDependencies ++= Seq(
      Dependencies.circeJawn,
      Dependencies.circeLiteral,
      Dependencies.circeGeneric,
      Dependencies.igluCoreCirce,
      Dependencies.specs2
    )
  )
