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

package io.github.openequella.graphql.api

import caliban.client.Operations.RootQuery
import caliban.client.SelectionBuilder
import io.github.openequella.graphql.ClientConfiguration
import io.github.openequella.graphql.api.views.{
  BaseEntitySecurityView,
  LanguageBundleView,
  LanguageStringView
}
import io.github.openequella.graphql.client._

/** Provides access to the openEQUELLA base entity API.
  */
object BaseEntityApi extends NestedQueryApi[BaseEntityQueries] {

  override protected def queryWrapper[A]
      : SelectionBuilder[BaseEntityQueries, A] => SelectionBuilder[RootQuery, A] =
    Queries.baseEntities

  /** Retrieves the name of a Base Entity by its unique identifier.
    *
    * @param id
    *   The unique identifier of the base entity.
    * @param cfg
    *   The client configuration.
    * @return
    *   Left containing a list of errors or Right if the operation was successful. Some if name was
    *   found, None if not.
    */
  def getNameById(id: Long)(implicit
      cfg: ClientConfiguration
  ): Either[List[ApiError], Option[LanguageBundleView]] = {
    val q = BaseEntityQueries.nameById(id) {
      (LanguageBundle.id ~ LanguageBundle.strings(LanguageStringView.selector))
        .mapN(LanguageBundleView.apply _)
    }

    query(q)
  }

  /** Retrieves the access control details of any Base Entity by its unique identifier.
    *
    * This is entity type agnostic - the owning entity service is resolved server side from the ID -
    * so it works for collections, schemas, workflows and any other base entity. The entity itself
    * comes from its type specific query (e.g. `CollectionDefinitionApi.getById`).
    *
    * An entity with no access control entries is a success carrying two empty lists; only an
    * unknown ID produces a `NotFoundError`. Callers must not read a failure as "this entity has no
    * access controls".
    *
    * @param id
    *   The unique identifier of the base entity.
    * @param cfg
    *   The client configuration.
    * @return
    *   Left containing a list of errors - including a `NotFoundError` if there is no such entity -
    *   or Right with the entity's access control details.
    * @see
    *   [[getNameById]]
    */
  def getSecurityById(id: Long)(implicit
      cfg: ClientConfiguration
  ): Either[List[ApiError], BaseEntitySecurityView] = {
    val q = BaseEntityQueries.securityById(id) {
      BaseEntitySecurityView.selector
    }

    flatQuery(q)
  }
}
