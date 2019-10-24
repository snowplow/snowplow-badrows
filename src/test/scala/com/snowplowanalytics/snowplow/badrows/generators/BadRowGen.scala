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

import cats.data.NonEmptyList

import org.scalacheck.Gen

import com.snowplowanalytics.snowplow.badrows.{BadRow, Failure, FailureDetails}
import com.snowplowanalytics.snowplow.badrows.SchemaValidationSpec.{mockJsonValue, mockProcessor}

object BadRowGen {

  val sizeViolation: Gen[BadRow.SizeViolation] =
    for {
      failure <- FailureGen.sizeViolationFailure
      payload <- PayloadGen.rawPayload
    } yield BadRow.SizeViolation(mockProcessor, failure, payload)

  val cpFormatViolation: Gen[BadRow.CPFormatViolation] =
    for {
      payload <- PayloadGen.rawPayload
      failure <- FailureGen.cpFormatViolationFailure
    } yield BadRow.CPFormatViolation(mockProcessor, failure, payload)

  val adapterFailures: Gen[BadRow.AdapterFailures] =
    for {
      adapterFailures <- FailureGen.adapterFailures
      collectorPayload <- PayloadGen.collectorPayload
    } yield BadRow.AdapterFailures(mockProcessor, adapterFailures, collectorPayload)

  val trackerProtocolViolations: Gen[BadRow.TrackerProtocolViolations] =
    for {
      failure <- FailureGen.trackerProtocolViolations
      payload <- PayloadGen.collectorPayload
    } yield BadRow.TrackerProtocolViolations(mockProcessor, failure, payload)

  val schemaViolations: Gen[BadRow.SchemaViolations] =
    for {
      failure <- FailureGen.schemaViolations
      payload <- PayloadGen.enrichmentPayload
    } yield BadRow.SchemaViolations(mockProcessor, failure, payload)

  val enrichmentFailures: Gen[BadRow.EnrichmentFailures] =
    for {
      failure <- FailureGen.enrichmentFailure
      payload <- PayloadGen.enrichmentPayload
    } yield BadRow.EnrichmentFailures(mockProcessor, failure, payload)

  val loaderParsingErrors: Gen[BadRow.LoaderParsingErrors] =
    for {
      payload <- PayloadGen.rawPayload
      failure <- Gen.nonEmptyListOf(Gen.alphaNumStr).map { errors =>
        Failure.LoaderParsingErrors(NonEmptyList.fromListUnsafe(errors))
      }
    } yield BadRow.LoaderParsingErrors(mockProcessor, failure, payload)

  val loaderIgluError: Gen[BadRow.LoaderIgluError] =
    for {
      payload <- PayloadGen.loaderPayload
      failure <- Gen.nonEmptyListOf(
        CommonGen.schemaKey.flatMap { schemaKey =>
          IgluClientErrorGen.clientError.map { error => FailureDetails.LoaderIgluError(schemaKey, error) }
        }
      ).map(l => Failure.LoaderIgluErrors(NonEmptyList.fromListUnsafe(l)))
    } yield BadRow.LoaderIgluError(mockProcessor, failure, payload)

  val loaderRuntimeErrorBadRowGen: Gen[BadRow.LoaderRuntimeErrors] =
    for {
      payload <- PayloadGen.loaderPayload
      failure <- Gen.alphaNumStr.map { e => Failure.LoaderRuntimeErrors(e) }
    } yield BadRow.LoaderRuntimeErrors(mockProcessor, failure, payload)

  val bqCastErrors: Gen[BadRow.BQCastErrors] =
    for {
      payload <- PayloadGen.loaderPayload
      failure <- Gen.nonEmptyListOf(
        for {
          schemaKey <- CommonGen.schemaKey
          bqCastErrorGen <- Gen.oneOf(
            bqFieldTypeGen.map(FailureDetails.BQCastErrorInfo.WrongType(mockJsonValue, _)),
            bqFieldTypeGen.map(FailureDetails.BQCastErrorInfo.NotAnArray(mockJsonValue, _)),
            Gen.alphaNumStr.map(FailureDetails.BQCastErrorInfo.MissingInValue(_, mockJsonValue))
          )
          bqCastErrors <- Gen.nonEmptyListOf(bqCastErrorGen)
        } yield FailureDetails.BQCastError(mockJsonValue, schemaKey, NonEmptyList.fromListUnsafe(bqCastErrors))
      ).map(e => Failure.BQCastErrors(NonEmptyList.fromListUnsafe(e)))
    } yield BadRow.BQCastErrors(mockProcessor, failure, payload)

  val bqRepeaterParsingErrorGen: Gen[BadRow.BQRepeaterParsingError] =
    for {
      payload <- PayloadGen.rawPayload
      failure <- for {
        message <- Gen.alphaNumStr
        location <- Gen.nonEmptyListOf(Gen.alphaNumStr)
      } yield Failure.BQRepeaterParsingError(message, location)
    } yield BadRow.BQRepeaterParsingError(mockProcessor, failure, payload)


  val bqRepeaterPubSubError: Gen[BadRow.BQRepeaterPubSubError] =
    for {
      payload <- PayloadGen.rawPayload
      failure <- Gen.alphaNumStr.map(e => Failure.BQRepeaterPubSubError(e))
    } yield BadRow.BQRepeaterPubSubError(mockProcessor, failure, payload)

  val bqRepeaterBigQueryError: Gen[BadRow.BQRepeaterBigQueryError] =
    for {
      payload <- PayloadGen.bqReconstructedEvent
      failure <- for {
        reason   <- Gen.option(Gen.alphaNumStr)
        location <- Gen.option(Gen.alphaNumStr)
        message  <- Gen.alphaNumStr
      } yield Failure.BQRepeaterBigQueryError(reason, location, message)
    } yield BadRow.BQRepeaterBigQueryError(mockProcessor, failure, payload)

  private def bqFieldTypeGen: Gen[String] =
    Gen.oneOf("Boolean", "Float", "Integer")
}
