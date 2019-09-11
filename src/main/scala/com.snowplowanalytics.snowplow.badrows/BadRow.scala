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

import cats.syntax.functor._

import com.snowplowanalytics.iglu.core.{SelfDescribingData, SchemaKey}
import com.snowplowanalytics.iglu.core.circe.instances._

sealed trait BadRow {
  def schemaKey: SchemaKey
  def selfDescribinData: SelfDescribingData[Json] = SelfDescribingData(schemaKey, this.asJson)
  def compact: String = selfDescribinData.asJson.noSpaces
}

object BadRow {
  implicit val badRowEncoder: Encoder[BadRow] = Encoder.instance {
    case f: CPFormatViolation => CPFormatViolation.badRowCPFormatViolationJsonEncoder.apply(f)
    case f: AdapterFailures => AdapterFailures.badRowAdapterFailuresJsonEncoder.apply(f)
    case f: TrackerProtocolViolations => TrackerProtocolViolations.badRowTrackerProtocolViolationsJsonEncoder.apply(f)
    case f: SchemaViolations => SchemaViolations.badRowSchemaViolationsJsonEncoder.apply(f)
    case f: EnrichmentFailures => EnrichmentFailures.badRowEnrichmentFailuresJsonEncoder.apply(f)
    case f: SizeViolation => SizeViolation.badRowSizeViolationJsonEncoder.apply(f)
    case f: LoaderParsingErrors => LoaderParsingErrors.badRowLoaderParsingErrorsJsonEncoder.apply(f)
    case f: LoaderIgluErrors => LoaderIgluErrors.badRowLoaderIgluErrorsJsonEncoder.apply(f)
    case f: BQCastErrors => BQCastErrors.badRowBQCastErrorsJsonEncoder.apply(f)
    case f: LoaderRuntimeErrors => LoaderRuntimeErrors.badRowLoaderRuntimeErrorsJsonEncoder.apply(f)
    case f: BQRepeaterParsingError => BQRepeaterParsingError.badRowBQRepeaterParsingErrorJsonEncoder.apply(f)
    case f: BQRepeaterPubSubError => BQRepeaterPubSubError.badRowBQRepeaterPubSubErrorJsonEncoder.apply(f)
    case f: BQRepeaterBigQueryError => BQRepeaterBigQueryError.badRowBQRepeaterBigQueryErrorJsonEncoder.apply(f)
  }
  implicit val badRowDecoder: Decoder[BadRow] = List[Decoder[BadRow]](
    CPFormatViolation.badRowCPFormatViolationJsonDecoder.widen,
    AdapterFailures.badRowAdapterFailuresJsonDecoder.widen,
    TrackerProtocolViolations.badRowTrackerProtocolViolationsJsonDecoder.widen,
    SchemaViolations.badRowSchemaViolationsJsonDecoder.widen,
    EnrichmentFailures.badRowEnrichmentFailuresJsonDecoder.widen,
    SizeViolation.badRowSizeViolationJsonDecoder.widen,
    LoaderParsingErrors.badRowLoaderParsingErrorsJsonDecoder.widen,
    LoaderIgluErrors.badRowLoaderIgluErrorsJsonDecoder.widen,
    BQCastErrors.badRowBQCastErrorsJsonDecoder.widen,
    LoaderRuntimeErrors.badRowLoaderRuntimeErrorsJsonDecoder.widen,
    BQRepeaterParsingError.badRowBQRepeaterParsingErrorJsonDecoder.widen,
    BQRepeaterPubSubError.badRowBQRepeaterPubSubErrorJsonDecoder.widen,
    BQRepeaterBigQueryError.badRowBQRepeaterBigQueryErrorJsonDecoder.widen
  ).reduceLeft(_ or _)

  /** Created when Scala Common Enrich tries to create a `CollectorPayload`
    * from the raw input event (e.g. serialized with Thrift), in the loading step.
    * For instance this can happen in case of malformed HTTP, truncation, invalid query string encoding in URL etc.
    * One `CollectorPayload` can contain several events.
    */
  final case class CPFormatViolation(processor: Processor, failure: Failure.CPFormatViolation, payload: Payload.RawPayload) extends BadRow {
    override def schemaKey: SchemaKey = Schemas.CPFormatViolation
  }
  object CPFormatViolation {
    implicit val badRowCPFormatViolationJsonEncoder: Encoder[CPFormatViolation] = deriveEncoder[CPFormatViolation]
    implicit val badRowCPFormatViolationJsonDecoder: Decoder[CPFormatViolation] = deriveDecoder[CPFormatViolation]
  }

