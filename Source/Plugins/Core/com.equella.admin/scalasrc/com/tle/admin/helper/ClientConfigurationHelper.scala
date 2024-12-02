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

import io.github.openequella.graphql.ClientConfiguration
import org.slf4j.LoggerFactory
import sttp.model.Uri
import sttp.model.headers.CookieWithMeta

import java.net.{CookieHandler, URL}
import scala.collection.mutable
import scala.jdk.CollectionConverters._

/**
  * Helper class for Java interop with the ClientConfiguration class. Main area of helping is loading
  * system cookies. As can be seen in the login methods
  * (e.g. `com.tle.admin.boot.Bootstrap#login(java.net.URL)`) the admin console uses the
  * `CookieHandler.getDefault` method to load system cookies. This helper class provides a way to
  * load system cookies into a ClientConfiguration object.
  *
  * The main cookie of interest is the `JSESSIONID` cookie. This cookie is used to maintain a
  * session with the openEQUELLA.
  *
  */
object ClientConfigurationHelper {
  private val LOGGER = LoggerFactory.getLogger(ClientConfigurationHelper.getClass)

  /**
    * Create a ClientConfiguration object from a java URL.
    */
  def create(url: URL): ClientConfiguration = {
    ClientConfiguration(Uri(url.toURI))
  }

  /**
    * Load system cookies into a ClientConfiguration object. With the knowledge that the knowledge
    * that the `CookieHandler` used in the admin console is `com.tle.client.ListCookieHandler` which
    * only stores the `Cookie` header, this method will only load cookies from the `Cookie` header.
    */
  def loadSystemCookies(cfg: ClientConfiguration): Unit = {
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
      .map(_.get(cfg.institutionUrl.toJavaUri, Map.empty[String, java.util.List[String]].asJava))
      .map(_.asScala) match {
      case Some(cookieMap) =>
        val cookies = convertCookies(cookieMap)
        LOGGER.debug("Loading system cookies: " + cookies)
        cfg.cookies.addAll(cookies)
      case None => LOGGER.warn("No system cookies found.")
    }
  }
}
