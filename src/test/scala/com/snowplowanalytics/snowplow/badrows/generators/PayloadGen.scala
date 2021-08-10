/*
 * Copyright (c) 2018-2021 Snowplow Analytics Ltd. All rights reserved.
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

import com.snowplowanalytics.snowplow.badrows.{NVP, Payload, SpecHelpers}
import org.scalacheck.{Arbitrary, Gen}

object PayloadGen {
  val rawPayload: Gen[Payload.RawPayload] =
    Gen.alphaNumStr.map { line => Payload.RawPayload(line) }

  private val nvp: Gen[NVP] =
    for {
      name <- CommonGen.strGen(512, Gen.alphaNumChar)
      value <- Gen.option(CommonGen.strGen(512, Gen.alphaNumChar))
    } yield NVP(name, value)

  val rawEvent: Gen[Payload.RawEvent] =
    for {
      vendor      <- CommonGen.strGen(64, Gen.alphaLowerChar)
      version     <- CommonGen.strGen(16, Gen.alphaNumChar)
      querystring <- CommonGen.listOfMaxN(100, nvp)
      contentType <- Gen.option(Gen.oneOf("text/plain", "application/json"))
      loaderName  <- CommonGen.strGen(32, Gen.alphaNumChar)
      encoding    <- CommonGen.strGen(32, Gen.alphaNumChar)
      hostname    <- Gen.option(Gen.alphaNumStr)
      timestamp   <- Gen.option(CommonGen.dateTimeGen)
      ipAddress   <- Gen.option(CommonGen.ipAddress)
      optStr      <- Gen.option(Gen.identifier) // The whole generator needs to be updated with more meaningful values
      headers     <- CommonGen.listOfMaxN(12, Gen.identifier)
      userId      <- Gen.option(Gen.uuid)
    } yield Payload.RawEvent(vendor, version, querystring, contentType, loaderName, encoding, hostname, timestamp, ipAddress, optStr, optStr, headers, userId)

  val partiallyEnrichedEvent: Gen[Payload.PartiallyEnrichedEvent] =
    for {
      app_id <- Gen.option(CommonGen.strGen(512, Gen.alphaNumChar))
      platform <- Gen.option(CommonGen.platform)
      etl_tstamp <- Gen.option(CommonGen.dateTimeGen).map(_.map(_.toString()))
      collector_tstamp <- Gen.option(CommonGen.dateTimeGen).map(_.map(_.toString()))
      dvce_created_tstamp <- Gen.option(CommonGen.dateTimeGen).map(_.map(_.toString()))
      event <- Gen.option(CommonGen.eventType)
      event_id <- Gen.option(Gen.uuid).map(_.map(_.toString()))
      txn_id <- Gen.option(Gen.uuid).map(_.map(_.toString()))
      name_tracker <- Gen.option(CommonGen.strGen(512, Gen.alphaNumChar))
      v_tracker <- Gen.option(CommonGen.version)
      v_collector <- Gen.option(CommonGen.version)
      v_etl <- Gen.option(CommonGen.version)
      user_id <- Gen.option(Gen.uuid).map(_.map(_.toString()))
      user_ipaddress <- Gen.option(CommonGen.ipAddress)
      user_fingerprint <- Gen.option(CommonGen.strGen(512, Gen.alphaNumChar))
      domain_userid <- Gen.option(Gen.uuid).map(_.map(_.toString()))
      domain_sessionidx <- Gen.option(Arbitrary.arbitrary[Int])
      network_userid <- Gen.option(Gen.uuid).map(_.map(_.toString()))
      geo_country <- Gen.option(CommonGen.strGen(3, Gen.alphaUpperChar))
      geo_region <- Gen.option(CommonGen.strGen(100, Gen.alphaNumChar))
      geo_city <- Gen.option(CommonGen.strGen(512, Gen.alphaChar))
      geo_zipcode <- Gen.option(CommonGen.strGen(6, Gen.alphaNumChar))
      geo_latitude <- Gen.option(Arbitrary.arbitrary[Float])
      geo_longitude  <- Gen.option(Arbitrary.arbitrary[Float])
      geo_region_name <- Gen.option(CommonGen.strGen(512, Gen.alphaChar))
      ip_isp <- Gen.option(CommonGen.strGen(512, Gen.alphaNumChar))
      ip_organization <- Gen.option(CommonGen.strGen(512, Gen.alphaNumChar))
      ip_domain <- Gen.option(CommonGen.strGen(512, Gen.alphaNumChar))
      ip_netspeed <- Gen.option(CommonGen.strGen(50, Gen.alphaNumChar))
      page_url <- Gen.option(CommonGen.strGen(512, Gen.alphaNumChar))
      page_title <- Gen.option(CommonGen.strGen(512, Gen.alphaNumChar))
      page_referrer <- Gen.option(CommonGen.strGen(512, Gen.alphaNumChar))
      page_urlscheme <- Gen.option(CommonGen.strGen(10, Gen.alphaNumChar))
      page_urlhost <- Gen.option(CommonGen.strGen(512, Gen.alphaNumChar))
      page_urlport <- Gen.option(Gen.chooseNum(1, 65000))
      page_urlpath <- Gen.option(CommonGen.strGen(512, Gen.alphaNumChar))
      page_urlquery <- Gen.option(CommonGen.strGen(512, Gen.alphaNumChar))
      page_urlfragment <- Gen.option(CommonGen.strGen(512, Gen.alphaNumChar))
      refr_urlscheme <- Gen.option(CommonGen.strGen(10, Gen.alphaNumChar))
      refr_urlhost <- Gen.option(CommonGen.strGen(512, Gen.alphaNumChar))
      refr_urlport <- Gen.option(Gen.chooseNum(1, 65000))
      refr_urlpath <- Gen.option(CommonGen.strGen(512, Gen.alphaNumChar))
      refr_urlquery <- Gen.option(CommonGen.strGen(512, Gen.alphaNumChar))
      refr_urlfragment <- Gen.option(CommonGen.strGen(512, Gen.alphaNumChar))
      refr_medium <- Gen.option(CommonGen.strGen(512, Gen.alphaNumChar))
      refr_source <- Gen.option(CommonGen.strGen(512, Gen.alphaNumChar))
      refr_term <- Gen.option(CommonGen.strGen(512, Gen.alphaNumChar))
      mkt_medium <- Gen.option(CommonGen.strGen(512, Gen.alphaNumChar))
      mkt_source <- Gen.option(CommonGen.strGen(512, Gen.alphaNumChar))
      mkt_term <- Gen.option(CommonGen.strGen(512, Gen.alphaNumChar))
      mkt_content <- Gen.option(CommonGen.strGen(512, Gen.alphaNumChar))
      mkt_campaign <- Gen.option(CommonGen.strGen(512, Gen.alphaNumChar))
      contexts <-  Gen.option(CommonGen.jsonGen).map(_.map(_.toString()))
      se_category <- Gen.option(CommonGen.strGen(512, Gen.alphaNumChar))
      se_action <- Gen.option(CommonGen.strGen(512, Gen.alphaNumChar))
      se_label <- Gen.option(CommonGen.strGen(512, Gen.alphaNumChar))
      se_property <- Gen.option(CommonGen.strGen(512, Gen.alphaNumChar))
      se_value <- Gen.option(CommonGen.strGen(512, Gen.alphaNumChar))
      unstruct_event <- Gen.option(CommonGen.jsonGen).map(_.map(_.toString()))
      tr_orderid <- Gen.option(Gen.uuid).map(_.map(_.toString()))
      tr_affiliation <- Gen.option(CommonGen.strGen(512, Gen.alphaNumChar))
      tr_total <- Gen.option(CommonGen.strGen(512, Gen.alphaNumChar))
      tr_tax <- Gen.option(CommonGen.strGen(512, Gen.alphaNumChar))
      tr_shipping <- Gen.option(CommonGen.strGen(512, Gen.alphaNumChar))
      tr_city <- Gen.option(CommonGen.strGen(512, Gen.alphaNumChar))
      tr_state <- Gen.option(CommonGen.strGen(512, Gen.alphaNumChar))
      tr_country <- Gen.option(CommonGen.strGen(512, Gen.alphaNumChar))
      ti_orderid <- Gen.option(Gen.uuid).map(_.map(_.toString()))
      ti_sku <- Gen.option(CommonGen.strGen(512, Gen.alphaNumChar))
      ti_name <- Gen.option(CommonGen.strGen(512, Gen.alphaNumChar))
      ti_category <- Gen.option(CommonGen.strGen(512, Gen.alphaNumChar))
      ti_price <- Gen.option(CommonGen.strGen(512, Gen.alphaNumChar))
      ti_quantity <- Gen.option(Arbitrary.arbitrary[Int])
      pp_xoffset_min <- Gen.option(Gen.chooseNum(1, 10000))
      pp_xoffset_max <- Gen.option(Gen.chooseNum(1, 10000))
      pp_yoffset_min <- Gen.option(Gen.chooseNum(1, 10000))
      pp_yoffset_max <- Gen.option(Gen.chooseNum(1, 10000))
      useragent <- Gen.option(CommonGen.strGen(512, Gen.alphaNumChar))
      br_name <- Gen.option(CommonGen.strGen(512, Gen.alphaNumChar))
      br_family <- Gen.option(CommonGen.strGen(512, Gen.alphaNumChar))
      br_version <- Gen.option(CommonGen.version)
      br_type <- Gen.option(CommonGen.strGen(512, Gen.alphaNumChar))
      br_renderengine <- Gen.option(CommonGen.strGen(512, Gen.alphaNumChar))
      br_lang <- Gen.option(CommonGen.strGen(512, Gen.alphaNumChar))
      br_features_pdf <- Gen.option(Gen.chooseNum(0, 1)).map(_.map(i => i.toByte))
      br_features_flash <- Gen.option(Gen.chooseNum(0, 1)).map(_.map(i => i.toByte))
      br_features_java <- Gen.option(Gen.chooseNum(0, 1)).map(_.map(i => i.toByte))
      br_features_director <- Gen.option(Gen.chooseNum(0, 1)).map(_.map(i => i.toByte))
      br_features_quicktime <- Gen.option(Gen.chooseNum(0, 1)).map(_.map(i => i.toByte))
      br_features_realplayer <- Gen.option(Gen.chooseNum(0, 1)).map(_.map(i => i.toByte))
      br_features_windowsmedia <- Gen.option(Gen.chooseNum(0, 1)).map(_.map(i => i.toByte))
      br_features_gears <- Gen.option(Gen.chooseNum(0, 1)).map(_.map(i => i.toByte))
      br_features_silverlight <- Gen.option(Gen.chooseNum(0, 1)).map(_.map(i => i.toByte))
      br_cookies <- Gen.option(Gen.chooseNum(0, 1)).map(_.map(i => i.toByte))
      br_colordepth <- Gen.option(CommonGen.strGen(512, Gen.alphaNumChar))
      br_viewwidth <- Gen.option(Gen.chooseNum(1, 10000))
      br_viewheight <- Gen.option(Gen.chooseNum(1, 10000))
      os_name <- Gen.option(CommonGen.strGen(512, Gen.alphaNumChar))
      os_family <- Gen.option(CommonGen.strGen(512, Gen.alphaNumChar))
      os_manufacturer <- Gen.option(CommonGen.strGen(512, Gen.alphaNumChar))
      os_timezone <- Gen.option(CommonGen.strGen(512, Gen.alphaNumChar))
      dvce_type <- Gen.option(CommonGen.strGen(512, Gen.alphaNumChar))
      dvce_ismobile <- Gen.option(Gen.chooseNum(0, 1)).map(_.map(i => i.toByte))
      dvce_screenwidth <- Gen.option(Gen.chooseNum(1, 10000))
      dvce_screenheight <- Gen.option(Gen.chooseNum(1, 10000))
      doc_charset <- Gen.option(CommonGen.strGen(512, Gen.alphaNumChar))
      doc_width <- Gen.option(Gen.chooseNum(1, 10000))
      doc_height <- Gen.option(Gen.chooseNum(1, 10000))
      tr_currency <- Gen.option(CommonGen.strGen(512, Gen.alphaNumChar))
      tr_total_base <- Gen.option(CommonGen.strGen(512, Gen.alphaNumChar))
      tr_tax_base <- Gen.option(CommonGen.strGen(512, Gen.alphaNumChar))
      tr_shipping_base <- Gen.option(CommonGen.strGen(512, Gen.alphaNumChar))
      ti_currency <- Gen.option(CommonGen.strGen(512, Gen.alphaNumChar))
      ti_price_base <- Gen.option(CommonGen.strGen(512, Gen.alphaNumChar))
      base_currency <- Gen.option(CommonGen.strGen(512, Gen.alphaNumChar))
      geo_timezone <- Gen.option(CommonGen.strGen(512, Gen.alphaNumChar))
      mkt_clickid <- Gen.option(Gen.uuid).map(_.map(_.toString()))
      mkt_network <- Gen.option(CommonGen.strGen(512, Gen.alphaNumChar))
      etl_tags <- Gen.option(CommonGen.strGen(512, Gen.alphaNumChar))
      dvce_sent_tstamp <- Gen.option(CommonGen.dateTimeGen).map(_.map(_.toString()))
      refr_domain_userid <- Gen.option(Gen.uuid).map(_.map(_.toString()))
      refr_dvce_tstamp <- Gen.option(CommonGen.dateTimeGen).map(_.map(_.toString()))
      derived_contexts <- Gen.option(CommonGen.strGen(512, Gen.alphaNumChar))
      domain_sessionid <- Gen.option(Gen.uuid).map(_.map(_.toString()))
      derived_tstamp  <- Gen.option(CommonGen.dateTimeGen).map(_.map(_.toString()))
      event_vendor <- Gen.option(CommonGen.schemaKey).map(_.map(_.vendor))
      event_name <- Gen.option(CommonGen.schemaKey).map(_.map(_.name))
      event_format <- Gen.option(CommonGen.schemaKey).map(_.map(_.format))
      event_version <- Gen.option(CommonGen.schemaKey).map(_.map(_.version.toString()))
      event_fingerprint <- Gen.option(CommonGen.strGen(512, Gen.alphaNumChar))
      true_tstamp <- Gen.option(CommonGen.dateTimeGen).map(_.map(_.toString()))
    } yield Payload.PartiallyEnrichedEvent(
      app_id,
      platform,
      etl_tstamp,
      collector_tstamp,
      dvce_created_tstamp,
      event,
      event_id,
      txn_id,
      name_tracker,
      v_tracker,
      v_collector,
      v_etl,
      user_id,
      user_ipaddress,
      user_fingerprint,
      domain_userid,
      domain_sessionidx,
      network_userid,
      geo_country,
      geo_region,
      geo_city,
      geo_zipcode,
      geo_latitude,
      geo_longitude,
      geo_region_name,
      ip_isp,
      ip_organization,
      ip_domain,
      ip_netspeed,
      page_url,
      page_title,
      page_referrer,
      page_urlscheme,
      page_urlhost,
      page_urlport,
      page_urlpath,
      page_urlquery,
      page_urlfragment,
      refr_urlscheme,
      refr_urlhost,
      refr_urlport,
      refr_urlpath,
      refr_urlquery,
      refr_urlfragment,
      refr_medium,
      refr_source,
      refr_term,
      mkt_medium,
      mkt_source,
      mkt_term,
      mkt_content,
      mkt_campaign,
      contexts,
      se_category,
      se_action,
      se_label,
      se_property,
      se_value,
      unstruct_event,
      tr_orderid,
      tr_affiliation,
      tr_total,
      tr_tax,
      tr_shipping,
      tr_city,
      tr_state,
      tr_country,
      ti_orderid,
      ti_sku,
      ti_name,
      ti_category,
      ti_price,
      ti_quantity,
      pp_xoffset_min,
      pp_xoffset_max,
      pp_yoffset_min,
      pp_yoffset_max,
      useragent,
      br_name,
      br_family,
      br_version,
      br_type,
      br_renderengine,
      br_lang,
      br_features_pdf,
      br_features_flash,
      br_features_java,
      br_features_director,
      br_features_quicktime,
      br_features_realplayer,
      br_features_windowsmedia,
      br_features_gears,
      br_features_silverlight,
      br_cookies,
      br_colordepth,
      br_viewwidth,
      br_viewheight,
      os_name,
      os_family,
      os_manufacturer,
      os_timezone,
      dvce_type,
      dvce_ismobile,
      dvce_screenwidth,
      dvce_screenheight,
      doc_charset,
      doc_width,
      doc_height,
      tr_currency,
      tr_total_base,
      tr_tax_base,
      tr_shipping_base,
      ti_currency,
      ti_price_base,
      base_currency,
      geo_timezone,
      mkt_clickid,
      mkt_network,
      etl_tags,
      dvce_sent_tstamp,
      refr_domain_userid,
      refr_dvce_tstamp,
      derived_contexts,
      domain_sessionid,
      derived_tstamp,
      event_vendor,
      event_name,
      event_format,
      event_version,
      event_fingerprint,
      true_tstamp
    )

  val collectorPayload: Gen[Payload.CollectorPayload] =
    for {
      vendor      <- CommonGen.strGen(64, Gen.alphaLowerChar)
      version     <- CommonGen.strGen(16, Gen.alphaNumChar)
      querystring <- CommonGen.listOfMaxN(100, nvp)
      body        <- Gen.option(Arbitrary.arbitrary[String])
      contentType <- Gen.option(Gen.oneOf("text/plain", "application/json", "application/json; encoding=utf-8" ))
      collector   <- CommonGen.strGen(32, Gen.alphaNumChar)
      encoding    <- CommonGen.strGen(32, Gen.alphaNumChar)
      timestamp   <- Gen.option(CommonGen.dateTimeGen)
      ipAddress   <- Gen.option(CommonGen.ipAddress)
      optStr      <- Gen.option(Gen.identifier) // The whole generator needs to be updated with more meaningful values
      headers     <- CommonGen.listOfMaxN(12, Gen.identifier)
      userId      <- Gen.option(Gen.uuid)
    } yield Payload.CollectorPayload(vendor, version, querystring, contentType, body, collector, encoding, optStr, timestamp, ipAddress, optStr, optStr, headers, userId)

  val enrichmentPayload: Gen[Payload.EnrichmentPayload] =
    for {
      event <- partiallyEnrichedEvent
      raw   <- rawEvent
    } yield Payload.EnrichmentPayload(event, raw)

  val loaderPayload: Gen[Payload.LoaderPayload] =
    Gen.const(Payload.LoaderPayload(SpecHelpers.ExampleEvent))
}
