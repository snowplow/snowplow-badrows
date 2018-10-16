package com.snowplowanalytics.snowplowbadrows

import cats.data.NonEmptyList

import io.circe.{Encoder, Json}
import io.circe.syntax._

import com.snowplowanalytics.iglu.core.SchemaKey

import BadRow._

sealed trait IgluError

sealed trait IgluParseError extends IgluError
object IgluParseError {
  case class InvalidPayload(original: Json) extends IgluParseError
  case class InvalidUri(uri: String) extends IgluParseError

  implicit val encoder: Encoder[IgluParseError] = Encoder.instance {
    case InvalidPayload(json) =>
      Json.fromFields(List("error" -> "INVALID_PAYLOAD".asJson, "json" -> json))
    case InvalidUri(uri) =>
      Json.fromFields(List("error" -> "INVALID_URI".asJson, "uri" -> Json.fromString(uri)))
  }
}

sealed trait IgluResolverError extends IgluError
object IgluResolverError {
  case class RegistryFailure(name: String, reason: String)

  case class SchemaNotFound(schemaKey: SchemaKey, failures: NonEmptyList[RegistryFailure]) extends IgluResolverError
  case class ValidationError(schemaKey: SchemaKey, processingMessage: ProcessingMessage) extends IgluResolverError

  implicit val registryFailureEncoder: Encoder[RegistryFailure] = Encoder.instance {
    case RegistryFailure(name, reason) =>
      Json.fromFields(List("name" -> name.asJson, "reason" -> reason.asJson))
  }

  implicit val encoder: Encoder[IgluResolverError] = Encoder.instance {
    case SchemaNotFound(schema, failures) =>
      Json.fromFields(List(
        "error" -> "SCHEMA_NOT_FOUND".asJson,
        "schemaKey" -> schema.asJson,
        "failures" -> failures.asJson))
    case ValidationError(schema, message) =>
      Json.fromFields(List(
        "error" -> "VALIDATION_ERROR".asJson,
        "schemaKey" -> schema.asJson,
        "processingMessage" -> message.asJson))
  }
}

