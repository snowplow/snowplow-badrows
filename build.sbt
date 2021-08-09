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

lazy val root = project.in(file("."))
  .settings(
    name := "snowplow-badrows",
    organization := "com.snowplowanalytics",
    scalaVersion := "2.12.11",
    crossScalaVersions := Seq("2.12.11", "2.13.2"),
    cancelable in Global := true
  )
  .settings(
    libraryDependencies := Dependencies.All,
  )
  .settings(BuildSettings.publishSettings)
