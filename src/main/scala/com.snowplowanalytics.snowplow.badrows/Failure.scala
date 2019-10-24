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
import cats.syntax.functor._

import io.circe.{Encoder, Decoder}
import io.circe.generic.semiauto._
import io.circe.syntax._
import io.circe.java8.time._

sealed trait Failure

object Failure {
  import FailureDetails._

  implicit val encodeFailure: Encoder[Failure] = Encoder.instance {
    case f: CPFormatViolation => CPFormatViolation.failureCPFormatViolationJsonEncoder.apply(f)
    case f: AdapterFailures => AdapterFailures.failureAdapterFailuresJsonEncoder.apply(f)
    case f: TrackerProtocolViolations => TrackerProtocolViolations.failureTrackerProtocolViolationsJsonEncoder.apply(f)
    case f: SchemaViolations => SchemaViolations.failureSchemaViolationsJsonEncoder.apply(f)
    case f: EnrichmentFailures => EnrichmentFailures.failureEnrichmentFailuresJsonEncoder.apply(f)
    case f: SizeViolation => SizeViolation.failureSizeViolationJsonEncoder.apply(f)
    case f: LoaderParsingErrors => LoaderParsingErrors.failureLoaderParsingErrorsJsonEncoder.apply(f)
    case f: LoaderIgluErrors => LoaderIgluErrors.failureLoaderIgluErrorsJsonEncoder.apply(f)
    case f: BQCastErrors => BQCastErrors.failureBQCastErrorsJsonEncoder.apply(f)
    case f: LoaderRuntimeErrors => LoaderRuntimeErrors.failureLoaderRuntimeErrorsJsonEncoder.apply(f)
    case f: BQRepeaterParsingError => BQRepeaterParsingError.failureBQRepeaterParsingErrorJsonEncoder.apply(f)
    case f: BQRepeaterPubSubError => BQRepeaterPubSubError.failureBQRepeaterPubSubErrorJsonEncoder.apply(f)
    case f: BQRepeaterBigQueryError => BQRepeaterBigQueryError.failureBQRepeaterBigQueryErrorJsonEncoder.apply(f)
  }
  implicit val failureDecoder: Decoder[Failure] = List[Decoder[Failure]](
    CPFormatViolation.failureCPFormatViolationJsonDecoder.widen,
    AdapterFailures.failureAdapterFailuresJsonDecoder.widen,
    TrackerProtocolViolations.failureTrackerProtocolViolationsJsonDecoder.widen,
    SchemaViolations.failureSchemaViolationsJsonDecoder.widen,
    EnrichmentFailures.failureEnrichmentFailuresJsonDecoder.widen,
    SizeViolation.failureSizeViolationJsonDecoder.widen,
    LoaderParsingErrors.failureLoaderParsingErrorsJsonDecoder.widen,
    LoaderIgluErrors.failureLoaderIgluErrorsJsonDecoder.widen,
    BQCastErrors.failureBQCastErrorsJsonDecoder.widen,
    LoaderRuntimeErrors.failureLoaderRuntimeErrorsJsonDecoder.widen,
    BQRepeaterParsingError.failureBQRepeaterParsingErrorJsonDecoder.widen,
    BQRepeaterPubSubError.failureBQRepeaterPubSubErrorJsonDecoder.widen,
    BQRepeaterBigQueryError.failureBQRepeaterBigQueryErrorJsonDecoder.widen
  ).reduceLeft(_ or _)

  final case class CPFormatViolation(
    timestamp: Instant,
    loader: String,
    message: CPFormatViolationMessage
  ) extends Failure
  object CPFormatViolation {
    implicit val failureCPFormatViolationJsonEncoder: Encoder[CPFormatViolation] = deriveEncoder
    implicit val failureCPFormatViolationJsonDecoder: Decoder[CPFormatViolation] = deriveDecoder
  }

  final case class AdapterFailures(
    timestamp: Instant,
    vendor: String,
    version: String,
    messages: NonEmptyList[AdapterFailure]
  ) extends Failure
  object AdapterFailures {
    implicit val failureAdapterFailuresJsonEncoder: Encoder[AdapterFailures] = deriveEncoder
    implicit val failureAdapterFailuresJsonDecoder: Decoder[AdapterFailures] = deriveDecoder
  }

  final case class TrackerProtocolViolations(
    timestamp: Instant,
    vendor: String,
    version: String,
    messages: NonEmptyList[TrackerProtocolViolation]
  ) extends Failure
  object TrackerProtocolViolations {
    implicit val failureTrackerProtocolViolationsJsonEncoder: Encoder[TrackerProtocolViolations] = deriveEncoder
    implicit val failureTrackerProtocolViolationsJsonDecoder: Decoder[TrackerProtocolViolations] = deriveDecoder
  }

  final case class SchemaViolations(timestamp: Instant, messages: NonEmptyList[SchemaViolation])
    extends Failure
  object SchemaViolations {
    implicit val failureSchemaViolationsJsonEncoder: Encoder[SchemaViolations] =
      deriveEncoder[SchemaViolations]
    implicit val failureSchemaViolationsJsonDecoder: Decoder[SchemaViolations] =
      deriveDecoder[SchemaViolations]
  }

