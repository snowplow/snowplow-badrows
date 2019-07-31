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

import cats.syntax.functor._
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto._

sealed trait Payload
object Payload {
  implicit val payloadEncoder: Encoder[Payload] = Encoder.instance {
    case p: RawPayload => deriveEncoder[RawPayload].apply(p)
    case p: CollectorPayload => deriveEncoder[CollectorPayload].apply(p)
    case p: EnrichmentPayload => deriveEncoder[EnrichmentPayload].apply(p)
  }
  implicit val payloadDecoder: Decoder[Payload] = List[Decoder[Payload]](
    deriveDecoder[RawPayload].widen,
    deriveDecoder[CollectorPayload].widen,
    deriveDecoder[EnrichmentPayload].widen
  ).reduceLeft(_ or _)

  final case class RawPayload(line: String) extends Payload

  final case class CollectorPayload(
    vendor: String,
    version: String,
    querystring: List[NVP], // Could be empty in future trackers
    contentType: Option[String], // Not always set
    body: Option[String], // Not set for GETs
    collector: String,
    encoding: String,
    hostname: Option[String],
    timestamp: Option[String], // Must have a timestamp
    ipAddress: Option[String],
    useragent: Option[String],
    refererUri: Option[String],
    headers: List[String], // Could be empty
    networkUserId: Option[String] // User ID generated by collector-set third-party cookie
  ) extends Payload

  final case class EnrichmentPayload(
    partiallyEnrichedEvent: PartiallyEnrichedEvent,
    rawEvent: RawEvent
  ) extends Payload

  final case class RawEvent(
    vendor: String,
    version: String,
    parameters: Map[String, String],
    contentType: Option[String],
    loaderName: String,
    encoding: String,
    hostname: Option[String],
    timestamp: Option[String],
    ipAddress: Option[String],
    useragent: Option[String],
    refererUri: Option[String],
    headers: List[String],
    userId: Option[String]
  )
  object RawEvent {
    implicit val rawEventEncoder: Encoder[RawEvent] = deriveEncoder
    implicit val rawEventDecoder: Decoder[RawEvent] = deriveDecoder
  }

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
}

final case class NVP(name: String, value: Option[String])
object NVP {
  implicit val nvpEncoder: Encoder[NVP] = deriveEncoder
  implicit val nvpDecoder: Decoder[NVP] = deriveDecoder
}
