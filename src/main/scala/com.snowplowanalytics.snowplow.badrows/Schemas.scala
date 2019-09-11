package com.snowplowanalytics.snowplow.badrows

import com.snowplowanalytics.iglu.core.{SchemaVer, SchemaKey}

object Schemas {
  val CPFormatViolation = SchemaKey("com.snowplowanalytics.snowplow.badrows", "collector_payload_format_violation", "jsonschema", SchemaVer.Full(1, 0, 0))
  val AdapterFailures = SchemaKey("com.snowplowanalytics.snowplow.badrows", "adapter_failures", "jsonschema", SchemaVer.Full(1, 0, 0))
  val TrackerProtocolViolations = SchemaKey("com.snowplowanalytics.snowplow.badrows", "tracker_protocol_violations", "jsonschema", SchemaVer.Full(1, 0, 0))
  val SchemaViolations = SchemaKey("com.snowplowanalytics.snowplow.badrows", "schema_violations", "jsonschema", SchemaVer.Full(1, 0, 0))
  val EnrichmentFailures = SchemaKey("com.snowplowanalytics.snowplow.badrows", "enrichment_failures", "jsonschema", SchemaVer.Full(1, 0, 0))
  val SizeViolation = SchemaKey("com.snowplowanalytics.snowplow.badrows", "size_violation", "jsonschema", SchemaVer.Full(1, 0, 0))
  val LoaderParsingError = SchemaKey("com.snowplowanalytics.snowplow.badrows", "loader_parsing_error", "jsonschema", SchemaVer.Full(1, 0, 0))
  val LoaderIgluError = SchemaKey("com.snowplowanalytics.snowplow.badrows", "loader_iglu_error", "jsonschema", SchemaVer.Full(1, 0, 0))
  val BQCastError = SchemaKey("com.snowplowanalytics.snowplow.badrows", "bq_cast_error", "jsonschema", SchemaVer.Full(1, 0, 0))
  val LoaderRuntimeError = SchemaKey("com.snowplowanalytics.snowplow.badrows", "loader_runtime_error", "jsonschema", SchemaVer.Full(1, 0, 1))
  val BQRepeaterParsingError = SchemaKey("com.snowplowanalytics.snowplow.badrows", "bq_repeater_parsing_error", "jsonschema", SchemaVer.Full(1, 0, 0))
  val BQRepeaterPubSubError = SchemaKey("com.snowplowanalytics.snowplow.badrows", "bq_repeater_pubsub_error", "jsonschema", SchemaVer.Full(1, 0, 0))
  val BQRepeaterBQError = SchemaKey("com.snowplowanalytics.snowplow.badrows", "bq_repeater_bigquery_error", "jsonschema", SchemaVer.Full(1, 0, 0))
}
