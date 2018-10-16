package com.snowplowanalytics.snowplowbadrows

import cats.data.NonEmptyList

import io.circe.{Encoder, Json}
import io.circe.syntax._

import com.snowplowanalytics.iglu.core.SchemaKey

import BadRow.{ ProcessingMessage, schemaKeyEncoder }

sealed trait IgluResolverError

object IgluResolverError {
  case class RegistryFailure(name: String, reason: String)

  /** Schema was not found (probably some registries respond with 500) */
  case class SchemaNotFound(schemaKey: SchemaKey, failures: NonEmptyList[RegistryFailure]) extends IgluResolverError
  /** Schema was found somewhere, but resolver has invalidated instance with it */
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

