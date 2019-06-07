package com.snowplowanalytics.snowplow.badrows

import java.time.Instant

import cats.data.NonEmptyList
import cats.syntax.functor._
import com.snowplowanalytics.iglu.client.ClientError
import com.snowplowanalytics.iglu.core.{SchemaCriterion, SchemaKey}
import com.snowplowanalytics.iglu.core.circe.CirceIgluCodecs._
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto._
import io.circe.java8.time._

object SchemaCriterionExt {
  implicit val encoder: Encoder[SchemaCriterion] = deriveEncoder
  implicit val decoder: Decoder[SchemaCriterion] = deriveDecoder
}

sealed trait Failure
object Failure {
  implicit val encodeFailure: Encoder[Failure] = Encoder.instance {
    case f: CPFormatViolation => deriveEncoder[CPFormatViolation].apply(f)
    case f: AdapterFailures => deriveEncoder[AdapterFailures].apply(f)
    case f: SchemaViolations => deriveEncoder[SchemaViolations].apply(f)
    case f: EnrichmentFailures => deriveEncoder[EnrichmentFailures].apply(f)
    case f: SizeViolation => deriveEncoder[SizeViolation].apply(f)
  }
  implicit val failureDecoder: Decoder[Failure] = List[Decoder[Failure]](
    deriveDecoder[CPFormatViolation].widen,
    deriveDecoder[AdapterFailures].widen,
    deriveDecoder[SchemaViolations].widen,
    deriveDecoder[EnrichmentFailures].widen,
    deriveDecoder[SizeViolation].widen
  ).reduceLeft(_ or _)

  final case class CPFormatViolation(
    timestamp: Instant,
    loader: String,
    message: CPFormatViolationMessage
  ) extends Failure

  final case class AdapterFailures(
    timestamp: Instant,
    vendor: String,
    version: String,
    messages: NonEmptyList[AdapterFailure]
  ) extends Failure

  final case class SchemaViolations(timestamp: Instant, messages: NonEmptyList[SchemaViolation])
    extends Failure

  final case class EnrichmentFailures(timestamp: Instant, messages: NonEmptyList[EnrichmentFailure])
    extends Failure

  final case class SizeViolation(
    timestamp: Instant,
    maximumAllowedSizeBytes: Int,
    actualSizeBytes: Int,
    expectation: String
  ) extends Failure
}

// COLLECTOR PAYLOAD FORMAT VIOLATION

sealed trait CPFormatViolationMessage
object CPFormatViolationMessage {
  implicit val cpFormatViolationMessageEncoder: Encoder[CPFormatViolationMessage] =
    Encoder.instance {
      case f: InputDataCPFormatViolationMessage =>
        deriveEncoder[InputDataCPFormatViolationMessage].apply(f)
      case f: FallbackCPFormatViolationMessage =>
        deriveEncoder[FallbackCPFormatViolationMessage].apply(f)
    }
  implicit val cpFormatViolationMessageDecoder: Decoder[CPFormatViolationMessage] =
    List[Decoder[CPFormatViolationMessage]](
      deriveDecoder[InputDataCPFormatViolationMessage].widen,
      deriveDecoder[FallbackCPFormatViolationMessage].widen
    ).reduceLeft(_ or _)

  final case class InputDataCPFormatViolationMessage(
    payloadField: String,
    value: Option[String],
    expectation: String
  ) extends CPFormatViolationMessage

  final case class FallbackCPFormatViolationMessage(error: String) extends CPFormatViolationMessage
}

// ADAPTER FAILURES

