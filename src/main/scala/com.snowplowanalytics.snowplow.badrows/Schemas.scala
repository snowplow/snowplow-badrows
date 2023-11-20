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
package com.snowplowanalytics.snowplow.badrows

import com.snowplowanalytics.iglu.core.{SchemaVer, SchemaKey}

object Schemas {

  val Vendor: String = "com.snowplowanalytics.snowplow.badrows"

  object Names {
    val SizeViolation: String = "size_violation"
    val CPFormatViolation: String = "collector_payload_format_violation"
    val AdapterFailures: String = "adapter_failures"
    val TrackerProtocolViolations: String = "tracker_protocol_violations"
    val SchemaViolations: String = "schema_violations"
    val EnrichmentFailures: String = "enrichment_failures"
    val LoaderParsingError: String = "loader_parsing_error"
    val LoaderIgluError: String = "loader_iglu_error"
    val LoaderRuntimeError: String = "loader_runtime_error"
    val LoaderRecoveryError: String = "loader_recovery_error"
    val RecoveryError: String = "recovery_error"
    val GenericError: String = "generic_error"
  }

  /** @see [[BadRow.SizeViolation]] */
  val SizeViolation = SchemaKey(Vendor, Names.SizeViolation, "jsonschema", SchemaVer.Full(1, 0, 0))

  /** @see [[BadRow.CPFormatViolation]] */
  val CPFormatViolation = SchemaKey(Vendor, Names.CPFormatViolation, "jsonschema", SchemaVer.Full(1, 0, 0))

  /** @see [[BadRow.AdapterFailures]] */
  val AdapterFailures = SchemaKey(Vendor, Names.AdapterFailures, "jsonschema", SchemaVer.Full(1, 0, 0))

  /** @see [[BadRow.TrackerProtocolViolations]] */
  val TrackerProtocolViolations = SchemaKey(Vendor, Names.TrackerProtocolViolations, "jsonschema", SchemaVer.Full(1, 0, 1))

  /** @see [[BadRow.SchemaViolations]] */
  val SchemaViolations = SchemaKey(Vendor, Names.SchemaViolations, "jsonschema", SchemaVer.Full(2, 0, 1))

  /** @see [[BadRow.EnrichmentFailures]] */
  val EnrichmentFailures = SchemaKey(Vendor, Names.EnrichmentFailures, "jsonschema", SchemaVer.Full(2, 0, 1))

  /** @see [[BadRow.LoaderParsingError]] */
  val LoaderParsingError = SchemaKey(Vendor, Names.LoaderParsingError, "jsonschema", SchemaVer.Full(2, 0, 0))

  /** @see [[BadRow.LoaderIgluError]] */
  val LoaderIgluError = SchemaKey(Vendor, Names.LoaderIgluError, "jsonschema", SchemaVer.Full(2, 0, 1))

  /** @see [[BadRow.LoaderRuntimeError]] */
  val LoaderRuntimeError = SchemaKey(Vendor, Names.LoaderRuntimeError, "jsonschema", SchemaVer.Full(1, 0, 1))

  /** @see [[BadRow.LoaderRecoveryError]] */
  val LoaderRecoveryError = SchemaKey(Vendor, Names.LoaderRecoveryError, "jsonschema", SchemaVer.Full(1, 0, 0))

  /** @see [[BadRow.RecoveryError]] */
  val RecoveryError = SchemaKey(Vendor, Names.RecoveryError, "jsonschema", SchemaVer.Full(1, 0, 1))

  /** @see [[BadRow.GenericError]] */
  val GenericError = SchemaKey(Vendor, Names.GenericError, "jsonschema", SchemaVer.Full(1, 0, 0))
}