  /** Created when Scala Common Enrich tries to create `RawEvent`(s) from a `CollectorPayload`,
    * in an adapter. This happens usually when the fields sent by a webhook (e.g. MailChimp)
    * don't match the fields that we expect for this webhook.
    */
  final case class AdapterFailures(processor: Processor, failure: Failure.AdapterFailures, payload: Payload.CollectorPayload) extends BadRow {
    override def schemaKey: SchemaKey = Schemas.AdapterFailures
  }
  object AdapterFailures {
    implicit val badRowAdapterFailuresJsonEncoder: Encoder[AdapterFailures] = deriveEncoder
    implicit val badRowAdapterFailuresJsonDecoder: Decoder[AdapterFailures] = deriveDecoder
  }

  /** Exactly like [[AdapterFailures]] but for Snowplow events only (with Tracker Protocol v2).
    * For instance this can happen when the HTTP body of a Snowplow event is empty.
    */ 
  final case class TrackerProtocolViolations(processor: Processor, failure: Failure.TrackerProtocolViolations, payload: Payload.CollectorPayload) extends BadRow {
    override def schemaKey: SchemaKey = Schemas.TrackerProtocolViolations
  }
  object TrackerProtocolViolations {
    implicit val badRowTrackerProtocolViolationsJsonEncoder: Encoder[TrackerProtocolViolations] = deriveEncoder
    implicit val badRowTrackerProtocolViolationsJsonDecoder: Decoder[TrackerProtocolViolations] = deriveDecoder
  }

  /** Created in the shredding step of Scala Common Enrich, after the enrichments,
    * when one of the contexts of the input event could not be validated against its schema,
    * or when an unstructured event could not be validated against its schema.
    */
  final case class SchemaViolations(processor: Processor, failure: Failure.SchemaViolations, payload: Payload.EnrichmentPayload) extends BadRow {
    override def schemaKey: SchemaKey = Schemas.SchemaViolations
  }
  object SchemaViolations {
    implicit val badRowSchemaViolationsJsonEncoder: Encoder[SchemaViolations] = deriveEncoder
    implicit val badRowSchemaViolationsJsonDecoder: Decoder[SchemaViolations] = deriveDecoder
  }

  /** Created in Scala Common Enrich when an enrichment fails.
    * For instance this can happen if the call to OpenWeather fails.
    */
  final case class EnrichmentFailures(processor: Processor, failure: Failure.EnrichmentFailures, payload: Payload.EnrichmentPayload) extends BadRow {
    override def schemaKey: SchemaKey = Schemas.EnrichmentFailures
  }
  object EnrichmentFailures {
    implicit val badRowEnrichmentFailuresJsonEncoder: Encoder[EnrichmentFailures] = deriveEncoder
    implicit val badRowEnrichmentFailuresJsonDecoder: Decoder[EnrichmentFailures] = deriveDecoder
  }

  /** Created in Scala Stream Collector when the event that it receives is bigger that the max authorized size.
    * This limit is usually determined by the message queue (e.g. 10MB for a message for PubSub).
    */ 
  final case class SizeViolation(processor: Processor, failure: Failure.SizeViolation, payload: Payload.RawPayload) extends BadRow {
    override def schemaKey: SchemaKey = Schemas.SizeViolation
  }
  object SizeViolation {
    implicit val badRowSizeViolationJsonEncoder: Encoder[SizeViolation] = deriveEncoder
    implicit val badRowSizeViolationJsonDecoder: Decoder[SizeViolation] = deriveDecoder
  }

  /** TODO */
  final case class LoaderParsingErrors(processor: Processor, failure: Failure.LoaderParsingErrors, payload: Payload.RawPayload) extends BadRow {
    override def schemaKey: SchemaKey = Schemas.LoaderParsingError
  }
  object LoaderParsingErrors {
    implicit val badRowLoaderParsingErrorsJsonEncoder: Encoder[LoaderParsingErrors] = deriveEncoder
    implicit val badRowLoaderParsingErrorsJsonDecoder: Decoder[LoaderParsingErrors] = deriveDecoder

    def apply(failure: Failure.LoaderParsingErrors, payload: Payload.RawPayload)(implicit processor: Processor): LoaderParsingErrors =
      LoaderParsingErrors(processor, failure, payload)
  }

