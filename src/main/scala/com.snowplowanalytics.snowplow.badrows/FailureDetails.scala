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

import com.snowplowanalytics.iglu.client.ClientError
import com.snowplowanalytics.iglu.core.{ParseError, SchemaCriterion, SchemaKey}
import com.snowplowanalytics.iglu.core.circe.CirceIgluCodecs._

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto._
import io.circe.Json

import cats.syntax.functor._

object FailureDetails {

  // COLLECTOR PAYLOAD FORMAT VIOLATION
  
  sealed trait CPFormatViolationMessage

  object CPFormatViolationMessage {
    final case class InputData(payloadField: String, value: Option[String], expectation: String) extends CPFormatViolationMessage
    final case class Fallback(error: String) extends CPFormatViolationMessage

    implicit val cpFormatViolationMessageEncoder: Encoder[CPFormatViolationMessage] =
      Encoder.instance {
        case f: InputData =>
          deriveEncoder[InputData].apply(f)
        case f: Fallback =>
          deriveEncoder[Fallback].apply(f)
      }
    implicit val cpFormatViolationMessageDecoder: Decoder[CPFormatViolationMessage] =
      List[Decoder[CPFormatViolationMessage]](
        deriveDecoder[InputData].widen,
        deriveDecoder[Fallback].widen
      ).reduceLeft(_ or _)
  }
  
  // ADAPTER FAILURES / TRACKER PROTOCOL VIOLATION

  /** Tracker Protocol is a special case of an Adapter */
  sealed trait AdapterFailureOrTrackerProtocolViolation extends Product with Serializable

  // ADAPTER FAILURES
  
  sealed trait AdapterFailure extends AdapterFailureOrTrackerProtocolViolation
  object AdapterFailure {
    implicit val adapterFailureEncoder: Encoder[AdapterFailure] = Encoder.instance {
      case f: NotJson => deriveEncoder[NotJson].apply(f)
      case f: NotIglu => deriveEncoder[NotIglu].apply(f)
      case f: InputData => deriveEncoder[InputData].apply(f)
      case f: SchemaMapping => deriveEncoder[SchemaMapping].apply(f)
    }
    implicit val adapterFailureDecoder: Decoder[AdapterFailure] = List[Decoder[AdapterFailure]](
      deriveDecoder[NotJson].widen,
      deriveDecoder[NotIglu].widen,
      deriveDecoder[InputData].widen,
      deriveDecoder[SchemaMapping].widen
    ).reduceLeft(_ or _)

    final case class NotJson(field: String, value: Option[String], error: String) extends AdapterFailure
    final case class NotIglu(json: Json, error: ParseError) extends AdapterFailure
    final case class InputData(field: String, value: Option[String], expectation: String) extends AdapterFailure
    final case class SchemaMapping(actual: Option[String], expectedMapping: Map[String, SchemaKey], expectation: String) extends AdapterFailure
  }
  
  // TRACKER PROTOCOL VIOLATIONS
  
  sealed trait TrackerProtocolViolation extends AdapterFailureOrTrackerProtocolViolation
  object TrackerProtocolViolation {
    implicit val TrackerProtocolViolationEncoder: Encoder[TrackerProtocolViolation] = Encoder.instance {
      case f: IgluError => deriveEncoder[IgluError].apply(f)
      case f: CriterionMismatch => deriveEncoder[CriterionMismatch].apply(f)
      case f: NotJson => deriveEncoder[NotJson].apply(f)
      case f: NotIglu => deriveEncoder[NotIglu].apply(f)
      case f: InputData => deriveEncoder[InputData].apply(f)
    }
    implicit val adapterFailureDecoder: Decoder[TrackerProtocolViolation] = List[Decoder[TrackerProtocolViolation]](
      deriveDecoder[IgluError].widen,
      deriveDecoder[CriterionMismatch].widen,
      deriveDecoder[NotJson].widen,
      deriveDecoder[NotIglu].widen,
      deriveDecoder[InputData].widen
    ).reduceLeft(_ or _)

    final case class InputData(field: String, value: Option[String], expectation: String) extends TrackerProtocolViolation
    final case class NotJson(field: String, value: Option[String], error: String) extends TrackerProtocolViolation
    final case class NotIglu(json: Json, error: ParseError) extends TrackerProtocolViolation
    final case class IgluError(schemaKey: SchemaKey, error: ClientError) extends TrackerProtocolViolation
    final case class CriterionMismatch(schemaKey: SchemaKey, schemaCriterion: SchemaCriterion) extends TrackerProtocolViolation
  }
  
  // SCHEMA VIOLATIONS
  
  sealed trait EnrichmentStageIssue
  
  sealed trait SchemaViolation extends EnrichmentStageIssue
  object SchemaViolation {
    implicit val schemaViolationEncoder: Encoder[SchemaViolation] = Encoder.instance {
      case f: NotJson => deriveEncoder[NotJson].apply(f)
      case f: NotIglu => deriveEncoder[NotIglu].apply(f)
      case f: CriterionMismatch => deriveEncoder[CriterionMismatch].apply(f)
      case f: IgluError => deriveEncoder[IgluError].apply(f)
    }
    implicit val schemaViolationDecoder: Decoder[SchemaViolation] = List[Decoder[SchemaViolation]](
      deriveDecoder[NotJson].widen,
      deriveDecoder[NotIglu].widen,
      deriveDecoder[CriterionMismatch].widen,
      deriveDecoder[IgluError].widen
    ).reduceLeft(_ or _)

