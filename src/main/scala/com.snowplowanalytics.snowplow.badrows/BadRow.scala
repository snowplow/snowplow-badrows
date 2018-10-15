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

import io.circe.{Encoder, Decoder}
import io.circe.generic.semiauto._
import io.circe.syntax._
import io.circe.Json

import cats.data.NonEmptyList
import cats.syntax.functor._

import com.snowplowanalytics.iglu.core.{SelfDescribingData, SchemaKey}
import com.snowplowanalytics.iglu.core.circe.instances._

import com.snowplowanalytics.snowplow.analytics.scalasdk.ParsingError

sealed trait BadRow {
  def schemaKey: SchemaKey
  def selfDescribingData: SelfDescribingData[Json] = SelfDescribingData(schemaKey, this.asJson)
  def compact: String = selfDescribingData.asJson.noSpaces
}

object BadRow {
  implicit val badRowEncoder: Encoder[BadRow] = Encoder.instance {
    // Stream Collector
    case f: SizeViolation => SizeViolation.badRowSizeViolationJsonEncoder.apply(f)
    // Scala Common Enrich
    case f: CPFormatViolation => CPFormatViolation.badRowCPFormatViolationJsonEncoder.apply(f)
    case f: AdapterFailures => AdapterFailures.badRowAdapterFailuresJsonEncoder.apply(f)
    case f: TrackerProtocolViolations => TrackerProtocolViolations.badRowTrackerProtocolViolationsJsonEncoder.apply(f)
    case f: SchemaViolations => SchemaViolations.badRowSchemaViolationsJsonEncoder.apply(f)
    case f: EnrichmentFailures => EnrichmentFailures.badRowEnrichmentFailuresJsonEncoder.apply(f)
    // Generic Loaders
    case f: LoaderParsingError => LoaderParsingError.badRowLoaderParsingErrorsJsonEncoder.apply(f)
    case f: LoaderIgluError => LoaderIgluError.badRowLoaderIgluErrorsJsonEncoder.apply(f)
    case f: LoaderRuntimeError => LoaderRuntimeError.badRowLoaderRuntimeErrorsJsonEncoder.apply(f)
    case f: LoaderRecoveryError => LoaderRecoveryError.badRowLoaderRecoveryErrorJsonEncoder.apply(f)
  }

  implicit val badRowDecoder: Decoder[BadRow] = List[Decoder[BadRow]](
    // Stream Collector
    SizeViolation.badRowSizeViolationJsonDecoder.widen,
    // Scala Common Enrich
    CPFormatViolation.badRowCPFormatViolationJsonDecoder.widen,
    AdapterFailures.badRowAdapterFailuresJsonDecoder.widen,
    TrackerProtocolViolations.badRowTrackerProtocolViolationsJsonDecoder.widen,
    SchemaViolations.badRowSchemaViolationsJsonDecoder.widen,
    EnrichmentFailures.badRowEnrichmentFailuresJsonDecoder.widen,
    // Generic Loaders
    LoaderRuntimeError.badRowLoaderRuntimeErrorsJsonDecoder.widen,
    LoaderParsingError.badRowLoaderParsingErrorsJsonDecoder.widen,
    LoaderIgluError.badRowLoaderIgluErrorsJsonDecoder.widen,
    LoaderRecoveryError.badRowLoaderRecoveryErrorJsonDecoder.widen
  ).reduceLeft(_ or _)

  /** Created by Collector when the event that it receives is bigger that the max authorized size.
    * This limit is usually determined by the message queue (e.g. 10MB for a message for PubSub).
    */
  final case class SizeViolation(processor: Processor, failure: Failure.SizeViolation, payload: Payload.RawPayload) extends BadRow {
    def schemaKey: SchemaKey = Schemas.SizeViolation
  }
  object SizeViolation {
    implicit val badRowSizeViolationJsonEncoder: Encoder[SizeViolation] =
      deriveEncoder[SizeViolation]
    implicit val badRowSizeViolationJsonDecoder: Decoder[SizeViolation] =
      deriveDecoder[SizeViolation]
  }

  /** Created by Scala Common Enrich, when it tries to create a `CollectorPayload`
    * from the raw input event (e.g. serialized with Thrift), in the loading step.
    * For instance this can happen in case of malformed HTTP, truncation, invalid query string encoding in URL etc.
    * One `CollectorPayload` can contain several events.
    */
  final case class CPFormatViolation(processor: Processor, failure: Failure.CPFormatViolation, payload: Payload.RawPayload) extends BadRow {
    def schemaKey: SchemaKey = Schemas.CPFormatViolation
  }
  object CPFormatViolation {
    implicit val badRowCPFormatViolationJsonEncoder: Encoder[CPFormatViolation] =
      deriveEncoder[CPFormatViolation]
    implicit val badRowCPFormatViolationJsonDecoder: Decoder[CPFormatViolation] =
      deriveDecoder[CPFormatViolation]
  }

  /** Created by Scala Common Enrich adapter, when it tries to convert a `CollectorPayload` into `RawEvent`s,
    * This happens usually when the fields sent by a webhook (e.g. MailChimp)
    * don't match the fields that we expect for this webhook.
    */
  final case class AdapterFailures(processor: Processor, failure: Failure.AdapterFailures, payload: Payload.CollectorPayload) extends BadRow {
    def schemaKey: SchemaKey = Schemas.AdapterFailures
  }
  object AdapterFailures {
    implicit val badRowAdapterFailuresJsonEncoder: Encoder[AdapterFailures] =
      deriveEncoder[AdapterFailures]
    implicit val badRowAdapterFailuresJsonDecoder: Decoder[AdapterFailures] =
      deriveDecoder[AdapterFailures]
  }

