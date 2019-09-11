package com.snowplowanalytics.snowplow.badrows

import cats.data.NonEmptyList
import io.circe.literal._
import io.circe.syntax._
import io.circe.{Json, parser, Decoder, Encoder}
import com.snowplowanalytics.iglu.client.ClientError.ValidationError
import com.snowplowanalytics.iglu.client.validator.ValidatorError.InvalidData
import com.snowplowanalytics.iglu.client.validator.ValidatorReport
import com.snowplowanalytics.iglu.client.{CirceValidator, Resolver}
import com.snowplowanalytics.iglu.core.{SchemaKey, SchemaVer}
import org.scalacheck.Gen
import org.scalacheck.Prop.forAll
import org.specs2.{ScalaCheck, Specification}
import SpecHelpers.IdInstances._

object SchemaValidationSpec {

  val resolverConfig = json"""
      {
         "schema":"iglu:com.snowplowanalytics.iglu/resolver-config/jsonschema/1-0-0",
         "data":{
            "cacheSize":500,
            "repositories":[
                {
                  "name":"Branch snowplow/snowplow bad rows",
                  "priority":0,
                  "vendorPrefixes":[
                    "com.snowplowanalytics"
                  ],
                  "connection":{
                    "http":{
                      "uri":"https://raw.githubusercontent.com/snowplow/iglu-central/bad-rows/"
                    }
                  }
                },
                {
                  "name":"Branch BQ loader bad rows",
                  "priority":2,
                  "vendorPrefixes":[
                    "com.snowplowanalytics"
                  ],
                  "connection":{
                    "http":{
                      "uri":"https://raw.githubusercontent.com/aldemirenes/iglu-central/bq-loader-bad-rows/"
                    }
                  }
                },
                {
                  "name":"TOREMOVE branch all bad rows",
                  "priority":1,
                  "vendorPrefixes":[
                    "com.snowplowanalytics"
                  ],
                  "connection":{
                    "http":{
                      "uri":"https://raw.githubusercontent.com/aldemirenes/iglu-central/all-bad-rows/"
                    }
                  }
                }
            ]
         }
      }
    """

  val resolver = Resolver.parse(resolverConfig).fold(e => throw new RuntimeException(e.toString), identity)

  val mockJsonValue = Json.obj("mockJsonKey" := "mockJsonValue")

  val mockProcessor = Processor("artifact", "0.0.1")

  val rawPayloadGen = Gen.alphaNumStr.map { line =>
    Payload.RawPayload(line)
  }

  // TODO ENES: generate event instead of giving static one
  val loaderPayloadGen = Gen.const(Payload.LoaderPayload(SpecHelpers.ExampleEvent))

  val selfDescribingEntityGen = Gen.alphaNumStr.map(Payload.SelfDescribingEntity(_, mockJsonValue))

  val reconstructedEventGen = Gen.listOf(selfDescribingEntityGen).map(Payload.BQReconstructedEvent(SpecHelpers.ExampleEvent, _))

  val parsingErrorBadRowGen = for {
    payload <- rawPayloadGen
    failure <- Gen.nonEmptyListOf(Gen.alphaNumStr).map { errors =>
      Failure.LoaderParsingErrors(NonEmptyList.fromListUnsafe(errors))
    }
  } yield BadRow.LoaderParsingErrors(mockProcessor, failure, payload)


  val schemaVerGen = for {
    model <- Gen.chooseNum(1, 9)
    revision <- Gen.chooseNum(0, 9)
    addition <- Gen.chooseNum(0, 9)
  } yield SchemaVer.Full(model, revision, addition)

  val schemaKeyGen = for {
    vendor <- Gen.identifier
    name <- Gen.identifier
    format <- Gen.identifier
    version <- schemaVerGen
  } yield SchemaKey(vendor, name, format, version)

  val loaderIgluErrorBadRowGen = for {
    payload <- loaderPayloadGen
    failure <- Gen.nonEmptyListOf(
      schemaKeyGen.map { schemaKey =>
        FailureDetails.LoaderIgluError(
          schemaKey,
          // TODO Enes: Create generator for ClientError
          ValidationError(InvalidData(NonEmptyList.of(ValidatorReport("message", None, List(), None))))
        )
      }
    ).map(l => Failure.LoaderIgluErrors(NonEmptyList.fromListUnsafe(l)))
  } yield BadRow.LoaderIgluErrors(mockProcessor, failure, payload)

  val bqFieldTypeGen = Gen.oneOf("Boolean", "Float", "Integer")

  val bqCastErrorsBadRowGen = for {
    payload <- loaderPayloadGen
    failure <- Gen.nonEmptyListOf(
      for {
        schemaKey <- schemaKeyGen
        bqCastErrorGen <- Gen.oneOf(
          bqFieldTypeGen.map(FailureDetails.BQCastErrorInfo.WrongType(mockJsonValue, _)),
          bqFieldTypeGen.map(FailureDetails.BQCastErrorInfo.NotAnArray(mockJsonValue, _)),
          Gen.alphaNumStr.map(FailureDetails.BQCastErrorInfo.MissingInValue(_, mockJsonValue))
        )
        bqCastErrors <- Gen.nonEmptyListOf(bqCastErrorGen)
      } yield FailureDetails.BQCastError(mockJsonValue, schemaKey, NonEmptyList.fromListUnsafe(bqCastErrors))
    ).map(e => Failure.BQCastErrors(NonEmptyList.fromListUnsafe(e)))
  } yield BadRow.BQCastErrors(mockProcessor, failure, payload)


