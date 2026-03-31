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
import io.github.openequella.graphql.api.views.NameValueView
import io.github.openequella.graphql.client._

/** Provides access to the openEQUELLA JavaScript API.
  */
object JavaScriptApi extends NestedQueryApi[JavaScriptQueries] {

  override protected def queryWrapper[A]
      : SelectionBuilder[JavaScriptQueries, A] => SelectionBuilder[RootQuery, A] =
    Queries.javaScript

  /** Lists the names and IDs of all JavaScript libraries.
    */
  def listLibraries(implicit
      cfg: ClientConfiguration
  ): Either[List[ApiError], List[NameValueView]] = {
    val q = JavaScriptQueries.libraries {
      NameValueView.selector
    }

    query(q)
  }

  /** Retrieves the names and IDs of all JavaScript modules for a given JavaScript library ID.
    *
    * @param libraryId
    *   The unique identifier of the JavaScript library.
    */
  def modulesByLibraryId(libraryId: String)(implicit
      cfg: ClientConfiguration
  ): Either[List[ApiError], Option[List[NameValueView]]] = {
    val q = JavaScriptQueries.modules(libraryId) {
      NameValueView.selector
    }

    query(q)
  }
}
