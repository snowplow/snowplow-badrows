/*
 * Copyright (c) 2018-2020 Snowplow Analytics Ltd. All rights reserved.
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
package com.snowplowanalytics.snowplow.badrows.generators

import java.time.Instant

import org.joda.time.DateTime

import cats.data.NonEmptyList

import io.circe.Json
import io.circe.syntax._

import com.snowplowanalytics.iglu.core.{ParseError, SchemaCriterion, SchemaKey, SchemaVer}

import com.snowplowanalytics.snowplow.analytics.scalasdk.ParsingError

import com.snowplowanalytics.snowplow.badrows.Processor

import org.scalacheck.{Arbitrary, Gen}

/** Generators for non-badrows data structures */
object CommonGen {

  //  Jan 01 2061 17:07:20, random date in future
  private val MaxTimestamp = 2871824840360L

  val schemaVer: Gen[SchemaVer.Full] =
    for {
      model <- Gen.chooseNum(1, 9)
      revision <- Gen.chooseNum(0, 9)
      addition <- Gen.chooseNum(0, 15)
    } yield SchemaVer.Full(model, revision, addition)

  val schemaKey: Gen[SchemaKey] =
    for {
      vendor <- Gen.identifier
      name <- Gen.identifier
      format <- Gen.identifier
      version <- schemaVer
    } yield SchemaKey(vendor, name, format, version)

  val schemaCriterion: Gen[SchemaCriterion] =
    for {
      vendor <- Gen.identifier
      name <- Gen.identifier
      format <- Gen.identifier
      model <- Gen.option(Gen.chooseNum(1, 9))
      revision <- if (model.isDefined) Gen.option(Gen.chooseNum(0, 0)) else Gen.const(None)
      addition <- if (revision.isDefined) Gen.option(Gen.chooseNum(0, 0)) else Gen.const(None)
    } yield SchemaCriterion(vendor, name, format, model, revision, addition)


  def strGen(n: Int, gen: Gen[Char]): Gen[String] =
    Gen.chooseNum(1, n).flatMap(len => Gen.listOfN(len, gen).map(_.mkString))

  implicit val instantArbitrary: Arbitrary[Instant] =
    Arbitrary {
      for {
        seconds <- Gen.chooseNum(0L, MaxTimestamp)
        nanos <- Gen.chooseNum(Instant.MIN.getNano, Instant.MAX.getNano)
      } yield Instant.ofEpochMilli(seconds).plusNanos(nanos.toLong)
    }

  val instantGen: Gen[Instant] =
    Arbitrary.arbitrary[Instant]

  val dateTimeGen: Gen[DateTime] =
    instantGen.map { instant => DateTime.parse(instant.toString) }

  def jsonGen: Gen[Json] =
    Gen.oneOf(
      Gen.asciiPrintableStr.map(Json.fromString),
      Gen.chooseNum(0, 3000).map(Json.fromInt),
      Gen.listOf(Gen.alphaStr).map(_.asJson),
      for { key <- Gen.identifier; value <- jsonGen } yield Json.obj(key -> value)
    )

  val parseError: Gen[ParseError] =
    Gen.oneOf(ParseError.InvalidData, ParseError.InvalidIgluUri, ParseError.InvalidSchema, ParseError.InvalidSchemaVer)

  val ipv4Address: Gen[String] =
    for {
      a <- Gen.chooseNum(0, 255)
      b <- Gen.chooseNum(0, 255)
      c <- Gen.chooseNum(0, 255)
      d <- Gen.chooseNum(0, 255)
    } yield s"$a.$b.$c.$d"

  val ipv6Address: Gen[String] =
    for {
      a <- Arbitrary.arbitrary[Short]
      b <- Arbitrary.arbitrary[Short]
      c <- Arbitrary.arbitrary[Short]
      d <- Arbitrary.arbitrary[Short]
      e <- Arbitrary.arbitrary[Short]
      f <- Arbitrary.arbitrary[Short]
      g <- Arbitrary.arbitrary[Short]
      h <- Arbitrary.arbitrary[Short]
    } yield f"$a%x:$b%x:$c%x:$d%x:$e%x:$f%x:$g%x:$h%x"

  val ipAddress: Gen[String] =
    Gen.oneOf(ipv4Address, ipv6Address)

  def listOfMaxN[T](n: Int, g: Gen[T]) =
    Gen.chooseNum(0, n).flatMap(nn => Gen.listOfN(nn, g))

  def nonEmptyListOfMaxN[T](n: Int, g: Gen[T]) =
    Gen.chooseNum(1, n).flatMap(nn => Gen.listOfN(nn, g))

  val parsingError: Gen[ParsingError] =
    Gen.oneOf(
      Gen.const(ParsingError.NotTSV),
      Gen.chooseNum(1, 131).map(ParsingError.FieldNumberMismatch.apply),
      Gen.nonEmptyListOf(rowDecodingErrorInfo).map(NonEmptyList.fromListUnsafe).map(ParsingError.RowDecodingError.apply)
    )

  val processor =
    Gen.oneOf(
      Processor("artifact", "0.0.1"),
      Processor("snowplow-snowflake-loader", "1.0.0-rc3"),
      Processor("snowplow-stream-enrich", "0.23.0-rc10")
    )

  private def rowDecodingErrorInfo: Gen[ParsingError.RowDecodingErrorInfo] =
    for {
      message <- strGen(256, Gen.alphaNumChar)
      key <- Gen.oneOf('collectorTstamp, 'eventId, 'trueTstamp, 'pageUrl)
      value <- strGen(256, Gen.alphaNumChar)
      data <- Gen.oneOf(true, false).map {
        case true => ParsingError.RowDecodingErrorInfo.InvalidValue(key, value, message)
        case false => ParsingError.RowDecodingErrorInfo.UnhandledRowDecodingError(message)
      }
    } yield data
}