    final case class NotJson(field: String, value: Option[String], error: String ) extends SchemaViolation
    final case class NotIglu(json: Json, error: ParseError) extends SchemaViolation
    final case class IgluError(schemaKey: SchemaKey, error: ClientError) extends SchemaViolation
    final case class CriterionMismatch(schemaKey: SchemaKey, schemaCriterion: SchemaCriterion) extends SchemaViolation
  }
  
  // ENRICHMENT FAILURES

  final case class EnrichmentFailure(enrichment: Option[EnrichmentInformation], message: EnrichmentFailureMessage) extends EnrichmentStageIssue
  object EnrichmentFailure {
    implicit val enrichmentFailureEncoder: Encoder[EnrichmentFailure] = deriveEncoder[EnrichmentFailure]
    implicit val enrichmentFailureDecoder: Decoder[EnrichmentFailure] = deriveDecoder[EnrichmentFailure]
  }
  
  sealed trait EnrichmentFailureMessage
  object EnrichmentFailureMessage {
    implicit val enrichmentFailureMessageEncoder: Encoder[EnrichmentFailureMessage] =
      Encoder.instance {
        case f: Simple =>
          deriveEncoder[Simple].apply(f)
        case f: InputData =>
          deriveEncoder[InputData].apply(f)
        case f: IgluError =>
          deriveEncoder[IgluError].apply(f)
      }
    implicit val enrichmentFailureMessageDecoder: Decoder[EnrichmentFailureMessage] =
      List[Decoder[EnrichmentFailureMessage]](
        deriveDecoder[Simple].widen,
        deriveDecoder[InputData].widen,
        deriveDecoder[IgluError].widen
      ).reduceLeft(_ or _)
  
    final case class Simple(error: String) extends EnrichmentFailureMessage
    final case class InputData(field: String, value: Option[String], expectation: String) extends EnrichmentFailureMessage
    final case class IgluError(schemaKey: SchemaKey, error: ClientError) extends EnrichmentFailureMessage
  }
  
  final case class EnrichmentInformation(schemaKey: SchemaKey, identifier: String)
  object EnrichmentInformation {
    implicit val enrichmentInformationEncoder: Encoder[EnrichmentInformation] =
      deriveEncoder[EnrichmentInformation]
    implicit val enrichmentInformationDecoder: Decoder[EnrichmentInformation] =
      deriveDecoder[EnrichmentInformation]
  }

  // LOADERS ERRORS
  
  sealed trait LoaderIgluError
  object LoaderIgluError {
    /** "Classic" error - loader couldn't find a schema (`ValidationError` should never happen) */
    final case class IgluError(schemaKey: SchemaKey, error: ClientError) extends LoaderIgluError
    /** Schema was fetched, but could not be transformed into proper JSON Schema AST (should never happen) */
    final case class InvalidSchema(schemaKey: SchemaKey, message: String) extends LoaderIgluError
    /** Loader couldn't fetch a `SchemaList` - entity necessary for proper shredding with migrations */
    final case class SchemaListNotFound(schemaCriterion: SchemaCriterion, error: ClientError) extends LoaderIgluError

    // Casting errors (applied to BQ Loader mostly)
    /** Value doesn't match expected type */
    final case class WrongType(schemaKey: SchemaKey, value: Json, expected: String) extends LoaderIgluError
    /** Field should be repeatable, but value is not an JSON Array */
    final case class NotAnArray(schemaKey: SchemaKey, value: Json, expected: String) extends LoaderIgluError
    /** Value is required by Schema, but missing in JSON object */
    final case class MissingInValue(schemaKey: SchemaKey, key: String, value: Json) extends LoaderIgluError

    implicit val badRowIgluErrorInfoCirceJsonEncoder: Encoder[LoaderIgluError] =
      Encoder.instance {
        case f: IgluError =>
          deriveEncoder[IgluError].apply(f)
        case f: InvalidSchema =>
          deriveEncoder[InvalidSchema].apply(f)
        case f: SchemaListNotFound =>
          deriveEncoder[SchemaListNotFound].apply(f)

        case f: WrongType =>
          deriveEncoder[WrongType].apply(f)
        case f: NotAnArray =>
          deriveEncoder[NotAnArray].apply(f)
        case f: MissingInValue =>
          deriveEncoder[MissingInValue].apply(f)
      }

    implicit val badRowIgluErrorInfoCirceJsonDecoder: Decoder[LoaderIgluError] =
      List[Decoder[LoaderIgluError]](
        deriveDecoder[IgluError].widen,
        deriveDecoder[InvalidSchema].widen,
        deriveDecoder[SchemaListNotFound].widen,

        deriveDecoder[WrongType].widen,
        deriveDecoder[NotAnArray].widen,
        deriveDecoder[MissingInValue].widen
      ).reduceLeft(_ or _)
  }

  sealed trait LoaderRecoveryError
  object LoaderRecoveryError {
    /** Payload object can not be converted back to enriched event format successfully */
    final case class ParsingError(message: String, location: List[String]) extends LoaderRecoveryError
    /** Error happened due third-party service (such as PubSub or BigQuery DB) */
    final case class RuntimeError(message: String, location: Option[String], reason: Option[String]) extends LoaderRecoveryError

    implicit val badRowLoaderRecoveryErrorCirceJsonDecoder: Decoder[LoaderRecoveryError] =
      List[Decoder[LoaderRecoveryError]](
        deriveDecoder[ParsingError].widen,
        deriveDecoder[RuntimeError].widen
      ).reduceLeft(_ or _)

    implicit val badRowLoaderRecoveryErrorCirceJsonEncoder: Encoder[LoaderRecoveryError] =
      Encoder.instance {
        case f: ParsingError =>
          deriveEncoder[ParsingError].apply(f)
        case f: RuntimeError =>
          deriveEncoder[RuntimeError].apply(f)
      }
  }
}
