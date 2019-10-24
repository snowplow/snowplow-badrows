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

package com.snowplowanalytics.snowplow.badrows

import java.util.UUID

import org.joda.time.DateTime

import cats.syntax.functor._

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto._
import io.circe.Json
import io.circe.syntax._

import com.snowplowanalytics.snowplow.analytics.scalasdk.Event

/** The payload contained in a bad row can be used to recover an event */
sealed trait Payload

object Payload {
  implicit val payloadEncoder: Encoder[Payload] = Encoder.instance {
    case p: RawPayload => RawPayload.payloadRawPayloadJsonEncoder.apply(p)
    case p: CollectorPayload => CollectorPayload.payloadCollectorPayloadJsonEncoder.apply(p)
    case p: EnrichmentPayload => EnrichmentPayload.payloadEnrichmentPayloadJsonEncoder.apply(p)
    case p: LoaderPayload => LoaderPayload.payloadLoaderPayloadJsonEncoder.apply(p)
    case p: BQReconstructedEvent => BQReconstructedEvent.payloadBQReconstructedEventJsonEncoder.apply(p)
  }

  implicit val payloadDecoder: Decoder[Payload] = List[Decoder[Payload]](
    RawPayload.payloadRawPayloadJsonDecoder.widen,
    CollectorPayload.payloadCollectorPayloadJsonDecoder.widen,
    EnrichmentPayload.payloadEnrichmentPayloadJsonDecoder.widen,
    LoaderPayload.payloadLoaderPayloadJsonDecoder.widen,
    BQReconstructedEvent.payloadBQReconstructedEventJsonDecoder.widen
  ).reduceLeft(_ or _)

  /** Payload received by a collector, before being interpreted (in Scala Common Enrich).
    * A raw payload can be serialized with Thrift, be a TSV or be a Cloudfront log for instance.
    */
  final case class RawPayload(line: String) extends Payload
  object RawPayload {
    implicit val payloadRawPayloadJsonEncoder: Encoder[RawPayload] = Encoder.instance { p => p.line.asJson }
    implicit val payloadRawPayloadJsonDecoder: Decoder[RawPayload] = Decoder.instance(_.as[String].map(RawPayload(_)))
  }

  /** Payload deserialized by a SCE Loader.
    * Contains the payload sent by a webhook or by a Snowplow tracker,
    * without knowing if the parameters are valid for the adapter that will
    * read it to create the RawEvent(s).
    */ 
  final case class CollectorPayload(
    vendor: String,
    version: String,
    querystring: List[NVP], // Could be empty in future trackers
    contentType: Option[String], // Not always set
    body: Option[String], // Not set for GETs
    collector: String,
    encoding: String,
    hostname: Option[String],
    timestamp: Option[DateTime], // Must have a timestamp
    ipAddress: Option[String],
    useragent: Option[String],
    refererUri: Option[String],
    headers: List[String], // Could be empty
    networkUserId: Option[UUID] // User ID generated by collector-set third-party cookie;
  ) extends Payload
  object CollectorPayload {
    implicit val payloadCollectorPayloadJsonEncoder: Encoder[CollectorPayload] =
      deriveEncoder[CollectorPayload]
    implicit val payloadCollectorPayloadJsonDecoder: Decoder[CollectorPayload] =
      deriveDecoder[CollectorPayload]
  }

  /** Contains the input of the enrichments as well as some output. */
  final case class EnrichmentPayload(enriched: PartiallyEnrichedEvent, raw: RawEvent) extends Payload
  object EnrichmentPayload {
    implicit val payloadEnrichmentPayloadJsonEncoder: Encoder[EnrichmentPayload] = deriveEncoder
    implicit val payloadEnrichmentPayloadJsonDecoder: Decoder[EnrichmentPayload] = deriveDecoder
  }

