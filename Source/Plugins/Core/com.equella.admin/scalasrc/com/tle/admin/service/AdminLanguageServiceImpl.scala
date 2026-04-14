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

package com.tle.admin.service

import com.tle.admin.graphql.conversion.LanguageViewConverter.toLanguage
import com.tle.beans.Language
import io.github.openequella.graphql.ClientConfiguration
import io.github.openequella.graphql.api.LanguageApi
import org.slf4j.{Logger, LoggerFactory}

import javax.inject.{Inject, Singleton}
import scala.jdk.CollectionConverters._
import java.util
import java.lang

@Singleton
class AdminLanguageServiceImpl @Inject() (implicit
    val cfg: ClientConfiguration
) extends AdminLanguageService {
  private val LOGGER: Logger = LoggerFactory.getLogger(classOf[AdminLanguageServiceImpl])

  override def getNames(
      bundleIds: util.Collection[lang.Long]
  ): util.Map[lang.Long, String] = {
    val scalaIds = bundleIds.asScala.map(_.longValue()).toList
    LanguageApi.namesByBundleIds(scalaIds) match {
      case Right(bundleNames) =>
        LOGGER.debug(s"Successfully resolved language bundle names for IDs: [$scalaIds]")
        bundleNames.map(view => Long.box(view.id) -> view.string).toMap.asJava
      case Left(errors) =>
        throw new ClientRequestException(
          s"Error resolving language bundle names with IDs: [$scalaIds].",
          errors
        )
    }
  }

  override def getLanguages: util.List[Language] = LanguageApi.listLanguages match {
    case Right(languages) =>
      LOGGER.debug(s"Successfully listed languages. Count: ${languages.size}")
      languages.map(toLanguage).asJava
    case Left(errors) =>
      throw new ClientRequestException("Error listing languages.", errors)
  }
}
