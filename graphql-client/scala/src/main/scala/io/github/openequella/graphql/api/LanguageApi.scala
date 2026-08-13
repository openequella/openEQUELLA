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
import io.github.openequella.graphql.api.views.{LanguageBundleNameView, LanguageView}
import io.github.openequella.graphql.client._

/** Provides access to the openEQUELLA language API.
  */
object LanguageApi extends NestedQueryApi[LanguageQueries] {

  override protected def queryWrapper[A]
      : SelectionBuilder[LanguageQueries, A] => SelectionBuilder[RootQuery, A] =
    Queries.language

  /** Lists all configured languages.
    */
  def listLanguages(implicit
      cfg: ClientConfiguration
  ): Either[List[ApiError], List[LanguageView]] = {
    val q = LanguageQueries.list {
      LanguageView.selector
    }

    query(q)
  }

  /** Resolves display names for the provided language bundle IDs.
    *
    * @param ids
    *   A list of language bundle IDs for which to retrieve display names.
    */
  def namesByBundleIds(ids: List[Long])(implicit
      cfg: ClientConfiguration
  ): Either[List[ApiError], List[LanguageBundleNameView]] = {
    val q = LanguageQueries.names(ids) {
      LanguageBundleNameView.selector
    }

    query(q)
  }
}
