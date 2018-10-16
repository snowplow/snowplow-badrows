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

import io.circe.jawn.parse

import java.util.Base64

import org.specs2.Specification

import cats.data.NonEmptyList

import Payload._
import BadRow._
import com.snowplowanalytics.iglu.core.circe.instances._

import io.circe.literal._

class BadRowSpec extends Specification { def is = s2"""
  Encode tracker_protocol_violation $e1
  Encode iglu_violation $e2
  """

  val exampleCollector = CollectorMeta("stream-collector", "utf-8", None, None, None, None, None, Nil, None)

  def e1 = {
    val violation =
      BadRow.TrackerProtocolViolation(SingleEvent(exampleCollector, Map("eid" -> "foo")), "eid does not match UUID format", "spark-enrich-1.14.0")

    val expected =
      json"""{
        "schema": "iglu:com.snowplowanalytics.snowplow.badrows/tracker_protocol_violation/jsonschema/1-0-0",
        "data": {
            "payload" : "c3RyZWFtLWNvbGxlY3Rvcgl1dGYtOAkJCQkJCQkJeyJlaWQiOiJmb28ifQ==",
            "message" : "eid does not match UUID format",
            "processor" : "spark-enrich-1.14.0"
        }
      }"""

    violation.toJson.normalize must beEqualTo(expected)
  }

  def e2 = {
    val jsonPayload = """{"data": {}}"""
    val payload = Map(
      "eid" -> "f5fb3fb5-1c6d-43b5-bc18-6b9ef496ae31",
      "cx" -> encode(jsonPayload)
    )
    val errors = NonEmptyList.of(IgluParseError.InvalidPayload(parse(jsonPayload).fold(throw _, identity)))

    val violation =
      BadRow.IgluViolation(SingleEvent(exampleCollector, payload), errors, "stream-enrich-0.11.0")

    val expected = json"""{
        "schema" : "iglu:com.snowplowanalytics.snowplow.badrows/iglu_violation/jsonschema/1-0-0",
        "data" : {
          "payload" : "c3RyZWFtLWNvbGxlY3Rvcgl1dGYtOAkJCQkJCQkJeyJlaWQiOiJmNWZiM2ZiNS0xYzZkLTQzYjUtYmMxOC02YjllZjQ5NmFlMzEiLCJjeCI6ImV5SmtZWFJoSWpvZ2UzMTkifQ==",
          "errors" : [
            {
              "error" : "INVALID_PAYLOAD",
              "json" : {"data" : {}}
            }
          ],
          "processor" : "stream-enrich-0.11.0"
        }
      }"""

    violation.toJson.normalize must beEqualTo(expected)
  }

  private def encode(s: String): String =
    new String(Base64.getEncoder.encode(s.getBytes()))
}
