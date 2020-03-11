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
package com.snowplowanalytics.snowplow.badrows.generators

import cats.data.NonEmptyList

import com.snowplowanalytics.snowplow.badrows.Failure

import org.scalacheck.Gen

object FailureGen {
  // 1Mb
  private val MaxBytesSize = 1000 * 1000

  val cpFormatViolationFailure: Gen[Failure.CPFormatViolation] =
    for {
      timestamp <- CommonGen.instantGen
      loader    <- Gen.oneOf("clj-tomcat", "cloudfront", "ndjson", "thrift", "tsv")
      message   <- FailureDetailsGen.cpFormatViolationMessage
    } yield Failure.CPFormatViolation(timestamp, loader, message)

  val sizeViolationFailure: Gen[Failure.SizeViolation] =
    for {
      timestamp           <- CommonGen.instantGen
      maximumBytesAllowed  = MaxBytesSize
      actualSizeBytes     <- Gen.chooseNum(MaxBytesSize, MaxBytesSize * 100)
      expectation          = "oversize collector payload"
    } yield Failure.SizeViolation(timestamp, maximumBytesAllowed, actualSizeBytes, expectation)

  val adapterFailures: Gen[Failure.AdapterFailures] =
    for {
      timestamp <- CommonGen.instantGen
      vendor    <- CommonGen.strGen(64, Gen.alphaNumChar)
      version   <- CommonGen.strGen(16, Gen.alphaNumChar)
      messages  <- CommonGen.nonEmptyListOfMaxN(12, FailureDetailsGen.adapterFailure).map(NonEmptyList.fromListUnsafe)
    } yield Failure.AdapterFailures(timestamp, vendor, version, messages)

  val trackerProtocolViolations: Gen[Failure.TrackerProtocolViolations] =
    for {
      timestamp <- CommonGen.instantGen
      vendor    <- CommonGen.strGen(64, Gen.alphaNumChar)
      version   <- CommonGen.strGen(16, Gen.alphaNumChar)
      messages  <- Gen.nonEmptyListOf(FailureDetailsGen.trackerProtocolViolation).map(NonEmptyList.fromListUnsafe)
    } yield Failure.TrackerProtocolViolations(timestamp, vendor, version, messages)

  val schemaViolations: Gen[Failure.SchemaViolations] =
    for {
      timestamp <- CommonGen.instantGen
      messages <- Gen.nonEmptyListOf(FailureDetailsGen.schemaViolation).map(NonEmptyList.fromListUnsafe)
    } yield Failure.SchemaViolations(timestamp, messages)

  val enrichmentFailure: Gen[Failure.EnrichmentFailures] =
    for {
      timestamp <- CommonGen.instantGen
      messages  <- Gen.nonEmptyListOf(FailureDetailsGen.enrichmentFailure).map(NonEmptyList.fromListUnsafe)
    } yield Failure.EnrichmentFailures(timestamp, messages)

  val recoveryFailure: Gen[Failure.RecoveryFailure] =
    for {
      message <- Gen.alphaNumStr
      configName <- Gen.option(Gen.alphaNumStr)
    } yield Failure.RecoveryFailure(message, configName)
}
