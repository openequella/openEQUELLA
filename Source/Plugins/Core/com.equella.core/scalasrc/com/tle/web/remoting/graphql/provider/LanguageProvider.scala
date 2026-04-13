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

package com.tle.web.remoting.graphql.provider

import com.tle.core.guice.Bind
import com.tle.core.i18n.service.LanguageService
import com.tle.core.security.impl.RequiresLogin
import com.tle.web.remoting.graphql.schema.types.{Language, LanguageBundleName}
import org.slf4j.LoggerFactory

import scala.jdk.CollectionConverters._
import javax.inject.{Inject, Singleton}

/** Provides GraphQL-layer access to system language configuration and i18n bundle resolution.
  * Delegates all business logic to [[LanguageService]].
  */
@Bind
@Singleton
class LanguageProvider @Inject() (languageService: LanguageService) {
  private val LOGGER = LoggerFactory.getLogger(classOf[LanguageProvider])

  @RequiresLogin(message = "Guest (unauthenticated) users cannot list configured languages.")
  def listLanguages: List[Language] = {
    LOGGER.debug("Listing all configured languages")
    languageService.getLanguages.asScala.map(Language(_)).toList
  }

  @RequiresLogin(message = "Guest (unauthenticated) users cannot resolve language bundle names.")
  def namesByBundleIds(bundleIds: List[Long]): List[LanguageBundleName] = {
    LOGGER.debug(s"Resolving language bundle names for IDs: ${bundleIds.mkString(",")}")
    val javaIds = bundleIds.map(Long.box).asJava

    languageService
      .getNames(javaIds)
      .asScala
      .map { case (bundleId, string) =>
        LanguageBundleName(bundleId, string)
      }
      .toList
  }
}
