/*
 * Copyright (c) 2018 Snowplow Analytics Ltd. All rights reserved.
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

package com.snowplowanalytics.snowplowbadrows

import java.time.Instant
import java.util.Base64

import cats.data.NonEmptyList
import cats.instances.list._
import cats.instances.either._
import cats.syntax.either._
import cats.syntax.show._
import cats.syntax.alternative._

import io.circe.{Decoder, Encoder, Json, JsonObject}
import io.circe.syntax._

import com.snowplowanalytics.iglu.core.{SchemaKey, SchemaVer, SelfDescribingData}
import com.snowplowanalytics.iglu.core.circe.instances._

import BadRow._

sealed trait BadRow {
  /** Everything necessary to recover/fix event(s) */
  def payload: Payload
  /** Application that produced bad row (Spark Enrich, Stream Enrich, Loader) */
  def processor: Processor
}

object BadRow {
  // Placeholders
  type Processor = String
  type ProcessingMessage = String     // Schema validation message
  type Event = String                 // Fully valid enriched event (from Analytics SDK)

  // Should be in iglu core
  implicit val schemaKeyEncoder: Encoder[SchemaKey] =
    Encoder.instance { schemaKey => Json.fromString(schemaKey.show) }

  // Stages of pipeline
  type RawPayload = String                      // Format is valid
  type CollectorPayload = Map[String, String]   // Tracker Protocol is valid
  type SnowplowPayload = String                 // All iglu payloads aren't corrupted

  type EnrichedEvent = String                   // No failures during enrichment
  type CorrectEvent = String                    // All schemas are valid

  val FormatViolationSchema = SchemaKey("com.snowplowanalytics.snowplow.badrows", "format_violation", "jsonschema", SchemaVer.Full(1,0,0))
  val TrackerProtocolViolationSchema = SchemaKey("com.snowplowanalytics.snowplow.badrows", "tracker_protocol_violation", "jsonschema", SchemaVer.Full(1,0,0))
  val IgluViolationSchema = SchemaKey("com.snowplowanalytics.snowplow.badrows", "iglu_violation", "jsonschema", SchemaVer.Full(1,0,0))
  val EnrichmentFailureSchema = SchemaKey("com.snowplowanalytics.snowplow.badrows", "enrichment_failure", "jsonschema", SchemaVer.Full(1,0,0))
  val SchemaInvalidationSchema = SchemaKey("com.snowplowanalytics.snowplow.badrows", "schema_invalidation", "jsonschema", SchemaVer.Full(1,0,0))
  val LoaderFailureSchema = SchemaKey("com.snowplowanalytics.snowplow.badrows", "loader_failure", "jsonschema", SchemaVer.Full(1,0,0))

  case class EnrichmentError(name: String, message: String)

  object EnrichmentError {
    implicit val encoder: Encoder[EnrichmentError] = ???
  }

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

  import IgluParseError._
  import IgluResolverError._

  def parseRawPayload(line: String): Either[FormatViolation, RawPayload] = ???
  def parseCollectorPayloads(rawPayload: RawPayload): List[Either[TrackerProtocolViolation, CollectorPayload]] = ???
  def parseSnowplowPayload(collectorPayload: CollectorPayload): Either[IgluViolation, SnowplowPayload] = ???

  def enrich(payload: SnowplowPayload): Either[BadRow, Event] = ???


  def process(line: String): Either[FormatViolation, (List[BadRow], List[Event])] =
    for {
      rawPayload                             <- parseRawPayload(line)
      (trackerViolations, collectorPayloads)  = parseCollectorPayloads(rawPayload).separate
      (igluViolations, snowplowPayloads)      = collectorPayloads.map(parseSnowplowPayload).separate
      (bad, good)                             = snowplowPayloads.map(enrich).separate
    } yield (trackerViolations ++ igluViolations ++ bad, good)



  /** Taken from Scala Common Enrich */
  final case class CollectorMeta(name: String,
                                 encoding: String,
                                 hostname: Option[String],
                                 timestamp: Option[Instant],
                                 ipAddress: Option[String],
                                 useragent: Option[String],
                                 refererUri: Option[String],
                                 headers: List[String],
                                 userId: Option[String]) {
    def asTsv: String =
      List(name, encoding, hostname.getOrElse(""), timestamp.map(_.toString).getOrElse(""),
        ipAddress.getOrElse(""), useragent.getOrElse(""), refererUri.getOrElse(""),
        headers.mkString(","), userId.getOrElse("")).mkString("\t")
  }

