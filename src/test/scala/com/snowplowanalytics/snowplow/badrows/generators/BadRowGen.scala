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

import java.nio.file.{ StandardOpenOption, Paths, Path, Files }

import cats.implicits._

import org.scalacheck.cats.implicits._

import cats.data.NonEmptyList
import org.scalacheck.Gen
import com.snowplowanalytics.snowplow.badrows.{BadRow, Failure}

object BadRowGen {

  val sizeViolation: Gen[BadRow.SizeViolation] =
    for {
      failure <- FailureGen.sizeViolationFailure
      payload <- PayloadGen.rawPayload
      processor <- CommonGen.processor
    } yield BadRow.SizeViolation(processor, failure, payload)

  val cpFormatViolation: Gen[BadRow.CPFormatViolation] =
    for {
      payload <- PayloadGen.rawPayload
      failure <- FailureGen.cpFormatViolationFailure
      processor <- CommonGen.processor
    } yield BadRow.CPFormatViolation(processor, failure, payload)

  val adapterFailures: Gen[BadRow.AdapterFailures] =
    for {
      adapterFailures <- FailureGen.adapterFailures
      collectorPayload <- PayloadGen.collectorPayload
      processor <- CommonGen.processor
    } yield BadRow.AdapterFailures(processor, adapterFailures, collectorPayload)

  val trackerProtocolViolations: Gen[BadRow.TrackerProtocolViolations] =
    for {
      failure <- FailureGen.trackerProtocolViolations
      payload <- PayloadGen.collectorPayload
      processor <- CommonGen.processor
    } yield BadRow.TrackerProtocolViolations(processor, failure, payload)

  val schemaViolations: Gen[BadRow.SchemaViolations] =
    for {
      failure <- FailureGen.schemaViolations
      payload <- PayloadGen.enrichmentPayload
      processor <- CommonGen.processor
    } yield BadRow.SchemaViolations(processor, failure, payload)

  val enrichmentFailures: Gen[BadRow.EnrichmentFailures] =
    for {
      failure <- FailureGen.enrichmentFailure
      payload <- PayloadGen.enrichmentPayload
      processor <- CommonGen.processor
    } yield BadRow.EnrichmentFailures(processor, failure, payload)

  val loaderParsingError: Gen[BadRow.LoaderParsingError] =
    for {
      payload <- PayloadGen.rawPayload
      failure <- CommonGen.parsingError
      processor <- CommonGen.processor
    } yield BadRow.LoaderParsingError(processor, failure, payload)

  val loaderIgluError: Gen[BadRow.LoaderIgluError] =
    for {
      payload <- PayloadGen.loaderPayload
      failure <- Gen.nonEmptyListOf(FailureDetailsGen.loaderIgluError).map(l => Failure.LoaderIgluErrors(NonEmptyList.fromListUnsafe(l)))
      processor <- CommonGen.processor
    } yield BadRow.LoaderIgluError(processor, failure, payload)

  val loaderRuntimeErrorBadRowGen: Gen[BadRow.LoaderRuntimeError] =
    for {
      payload <- PayloadGen.loaderPayload
      failure <- Gen.alphaNumStr
      processor <- CommonGen.processor
    } yield BadRow.LoaderRuntimeError(processor, failure, payload)

  val loaderRecoveryErrorBadRowGen: Gen[BadRow.LoaderRecoveryError] =
    for {
      payload <- PayloadGen.rawPayload
      failure <- FailureDetailsGen.loaderRecoveryError.map(e => Failure.LoaderRecoveryFailure(e))
      processor <- CommonGen.processor
    } yield BadRow.LoaderRecoveryError(processor, failure, payload)


  def create(path: Option[Path]) = {
    val outputPath = path.getOrElse(Paths.get(sys.env("HOME"), "badrows.json"))
    def write(data: Vector[BadRow]): Unit =
      data.foreach { row =>
        Files.write(outputPath, (row.compact ++ "\r\n").getBytes, StandardOpenOption.APPEND)
      }

    val gen = for {
      sizeViolations                <- Vector.fill(20)(sizeViolation).sequence
      _ = write(sizeViolations)
      cpFormatViolations            <- Vector.fill(50)(cpFormatViolation).sequence
      _ = write(cpFormatViolations)
      adapterFailuress              <- Vector.fill(20)(adapterFailures).sequence
      _ = write(adapterFailuress)
      trackerProtocolViolationss    <- Vector.fill(50)(trackerProtocolViolations).sequence
      _ = write(trackerProtocolViolationss)
      schemaViolationss             <- Vector.fill(200)(schemaViolations).sequence
      _ = write(schemaViolationss)
      enrichmentFailuress           <- Vector.fill(100)(enrichmentFailures).sequence
      _ = write(enrichmentFailuress)
      loaderParsingErrors           <- Vector.fill(20)(loaderParsingError).sequence
      _ = write(loaderParsingErrors)
      loaderIgluErrors              <- Vector.fill(20)(loaderIgluError).sequence
      _ = write(loaderIgluErrors)
      loaderRuntimeErrorErrors      <- Vector.fill(50)(loaderRuntimeErrorBadRowGen).sequence
      _ = write(loaderRuntimeErrorErrors)
      loaderRecoveryErrorBadRowGens <- Vector.fill(20)(loaderRecoveryErrorBadRowGen).sequence
      _ = write(loaderRecoveryErrorBadRowGens)
    } yield ()

    gen.sample.get
  }
}
