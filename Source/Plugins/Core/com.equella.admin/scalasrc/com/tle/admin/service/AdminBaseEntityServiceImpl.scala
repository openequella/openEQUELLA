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

import com.tle.admin.graphql.conversion.LanguageBundleViewConverter.toLanguageBundle
import com.tle.admin.helper.GraphQLQueryHelper.getEntity
import com.tle.beans.entity.LanguageBundle
import io.github.openequella.graphql.ClientConfiguration
import io.github.openequella.graphql.api.BaseEntityApi
import org.slf4j.{Logger, LoggerFactory}

import javax.inject.{Inject, Singleton}

@Singleton
class AdminBaseEntityServiceImpl @Inject() (implicit
    val cfg: ClientConfiguration
) extends AdminBaseEntityService {
  private implicit val LOGGER: Logger = LoggerFactory.getLogger(classOf[AdminBaseEntityServiceImpl])

  override def getNameForId(id: Long): Option[LanguageBundle] =
    getEntity("Name [by ID]", id, BaseEntityApi.getNameById).map(toLanguageBundle)
}
