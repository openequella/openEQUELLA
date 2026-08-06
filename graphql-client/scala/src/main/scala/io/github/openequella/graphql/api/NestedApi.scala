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

object NestedApi {

  /** Represents a function that builds a nested paginated query. Taking the arguments:
    *   - first: The maximum number of items to return when paginating forward.
    *   - last: The maximum number of items to return when paginating backward.
    *   - before: The cursor of the item before the first item to return.
    *   - after: The cursor of the item after the last item to return.
    *
    * @tparam Q
    *   The nested query type.
    * @tparam R
    *   The type of the items in the connection - i.e. the type of entity view being retrieved in
    *   pages.
    */
  final type PaginatedQueryBuilder[Q, R] = (
      Option[Int],
      Option[Int],
      Option[String],
      Option[String]
  ) => PaginatedQuery[Q, R]

  /** Represents a nested paginated query. This is a selection builder that returns a connection
    * view.
    *
    * @tparam Q
    *   The nested query type.
    * @tparam R
    *   The type of the items in the connection - i.e. the type of entity view being retrieved in
    *   pages.
    */
  final type PaginatedQuery[Q, R] = SelectionBuilder[
    Q,
    Option[ConnectionView[R]]
  ]
}

/** Mixin trait for APIs that have nested query structures.
  *
  * @tparam Q
  *   The root query type for the nested API.
  */
trait NestedQueryApi[Q] {

  /** Wrapper for query calls to adapt to the nested query API structure.
    */
  protected def queryWrapper[A]
      : SelectionBuilder[Q, A] => SelectionBuilder[_root_.caliban.client.Operations.RootQuery, A]

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

  /** Executes a query whose result is wrapped in `Option` and flattens it, treating `None` as an
    * error.
    *
    * This is a convenience wrapper for `flattenResult(query(selection))`.
    *
    * Use this only when a missing value is exceptional for the caller. Queries where `None`
    * represents a normal "not found" result should continue to use `query` directly.
    *
    * @param selection
    *   The selection builder for the query, returning `Option[A]`.
    * @param cfg
    *   The client configuration.
    * @tparam A
    *   The unwrapped result type.
    * @return
    *   Either a list of ApiError or the unwrapped result of type A.
    */
  protected def flatQuery[A](selection: SelectionBuilder[Q, Option[A]])(implicit
      cfg: ClientConfiguration
  ): Either[List[ApiError], A] =
    flattenResult(query(selection))

  /** Handles paginated query calls for nested query API structure. This method automatically wraps
    * the query builder with the `queryWrapper` before executing the paginated query, eliminating
    * the need to manually call `queryWrapper` in each paginated query method.
    *
    * @param pagination
    *   Detail the number of items to return, and whether to page through forward or backwards.
    * @param nestedQueryBuilder
    *   A function that builds a nested query with pagination parameters (first, last, before,
    *   after) and returns a SelectionBuilder for the nested query type Q.
    * @param cfg
    *   The client configuration.
    * @tparam R
    *   The type of the items in the connection.
    * @return
    *   Either a list of ApiError or the PaginationResult containing the items and pagination
    *   information.
    */
  protected def queryPaginated[R](pagination: Pagination)(
      nestedQueryBuilder: NestedApi.PaginatedQueryBuilder[Q, R]
  )(implicit cfg: ClientConfiguration): Either[List[ApiError], PaginationResult[R]] =
    queryWithPagination(pagination) { (first, last, before, after) =>
      queryWrapper(nestedQueryBuilder(first, last, before, after))
    }
}

/** Mixin trait for APIs that have nested mutation structures.
  *
  * @tparam M
  *   The root mutation type for the nested API.
  */
trait NestedMutationApi[M] {

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

  /** Executes a mutation whose result is wrapped in `Option` and flattens it, treating `None` as an
    * error. This is the standard pattern for Caliban mutations, which generate `Option`-wrapped
    * return types due to GraphQL's nullable-by-default semantics.
    *
    * This is a convenience wrapper for `flattenResult(mutate(mutation))`.
    *
    * @param mutation
    *   The selection builder for the mutation, returning `Option[A]`.
    * @param cfg
    *   The client configuration.
    * @tparam A
    *   The unwrapped result type.
    * @return
    *   Either a list of ApiError or the unwrapped result of type A.
    */
  protected def flatMutate[A](mutation: SelectionBuilder[M, Option[A]])(implicit
      cfg: ClientConfiguration
  ): Either[List[ApiError], A] =
    flattenResult(mutate(mutation))
}

/** Mixin trait for APIs that have nested query and mutation structures.
  *
  * This trait combines both query and mutation capabilities by extending NestedQueryApi and
  * NestedMutationApi. Use this trait when your API requires both operations. For APIs that only
  * need one type of operation, extend NestedQueryApi or NestedMutationApi directly.
  *
  * @tparam Q
  *   The root query type for the nested API.
  * @tparam M
  *   The root mutation type for the nested API.
  */
trait NestedApi[Q, M] extends NestedQueryApi[Q] with NestedMutationApi[M]
