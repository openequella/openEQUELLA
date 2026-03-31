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
import com.tle.core.javascript.JavascriptService
import com.tle.core.security.impl.RequiresLogin
import com.tle.web.remoting.graphql.schema.types.NameValue
import org.slf4j.LoggerFactory

import scala.jdk.CollectionConverters._
import javax.inject.{Inject, Singleton}

/** A Provider for operations involving JavaScript libraries and modules. Ultimately proxied to the
  * `JavascriptService`.
  */
@Bind
@Singleton
class JavaScriptProvider @Inject() (javaScriptService: JavascriptService) {
  private val LOGGER = LoggerFactory.getLogger(classOf[JavaScriptProvider])

  @RequiresLogin(message =
    "Guest (unauthenticated) users cannot list JavaScript library names and IDs."
  )
  def listLibraries: List[NameValue] = {
    LOGGER.debug("Listing all JavaScript library names and IDs")
    javaScriptService.getAllJavascriptLibraryNames.asScala.map(NameValue(_)).toList
  }

  @RequiresLogin(message =
    "Guest (unauthenticated) users cannot get JavaScript module names and IDs."
  )
  def modulesByLibraryId(libraryId: String): Option[List[NameValue]] = {
    LOGGER.debug(s"Getting all JavaScript module names and IDs for library ID: $libraryId")

    noneIfNotFound {
      javaScriptService.getAllJavascriptModuleNames(libraryId)
    }.map(modules => modules.asScala.map(NameValue(_)).toList)
  }
}
