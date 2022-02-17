/*
 * Copyright (c) 2018-2022 Snowplow Analytics Ltd. All rights reserved.
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

import cats.data.NonEmptyList

import com.snowplowanalytics.iglu.client.ClientError
import com.snowplowanalytics.iglu.client.resolver.LookupHistory
import com.snowplowanalytics.iglu.client.resolver.registries.RegistryError
import com.snowplowanalytics.iglu.client.validator.{ValidatorError, ValidatorReport}

import org.scalacheck.Gen

import scala.collection.immutable.SortedMap

object IgluClientErrorGen {

  def clientError: Gen[ClientError] =
    Gen.oneOf(resolutionError, validationError)

  val registryError: Gen[RegistryError] =
    Gen.oneOf(
      RegistryError.NotFound,
      RegistryError.ClientFailure("OutOfMemoryError"),
      RegistryError.RepoFailure("502 Service is not available"),
      RegistryError.RepoFailure("Timeout error")
    )

  val lookupHistory: Gen[LookupHistory] =
    for {
      errors <- Gen.nonEmptyListOf(registryError).map(_.toSet)
      attempts <- Gen.chooseNum(1, 50)
      lastAttempt <- CommonGen.instantGen
    } yield LookupHistory(errors, attempts, lastAttempt)

  val resolutionError: Gen[ClientError.ResolutionError] =
    Gen
      .identifier
      .flatMap { k => lookupHistory.flatMap { v => (k, v) } }
      .flatMap { kv => Gen.nonEmptyListOf(kv) }
      .map(_.foldLeft(SortedMap.empty[String, LookupHistory])(_ + _))
      .map(ClientError.ResolutionError(_))

  val validatorReport: Gen[ValidatorReport] =
    for {
      message <- Gen.asciiPrintableStr
      path <- Gen.option(Gen.identifier.map { i => "$." ++ i })
      targets <- Gen.listOf(Gen.oneOf("maximum", "oneOf", "enum")).map(_.distinct)
      keyword <- Gen.option(Gen.identifier)
    } yield ValidatorReport(message, path, targets, keyword)

  val invalidData: Gen[ValidatorError.InvalidData] =
    Gen
      .nonEmptyListOf(validatorReport)
      .map(NonEmptyList.fromListUnsafe)
      .map(ValidatorError.InvalidData.apply)

  val schemaIssue: Gen[ValidatorError.SchemaIssue] =
    for {
      path    <- Gen.identifier.map { i => "$." ++ i }
      message <- Gen.asciiPrintableStr
    } yield ValidatorError.SchemaIssue(path, message)

  val invalidSchema: Gen[ValidatorError.InvalidSchema] =
    Gen.nonEmptyListOf(schemaIssue)
      .map(NonEmptyList.fromListUnsafe)
      .map(ValidatorError.InvalidSchema.apply)

  val validatorError: Gen[ValidatorError] =
    Gen.oneOf(invalidData, invalidSchema)

  val validationError: Gen[ClientError.ValidationError] =
    validatorError.map(ClientError.ValidationError.apply)

}
