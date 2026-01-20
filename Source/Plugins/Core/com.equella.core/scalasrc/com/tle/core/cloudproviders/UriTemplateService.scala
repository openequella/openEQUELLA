/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0, (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.core.cloudproviders

import sttp.model.{Uri, UriInterpolator}

import scala.util.parsing.combinator.{JavaTokenParsers, RegexParsers}

sealed trait UriTemplate
case class Text(text: String)        extends UriTemplate
case class Replacement(text: String) extends UriTemplate

case class UriParseError(msg: String) extends Throwable

object ServiceUriParser extends RegexParsers with JavaTokenParsers {
  private def textOnly: Parser[Text] = """[^$]+""".r ^^ { Text.apply }
  private def escapeDollar: Parser[Text] = "$$" ^^ { _ =>
    Text("$")
  }
  private def text: Parser[Text] = rep1(textOnly | escapeDollar) ^^ { l =>
    Text(l.map(_.text).mkString)
  }
  private def replacement: Parser[Replacement] = ("${" ~> ident <~ "}") ^^ { Replacement.apply }
  private def uriTemplateParser: Parser[List[UriTemplate]] = phrase(rep1(text | replacement))

  def parse(str: String): Either[UriParseError, List[UriTemplate]] =
    parse(uriTemplateParser, str) match {
      case Success(result, _) => Right(result)
      case NoSuccess(msg, _)  => Left(UriParseError(msg))
    }

}

object UriTemplateService {

  case class CollectTemplate(
      raw: List[String] = List.empty,
      args: List[Any] = List.empty,
      previous: Option[UriTemplate] = None
  )

  def replaceVariables(
      template: String,
      baseurl: String,
      variables: Map[String, Any]
  ): Either[UriParseError, Uri] = {

    def maybeBlank(strings: List[String], prev: Option[UriTemplate]): List[String] = prev match {
      case Some(Text(_)) => strings
      case _             => "" :: strings
    }

    ServiceUriParser
      .parse(template)
      .map { uriParts =>
        val collected = uriParts.foldRight(CollectTemplate()) {
          case (uriTemplate, CollectTemplate(raw, args, last)) =>
            uriTemplate match {
              case Text(t) => CollectTemplate(t :: raw, args, Some(uriTemplate))
              case Replacement(v) =>
                val replaceValue = v match {
                  case "baseUrl" => baseurl
                  case o         => variables.getOrElse(v, None)
                }
                CollectTemplate(maybeBlank(raw, last), replaceValue :: args, Some(uriTemplate))
            }
        }
        val rawStrings = StringContext(maybeBlank(collected.raw, collected.previous): _*)
        UriInterpolator.interpolate(rawStrings, collected.args: _*)
      }
  }
}