  sealed trait Payload {
    def metadata: CollectorMeta

    def asLine: String = this match {
      case Payload.RawPayload(metadata, rawEvent) =>
        new String(Base64.getEncoder.encode((metadata.asTsv ++ "\t" ++ rawEvent).getBytes))
      case Payload.SingleEvent(metadata, event) =>
        val eventJson = Json.fromJsonObject(JsonObject.fromMap(event.mapValues(Json.fromString))).noSpaces
        new String(Base64.getEncoder.encode((metadata.asTsv ++ "\t" ++ eventJson).getBytes))
    }
  }

  object Payload {
    /** Completely invalid payload; RawEvent from SCE */
    case class RawPayload(metadata: CollectorMeta, rawEvent: String) extends Payload
    /** Valid GET/JSON */
    case class SingleEvent(metadata: CollectorMeta, event: Map[String, String]) extends Payload
  }

  /**
    * Zero validation level.
    * Completely invalid payload, malformed HTTP, something that cannot be parsed:
    * - truncation
    * - garbage
    * - invalid URL query string encoding
    * - robots
    *
    * Everything else if parsed into `RawPayload` can throw POST-bombs
    */
  case class FormatViolation(payload: Payload.RawPayload, message: String, processor: Processor) extends BadRow

  /**
    * First validation level.
    * Valid HTTP, but invalid Snowplow
    * - OPTIONS request
    * - unknown query parameters
    * - any runtime exceptions happened before `EnrichmentManager`
    *
    * @param payload full GET/POST payload
    */
  case class TrackerProtocolViolation(payload: Payload.SingleEvent, message: String, processor: Processor) extends BadRow

  /**
    * Second validation level.
    * Any iglu protocol violation in a *single event*:
    * - missing iglu: protocol
    * - invalid iglu URI
    * - not self-describing data (missing `data` or `schema`)
    * - invalid SchemaVer
    *
    * @param payload single event payload
    */
  case class IgluViolation(payload: Payload.SingleEvent, errors: NonEmptyList[IgluParseError], processor: Processor) extends BadRow

  /**
    * Third validation level:
    * Runtime/validation errors in `EnrichmentManager`:
    * - runtime exception during Weather/lookup enrichments
    *
    * Usually can be just retried
    *
    * @param payload single event payload
    */
  case class EnrichmentFailure(payload: Payload.SingleEvent, errors: NonEmptyList[EnrichmentError], processor: Processor) extends BadRow

  /**
    * Third validation level:
    * Context or self-describing event is not valid against its schema.
    * Most common error-type.
    * - invalid property
    * - schema not found
    *
    * @param payload single event payload (not just failed schema)
    */
  case class SchemaInvalidation(payload: Payload.SingleEvent, errors: NonEmptyList[IgluResolverError], processor: Processor) extends BadRow

  /**
    * Post-enrichment failure:
    * - shredder or BQ loader failed schema validation
    *
    * @param payload enriched event (TSV or JSON)
    */
  case class LoaderFailure(payload: String, errors: NonEmptyList[SelfDescribingData[Json]], processor: Processor)


  implicit val payloadEncoder: Encoder[Payload] =
    Encoder.instance(payload => Json.fromString(payload.asLine))

  val badRowEncoder: Encoder[BadRow] = Encoder.instance {
    case FormatViolation(payload, message, processor) => Json.fromFields(List(
      "payload" -> (payload: Payload).asJson,
      "message" -> message.asJson,
      "processor" -> processor.asJson))
    case TrackerProtocolViolation(payload, message, processor) => Json.fromFields(List(
      "payload" -> (payload: Payload).asJson,
      "message" -> message.asJson,
      "processor" -> processor.asJson))
    case IgluViolation(payload, errors, processor) => Json.fromFields(List(
      "payload" -> (payload: Payload).asJson,
      "errors" -> errors.asJson,
      "processor" -> processor.asJson))
    case EnrichmentFailure(payload, errors, processor) => Json.fromFields(List(
      "payload" -> (payload: Payload).asJson,
      "errors" -> errors.asJson,
      "processor" -> processor.asJson))
    case SchemaInvalidation(payload, errors, processor) => Json.fromFields(List(
      "payload" -> (payload: Payload).asJson,
      "errors" -> errors.asJson,
      "processor" -> processor.asJson))
  }

}
