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

package com.snowplowanalytics.snowplow.badrows

import java.time.Instant

import cats.data.NonEmptyList

import io.circe.literal._
import io.circe.syntax._

import org.specs2.Specification

import com.snowplowanalytics.iglu.core.ParseError

class FailureSpec extends Specification {
  import FailureDetails._

  def is = s2"""
  encode CPFormatViolation $e1
  encode AdapterFailures $e2
  encode SchemaViolations $e3
  encode EnrichmentFailures $e4
  encode SizeViolation $e5
  """

  def e1 = {
    import CPFormatViolationMessage._
    val f: Failure = Failure.CPFormatViolation(
      Instant.ofEpochMilli(1000L),
      "tsv",
      Fallback("failure")
    )
    val expected = json"""{
      "timestamp" : "1970-01-01T00:00:01Z",
      "loader" : "tsv",
      "message" : {
        "error" : "failure"
      }
    }"""
    f.asJson must beEqualTo(expected)
  }

  def e2 = {
    import AdapterFailure._
    val f: Failure = Failure.AdapterFailures(
      Instant.ofEpochMilli(1000L),
      "com.hubspot",
      "v1",
      NonEmptyList.one(NotIglu(json"{}", ParseError.InvalidSchemaVer))
    )
    val expected = json"""{
      "timestamp" : "1970-01-01T00:00:01Z",
      "vendor" : "com.hubspot",
      "version" : "v1",
      "messages" : [
        {
          "json" : {},
          "error" : "INVALID_SCHEMAVER"
        }
      ]
    }"""
    f.asJson must beEqualTo(expected)
  }

  def e3 = {
    import SchemaViolation._
    val f: Failure = Failure.SchemaViolations(
      Instant.ofEpochMilli(1000L),
      NonEmptyList.one(NotIglu(json"{}", ParseError.InvalidData))
    )
    val expected = json"""{
      "timestamp" : "1970-01-01T00:00:01Z",
      "messages" : [
        {
          "json" : {},
          "error" : "INVALID_DATA_PAYLOAD"
        }
      ]
    }"""
    f.asJson must beEqualTo(expected)
  }

  def e4 = {
    import EnrichmentFailureMessage._
    val f: Failure = Failure.EnrichmentFailures(
      Instant.ofEpochMilli(1000L),
      NonEmptyList.one(EnrichmentFailure(None, Simple("invalid api key")))
    )
    val expected = json"""{
      "timestamp": "1970-01-01T00:00:01Z",
      "messages": [
        {
          "enrichment": null,
          "message": {
            "error": "invalid api key"
          }
        }
      ]
    }"""
    f.asJson must beEqualTo(expected)
  }

  def e5 = {
    val f: Failure = Failure.SizeViolation(
      Instant.ofEpochMilli(1000L),
      200,
      400,
      "exceeded"
    )
    val expected = json"""{
      "timestamp": "1970-01-01T00:00:01Z",
      "maximumAllowedSizeBytes": 200,
      "actualSizeBytes": 400,
      "expectation": "exceeded"
    }"""
    f.asJson must beEqualTo(expected)
  }
}
