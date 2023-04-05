/*
 * Copyright (c) 2018-2021 Snowplow Analytics Ltd. All rights reserved.
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
    val cats         = "2.6.1"
    val circe        = "0.14.1"
    val igluClient   = "1.4.0"
    val jodaTime     = "2.10.10"
    val analyticsSdk = "2.1.0"
    val specs2       = "4.9.4"
    val scalaCheck   = "1.14.3"
  }

  val All = List(
    "org.typelevel"         %% "cats-core"                    % V.cats,
    "io.circe"              %% "circe-generic"                % V.circe,
    "com.snowplowanalytics" %% "iglu-scala-client"            % V.igluClient excludeAll(
      // We only use iglu-scala-client for the simple case classes it defines.
      // This excludes anything related to http transactions.
      ExclusionRule.everything
    ),
    "com.snowplowanalytics" %% "snowplow-scala-analytics-sdk" % V.analyticsSdk,
    "joda-time"             %  "joda-time"                    % V.jodaTime,

    "io.circe"              %% "circe-jawn"                   % V.circe      % Test,
    "io.circe"              %% "circe-literal"                % V.circe      % Test,
    "org.specs2"            %% "specs2-core"                  % V.specs2     % Test,
    "org.specs2"            %% "specs2-scalacheck"            % V.specs2     % Test,
    "org.scalacheck"        %% "scalacheck"                   % V.scalaCheck % Test,
    "com.snowplowanalytics" %% "iglu-scala-client"            % V.igluClient % Test
  )
}
