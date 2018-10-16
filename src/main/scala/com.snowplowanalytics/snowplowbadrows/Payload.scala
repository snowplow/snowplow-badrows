package com.snowplowanalytics.snowplowbadrows

import java.time.Instant
import java.util.Base64

import io.circe.{Json, JsonObject}

sealed trait Payload {
  def asLine: String = this match {
    case Payload.RawPayload(metadata, rawEvent) =>
      new String(Base64.getEncoder.encode((metadata.asTsv ++ "\t" ++ rawEvent).getBytes))
    case Payload.SingleEvent(metadata, event) =>
      val eventJson = Json.fromJsonObject(JsonObject.fromMap(event.mapValues(Json.fromString))).noSpaces
      new String(Base64.getEncoder.encode((metadata.asTsv ++ "\t" ++ eventJson).getBytes))
  }
}

object Payload {
  /** Taken from Scala Common Enrich */
  final case class CollectorMeta(name: String,
                                 encoding: String,
                                 hostname: Option[String],
                                 timestamp: Option[Instant],
                                 ipAddress: Option[String],
                                 useragent: Option[String],
                                 refererUri: Option[String],
                                 headers: List[String],
                                 userId: Option[String]) {
    def asTsv: String =
      List(name, encoding, hostname.getOrElse(""), timestamp.map(_.toString).getOrElse(""),
        ipAddress.getOrElse(""), useragent.getOrElse(""), refererUri.getOrElse(""),
        headers.mkString(","), userId.getOrElse("")).mkString("\t")
  }


  /** Completely invalid payload; RawEvent from SCE */
  case class RawPayload(metadata: CollectorMeta, rawEvent: String) extends Payload
  /** Valid GET/JSON */
  case class SingleEvent(metadata: CollectorMeta, event: Map[String, String]) extends Payload

  case class Enriched(data: String) extends Payload
}

