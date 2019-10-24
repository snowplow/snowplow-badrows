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
import sbt._

object Dependencies {

  // Speed-up fetching of Snowplow assets
  lazy val SnowplowBintray = "Snowplow Bintray" at "https://snowplow.bintray.com/snowplow-maven"

  object V {
    val circe        = "0.11.1"
    val igluClient   = "0.6.1-M1"
    val jodaTime     = "2.10.1"
    val analyticsSdk = "1.0.0-M2"
    val specs2       = "4.8.0"
    val scalaCheck   = "1.14.0"
  }

  val All = List(
    "io.circe"              %% "circe-generic"                % V.circe,
    "io.circe"              %% "circe-java8"                  % V.circe,
    "com.snowplowanalytics" %% "iglu-scala-client"            % V.igluClient,
    "com.snowplowanalytics" %% "snowplow-scala-analytics-sdk" % V.analyticsSdk,
    "joda-time"             %  "joda-time"                    % V.jodaTime,

    "io.circe"              %% "circe-jawn"                   % V.circe      % Test,
    "io.circe"              %% "circe-literal"                % V.circe      % Test,
    "org.specs2"            %% "specs2-core"                  % V.specs2     % Test,
    "org.specs2"            %% "specs2-scalacheck"            % V.specs2     % Test,
    "org.scalacheck"        %% "scalacheck"                   % V.scalaCheck % Test
  )
}