  final case class EnrichmentFailures(timestamp: Instant, messages: NonEmptyList[EnrichmentFailure])
    extends Failure
  object EnrichmentFailures {
    implicit val failureEnrichmentFailuresJsonEncoder: Encoder[EnrichmentFailures] =
      deriveEncoder[EnrichmentFailures]
    implicit val failureEnrichmentFailuresJsonDecoder: Decoder[EnrichmentFailures] =
      deriveDecoder[EnrichmentFailures]
  }

  final case class SizeViolation(
    timestamp: Instant,
    maximumAllowedSizeBytes: Int,
    actualSizeBytes: Int,
    expectation: String
  ) extends Failure
  object SizeViolation {
    implicit val failureSizeViolationJsonEncoder: Encoder[SizeViolation] =
      deriveEncoder[SizeViolation]
    implicit val failureSizeViolationJsonDecoder: Decoder[SizeViolation] =
      deriveDecoder[SizeViolation]
  }

  /**
    * Represents the failure case where data can not be parsed as a proper event
    * @param errors  errors in the end of the parsing process
    */
  final case class LoaderParsingErrors(errors: NonEmptyList[String]) extends Failure
  object LoaderParsingErrors {
    implicit val failureLoaderParsingErrorsJsonEncoder: Encoder[LoaderParsingErrors] = Encoder.instance(_.errors.asJson)
    implicit val failureLoaderParsingErrorsJsonDecoder: Decoder[LoaderParsingErrors] = Decoder.instance(_.as[NonEmptyList[String]].map(LoaderParsingErrors(_)))
  }

  /**
    * Represents errors which occurs when making validation
    * against a schema with Iglu client
    */
  final case class LoaderIgluErrors(errors: NonEmptyList[LoaderIgluError]) extends Failure
  object LoaderIgluErrors {
    implicit val failureLoaderIgluErrorsJsonEncoder: Encoder[LoaderIgluErrors] = Encoder.instance(_.errors.asJson)
    implicit val failureLoaderIgluErrorsJsonDecoder: Decoder[LoaderIgluErrors] = Decoder.instance(_.as[NonEmptyList[LoaderIgluError]].map(LoaderIgluErrors(_)))
  }

  /**
    * Represents errors which occurs when trying to turn JSON value
    * into BigQuery-compatible row
    */
  final case class BQCastErrors(errors: NonEmptyList[BQCastError]) extends Failure
  object BQCastErrors {
    implicit val failureBQCastErrorsJsonEncoder: Encoder[BQCastErrors] = Encoder.instance(_.errors.asJson)
    implicit val failureBQCastErrorsJsonDecoder: Decoder[BQCastErrors] = Decoder.instance(_.as[NonEmptyList[BQCastError]].map(BQCastErrors(_)))
  }

  /**
    * Represents errors which are not expected to happen. For example,
    * if some error happens during parsing the schema json to Ddl schema,
    * this is due to some bugs in SchemaDDL library which is unlikely to
    * happen. Therefore, these types error collected under one type of error
    */
  final case class LoaderRuntimeErrors(error: String) extends Failure
  object LoaderRuntimeErrors {
    implicit val failureLoaderRuntimeErrorsJsonEncoder: Encoder[LoaderRuntimeErrors] = Encoder.instance(_.error.asJson)
    implicit val failureLoaderRuntimeErrorsJsonDecoder: Decoder[LoaderRuntimeErrors] = Decoder.instance(_.as[String].map(LoaderRuntimeErrors(_)))
  }

  /**
    * Represents situations where payload object can not be
    * converted back to enriched event format successfully
    * @param message error message
    * @param location location in the JSON object where error happened
    */
  final case class BQRepeaterParsingError(message: String, location: List[String]) extends Failure
  object BQRepeaterParsingError {
    implicit val failureBQRepeaterParsingErrorJsonEncoder: Encoder[BQRepeaterParsingError] = deriveEncoder
    implicit val failureBQRepeaterParsingErrorJsonDecoder: Decoder[BQRepeaterParsingError] = deriveDecoder
  }

  /**
    * Represents error which occurs while trying to fetch events
    * from PubSub topic
    */
  final case class BQRepeaterPubSubError(error: String) extends Failure
  object BQRepeaterPubSubError {
    implicit val failureBQRepeaterPubSubErrorJsonEncoder: Encoder[BQRepeaterPubSubError] = Encoder.instance(_.error.asJson)
    implicit val failureBQRepeaterPubSubErrorJsonDecoder: Decoder[BQRepeaterPubSubError] = Decoder.instance(_.as[String].map(BQRepeaterPubSubError(_)))
  }

  /**
    * Represents errors which occurs while trying to insert the event
    * to BigQuery via BigQuery SDK
    */
  final case class BQRepeaterBigQueryError(reason: Option[String], location: Option[String], message: String) extends Failure
  object BQRepeaterBigQueryError {
    implicit val failureBQRepeaterBigQueryErrorJsonEncoder: Encoder[BQRepeaterBigQueryError] = deriveEncoder
    implicit val failureBQRepeaterBigQueryErrorJsonDecoder: Decoder[BQRepeaterBigQueryError] = deriveDecoder
  }
}
