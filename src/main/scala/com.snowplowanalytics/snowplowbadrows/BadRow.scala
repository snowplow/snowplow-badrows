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

import io.circe.Json

import cats.instances.list._
import cats.instances.either._
import cats.syntax.either._
import cats.syntax.alternative._

import com.snowplowanalytics.iglu.core.SchemaKey

import cats.data.NonEmptyList

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


  // Stages of pipeline
  type RawPayload = String                      // Format is valid
  type CollectorPayload = Map[String, String]   // Tracker Protocol is valid
  type SnowplowPayload = String                 // All iglu payloads aren't corrupted

  type EnrichedEvent = String                   // No failures during enrichment
  type CorrectEvent = String                    // All schemas are valid

  case class EnrichmentError(name: String, message: String)

  sealed trait IgluParseError
  object IgluParseError {
    case class InvalidPayload(original: Json) extends IgluParseError
    case class InvalidUri(uri: String) extends IgluParseError
  }

  sealed trait IgluResolverError
  object IgluResolverError {
    case class RegistryFailure(name: String, reason: String)

    case class SchemaNotFound(schemaKey: SchemaKey, failures: NonEmptyList[RegistryFailure]) extends IgluResolverError
    case class ValidationError(schemaKey: SchemaKey, processingMessage: ProcessingMessage) extends IgluResolverError
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
                                 userId: Option[String])

  sealed trait Payload {
    def metadata: CollectorMeta
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
  case class EnrichmentFailure(payload: Payload.SingleEvent, errors: NonEmptyList[EnrichmentError], processor: Processor)

  /**
    * Third validation level:
    * Context or self-describing event is not valid against its schema.
    * Most common error-type.
    * - invalid property
    * - schema not found
    *
    * @param payload single event payload (not just failed schema)
    */
  case class SchemaInvalidation(payload: Payload.SingleEvent, errors: NonEmptyList[IgluResolverError], processor: Processor)

  // Enriched event actually,
  case class LoaderFailure(payload: Payload.SingleEvent, errors: NonEmptyList[Json], processor: Processor)

  // TODOs
  // * Granular iglu errors

  // Issues
  // How does Loader-badrows match (shredder, bq loader)
  // Loaders also can add schema'ed data (deduplication)
  // Two types of third-level errors, they should be aggregated
}
