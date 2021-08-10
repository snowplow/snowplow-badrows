/*
 * Copyright (c) 2018-2021 Snowplow Analytics Ltd. All rights reserved.
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
package com.snowplowanalytics.snowplow

import org.joda.time.DateTime

import cats.syntax.either._

import io.circe.{Decoder, Encoder}

import com.snowplowanalytics.iglu.core.{ParseError, SchemaCriterion}

package object badrows {
  implicit val dateTimeEncoder: Encoder[DateTime] =
    Encoder[String].contramap(_.toString)
  implicit val dateTimeDecoder: Decoder[DateTime] =
    Decoder[String].emap { s => Either.catchNonFatal(DateTime.parse(s)).leftMap(_.getMessage) }

  implicit val igluParseErrorEncoder: Encoder[ParseError] =
    Encoder[String].contramap(_.code)
  implicit val igluParseErrorDecoder: Decoder[ParseError] =
    Decoder[String].emap { s => ParseError.parse(s).toRight(s"$s is invalid Iglu ParseError") }

  // Iglu Core codecs work with objects, which is incompatible with our schema
  implicit val igluSchemaCriterionEncoder: Encoder[SchemaCriterion] =
    Encoder[String].contramap(_.asString)
  implicit val igluSchemaCriterionDecoder: Decoder[SchemaCriterion] =
    Decoder[String].emap { s => SchemaCriterion.parse(s).toRight(s"$s is invalid Iglu schema criterion") }
}
