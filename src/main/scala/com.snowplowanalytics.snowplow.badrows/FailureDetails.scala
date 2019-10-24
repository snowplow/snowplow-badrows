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

import io.circe.{Encoder, Decoder}
import io.circe.generic.semiauto._
import io.circe.syntax._
import io.circe.Json

import cats.syntax.functor._
import cats.data.NonEmptyList

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
      deriveDecoder[InputData].widen,
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
    implicit val enrichmentFailureEncoder: Encoder[EnrichmentFailure] = deriveEncoder
    implicit val enrichmentFailureDecoder: Decoder[EnrichmentFailure] = deriveDecoder
  }
  
  sealed trait EnrichmentFailureMessage
  object EnrichmentFailureMessage {
    implicit val enrichmentFailureMessageEncoder: Encoder[EnrichmentFailureMessage] =
      Encoder.instance {
        case f: Simple =>
          deriveEncoder[Simple].apply(f)
        case f: InputData =>
          deriveEncoder[InputData].apply(f)
      }
    implicit val enrichmentFailureMessageDecoder: Decoder[EnrichmentFailureMessage] =
      List[Decoder[EnrichmentFailureMessage]](
        deriveDecoder[Simple].widen,
        deriveDecoder[InputData].widen
      ).reduceLeft(_ or _)
  
    final case class Simple(error: String) extends EnrichmentFailureMessage
    final case class InputData(field: String, value: Option[String], expectation: String) extends EnrichmentFailureMessage
  }
  
  final case class EnrichmentInformation(schemaKey: SchemaKey, identifier: String)
  object EnrichmentInformation {
    implicit val enrichmentInformationEncoder: Encoder[EnrichmentInformation] = deriveEncoder
    implicit val enrichmentInformationDecoder: Decoder[EnrichmentInformation] = deriveDecoder
  }
  
  final case class LoaderIgluError(schemaKey: SchemaKey, error: ClientError)
  object LoaderIgluError {
    implicit val badRowIgluErrorInfoCirceJsonEncoder: Encoder[LoaderIgluError] = deriveEncoder[LoaderIgluError]
    implicit val badRowIgluErrorInfoCirceJsonDecoder: Decoder[LoaderIgluError] = deriveDecoder[LoaderIgluError]
  }
  
  final case class BQCastError(data: Json, schemaKey: SchemaKey, errors: NonEmptyList[BQCastErrorInfo])
  object BQCastError {
    implicit val badRowBqCastErrorJsonEncoder: Encoder[BQCastError] = deriveEncoder[BQCastError]
    implicit val badRowBqCastErrorJsonDecoder: Decoder[BQCastError] = deriveDecoder[BQCastError]
  }
  
  sealed trait BQCastErrorInfo extends Product with Serializable
  object BQCastErrorInfo {
    implicit val badRowBqCastErrorInfoJsonEncoder: Encoder[BQCastErrorInfo] = Encoder.instance {
      case WrongType(value, expected) => Json.obj(
        "wrongType" := Json.obj(
          "value" := value,
          "expectedType":= expected.asJson
        )
      )
      case NotAnArray(value, expected) => Json.obj(
        "notAnArray" := Json.obj(
          "value" := value,
          "expectedType":= expected.asJson
        )
      )
      case MissingInValue(key, value) => Json.obj(
        "missingInValue" := Json.obj(
          "missingKey" := key,
          "value":= value.asJson
        )
      )
    }
  
    implicit val badRowBqCastErrorInfoJsonDecoder: Decoder[BQCastErrorInfo] = Decoder.instance { p =>
      p.keys.getOrElse(List()).toList match {
        case l if l.contains("wrongType") => for {
          value <- p.downField("wrongType").downField("value").as[Json]
          expected <- p.downField("wrongType").downField("expectedType").as[String]
        } yield WrongType(value, expected)
        case l if l.contains("notAnArray") => for {
          value <- p.downField("notAnArray").downField("value").as[Json]
          expected <- p.downField("notAnArray").downField("expectedType").as[String]
        } yield NotAnArray(value, expected)
        case l if l.contains("missingInValue") => for {
          key <- p.downField("missingInValue").downField("missingKey").as[String]
          value <- p.downField("missingInValue").downField("value").as[Json]
        } yield MissingInValue(key, value)
      }
    }
  
    /** Value doesn't match expected type */
    final case class WrongType(value: Json, expected: String) extends BQCastErrorInfo
    /** Field should be repeatable, but value is not an JSON Array */
    final case class NotAnArray(value: Json, expected: String) extends BQCastErrorInfo
    /** Value is required by Schema, but missing in JSON object */
    final case class MissingInValue(key: String, value: Json) extends BQCastErrorInfo
  }
}
