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

import com.tle.admin.graphql.conversion.PluginDetailsViewConverter
import com.tle.beans.plugin.PluginDetails
import io.github.openequella.graphql.ClientConfiguration
import io.github.openequella.graphql.api.AdminConsolePluginApi
import org.slf4j.{Logger, LoggerFactory}

import java.util
import javax.inject.{Inject, Singleton}
import scala.jdk.CollectionConverters._

@Singleton
class AdminConsolePluginServiceImpl @Inject() (implicit
    val cfg: ClientConfiguration
) extends AdminConsolePluginService {
  private val LOGGER: Logger = LoggerFactory.getLogger(classOf[AdminConsolePluginServiceImpl])

  override def listPlugins(): util.List[PluginDetails] = AdminConsolePluginApi.listPlugins match {
    case Right(pluginViews) =>
      LOGGER.debug(s"Successfully listed admin console plugins. Count: ${pluginViews.size}")
      pluginViews.map(PluginDetailsViewConverter.toPluginDetails).asJava
    case Left(errors) =>
      throw new ClientRequestException("Error listing admin console plugins.", errors)
  }
}