  /** Input event that gets enriched.
    * Unlike [[CollectorPayload]] represents a single event
    */
  final case class RawEvent(
    vendor: String,
    version: String,
    parameters: Map[String, String],
    contentType: Option[String],
    loaderName: String,
    encoding: String,
    hostname: Option[String],
    timestamp: Option[DateTime],
    ipAddress: Option[String],
    useragent: Option[String],
    refererUri: Option[String],
    headers: List[String],
    userId: Option[UUID]
  )
  object RawEvent {
    implicit val rawEventEncoder: Encoder[RawEvent] = deriveEncoder
    implicit val rawEventDecoder: Decoder[RawEvent] = deriveDecoder
  }

  /** Can contain all the fields added by the enrichments are just a part if an enrichment failed. */
  final case class PartiallyEnrichedEvent(
    app_id: Option[String],
    platform: Option[String],
    etl_tstamp: Option[String],
    collector_tstamp: String,
    dvce_created_tstamp: Option[String],
    event: Option[String],
    event_id: Option[String],
    txn_id: Option[String],
    name_tracker: Option[String],
    v_tracker: Option[String],
    v_collector: String,
    v_etl: String,
    user_id: Option[String],
    user_ipaddress: Option[String],
    user_fingerprint: Option[String],
    domain_userid: Option[String],
    domain_sessionidx: Option[Int],
    network_userid: Option[String],
    geo_country: Option[String],
    geo_region: Option[String],
    geo_city: Option[String],
    geo_zipcode: Option[String],
    geo_latitude: Option[Float],
    geo_longitude: Option[Float],
    geo_region_name: Option[String],
    ip_isp: Option[String],
    ip_organization: Option[String],
    ip_domain: Option[String],
    ip_netspeed: Option[String],
    page_url: Option[String],
    page_title: Option[String],
    page_referrer: Option[String],
    page_urlscheme: Option[String],
    page_urlhost: Option[String],
    page_urlport: Option[Int],
    page_urlpath: Option[String],
    page_urlquery: Option[String],
    page_urlfragment: Option[String],
    refr_urlscheme: Option[String],
    refr_urlhost: Option[String],
    refr_urlport: Option[Int],
    refr_urlpath: Option[String],
    refr_urlquery: Option[String],
    refr_urlfragment: Option[String],
    refr_medium: Option[String],
    refr_source: Option[String],
    refr_term: Option[String],
    mkt_medium: Option[String],
    mkt_source: Option[String],
    mkt_term: Option[String],
    mkt_content: Option[String],
    mkt_campaign: Option[String],
    contexts: Option[String],
    se_category: Option[String],
    se_action: Option[String],
    se_label: Option[String],
    se_property: Option[String],
    se_value: Option[String],
    unstruct_event: Option[String],
    tr_orderid: Option[String],
    tr_affiliation: Option[String],
    tr_total: Option[String],
    tr_tax: Option[String],
    tr_shipping: Option[String],
    tr_city: Option[String],
    tr_state: Option[String],
    tr_country: Option[String],
    ti_orderid: Option[String],
    ti_sku: Option[String],
    ti_name: Option[String],
    ti_category: Option[String],
    ti_price: Option[String],
    ti_quantity: Option[Int],
    pp_xoffset_min: Option[Int],
    pp_xoffset_max: Option[Int],
    pp_yoffset_min: Option[Int],
    pp_yoffset_max: Option[Int],
    useragent: Option[String],
    br_name: Option[String],
    br_family: Option[String],
    br_version: Option[String],
    br_type: Option[String],
    br_renderengine: Option[String],
    br_lang: Option[String],
    br_features_pdf: Option[Byte],
    br_features_flash: Option[Byte],
    br_features_java: Option[Byte],
    br_features_director: Option[Byte],
    br_features_quicktime: Option[Byte],
    br_features_realplayer: Option[Byte],
    br_features_windowsmedia: Option[Byte],
    br_features_gears: Option[Byte],
    br_features_silverlight: Option[Byte],
    br_cookies: Option[Byte],
    br_colordepth: Option[String],
    br_viewwidth: Option[Int],
    br_viewheight: Option[Int],
    os_name: Option[String],
    os_family: Option[String],
    os_manufacturer: Option[String],
    os_timezone: Option[String],
    dvce_type: Option[String],
    dvce_ismobile: Option[Byte],
    dvce_screenwidth: Option[Int],
    dvce_screenheight: Option[Int],
    doc_charset: Option[String],
    doc_width: Option[Int],
    doc_height: Option[Int],
    tr_currency: Option[String],
    tr_total_base: Option[String],
    tr_tax_base: Option[String],
    tr_shipping_base: Option[String],
    ti_currency: Option[String],
    ti_price_base: Option[String],
    base_currency: Option[String],
    geo_timezone: Option[String],
    mkt_clickid: Option[String],
    mkt_network: Option[String],
    etl_tags: Option[String],
    dvce_sent_tstamp: Option[String],
    refr_domain_userid: Option[String],
    refr_dvce_tstamp: Option[String],
    derived_contexts: Option[String],
    domain_sessionid: Option[String],
    derived_tstamp: Option[String],
    event_vendor: Option[String],
    event_name: Option[String],
    event_format: Option[String],
    event_version: Option[String],
    event_fingerprint: Option[String],
    true_tstamp: Option[String]
  )
  object PartiallyEnrichedEvent {
    implicit val partiallyEnrichedEventEncoder: Encoder[PartiallyEnrichedEvent] = deriveEncoder
    implicit val partiallyEnrichedEventDecoder: Decoder[PartiallyEnrichedEvent] = deriveDecoder
  }

