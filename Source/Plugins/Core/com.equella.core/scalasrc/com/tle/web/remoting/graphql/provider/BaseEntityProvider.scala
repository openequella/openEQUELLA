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

import com.tle.core.entity.service.BaseEntityService
import com.tle.core.guice.Bind
import com.tle.web.remoting.graphql.schema.types.LanguageBundle

import javax.inject.{Inject, Singleton}

/** A Provider for operations involving Base Entities. Ultimately proxied to the
  * `BaseEntityService`.
  */
@Bind
@Singleton
class BaseEntityProvider @Inject() (baseEntityService: BaseEntityService) {

  /** Retrieve the name of the Base Entity by its unique ID.
    *
    * @param id
    *   the unique ID of the entity to retrieve
    * @return
    *   the GraphQL `LanguageBundle` object, or `None` if no entity is found
    */
  def nameById(id: Long): Option[LanguageBundle] =
    Option(baseEntityService.getNameForId(id)).map(LanguageBundle.apply)
}
