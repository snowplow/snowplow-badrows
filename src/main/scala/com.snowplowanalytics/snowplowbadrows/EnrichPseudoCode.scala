package com.snowplowanalytics.snowplowbadrows

import BadRow._

import cats.instances.list._
import cats.instances.either._
import cats.syntax.either._
import cats.syntax.alternative._

object EnrichPseudoCode {
  def parseRawPayload(line: String): Either[FormatViolation, RawPayload] = ???
  def parseCollectorPayloads(rawPayload: RawPayload): List[Either[TrackerProtocolViolation, CollectorPayload]] = ???
  def parseSnowplowPayload(collectorPayload: CollectorPayload): Either[IgluViolation, SnowplowPayload] = ???
  def enrich(payload: SnowplowPayload): Either[BadRow, Event] = ???


  /**
    * Either fail on first step of parsing collector payload or
    * proceed and split all payloads into good events and bad (for specific reason row)
    */
  def process(line: String): Either[FormatViolation, (List[BadRow], List[Event])] =
    for {
      // At this step, line is just random string (array of bytes) we received from collector
      // rawPayload is at least valid JSON or GET, still it contain many events
      rawPayload                             <- parseRawPayload(line)
      // At this step, collectorPayloads are collector-agnostic, e.g. we don't know if
      // it was stream collector or clojure, payloads are just individual Maps
      // from now and then - all events fail only individually
      (trackerViolations, collectorPayloads)  = parseCollectorPayloads(rawPayload).separate
      // At this step, snowplowPayloads contain meaningful set of properties (e.g. all self-describing
      // properties are indeed self-describing)
      (igluViolations, snowplowPayloads)      = collectorPayloads.map(parseSnowplowPayload).separate
      // Bad is either failed enrichment or schema-invalidation
      (bad, good)                             = snowplowPayloads.map(enrich).separate
    } yield (trackerViolations ++ igluViolations ++ bad, good)
}
