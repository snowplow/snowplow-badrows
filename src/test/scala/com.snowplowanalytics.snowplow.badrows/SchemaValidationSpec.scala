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
package com.snowplowanalytics.snowplow.badrows

import java.net.URI

import cats.Id

import io.circe.literal._
import io.circe.syntax._
import io.circe.{Json, parser, Decoder, Encoder}

import org.scalacheck.Prop.forAll

import org.specs2.ScalaCheck
import org.specs2.mutable.Specification

import com.snowplowanalytics.iglu.client.{CirceValidator, Resolver}
import com.snowplowanalytics.iglu.client.resolver.registries.Registry

import SpecHelpers.IdInstances._
import generators.BadRowGen

class SchemaValidationSpec extends Specification with ScalaCheck {
  import SchemaValidationSpec._

  "Stream Collector" >> {
    s"${Schemas.SizeViolation.toSchemaUri} (SizeViolation)" >>
      forAll(BadRowGen.sizeViolation) { f => validateBadRow[BadRow.SizeViolation](f) must beRight }
  }

  "Scala Common Enrich 1.0.0" >> {
    s"${Schemas.CPFormatViolation.toSchemaUri} (CPFormatViolation)" >>
       forAll(BadRowGen.cpFormatViolation) { f => validateBadRow[BadRow.CPFormatViolation](f) must beRight }

     s"${Schemas.AdapterFailures.toSchemaUri} (AdapterFailures)" >>
       forAll(BadRowGen.adapterFailures) { f => validateBadRow[BadRow.AdapterFailures](f) must beRight }

     s"${Schemas.TrackerProtocolViolations.toSchemaUri} (TrackerProtocolViolations)" >>
       forAll(BadRowGen.trackerProtocolViolations) { f => validateBadRow[BadRow.TrackerProtocolViolations](f) must beRight }

     s"${Schemas.SchemaViolations.toSchemaUri} (SchemaViolations)" >>
       forAll(BadRowGen.schemaViolations) { f => validateBadRow[BadRow.SchemaViolations](f) must beRight }

     s"${Schemas.EnrichmentFailures.toSchemaUri} (EnrichmentFailures)" >>
       forAll(BadRowGen.enrichmentFailures) { f => validateBadRow[BadRow.EnrichmentFailures](f) must beRight }
  }

  "Loaders" >> {
    s"${Schemas.LoaderParsingError.toSchemaUri} (LoaderParsingError)" >>
      forAll(BadRowGen.loaderParsingError) { f => validateBadRow[BadRow.LoaderParsingError](f) must beRight }

    s"${Schemas.LoaderIgluError.toSchemaUri} (LoaderIgluError)" >>
      forAll(BadRowGen.loaderIgluError) { f => validateBadRow[BadRow.LoaderIgluError](f) must beRight }

    s"${Schemas.LoaderRuntimeError.toSchemaUri} (LoaderRuntimeError)" >>
      forAll(BadRowGen.loaderRuntimeErrorBadRowGen) { f => validateBadRow[BadRow.LoaderRuntimeError](f) must beRight }

    s"${Schemas.LoaderRecoveryError.toSchemaUri} (LoaderRecoveryError)" >>
      forAll(BadRowGen.loaderRecoveryErrorBadRowGen) { f => validateBadRow[BadRow.LoaderRecoveryError](f) must beRight }
  }
}

object SchemaValidationSpec {

  def validateBadRow[A <: BadRow: Decoder: Encoder](badRow: BadRow) = {
    // JSON reparsing is added in order to check decoding
    val encoded = parser.parse(badRow.selfDescribingData.data.noSpaces)
      .getOrElse(throw new RuntimeException("Error while parsing bad row json"))
    val decoded = encoded.as[A].getOrElse(throw new RuntimeException(s"Error while decoding bad row: ${encoded.as[A]}"))
      .asJson
    val schema = resolver.lookupSchema(badRow.schemaKey)
    CirceValidator.validate(decoded, schema.getOrElse(throw new RuntimeException(s"Schema could not be found: $schema")))
  }

  val http = Registry.HttpConnection(URI.create("http://iglucentral.com/"), None)
  val igluCentral = Registry.Http(Registry.Config("Iglu Central", 0, List("com.snowplowanalytics.snowplow.badrows")), http)

  val resolver: Resolver[Id] = Resolver.init[Id](10, None, igluCentral)

  val mockJsonValue =
    Json.obj("mockJsonKey" := "mockJsonValue")
}