  /** Exactly like [[AdapterFailures]] but for Snowplow events only (with Tracker Protocol v2).
    * For instance this can happen when the HTTP body of a Snowplow event is empty.
    */
  final case class TrackerProtocolViolations(processor: Processor, failure: Failure.TrackerProtocolViolations, payload: Payload.CollectorPayload) extends BadRow {
    def schemaKey: SchemaKey = Schemas.TrackerProtocolViolations
  }
  object TrackerProtocolViolations {
    implicit val badRowTrackerProtocolViolationsJsonEncoder: Encoder[TrackerProtocolViolations] =
      deriveEncoder[TrackerProtocolViolations]
    implicit val badRowTrackerProtocolViolationsJsonDecoder: Decoder[TrackerProtocolViolations] =
      deriveDecoder[TrackerProtocolViolations]
  }

  /** Created in the shredding step of Scala Common Enrich, after the enrichments,
    * when one of the contexts of the input event could not be validated against its schema,
    * or when an unstructured event could not be validated against its schema.
    */
  final case class SchemaViolations(processor: Processor, failure: Failure.SchemaViolations, payload: Payload.EnrichmentPayload) extends BadRow {
    def schemaKey: SchemaKey = Schemas.SchemaViolations
  }
  object SchemaViolations {
    implicit val badRowSchemaViolationsJsonEncoder: Encoder[SchemaViolations] =
      deriveEncoder[SchemaViolations]
    implicit val badRowSchemaViolationsJsonDecoder: Decoder[SchemaViolations] =
      deriveDecoder[SchemaViolations]
  }

  /** Created in Scala Common Enrich when an enrichment fails.
    * For instance this can happen if the call to OpenWeather fails.
    */
  final case class EnrichmentFailures(processor: Processor, failure: Failure.EnrichmentFailures, payload: Payload.EnrichmentPayload) extends BadRow {
    def schemaKey: SchemaKey = Schemas.EnrichmentFailures
  }
  object EnrichmentFailures {
    implicit val badRowEnrichmentFailuresJsonEncoder: Encoder[EnrichmentFailures] =
      deriveEncoder[EnrichmentFailures]
    implicit val badRowEnrichmentFailuresJsonDecoder: Decoder[EnrichmentFailures] =
      deriveDecoder[EnrichmentFailures]
  }

  /** Created in any Loader (via Analytics SDK), if parsing of a canonical TSV event format has failed,
    * e.g. if line has not enough columns (not 131) or event_id is not UUID
    */
  final case class LoaderParsingError(processor: Processor, failure: ParsingError, payload: Payload.RawPayload) extends BadRow {
    def schemaKey: SchemaKey = Schemas.LoaderParsingError
  }
  object LoaderParsingError {
    implicit val badRowLoaderParsingErrorsJsonEncoder: Encoder[LoaderParsingError] =
      deriveEncoder[LoaderParsingError]
    implicit val badRowLoaderParsingErrorsJsonDecoder: Decoder[LoaderParsingError] =
      deriveDecoder[LoaderParsingError]
  }

  /** Error happened in a loader and caused by Iglu subsystem (usually some schema is not available) */
  final case class LoaderIgluError(processor: Processor, failure: Failure.LoaderIgluErrors, payload: Payload.LoaderPayload) extends BadRow {
    // 1-0-0 wasn't available in snowplow-badrows and never available in any final assets
    def schemaKey: SchemaKey = Schemas.LoaderIgluError
  }
  object LoaderIgluError {
    implicit val badRowLoaderIgluErrorsJsonEncoder: Encoder[LoaderIgluError] =
      deriveEncoder[LoaderIgluError]
    implicit val badRowLoaderIgluErrorsJsonDecoder: Decoder[LoaderIgluError] =
      deriveDecoder[LoaderIgluError]
  }

  /** Any unhandled IO error, such as one happened in DynamoDB during cross-batch deduplication */
  final case class LoaderRuntimeError(processor: Processor, failure: String, payload: Payload.LoaderPayload) extends BadRow {
    def schemaKey: SchemaKey = Schemas.LoaderRuntimeError
  }
  object LoaderRuntimeError {
    implicit val badRowLoaderRuntimeErrorsJsonEncoder: Encoder[LoaderRuntimeError] =
      deriveEncoder[LoaderRuntimeError]
    implicit val badRowLoaderRuntimeErrorsJsonDecoder: Decoder[LoaderRuntimeError] =
      deriveDecoder[LoaderRuntimeError]
  }

  /**
    * Recovery software couldn't re-insert the row into DB due a runtime failure or invalid data in a source
    * This is not a payload for generic "recovery job", but for loader-specific, such as BigQuery Repeater
    */
  final case class LoaderRecoveryError(processor: Processor, failure: Failure.LoaderRecoveryFailure, payload: Payload.RawPayload) extends BadRow {
    def schemaKey: SchemaKey = Schemas.LoaderRecoveryError
  }
  object LoaderRecoveryError {
    implicit val badRowLoaderRecoveryErrorJsonEncoder: Encoder[LoaderRecoveryError] =
      deriveEncoder[LoaderRecoveryError]
    implicit val badRowLoaderRecoveryErrorJsonDecoder: Decoder[LoaderRecoveryError] =
      deriveDecoder[LoaderRecoveryError]
  }
}
