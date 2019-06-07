
lazy val circeVersion = "0.11.1"
lazy val igluCoreVersion = "0.5.1"
lazy val igluClientVersion = "0.6.0-M7"
lazy val specs2Version = "4.5.1"

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
    libraryDependencies ++= (Seq(
      "io.circe" %% "circe-generic",
      "io.circe" %% "circe-java8",
      "io.circe" %% "circe-jawn",
      "io.circe" %% "circe-literal"
    ).map(_ % circeVersion) match {
      case h1 :: h2 :: t => h1 :: h2 :: t.map(_ % Test)
    }) ++ Seq(
      "com.snowplowanalytics" %% "iglu-core-circe" % igluCoreVersion,
      "com.snowplowanalytics" %% "iglu-scala-client" % igluClientVersion,
      "org.specs2" %% "specs2-core" % specs2Version % Test
    )
  )
