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

import cats.syntax.option._

import io.circe.literal._
import io.circe.syntax._
import io.circe.parser.decode

import org.specs2.Specification
import org.specs2.ScalaCheck

import org.scalacheck.Prop.forAll

import org.scalacheck.{Arbitrary, Gen}

class PayloadSpec extends Specification with ScalaCheck {
  import Payload._
  def is = s2"""
  encode RawPayload $e1
  encode CollectorPayload $e2
  encode EnrichmentPayload $e3
  decode EnrichmentPayload with $$.payload.raw.parameters as object (1-0-0) $e4
  """

  def e1 = {
    val p: Payload = RawPayload("ah")
    val expected = json""""ah""""
    p.asJson must beEqualTo(expected)
  }

  def e2 = {
    val p: Payload = CollectorPayload(
      "com.hubspot",
      "v1",
      List.empty,
      None,
      "{}".some,
      "ssc",
      "UTF-8",
      None,
      None,
      "127.0.0.1".some,
      None,
      None,
      List("content-type: application/json"),
      None
    )
    val expected = json"""{
      "vendor": "com.hubspot",
      "version": "v1",
      "querystring": [],
      "contentType": null,
      "body": "{}",
      "collector": "ssc",
      "encoding": "UTF-8",
      "hostname": null,
      "timestamp": null,
      "ipAddress": "127.0.0.1",
      "useragent": null,
      "refererUri": null,
      "headers": ["content-type: application/json"],
      "networkUserId" : null
    }"""
    p.asJson must beEqualTo(expected)
  }

  def e3 = {
    val p: Payload = EnrichmentPayload(
      PartiallyEnrichedEvent(
        None,
        None,
        None,
        Some("collector_tstamp"),
        None,
        None,
        None,
        None,
        None,
        None,
        Some("v_collector"),
        Some("v_etl"),
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None
      ), RawEvent(
        "vendor",
        "version",
        List(
          NVP("field1", Some("value1")),
          NVP("field1", Some("value2")),
          NVP("field2", None)
        ),
        None,
        "tsv",
        "UTF-8",
        None,
        None,
        None,
        None,
        None,
        Nil,
        None
      )
    )
    val expected = json"""{
      "enriched": {
        "app_id" : null,
        "platform" : null,
        "etl_tstamp" : null,
        "collector_tstamp" : "collector_tstamp",
        "dvce_created_tstamp" : null,
        "event" : null,
        "event_id" : null,
        "txn_id" : null,
        "name_tracker" : null,
        "v_tracker" : null,
        "v_collector" : "v_collector",
        "v_etl" : "v_etl",
        "user_id" : null,
        "user_ipaddress" : null,
        "user_fingerprint" : null,
        "domain_userid" : null,
        "domain_sessionidx" : null,
        "network_userid" : null,
        "geo_country" : null,
        "geo_region" : null,
        "geo_city" : null,
        "geo_zipcode" : null,
        "geo_latitude" : null,
        "geo_longitude" : null,
        "geo_region_name" : null,
        "ip_isp" : null,
        "ip_organization" : null,
        "ip_domain" : null,
        "ip_netspeed" : null,
        "page_url" : null,
        "page_title" : null,
        "page_referrer" : null,
        "page_urlscheme" : null,
        "page_urlhost" : null,
        "page_urlport" : null,
        "page_urlpath" : null,
        "page_urlquery" : null,
        "page_urlfragment" : null,
        "refr_urlscheme" : null,
        "refr_urlhost" : null,
        "refr_urlport" : null,
        "refr_urlpath" : null,
        "refr_urlquery" : null,
        "refr_urlfragment" : null,
        "refr_medium" : null,
        "refr_source" : null,
        "refr_term" : null,
        "mkt_medium" : null,
        "mkt_source" : null,
        "mkt_term" : null,
        "mkt_content" : null,
        "mkt_campaign" : null,
        "contexts" : null,
        "se_category" : null,
        "se_action" : null,
        "se_label" : null,
        "se_property" : null,
        "se_value" : null,
        "unstruct_event" : null,
        "tr_orderid" : null,
        "tr_affiliation" : null,
        "tr_total" : null,
        "tr_tax" : null,
        "tr_shipping" : null,
        "tr_city" : null,
        "tr_state" : null,
        "tr_country" : null,
        "ti_orderid" : null,
        "ti_sku" : null,
        "ti_name" : null,
        "ti_category" : null,
        "ti_price" : null,
        "ti_quantity" : null,
        "pp_xoffset_min" : null,
        "pp_xoffset_max" : null,
        "pp_yoffset_min" : null,
        "pp_yoffset_max" : null,
        "useragent" : null,
        "br_name" : null,
        "br_family" : null,
        "br_version" : null,
        "br_type" : null,
        "br_renderengine" : null,
        "br_lang" : null,
        "br_features_pdf" : null,
        "br_features_flash" : null,
        "br_features_java" : null,
        "br_features_director" : null,
        "br_features_quicktime" : null,
        "br_features_realplayer" : null,
        "br_features_windowsmedia" : null,
        "br_features_gears" : null,
        "br_features_silverlight" : null,
        "br_cookies" : null,
        "br_colordepth" : null,
        "br_viewwidth" : null,
        "br_viewheight" : null,
        "os_name" : null,
        "os_family" : null,
        "os_manufacturer" : null,
        "os_timezone" : null,
        "dvce_type" : null,
        "dvce_ismobile" : null,
        "dvce_screenwidth" : null,
        "dvce_screenheight" : null,
        "doc_charset" : null,
        "doc_width" : null,
        "doc_height" : null,
        "tr_currency" : null,
        "tr_total_base" : null,
        "tr_tax_base" : null,
        "tr_shipping_base" : null,
        "ti_currency" : null,
        "ti_price_base" : null,
        "base_currency" : null,
        "geo_timezone" : null,
        "mkt_clickid" : null,
        "mkt_network" : null,
        "etl_tags" : null,
        "dvce_sent_tstamp" : null,
        "refr_domain_userid" : null,
        "refr_dvce_tstamp" : null,
        "derived_contexts" : null,
        "domain_sessionid" : null,
        "derived_tstamp" : null,
        "event_vendor" : null,
        "event_name" : null,
        "event_format" : null,
        "event_version" : null,
        "event_fingerprint" : null,
        "true_tstamp" : null
      }, "raw": {
        "vendor": "vendor",
        "version": "version",
         "parameters" : [
          {
            "name" : "field1",
            "value" : "value1"
          },
          {
            "name" : "field1",
            "value" : "value2"
          },
          {
            "name" : "field2",
            "value" : null
          }
        ],
        "contentType": null,
        "loaderName": "tsv",
        "encoding": "UTF-8",
        "hostname": null,
        "timestamp": null,
        "ipAddress": null,
        "useragent": null,
        "refererUri": null,
        "headers": [],
        "userId": null
      }
    }"""
    p.asJson must beEqualTo(expected)
  }

  def e4 = {
    val rawEventJsonGen: Gen[String] =
      for {
        queryString <- Arbitrary.arbitrary[Map[String, String]]
      } yield s"""
      {
        "vendor": "vendor",
        "version": "version",
        "parameters" : ${queryString.asJson},
        "contentType": null,
        "loaderName": "tsv",
        "encoding": "UTF-8",
        "hostname": null,
        "timestamp": null,
        "ipAddress": null,
        "useragent": null,
        "refererUri": null,
        "headers": [],
        "userId": null
      }
      """
    forAll(rawEventJsonGen) { s => decode[RawEvent](s) must beRight }
  }
}
