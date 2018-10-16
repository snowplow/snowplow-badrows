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

package com.snowplowanalytics.snowplowbadrows

import org.specs2.Specification

import Payload._
import BadRow._
import com.snowplowanalytics.iglu.core.circe.instances._

import io.circe.literal._

class BadRowSpec extends Specification { def is = s2"""
  One plus one test $e1
  """

  val exampleCollector = CollectorMeta("stream-collector", "utf-8", None, None, None, None, None, Nil, None)

  def e1 = {
    val violation =
      BadRow.TrackerProtocolViolation(SingleEvent(exampleCollector, Map("eid" -> "foo")), "eid does not have UUID format", "spark-enrich")

    val expected =
      json"""{
        "schema": "iglu:com.snowplowanalytics.snowplow.badrows/tracker_protocol_violation/jsonschema/1-0-0",
        "data": {
            "payload" : "c3RyZWFtLWNvbGxlY3Rvcgl1dGYtOAkJCQkJCQkJeyJlaWQiOiJmb28ifQ==",
            "message" : "eid does not have UUID format",
            "processor" : "spark-enrich"
        }
      }"""

    violation.toJson.normalize must beEqualTo(expected)


  }
}
