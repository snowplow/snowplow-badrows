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

import java.util.UUID
import java.time.Instant
import java.util.concurrent.TimeUnit
import cats.{Applicative, Id}
import cats.effect.Clock
import com.snowplowanalytics.snowplow.analytics.scalasdk.Event
import com.snowplowanalytics.snowplow.analytics.scalasdk.SnowplowEvent.{Contexts, UnstructEvent}

import scala.concurrent.duration.FiniteDuration

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

      override def applicative: Applicative[Id] = Applicative[Id]

      override def monotonic: Id[FiniteDuration] = FiniteDuration(System.nanoTime(), TimeUnit.NANOSECONDS)

      override def realTime: Id[FiniteDuration] = FiniteDuration(System.currentTimeMillis(), TimeUnit.MILLISECONDS)

    }
  }
}
