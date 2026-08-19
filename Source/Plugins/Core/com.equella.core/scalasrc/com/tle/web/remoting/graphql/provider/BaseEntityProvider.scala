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
import com.tle.core.security.impl.RequiresLogin
import com.tle.web.remoting.graphql.ErrorCode
import com.tle.web.remoting.graphql.schema.types.{BaseEntitySecurity, LanguageBundle}

import javax.inject.{Inject, Singleton}
import org.slf4j.LoggerFactory

import scala.jdk.OptionConverters._

/** A Provider for operations involving Base Entities. Ultimately proxied to the
  * `BaseEntityService`.
  */
@Bind
@Singleton
class BaseEntityProvider @Inject() (baseEntityService: BaseEntityService) {
  private val LOGGER = LoggerFactory.getLogger(classOf[BaseEntityProvider])

  private def entityNotFound(id: Long): ProviderError =
    ProviderError(s"Base entity with id of $id not found", ErrorCode.NOT_FOUND)

  /** Retrieve the name of the Base Entity by its unique ID.
    *
    * @param id
    *   the unique ID of the entity to retrieve
    * @return
    *   the GraphQL `LanguageBundle` object, or `None` if no entity is found
    */
  @RequiresLogin(message = "Guest (unauthenticated) users cannot get base entity name.")
  def nameById(id: Long): Option[LanguageBundle] =
    Option(baseEntityService.getNameForId(id)).map(LanguageBundle.apply)

  /** Retrieve the access control details of any Base Entity by its unique ID.
    *
    * This is entity type agnostic - the owning entity service is resolved server side from the ID -
    * and provides the ACL half of the legacy `RemoteAbstractEntityService.getReadOnlyPack`. The
    * entity half comes from the type specific `byId` query (e.g. `collection.byId`), which may be
    * selected in the same GraphQL document.
    *
    * Not-found is reported as an error rather than as `None`, because an entity with no ACLs is a
    * perfectly normal success case which returns empty lists. Callers must not read "we could not
    * find it" as "it has no access controls".
    *
    * SECURITY: being entity type agnostic, this provider cannot carry a `@SecureEntity` annotation,
    * and so - unlike the per-entity providers - it cannot use
    * `@RequiresPrivilege(EDIT_VIRTUAL_BASE)`, as the virtual privilege would have nothing to
    * resolve against. All of the ACL enforcement for this operation therefore lives in the
    * `@SecureOnReturn` of the concrete entity service's `getReadOnlyPack`, which
    * `BaseEntityService` delegates to. Do not replace that delegation with a direct, unsecured
    * lookup.
    *
    * @param id
    *   the unique ID of the entity whose access control details are to be retrieved
    * @return
    *   the GraphQL `BaseEntitySecurity` object, or a NOT_FOUND error if no such entity exists in
    *   the current institution
    */
  @RequiresLogin(message = "Guest (unauthenticated) users cannot get base entity security details.")
  def securityById(id: Long): Either[ProviderError, BaseEntitySecurity] = {
    LOGGER.debug(s"Retrieving security details for base entity by ID: $id")
    noneIfNotFound(baseEntityService.getReadOnlyPack(id))
      .flatMap(_.toScala)
      .map(BaseEntitySecurity.apply)
      .toRight(entityNotFound(id))
  }
}