sealed trait AdapterFailure
object AdapterFailure {
  import SchemaCriterionExt._
  implicit val adapterFailureEncoder: Encoder[AdapterFailure] = Encoder.instance {
    case f: IgluErrorAdapterFailure => deriveEncoder[IgluErrorAdapterFailure].apply(f)
    case f: SchemaCritAdapterFailure => deriveEncoder[SchemaCritAdapterFailure].apply(f)
    case f: NotJsonAdapterFailure => deriveEncoder[NotJsonAdapterFailure].apply(f)
    case f: NotSDAdapterFailure => deriveEncoder[NotSDAdapterFailure].apply(f)
    case f: InputDataAdapterFailure => deriveEncoder[InputDataAdapterFailure].apply(f)
    case f: SchemaMappingAdapterFailure => deriveEncoder[SchemaMappingAdapterFailure].apply(f)
  }
  implicit val adapterFailureDecoder: Decoder[AdapterFailure] = List[Decoder[AdapterFailure]](
    deriveDecoder[IgluErrorAdapterFailure].widen,
    deriveDecoder[SchemaCritAdapterFailure].widen,
    deriveDecoder[NotJsonAdapterFailure].widen,
    deriveDecoder[NotSDAdapterFailure].widen,
    deriveDecoder[InputDataAdapterFailure].widen,
    deriveDecoder[SchemaMappingAdapterFailure].widen
  ).reduceLeft(_ or _)

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
}

// SCHEMA VIOLATIONS

sealed trait EnrichmentStageIssue

sealed trait SchemaViolation extends EnrichmentStageIssue
object SchemaViolation {
  import SchemaCriterionExt._
  implicit val schemaViolationEncoder: Encoder[SchemaViolation] = Encoder.instance {
    case f: NotJsonSchemaViolation => deriveEncoder[NotJsonSchemaViolation].apply(f)
    case f: NotSDSchemaViolation => deriveEncoder[NotSDSchemaViolation].apply(f)
    case f: IgluErrorSchemaViolation => deriveEncoder[IgluErrorSchemaViolation].apply(f)
    case f: SchemaCritSchemaViolation => deriveEncoder[SchemaCritSchemaViolation].apply(f)
  }
  implicit val schemaViolationDecoder: Decoder[SchemaViolation] = List[Decoder[SchemaViolation]](
    deriveDecoder[NotJsonSchemaViolation].widen,
    deriveDecoder[NotSDSchemaViolation].widen,
    deriveDecoder[IgluErrorSchemaViolation].widen,
    deriveDecoder[SchemaCritSchemaViolation].widen
  ).reduceLeft(_ or _)

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
}

// ENRICHMENT FAILURES

final case class EnrichmentFailure(
  enrichment: Option[EnrichmentInformation],
  message: EnrichmentFailureMessage
) extends EnrichmentStageIssue
object EnrichmentFailure {
  implicit val enrichmentFailureEncoder: Encoder[EnrichmentFailure] = deriveEncoder
  implicit val enrichmentFailureDecoder: Decoder[EnrichmentFailure] = deriveDecoder
}

sealed trait EnrichmentFailureMessage
object EnrichmentFailureMessage {
  implicit val enrichmentFailureMessageEncoder: Encoder[EnrichmentFailureMessage] =
    Encoder.instance {
      case f: SimpleEnrichmentFailureMessage =>
        deriveEncoder[SimpleEnrichmentFailureMessage].apply(f)
      case f: InputDataEnrichmentFailureMessage =>
        deriveEncoder[InputDataEnrichmentFailureMessage].apply(f)
    }
  implicit val enrichmentFailureMessageDecoder: Decoder[EnrichmentFailureMessage] =
    List[Decoder[EnrichmentFailureMessage]](
      deriveDecoder[SimpleEnrichmentFailureMessage].widen,
      deriveDecoder[InputDataEnrichmentFailureMessage].widen
    ).reduceLeft(_ or _)

  final case class SimpleEnrichmentFailureMessage(error: String) extends EnrichmentFailureMessage

  final case class InputDataEnrichmentFailureMessage(
    field: String,
    value: Option[String],
    expectation: String
  ) extends EnrichmentFailureMessage
}

final case class EnrichmentInformation(schemaKey: SchemaKey, identifier: String)
object EnrichmentInformation {
  implicit val enrichmentInformationEncoder: Encoder[EnrichmentInformation] = deriveEncoder
  implicit val enrichmentInformationDecoder: Decoder[EnrichmentInformation] = deriveDecoder
}

