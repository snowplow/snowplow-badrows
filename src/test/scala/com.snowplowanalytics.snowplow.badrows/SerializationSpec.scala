/*
 * Copyright (c) 2018-2023 Snowplow Analytics Ltd. All rights reserved.
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

import io.circe.literal._
import io.circe.{Json, ParsingFailure}
import io.circe.jawn.JawnParser
import org.scalacheck.Prop.forAll
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification
import org.specs2.matcher.Matcher

import com.snowplowanalytics.snowplow.badrows.generators.BadRowGen
import com.snowplowanalytics.iglu.core.SelfDescribingData

class SerializationSpec extends Specification with ScalaCheck {

  private val parser = new JawnParser

  "bad rows must roundtrip via json string" >>
      forAll(BadRowGen.anyBadRowGen) { input =>
        val roundTripped = parser.parse(input.compact)
        roundTripped must matchOriginal(input)
    }

  def matchOriginal(expected: BadRow): Matcher[Either[ParsingFailure, Json]] = { (result: Either[ParsingFailure, Json]) =>
    result must beRight { (json: Json) =>
      json.as[SelfDescribingData[BadRow]] must beRight { (sdd: SelfDescribingData[BadRow]) =>
        sdd.data must beEqualTo(expected)
      }
    }
  }
}
