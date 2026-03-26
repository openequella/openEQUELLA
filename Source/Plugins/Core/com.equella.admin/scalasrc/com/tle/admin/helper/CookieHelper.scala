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

package com.tle.admin.helper

import org.slf4j.{Logger, LoggerFactory}
import sttp.model.Uri
import sttp.model.headers.CookieWithMeta

import java.net.CookieHandler
import scala.collection.mutable
import scala.jdk.CollectionConverters._

object CookieHelper {
  private val LOGGER: Logger = LoggerFactory.getLogger(CookieHelper.getClass)

  /** Extract the cookies from the system store targeting the provided URI. Loads cookies from the
    * `Cookie` header as returned by the system `CookieHandler`.
    * @param uri
    *   The URI to target for cookies.
    */
  def getSystemCookies(uri: Uri): Seq[CookieWithMeta] = {
    def convertCookies(cookieMap: mutable.Map[String, java.util.List[String]]) =
      cookieMap("Cookie").asScala
        .map { cookie =>
          CookieWithMeta.parse(cookie) match {
            case Left(error) =>
              LOGGER.error(s"Error parsing cookie [$cookie]: $error")
              None
            case Right(cwm) => Some(cwm)
          }
        }
        .collect({ case Some(cookie) => cookie })

    Option(CookieHandler.getDefault)
      .map(_.get(uri.toJavaUri, Map.empty[String, java.util.List[String]].asJava))
      .map(_.asScala) match {
      case Some(cookieMap) =>
        val cookies = convertCookies(cookieMap)
        LOGGER.debug("Loading system cookies: " + cookies)
        cookies.toSeq
      case None =>
        LOGGER.warn("No system cookies found.")
        Seq.empty
    }
  }
}
