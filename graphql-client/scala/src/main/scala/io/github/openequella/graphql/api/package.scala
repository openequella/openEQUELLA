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

package io.github.openequella.graphql

import caliban.client.{Operations, SelectionBuilder}

package object api {

  /** Represents a function that builds a paginated query. Taking the arguments:
    *   - limit: The maximum number of items to return.
    *   - offset: The number of items to skip.
    *   - before: The cursor of the item before the first item to return.
    *   - after: The cursor of the item after the last item to return.
    *
    * @tparam R
    *   The type of the items in the connection - i.e. the type of entity view being retrieved in
    *   pages.
    */
  final type PaginatedQueryBuilder[R] = (
      Option[Int],
      Option[Int],
      Option[String],
      Option[String]
  ) => PaginatedQuery[R]

  /** Represents a paginated query. This is a selection builder that returns a connection view.
    *
    * @tparam R
    *   The type of the items in the connection - i.e. the type of entity view being retrieved in
    *   pages.
    */
  final type PaginatedQuery[R] = SelectionBuilder[Operations.RootQuery, Option[ConnectionView[R]]]

  /** Flattens the result of an operation that returns an `Either[List[ApiError], Option[A]]` to an
    * `Either[List[ApiError], A]`. This is needed because Caliban returns an `Option[A]` for
    * operations that return an effect representing any operation which can fail.
    *
    * See more at:
    * <https://ghostdogpr.github.io/caliban/faq/#the-auto-generated-schema-shows-a-field-is-nullable-but-i-want-it-non-nullable-instead>
    */
  def flattenResult[A](result: Either[List[ApiError], Option[A]]): Either[List[ApiError], A] =
    result.flatMap {
      case Some(a) => Right(a)
      case None => Left(List(UnknownError("Although operation successful, no data was returned.")))
    }

  /** Execute a paginated query with the given pagination.
    *
    * @param pagination
    *   the pagination to use - i.e. the limit and cursor.
    * @param queryBuilder
    *   the query builder to use to build the query with the given pagination.
    * @param cfg
    *   the `Client` configuration.
    * @tparam A
    *   the type of the items that are being retrieved in pages.
    * @return
    *   the result of the paginated query. (i.e the page based on the supplied pagination)
    */
  def queryWithPagination[A](
      pagination: Pagination
  )(
      queryBuilder: PaginatedQueryBuilder[A]
  )(implicit cfg: ClientConfiguration): Either[List[ApiError], PaginationResult[A]] = {
    val q = withPagination[A](pagination)(queryBuilder)
    withPaginationResult[A](pagination) {
      Client.query(q)(cfg)
    }
  }

  /** Call the supplied query builder with the given pagination, so that the query is built with the
    * pagination correct pagination parameters based on whether Forward or Backward pagination is
    * being used.
    *
    * @param pagination
    *   the pagination details to use
    * @param queryBuilder
    *   a function to build a query with based on the provided pagination
    * @tparam R
    *   the type of the items in the connection
    * @return
    *   the paginated query which can be used with `Client.query`
    */
  private def withPagination[R](pagination: Pagination)(
      queryBuilder: PaginatedQueryBuilder[R]
  ): PaginatedQuery[R] =
    pagination match {
      case ForwardPagination(limit, after) =>
        queryBuilder(Some(limit), None, None, after)
      case BackwardPagination(limit, before) =>
        queryBuilder(None, Some(limit), before, None)
    }

  /** Convert the result of a paginated query into a `PaginationResult` which can be used to
    * continue the query, as well as extracting the items for ease of use.
    * @param pagination
    *   the pagination details used in the query - these need to match to ensure the correct
    *   continuation information is returned.
    * @param response
    *   the response from `Client.query` which should contain the connection view.
    * @tparam A
    *   the type of the items in the connection
    * @return
    *   the result of the paginated query as a `PaginationResult`
    */
  private def withPaginationResult[A](pagination: Pagination)(
      response: => Either[List[ApiError], Option[ConnectionView[A]]]
  ): Either[List[ApiError], PaginationResult[A]] =
    response match {
      case Right(result) =>
        result match {
          case Some(ConnectionView(pageInfo, edges)) =>
            val items    = edges.map(_.node)
            val continue = ContinuationPagination(pagination, pageInfo)

            Right(PaginationResult(items, continue))
          case None => Right(PaginationResult.none)

        }
      case Left(errors) => Left(errors)
    }
}
