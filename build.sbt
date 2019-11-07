/*
 * Copyright (c) 2018-2019 Snowplow Analytics Ltd. All rights reserved.
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

lazy val root = project.in(file("."))
  .settings(
    name := "snowplow-badrows",
    version := "0.1.0-M7",
    organization := "com.snowplowanalytics",
    scalaVersion := "2.12.10",
    scalacOptions := BuildSettings.scalacOptions,
    cancelable in Global := true,
    scalacOptions in (Compile, console) --= Seq("-Ywarn-unused:imports", "-Xfatal-warnings")
  )
  .settings(
    libraryDependencies := Dependencies.All,
    resolvers += Dependencies.SnowplowBintray
  )
  .settings(BuildSettings.publishSettings)
