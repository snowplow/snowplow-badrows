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
package com.snowplowanalytics.snowplow.badrows.generators

import com.snowplowanalytics.snowplow.badrows.{NVP, Payload, SpecHelpers}
import org.scalacheck.{Arbitrary, Gen}

object PayloadGen {
  val rawPayload: Gen[Payload.RawPayload] =
    Gen.alphaNumStr.map { line => Payload.RawPayload(line) }

  val rawEvent: Gen[Payload.RawEvent] =
    for {
      vendor      <- CommonGen.strGen(64, Gen.alphaLowerChar)
      version     <- CommonGen.strGen(16, Gen.alphaNumChar)
      querystring <- Arbitrary.arbitrary[Map[String, String]]
      contentType <- Gen.option(Gen.oneOf("text/plain", "application/json"))
      loaderName  <- CommonGen.strGen(32, Gen.alphaNumChar)
      encoding    <- CommonGen.strGen(32, Gen.alphaNumChar)
      hostname    <- Gen.option(Gen.alphaNumStr)
      timestamp   <- Gen.option(CommonGen.dateTimeGen)
      ipAddress   <- Gen.option(CommonGen.ipAddress)
      optStr      <- Gen.option(Gen.identifier) // The whole generator needs to be updated with more meaningful values
      headers     <- CommonGen.listOfMaxN(12, Gen.identifier)
      userId      <- Gen.option(Gen.uuid)
    } yield Payload.RawEvent(vendor, version, querystring, contentType, loaderName, encoding, hostname, timestamp, ipAddress, optStr, optStr, headers, userId)

  private val nvp: Gen[NVP] =
    for {
      name <- CommonGen.strGen(512, Gen.alphaNumChar)
      value <- Gen.option(CommonGen.strGen(512, Gen.alphaNumChar))
    } yield NVP(name, value)

  val partiallyEnrichedEvent: Gen[Payload.PartiallyEnrichedEvent] =
    Gen.const(Payload.PartiallyEnrichedEvent(None,None,None,"2019-10-22",None,None,None,None,None,None,"0.10.0","0.2.0",None,None,
      None,None,None,None,None,None,None,None,None,None,None,None,None,None,None,None,None,None,None,None,None,None,
      None,None,None,None,None,None,None,None,None,None,None,None,None,None,None,None,None,None,None,None,None,None,
      None,None,None,None,None,None,None,None,None,None,None,None,None,None,None,None,None,None,None,None,None,None,
      None,None,None,None,None,None,None,None,None,None,None,None,None,None,None,None,None,None,None,None,None,None,
      None,None,None,None,None,None,None,None,None,None,None,None,None,None,None,None,None,None,None,None,None,None,
      None,None,None,None,None,None,None))

  val collectorPayload: Gen[Payload.CollectorPayload] =
    for {
      vendor      <- CommonGen.strGen(64, Gen.alphaLowerChar)
      version     <- CommonGen.strGen(16, Gen.alphaNumChar)
      querystring <- CommonGen.listOfMaxN(100, nvp)
      body        <- Gen.option(Arbitrary.arbitrary[String])
      contentType <- Gen.option(Gen.oneOf("text/plain", "application/json", "application/json; encoding=utf-8" ))
      collector   <- CommonGen.strGen(32, Gen.alphaNumChar)
      encoding    <- CommonGen.strGen(32, Gen.alphaNumChar)
      timestamp   <- Gen.option(CommonGen.dateTimeGen)
      ipAddress   <- Gen.option(CommonGen.ipAddress)
      optStr      <- Gen.option(Gen.identifier) // The whole generator needs to be updated with more meaningful values
      headers     <- CommonGen.listOfMaxN(12, Gen.identifier)
      userId      <- Gen.option(Gen.uuid)
    } yield Payload.CollectorPayload(vendor, version, querystring, contentType, body, collector, encoding, optStr, timestamp, ipAddress, optStr, optStr, headers, userId)

  val enrichmentPayload: Gen[Payload.EnrichmentPayload] =
    for {
      event <- partiallyEnrichedEvent
      raw   <- rawEvent
    } yield Payload.EnrichmentPayload(event, raw)

  val loaderPayload: Gen[Payload.LoaderPayload] =
    Gen.const(Payload.LoaderPayload(SpecHelpers.ExampleEvent))
}