  /** TODO */
  final case class LoaderIgluErrors(processor: Processor, failure: Failure.LoaderIgluErrors, payload: Payload.LoaderPayload) extends BadRow {
    override def schemaKey: SchemaKey = Schemas.LoaderIgluError
  }
  object LoaderIgluErrors {
    implicit val badRowLoaderIgluErrorsJsonEncoder: Encoder[LoaderIgluErrors] = deriveEncoder
    implicit val badRowLoaderIgluErrorsJsonDecoder: Decoder[LoaderIgluErrors] = deriveDecoder

    def apply(failure: Failure.LoaderIgluErrors, payload: Payload.LoaderPayload)(implicit processor: Processor): LoaderIgluErrors =
      LoaderIgluErrors(processor, failure, payload)
  }

  /** TODO */
  final case class BQCastErrors(processor: Processor, failure: Failure.BQCastErrors, payload: Payload.LoaderPayload) extends BadRow {
    override def schemaKey: SchemaKey = Schemas.BQCastError
  }
  object BQCastErrors {
    implicit val badRowBQCastErrorsJsonEncoder: Encoder[BQCastErrors] = deriveEncoder
    implicit val badRowBQCastErrorsJsonDecoder: Decoder[BQCastErrors] = deriveDecoder

    def apply(failure: Failure.BQCastErrors, payload: Payload.LoaderPayload)(implicit processor: Processor): BQCastErrors =
      BQCastErrors(processor, failure, payload)
  }

  /** TODO */
  final case class LoaderRuntimeErrors(processor: Processor, failure: Failure.LoaderRuntimeErrors, payload: Payload.LoaderPayload) extends BadRow {
    override def schemaKey: SchemaKey = Schemas.LoaderRuntimeError
  }
  object LoaderRuntimeErrors {
    implicit val badRowLoaderRuntimeErrorsJsonEncoder: Encoder[LoaderRuntimeErrors] = deriveEncoder
    implicit val badRowLoaderRuntimeErrorsJsonDecoder: Decoder[LoaderRuntimeErrors] = deriveDecoder

    def apply(failure: Failure.LoaderRuntimeErrors, payload: Payload.LoaderPayload)(implicit processor: Processor): LoaderRuntimeErrors =
      LoaderRuntimeErrors(processor, failure, payload)
  }

  /** TODO */
  final case class BQRepeaterParsingError(processor: Processor, failure: Failure.BQRepeaterParsingError, payload: Payload.RawPayload) extends BadRow {
    override def schemaKey: SchemaKey = Schemas.BQRepeaterParsingError
  }
  object BQRepeaterParsingError {
    implicit val badRowBQRepeaterParsingErrorJsonEncoder: Encoder[BQRepeaterParsingError] = deriveEncoder
    implicit val badRowBQRepeaterParsingErrorJsonDecoder: Decoder[BQRepeaterParsingError] = deriveDecoder

    def apply(failure: Failure.BQRepeaterParsingError, payload: Payload.RawPayload)(implicit processor: Processor): BQRepeaterParsingError =
      BQRepeaterParsingError(processor, failure, payload)
  }

  /** TODO */
  final case class BQRepeaterPubSubError(processor: Processor, failure: Failure.BQRepeaterPubSubError, payload: Payload.RawPayload) extends BadRow {
    override def schemaKey: SchemaKey = Schemas.BQRepeaterPubSubError
  }
  object BQRepeaterPubSubError {
    implicit val badRowBQRepeaterPubSubErrorJsonEncoder: Encoder[BQRepeaterPubSubError] = deriveEncoder
    implicit val badRowBQRepeaterPubSubErrorJsonDecoder: Decoder[BQRepeaterPubSubError] = deriveDecoder

    def apply(failure: Failure.BQRepeaterPubSubError, payload: Payload.RawPayload)(implicit processor: Processor): BQRepeaterPubSubError =
      BQRepeaterPubSubError(processor, failure, payload)
  }

  /** TODO */
  final case class BQRepeaterBigQueryError(processor: Processor, failure: Failure.BQRepeaterBigQueryError, payload: Payload.BQReconstructedEvent) extends BadRow {
    override def schemaKey: SchemaKey = Schemas.BQRepeaterBQError
  }
  object BQRepeaterBigQueryError {
    implicit val badRowBQRepeaterBigQueryErrorJsonEncoder: Encoder[BQRepeaterBigQueryError] = deriveEncoder
    implicit val badRowBQRepeaterBigQueryErrorJsonDecoder: Decoder[BQRepeaterBigQueryError] = deriveDecoder

    def apply(failure: Failure.BQRepeaterBigQueryError, payload: Payload.BQReconstructedEvent)(implicit processor: Processor): BQRepeaterBigQueryError =
      BQRepeaterBigQueryError(processor, failure, payload)
  }
}
