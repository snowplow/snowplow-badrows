package com.snowplowanalytics.snowplow.badrows

import java.util.UUID
import java.time.Instant
import java.util.concurrent.TimeUnit

import cats.Id
import cats.effect.Clock

import com.snowplowanalytics.snowplow.analytics.scalasdk.Event
import com.snowplowanalytics.snowplow.analytics.scalasdk.SnowplowEvent.{Contexts, UnstructEvent}

object SpecHelpers {
  def emptyEvent(id: UUID, collectorTstamp: Instant, vCollector: String, vTstamp: String): Event =
    Event(None, None, None, collectorTstamp, None, None, id, None, None, None, vCollector, vTstamp, None, None, None,
      None, None, None, None, None, None, None, None, None, None, None, None, None, None, None, None, None, None, None,
      None, None, None, None, None, None, None, None, None, None, None, None, None, None, None, None, None, None,
      Contexts(Nil), None, None, None, None, None, UnstructEvent(None), None, None, None, None, None, None, None, None,
      None, None, None, None, None, None, None, None, None, None, None, None, None, None, None, None, None, None, None,
      None, None, None, None, None, None, None, None, None, None, None, None, None, None, None, None, None, None, None,
      None, None, None, None, None, None, None, None, None, None, None, None, None, None, None, None, None,
      Contexts(Nil), None, None, None, None, None, None, None, None)

  val ExampleEvent = emptyEvent(
    UUID.fromString("ba553b7f-63d5-47ad-8697-06016b472c34"),
    Instant.ofEpochMilli(1550477167580L),
    "bq-loader-test",
    "bq-loader-test")

  object IdInstances {
    implicit val idClock: Clock[Id] = new Clock[Id] {
      def realTime(unit: TimeUnit): Id[Long] =
        unit.convert(System.currentTimeMillis(), TimeUnit.MILLISECONDS)

      def monotonic(unit: TimeUnit): Id[Long] =
        unit.convert(System.nanoTime(), TimeUnit.NANOSECONDS)
    }
  }
}
