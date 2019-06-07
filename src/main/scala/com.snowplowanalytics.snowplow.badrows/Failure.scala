package com.snowplowanalytics.snowplow.badrows

import java.time.Instant

import cats.data.NonEmptyList
import com.snowplowanalytics.iglu.client.ClientError
import com.snowplowanalytics.iglu.core.{SchemaCriterion, SchemaKey}
import io.circe.Encoder
import io.circe.generic.auto._
import io.circe.java8.time._
import io.circe.syntax._

sealed trait Failure
object Failure {
  implicit val encodeFailure: Encoder[Failure] = Encoder.instance {
    case f: CPFormatViolation => f.asJson
    case f: AdapterFailures => f.asJson
    case f: SchemaViolations => f.asJson
    case f: EnrichmentFailures => f.asJson
    case f: SizeViolation => f.asJson
  }
}

// COLLECTOR PAYLOAD FORMAT VIOLATION

final case class CPFormatViolation(
  timestamp: Instant,
  loader: String,
  message: CPFormatViolationMessage
) extends Failure

sealed trait CPFormatViolationMessage
object CPFormatViolationMessage {
  implicit val encodeCPFormatViolationMessage: Encoder[CPFormatViolationMessage] =
    Encoder.instance {
      case f: InputDataCPFormatViolationMessage => f.asJson
      case f: FallbackCPFormatViolationMessage => f.asJson
    }
}
final case class InputDataCPFormatViolationMessage(
  payloadField: String,
  value: Option[String],
  expectation: String
) extends CPFormatViolationMessage
final case class FallbackCPFormatViolationMessage(error: String) extends CPFormatViolationMessage

// ADAPTER FAILURES

final case class AdapterFailures(
  timestamp: Instant,
  vendor: String,
  version: String,
  messages: NonEmptyList[AdapterFailure]
) extends Failure

sealed trait AdapterFailure
object AdapterFailure {
  implicit val encodeAdapterFailure: Encoder[AdapterFailure] = Encoder.instance {
    case f: IgluErrorAdapterFailure => f.asJson
    case f: SchemaCritAdapterFailure => f.asJson
    case f: NotJsonAdapterFailure => f.asJson
    case f: NotSDAdapterFailure => f.asJson
    case f: InputDataAdapterFailure => f.asJson
    case f: SchemaMappingAdapterFailure => f.asJson
  }
}

// tracker protocol
final case class IgluErrorAdapterFailure(schemaKey: SchemaKey, error: ClientError)
    extends AdapterFailure
final case class SchemaCritAdapterFailure(schemaKey: SchemaKey, schemaCriterion: SchemaCriterion)
    extends AdapterFailure
// both tp and webhook
final case class NotJsonAdapterFailure(
  field: String,
  value: Option[String],
  error: String
) extends AdapterFailure
final case class NotSDAdapterFailure(json: String, error: String) extends AdapterFailure
final case class InputDataAdapterFailure(
  field: String,
  value: Option[String],
  expectation: String
) extends AdapterFailure
// webhook adapters
final case class SchemaMappingAdapterFailure(
  actual: Option[String],
  expectedMapping: Map[String, String],
  expectation: String
) extends AdapterFailure

// SCHEMA VIOLATIONS

sealed trait EnrichmentStageIssue

final case class SchemaViolations(timestamp: Instant, messages: NonEmptyList[SchemaViolation])
    extends Failure

sealed trait SchemaViolation extends EnrichmentStageIssue
object SchemaViolation {
  implicit val schemaViolationEncoder: Encoder[SchemaViolation] = Encoder.instance {
    case f: NotJsonSchemaViolation => f.asJson
    case f: NotSDSchemaViolation => f.asJson
    case f: IgluErrorSchemaViolation => f.asJson
    case f: SchemaCritSchemaViolation => f.asJson
  }
}

final case class NotJsonSchemaViolation(
  field: String,
  value: Option[String],
  error: String
) extends SchemaViolation
final case class NotSDSchemaViolation(json: String, error: String) extends SchemaViolation
final case class IgluErrorSchemaViolation(schemaKey: SchemaKey, error: ClientError)
    extends SchemaViolation
final case class SchemaCritSchemaViolation(schemaKey: SchemaKey, schemaCriterion: SchemaCriterion)
    extends SchemaViolation

// ENRICHMENT FAILURES

final case class EnrichmentFailures(timestamp: Instant, messages: NonEmptyList[EnrichmentFailure])
    extends Failure

final case class EnrichmentFailure(
  enrichment: Option[EnrichmentInformation],
  message: EnrichmentFailureMessage
) extends EnrichmentStageIssue

sealed trait EnrichmentFailureMessage
object EnrichmentFailureMessage {
  implicit val enrichmentFailureMessageEncoder: Encoder[EnrichmentFailureMessage] =
    Encoder.instance {
      case f: SimpleEnrichmentFailureMessage => f.asJson
      case f: InputDataEnrichmentFailureMessage => f.asJson
    }
}

final case class SimpleEnrichmentFailureMessage(error: String) extends EnrichmentFailureMessage
final case class InputDataEnrichmentFailureMessage(
  field: String,
  value: Option[String],
  expectation: String
) extends EnrichmentFailureMessage

final case class EnrichmentInformation(schemaKey: SchemaKey, identifier: String)

// SIZE VIOLATION

final case class SizeViolation(
  timestamp: Instant,
  maximumAllowedSizeBytes: Int,
  actualSizeBytes: Int,
  expectation: String
) extends Failure
