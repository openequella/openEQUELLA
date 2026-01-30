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

import caliban.client.SelectionBuilder
import io.github.openequella.graphql.{Client, ClientConfiguration}

/** Mixin trait for APIs that have nested query and mutation structures.
  *
  * @tparam Q
  *   The root query type for the nested API.
  * @tparam M
  *   The root mutation type for the nested API.
  */
trait NestedApi[Q, M] {

  /** Wrapper for query calls to adapt to the nested query API structure.
    */
  protected def queryWrapper[A]
      : SelectionBuilder[Q, A] => SelectionBuilder[_root_.caliban.client.Operations.RootQuery, A]

  /** Wrapper for mutation calls to adapt to the nested mutation API structure.
    */
  protected def mutationWrapper[A]
      : SelectionBuilder[M, A] => SelectionBuilder[_root_.caliban.client.Operations.RootMutation, A]

  /** Handles the mutations calls for nested mutation API structure.
    *
    * @param mutation
    *   The selection builder for the mutation.
    * @param cfg
    *   The client configuration.
    * @tparam R
    *   The result type of the mutation.
    * @return
    *   Either a list of ApiError or the result of type R.
    */
  protected def mutate[R](mutation: SelectionBuilder[M, R])(implicit
      cfg: ClientConfiguration
  ): Either[List[ApiError], R] =
    Client.mutate(mutationWrapper(mutation))

  /** Handles the query calls for nested query API structure.
    *
    * @param query
    *   The selection builder for the query.
    * @param cfg
    *   The client configuration.
    * @tparam R
    *   The result type of the query.
    * @return
    *   Either a list of ApiError or the result of type R.
    */
  protected def query[R](query: SelectionBuilder[Q, R])(implicit
      cfg: ClientConfiguration
  ): Either[List[ApiError], R] =
    Client.query(queryWrapper(query))
}
