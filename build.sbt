/*
 * Copyright (c) 2018-2020 Snowplow Analytics Ltd. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */

import BuildSettings.{ scalacOptions => opts, scalacOptions212 }

lazy val root = project.in(file("."))
  .settings(
    name := "snowplow-badrows",
    version := "1.0.0-M2",
    organization := "com.snowplowanalytics",
    scalaVersion := "2.12.10",
    crossScalaVersions := Seq("2.11.12", "2.12.10"),
    scalacOptions := { if (scalaVersion.value.startsWith("2.11")) opts else opts ++ scalacOptions212 },
    cancelable in Global := true,
    scalacOptions in (Compile, console) --= Seq("-Ywarn-unused:imports", "-Xfatal-warnings")
  )
  .settings(
    libraryDependencies := Dependencies.All,
    resolvers += Dependencies.SnowplowBintray
  )
  .settings(BuildSettings.publishSettings)
