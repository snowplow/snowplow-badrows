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
package com.snowplowanalytics.snowplow.badrows

import java.time.Instant

import cats.data.NonEmptyList
import cats.syntax.functor._

import io.circe.{Encoder, Decoder}
import io.circe.generic.semiauto._
import io.circe.syntax._

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
    case f: LoaderRecoveryFailure => LoaderRecoveryFailure.failureLoaderRecoveryJsonEncoder.apply(f)
    case f: RecoveryFailure => RecoveryFailure.failureRecoveryJsonEncoder.apply(f)
    case f: GenericFailure => GenericFailure.failureGenericJsonEncoder.apply(f)
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
    LoaderRecoveryFailure.failureLoaderRecoveryJsonDecoder.widen,
    RecoveryFailure.failureRecoveryJsonDecoder.widen,
    GenericFailure.failureGenericJsonDecoder.widen
  ).reduceLeft(_ or _)

  final case class CPFormatViolation(
    timestamp: Instant,
    loader: String,
    message: CPFormatViolationMessage
  ) extends Failure
  object CPFormatViolation {
    implicit val failureCPFormatViolationJsonEncoder: Encoder[CPFormatViolation] =
      deriveEncoder[CPFormatViolation]
    implicit val failureCPFormatViolationJsonDecoder: Decoder[CPFormatViolation] =
      deriveDecoder[CPFormatViolation]
  }

  final case class AdapterFailures(
    timestamp: Instant,
    vendor: String,
    version: String,
    messages: NonEmptyList[AdapterFailure]
  ) extends Failure
  object AdapterFailures {
    implicit val failureAdapterFailuresJsonEncoder: Encoder[AdapterFailures] =
      deriveEncoder[AdapterFailures]
    implicit val failureAdapterFailuresJsonDecoder: Decoder[AdapterFailures] =
      deriveDecoder[AdapterFailures]
  }

  final case class TrackerProtocolViolations(
    timestamp: Instant,
    vendor: String,
    version: String,
    messages: NonEmptyList[TrackerProtocolViolation]
  ) extends Failure
  object TrackerProtocolViolations {
    implicit val failureTrackerProtocolViolationsJsonEncoder: Encoder[TrackerProtocolViolations] =
      deriveEncoder[TrackerProtocolViolations]
    implicit val failureTrackerProtocolViolationsJsonDecoder: Decoder[TrackerProtocolViolations] =
      deriveDecoder[TrackerProtocolViolations]
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
    implicit val failureLoaderParsingErrorsJsonEncoder: Encoder[LoaderParsingErrors] =
      Encoder.instance(_.errors.asJson)
    implicit val failureLoaderParsingErrorsJsonDecoder: Decoder[LoaderParsingErrors] =
      Decoder.instance(_.as[NonEmptyList[String]].map(LoaderParsingErrors(_)))
  }

  /**
    * Represents errors which occurs when making validation
    * against a schema with Iglu client
    */
  final case class LoaderIgluErrors(errors: NonEmptyList[LoaderIgluError]) extends Failure
  object LoaderIgluErrors {
    implicit val failureLoaderIgluErrorsJsonEncoder: Encoder[LoaderIgluErrors] =
      Encoder.instance(_.errors.asJson)
    implicit val failureLoaderIgluErrorsJsonDecoder: Decoder[LoaderIgluErrors] =
      Decoder.instance(_.as[NonEmptyList[LoaderIgluError]].map(LoaderIgluErrors(_)))
  }

  /**
    * An error happened in aux software that tried to recover loader failure
    * (e.g. Snowplow BigQuery Repeater). Usually unrecoverable and contains
    * poorly structured data returned by Java SDK
    */
  final case class LoaderRecoveryFailure(error: LoaderRecoveryError) extends Failure
  object LoaderRecoveryFailure {
    implicit val failureLoaderRecoveryJsonEncoder: Encoder[LoaderRecoveryFailure] =
      deriveEncoder[LoaderRecoveryFailure]
    implicit val failureLoaderRecoveryJsonDecoder: Decoder[LoaderRecoveryFailure] =
      deriveDecoder[LoaderRecoveryFailure]
  }

  /**
    * A recovery scenario applied in recovery process was unsuccessful.
    * @param error cause of failure
    * @param configName name of recovery flow configuration
    */
  final case class RecoveryFailure(error: String, configName: Option[String] = None) extends Failure
  object RecoveryFailure {
    implicit val failureRecoveryJsonEncoder: Encoder[RecoveryFailure] =
      deriveEncoder[RecoveryFailure]
    implicit val failureRecoveryJsonDecoder: Decoder[RecoveryFailure] =
      deriveDecoder[RecoveryFailure]
  }
  
  /** Failure for a generic bad row. */
  final case class GenericFailure(timestamp: Instant, errors: NonEmptyList[String]) extends Failure
  object GenericFailure {
    implicit val failureGenericJsonEncoder: Encoder[GenericFailure] =
      deriveEncoder[GenericFailure]
    implicit val failureGenericJsonDecoder: Decoder[GenericFailure] =
      deriveDecoder[GenericFailure]
  }
}
