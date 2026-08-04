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
import com.tle.core.download.PluginDownloadService
import com.tle.core.security.impl.RequiresLogin
import com.tle.web.remoting.graphql.schema.types.PluginDetails
import org.slf4j.LoggerFactory

import scala.jdk.CollectionConverters._
import javax.inject.{Inject, Singleton}

/** Provides GraphQL-layer access to admin console plugin information. Delegates all business logic
  * to [[PluginDownloadService]].
  */
@Bind
@Singleton
class AdminConsolePluginProvider @Inject() (pluginDownloadService: PluginDownloadService) {
  private val LOGGER = LoggerFactory.getLogger(classOf[AdminConsolePluginProvider])
  // The type of plugin that the PluginDownloadService uses to identify plugins available for the admin console.
  private final val ADMIN_CONSOLE_PLUGIN_TYPE = "admin-console"

  @RequiresLogin(message =
    "Guest (unauthenticated) users cannot retrieve admin console plugin details."
  )
  def listPlugins: Either[ProviderError, List[PluginDetails]] = {
    LOGGER.debug(s"Listing all admin console plugin details")
    ProviderError.Try(s"Failed to retrieve admin console plugin details") {
      pluginDownloadService
        .getAllPluginDetails(ADMIN_CONSOLE_PLUGIN_TYPE)
        .asScala
        .map(info => PluginDetails(info.getBaseUrl.toString, info.getManifestXml))
        .toList
    }
  }
}