  /** Entirely valid and parsed Snowplow enriched event */
  final case class LoaderPayload(event: Event) extends Payload
  object LoaderPayload {
    implicit val payloadLoaderPayloadJsonEncoder: Encoder[LoaderPayload] = Encoder.instance { p => Event.jsonEncoder.apply(p.event) }
    implicit val payloadLoaderPayloadJsonDecoder: Decoder[LoaderPayload] = Event.eventDecoder.map(LoaderPayload(_))
  }

  /**
    * Represents event which is reconstructed from payload of the failed insert
    * It consists of two part because schema keys of the self describing JSONs
    * such as unstruct_event, contexts or derived_contexts are shredded in the
    * payload. For example schema key with com.snowplowanalytics.snowplow as
    * vendor, web_page as name, 1.0.0 as version is converted to
    * "contexts_com_snowplowanalytics_snowplow_web_page_1_0_0" and this key could
    * not be reconverted to its original schema key deterministically. Therefore,
    * they can not be behaved like self describing JSONs after reconstruction because
    * their schema key are unknown. Therefore, reconstructed event split into two part
    * as atomic and self describing entities
    * @param atomic event instance which only consists of atomic event parts
    * @param selfDescribingEntities list of SelfDescribingEntity instances
    *        they consist of reconstructed self describing JSONs from the payload
    */
  final case class BQReconstructedEvent(atomic: Event, selfDescribingEntities: List[SelfDescribingEntity]) extends Payload
  object BQReconstructedEvent {
    implicit val payloadBQReconstructedEventJsonEncoder: Encoder[BQReconstructedEvent] = deriveEncoder
    implicit val payloadBQReconstructedEventJsonDecoder: Decoder[BQReconstructedEvent] = deriveDecoder
  }

  /**
    * Represents self describing entities with shredded type
    * @param shreddedType shredded type e.g. "contexts_com_snowplowanalytics_snowplow_web_page_1_0_0"
    * @param data JSON object which can be unstruct_event, array of contexts or array of derived_contexts
    */
  final case class SelfDescribingEntity(shreddedType: String, data: Json)
  object SelfDescribingEntity {
    implicit val selfDescribingEntityEncoder: Encoder[SelfDescribingEntity] = deriveEncoder[SelfDescribingEntity]
    implicit val selfDescribingEntityDecoder: Decoder[SelfDescribingEntity] = deriveDecoder[SelfDescribingEntity]
  }
}

/** Helper used to define a [[Payload.CollectorPayload]]. Isomoprhic to `org.apache.http.NaveValuePair` */
final case class NVP(name: String, value: Option[String])
object NVP {
  implicit val nvpEncoder: Encoder[NVP] = deriveEncoder
  implicit val nvpDecoder: Decoder[NVP] = deriveDecoder
}
