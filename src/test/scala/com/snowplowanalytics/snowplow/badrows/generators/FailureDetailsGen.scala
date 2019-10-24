/*
 * Copyright (c) 2018-2019 Snowplow Analytics Ltd. All rights reserved.
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

import org.scalacheck.{Arbitrary, Gen}

import com.snowplowanalytics.snowplow.badrows.FailureDetails

object FailureDetailsGen {

  def adapterFailure: Gen[FailureDetails.AdapterFailure] =
    Gen.oneOf(adapterFailureNotJson, adapterFailureNotSd, adapterFailureInputData, adapterFailureSchemaMapping)

  def cpFormatViolationMessage: Gen[FailureDetails.CPFormatViolationMessage] =
    Gen.oneOf(cpFormatViolationMessageFallback, cpFormatViolationMessageInputData)

  def trackerProtocolViolation: Gen[FailureDetails.TrackerProtocolViolation] =
    Gen.oneOf(trackerProtocolViolationIgluError, trackerProtocolSchemaCrit, trackerProtocolViolationInputData,
      trackerProtocolViolationNotJson, trackerProtocolViolationNotSd)

  def schemaViolation: Gen[FailureDetails.SchemaViolation] =
    Gen.oneOf(schemaViolationSchemaCrit, schemalViolationNotJson, schemaViolationNotSd, schemaViolationInputData)

  def enrichmentFailure: Gen[FailureDetails.EnrichmentFailure] =
    for {
      information <- Gen.option(enrichmentInformation)
      message     <- enrichmentMessage
    } yield FailureDetails.EnrichmentFailure(information, message)

  // CPFormatViolationMessage

  val cpFormatViolationMessageInputData: Gen[FailureDetails.CPFormatViolationMessage] =
    for {
      payloadField <- CommonGen.strGen(64, Gen.alphaNumChar)
      value        <- Gen.option(Gen.alphaNumStr)
      expectation  <- CommonGen.strGen(256, Gen.alphaNumChar)
    } yield FailureDetails.CPFormatViolationMessage.InputData(payloadField, value, expectation)

  val cpFormatViolationMessageFallback: Gen[FailureDetails.CPFormatViolationMessage.Fallback] =
    CommonGen.strGen(512, Gen.alphaNumChar).map { e => FailureDetails.CPFormatViolationMessage.Fallback(e) }

  // AdapterFailure

  val adapterFailureNotJson: Gen[FailureDetails.AdapterFailure.NotJson] =
    for {
      field <- CommonGen.strGen(64, Gen.alphaNumChar)
      value <- Gen.some(Arbitrary.arbitrary[String])
      error <- Gen.asciiPrintableStr
    } yield FailureDetails.AdapterFailure.NotJson(field, value, error)

  val adapterFailureNotSd: Gen[FailureDetails.AdapterFailure.NotIglu] =
    for {
      json  <- CommonGen.jsonGen
      error <- CommonGen.parseError
    } yield FailureDetails.AdapterFailure.NotIglu(json, error)

  val adapterFailureInputData: Gen[FailureDetails.AdapterFailure.InputData] =
    for {
      field       <- CommonGen.strGen(64, Gen.alphaNumChar)
      value       <- Gen.some(Arbitrary.arbitrary[String])
      expectation <- CommonGen.strGen(256, Gen.asciiPrintableChar)
    } yield FailureDetails.AdapterFailure.InputData(field, value, expectation)

  val adapterFailureSchemaMapping: Gen[FailureDetails.AdapterFailure.SchemaMapping] =
    for {
      actual          <- Gen.option(CommonGen.strGen(256, Arbitrary.arbitrary[Char]))
      expectedMapping <- Gen.mapOf(Gen.identifier.flatMap { i => CommonGen.schemaKey.flatMap { key => (i, key) } })
      expectation     <- CommonGen.strGen(256, Gen.asciiPrintableChar)
    } yield FailureDetails.AdapterFailure.SchemaMapping(actual, expectedMapping, expectation)

  // TrackerProtocolViolation

  val trackerProtocolViolationInputData: Gen[FailureDetails.TrackerProtocolViolation.InputData] =
    for {
      field       <- CommonGen.strGen(64, Gen.alphaNumChar)
      value       <- Gen.some(Arbitrary.arbitrary[String])
      expectation <- CommonGen.strGen(256, Gen.asciiPrintableChar)
    } yield FailureDetails.TrackerProtocolViolation.InputData(field, value, expectation)

  val trackerProtocolViolationNotJson: Gen[FailureDetails.TrackerProtocolViolation.NotJson] =
    for {
      field <- CommonGen.strGen(64, Gen.asciiPrintableChar)
      value <- Gen.option(Arbitrary.arbitrary[String])
      error <- Gen.asciiPrintableStr
    } yield FailureDetails.TrackerProtocolViolation.NotJson(field, value, error)

  val trackerProtocolViolationNotSd: Gen[FailureDetails.TrackerProtocolViolation.NotIglu] =
    for {
      json  <- CommonGen.jsonGen
      error <- CommonGen.parseError
    } yield FailureDetails.TrackerProtocolViolation.NotIglu(json, error)

  val trackerProtocolSchemaCrit: Gen[FailureDetails.TrackerProtocolViolation.CriterionMismatch] =
    for {
      schemaKey       <- CommonGen.schemaKey
      schemaCriterion <- CommonGen.schemaCriterion
    } yield FailureDetails.TrackerProtocolViolation.CriterionMismatch(schemaKey, schemaCriterion)

  val trackerProtocolViolationIgluError: Gen[FailureDetails.TrackerProtocolViolation.IgluError] =
    for {
      schemaKey <- CommonGen.schemaKey
      error     <- IgluClientErrorGen.clientError
    } yield FailureDetails.TrackerProtocolViolation.IgluError(schemaKey, error)

  // SchemaViolation

  val schemaViolationSchemaCrit: Gen[FailureDetails.SchemaViolation.CriterionMismatch] =
    for {
      schemaKey       <- CommonGen.schemaKey
      schemaCriterion <- CommonGen.schemaCriterion
    } yield FailureDetails.SchemaViolation.CriterionMismatch(schemaKey, schemaCriterion)

  val schemalViolationNotJson: Gen[FailureDetails.SchemaViolation.NotJson] =
    for {
      field <- CommonGen.strGen(64, Gen.asciiPrintableChar)
      value <- Gen.option(Arbitrary.arbitrary[String])
      error <- Gen.asciiPrintableStr
    } yield FailureDetails.SchemaViolation.NotJson(field, value, error)

  val schemaViolationNotSd: Gen[FailureDetails.SchemaViolation.NotIglu] =
    for {
      json  <- CommonGen.jsonGen
      error <- CommonGen.parseError
    } yield FailureDetails.SchemaViolation.NotIglu(json, error)

  val schemaViolationInputData: Gen[FailureDetails.SchemaViolation.IgluError] =
    for {
      schemaKey <- CommonGen.schemaKey
      error     <- IgluClientErrorGen.clientError
    } yield FailureDetails.SchemaViolation.IgluError(schemaKey, error)

  // EnrichmentFailure

  val enrichmentFailureMessageSimple: Gen[FailureDetails.EnrichmentFailureMessage.Simple] =
    Gen.asciiPrintableStr.map(message => FailureDetails.EnrichmentFailureMessage.Simple(message))

  val enrichmentFailureMessageInputData: Gen[FailureDetails.EnrichmentFailureMessage.InputData] =
    for {
      field       <- CommonGen.strGen(64, Gen.asciiPrintableChar)
      value       <- Gen.option(Arbitrary.arbitrary[String])
      expectation <- CommonGen.strGen(256, Gen.asciiPrintableChar)
    } yield FailureDetails.EnrichmentFailureMessage.InputData(field, value, expectation)

  val enrichmentInformation: Gen[FailureDetails.EnrichmentInformation] =
    for {
      schemaKey  <- CommonGen.schemaKey
      identifier <- Gen.identifier
    } yield FailureDetails.EnrichmentInformation(schemaKey, identifier)

  val enrichmentMessage: Gen[FailureDetails.EnrichmentFailureMessage] =
    Gen.oneOf(enrichmentFailureMessageSimple, enrichmentFailureMessageInputData)
}
