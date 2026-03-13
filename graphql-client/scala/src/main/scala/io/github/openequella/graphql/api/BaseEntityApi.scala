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
import io.github.openequella.graphql.api.views.{LanguageBundleView, LanguageStringView}
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
}
