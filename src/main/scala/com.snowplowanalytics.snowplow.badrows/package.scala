package com.snowplowanalytics.snowplow

import org.joda.time.DateTime
import cats.syntax.either._
import com.snowplowanalytics.iglu.core.{ParseError, SchemaCriterion}

import io.circe.{Decoder, Encoder}

package object badrows {
  implicit val dateTimeEncoder: Encoder[DateTime] =
    Encoder[String].contramap(_.toString)
  implicit val dateTimeDecoder: Decoder[DateTime] =
    Decoder[String].emap { s => Either.catchNonFatal(DateTime.parse(s)).leftMap(_.getMessage) }

  implicit val igluParseErrorEncoder: Encoder[ParseError] =
    Encoder[String].contramap(_.code)
  implicit val igluParseErrorDecoder: Decoder[ParseError] =
    Decoder[String].emap { s => ParseError.parse(s).toRight(s"$s is invalid Iglu ParseError") }

  implicit val igluSchemaCriterionEncoder: Encoder[SchemaCriterion] =
    Encoder[String].contramap(_.asString)
  implicit val igluSchemaCriterionDecoder: Decoder[SchemaCriterion] =
    Decoder[String].emap { s => SchemaCriterion.parse(s).toRight(s"$s is invalid Iglu schema criterion") }
}
