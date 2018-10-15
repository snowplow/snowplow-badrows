/*
 * Copyright (c) 2017 Snowplow Analytics Ltd. All rights reserved.
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

package com.snowplowanalytics.snowplowbadrows

import java.io.File

object Main {

  /**
   * Raw CLI configuration used to extract options from command line
   * Created solely for private `rawCliConfig` value and can contain
   * incorrect state that should be handled by `transform` function
   */
  private case class CliConfig(value: Int, input: File, verbose: Boolean)

  /**
   * Starting raw value, required by `parser`
   */
  private val rawCliConfig = CliConfig(0, new File("."), false)

  /**
   * End application config parsed from CLI. Unlike `CliConfig`
   */
  case class AppConfig(value: Int, input: File, verbose: Boolean)


  /**
   * Check that raw config contains valid stat
   */
  def transform(raw: CliConfig): Either[String, AppConfig] = 
    if (raw.value < 0) Left("Value cannot be less than 0")
    else if (!raw.input.exists) Left(s"File [${raw.input}] does not exist")
    else Right(AppConfig(raw.value, raw.input, raw.verbose))

  /**
   * Scopt parser providing necessary argument annotations and basic validation
   */
  private val parser = new scopt.OptionParser[CliConfig](generated.ProjectMetadata.name) {
    head(generated.ProjectMetadata.name, generated.ProjectMetadata.version)

    opt[Int]('v', "value").required().action( (x, c) =>
      c.copy(value = x) ).text("Some value greater than zero")

    opt[File]('i', "input").required().valueName("<file>").
      action( (x, c) => c.copy(input = x) ).
      text("Input path")

    opt[Unit]("verbose").action( (_, c) =>
      c.copy(verbose = true) ).text("Verbose output")

    help("help").text("prints this usage text")
  }


  def main(args: Array[String]): Unit = {
    println("Hello com.snowplowanalytics.snowplow-badrows!")

    parser.parse(args, rawCliConfig).map(transform) match {
      case Some(Right(appConfig)) => println(s"Success: " + appConfig.toString)
      case Some(Left(error)) => 
        // Failed transformation
        println(error)
        sys.exit(1)
      case None =>
        // Invalid arguments
        sys.exit(1)
    }
  }
}
