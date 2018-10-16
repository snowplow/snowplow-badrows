/*
 * Copyright (c) 2017 Snowplow Analytics Ltd. All rights reserved.
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
import sbt._

object Dependencies {

  object V {
    val circe            = "0.9.0"
    val igluCore         = "0.3.0"
    val specs2           = "4.3.2"
  }

  val circeGeneric     = "io.circe"              %% "circe-generic"            % V.circe
  val circeJawn        = "io.circe"              %% "circe-jawn"               % V.circe          % "test"
  val circeLiteral     = "io.circe"              %% "circe-literal"            % V.circe          % "test"
  val igluCoreCirce    = "com.snowplowanalytics" %% "iglu-core-circe"          % V.igluCore
  // Scala (test only)
  val specs2           = "org.specs2"            %% "specs2-core"              % V.specs2         % "test"
}