  val loaderRuntimeErrorBadRowGen = for {
    payload <- loaderPayloadGen
    failure <- Gen.alphaNumStr.map { e =>
      Failure.LoaderRuntimeErrors(e)
    }
  } yield BadRow.LoaderRuntimeErrors(mockProcessor, failure, payload)

  val bqRepeaterParsingErrorGen = for {
    payload <- rawPayloadGen
    failure <- for {
      message <- Gen.alphaNumStr
      location <- Gen.nonEmptyListOf(Gen.alphaNumStr)
    } yield Failure.BQRepeaterParsingError(message, location)
  } yield BadRow.BQRepeaterParsingError(mockProcessor, failure, payload)

  val bqRepeaterPubSubErrorGen = for {
    payload <- rawPayloadGen
    failure <- Gen.alphaNumStr.map(e => Failure.BQRepeaterPubSubError(e))
  } yield BadRow.BQRepeaterPubSubError(mockProcessor, failure, payload)

  val bqRepeaterBigQueryErrorGen = for {
    payload <- reconstructedEventGen
    failure <- for {
      reason <- Gen.option(Gen.alphaNumStr)
      location <- Gen.option(Gen.alphaNumStr)
      message <- Gen.alphaNumStr
    } yield Failure.BQRepeaterBigQueryError(reason, location, message)
  } yield BadRow.BQRepeaterBigQueryError(mockProcessor, failure, payload)

  def strGen(n: Int): Gen[String] = Gen.alphaNumStr.map(s => s.substring(0, Math.min(s.length(), n)))

  val inputDataCPFormatViolationMessageGen: Gen[FailureDetails.CPFormatViolationMessage] = for {
    payloadField <- strGen(64)
    value <- Gen.option(Gen.alphaNumStr)
    expectation <- strGen(256)
  } yield FailureDetails.CPFormatViolationMessage.InputData(payloadField, value, expectation)

  val fallbackCPFormatViolationMessage: Gen[FailureDetails.CPFormatViolationMessage] = strGen(512).map { e =>
    FailureDetails.CPFormatViolationMessage.Fallback(e)
  }

  // TODO Enes: Generate properly
  val instantGen = Gen.const(java.time.Instant.now())

  val cpFormatViolationFailureGen = for {
    timestamp <- instantGen
    loader <- Gen.oneOf("clj-tomcat", "cloudfront", "ndjson", "thrift", "tsv")
    message <- Gen.oneOf(inputDataCPFormatViolationMessageGen, fallbackCPFormatViolationMessage)
  } yield Failure.CPFormatViolation(timestamp, loader, message)

  val cpFormatViolationBadRowGen = for {
    payload <- rawPayloadGen
    failure <- cpFormatViolationFailureGen
  } yield BadRow.CPFormatViolation(mockProcessor, failure, payload)

  def validateBadRow[F <: BadRow : Decoder : Encoder](badRow: BadRow) = {
    // reparsing json is added in order to check decoding
    val encoded = parser.parse(badRow.selfDescribinData.data.noSpaces)
      .getOrElse(throw new RuntimeException("Error while parsing bad row json"))
    val decoded = encoded.as[F].getOrElse(throw new RuntimeException(s"Error while decoding bad row: ${encoded.as[F]}"))
      .asJson
    val schema = resolver.lookupSchema(badRow.schemaKey, 3)
    CirceValidator.validate(decoded, schema.getOrElse(throw new RuntimeException(s"Schema could not be found: $schema")))
  }
}

class SchemaValidationSpec extends Specification with ScalaCheck { def is = s2"""
    parsing error $e1
    loader iglu error $e2
    bq cast error $e3
    loader run time error $e4
    bq repeater parsing error $e5
    bq repeater pubsub error $e5
    bq repeater bigquery error $e7
    cp format violation $e8
  """
  import SchemaValidationSpec._

  def e1 = {
    forAll(parsingErrorBadRowGen) {
      f => validateBadRow[BadRow.LoaderParsingErrors](f) must beRight
    }
  }

  def e2 = {
    forAll(loaderIgluErrorBadRowGen) {
      f => validateBadRow[BadRow.LoaderIgluErrors](f) must beRight
    }
  }

  def e3 = {
    forAll(bqCastErrorsBadRowGen) {
      f => validateBadRow[BadRow.BQCastErrors](f) must beRight
    }
  }

  def e4 = {
    forAll(loaderRuntimeErrorBadRowGen) {
      f => validateBadRow[BadRow.LoaderRuntimeErrors](f) must beRight
    }
  }

  def e5 = {
    forAll(bqRepeaterParsingErrorGen) {
      f => validateBadRow[BadRow.BQRepeaterParsingError](f) must beRight
    }
  }

  def e6 = {
    forAll(bqRepeaterPubSubErrorGen) {
      f => validateBadRow[BadRow.BQRepeaterPubSubError](f) must beRight
    }
  }

  def e7 = {
    forAll(bqRepeaterBigQueryErrorGen) {
      f => validateBadRow[BadRow.BQRepeaterBigQueryError](f) must beRight
    }
  }

  def e8 = {
    forAll(cpFormatViolationBadRowGen) {
      f => validateBadRow[BadRow.CPFormatViolation](f) must beRight
    }
  }
}
