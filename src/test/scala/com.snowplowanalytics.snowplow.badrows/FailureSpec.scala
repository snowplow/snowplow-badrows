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

import io.circe.literal._
import io.circe.syntax._
import org.specs2.Specification
import cats.data.NonEmptyList

class FailureSpec extends Specification {
  def is = s2"""
  encode CPFormatViolation $e1
  encode AdapterFailures $e2
  encode SchemaViolations $e3
  encode EnrichmentFailures $e4
  encode SizeViolation $e5
  """

  def e1 = {
    val f: Failure = CPFormatViolation(
      Instant.ofEpochMilli(1000L),
      "tsv",
      FallbackCPFormatViolationMessage("failure")
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
    val f: Failure = AdapterFailures(
      Instant.ofEpochMilli(1000L),
      "com.hubspot",
      "v1",
      NonEmptyList.one(NotSDAdapterFailure("{}", "not sd"))
    )
    val expected = json"""{
      "timestamp" : "1970-01-01T00:00:01Z",
      "vendor" : "com.hubspot",
      "version" : "v1",
      "messages" : [
        {
          "json" : "{}",
          "error" : "not sd"
        }
      ]
    }"""
    f.asJson must beEqualTo(expected)
  }

  def e3 = {
    val f: Failure = SchemaViolations(
      Instant.ofEpochMilli(1000L),
      NonEmptyList.one(NotSDSchemaViolation("{}", "not sd"))
    )
    val expected = json"""{
      "timestamp" : "1970-01-01T00:00:01Z",
      "messages" : [
        {
          "json" : "{}",
          "error" : "not sd"
        }
      ]
    }"""
    f.asJson must beEqualTo(expected)
  }

  def e4 = {
    val f: Failure = EnrichmentFailures(
      Instant.ofEpochMilli(1000L),
      NonEmptyList.one(EnrichmentFailure(None, SimpleEnrichmentFailureMessage("invalid api key")))
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
    val f: Failure = SizeViolation(
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
