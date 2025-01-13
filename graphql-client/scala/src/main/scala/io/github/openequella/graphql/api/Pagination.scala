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

/** Represents the Page Info part of Connection (pagination) GraphQL responses.
  *
  * @param hasNextPage
  *   Whether there is a next page.
  * @param hasPreviousPage
  *   Whether there is a previous page.
  * @param startCursor
  *   The cursor of the first item in the page.
  * @param endCursor
  *   The cursor of the last item in the page.
  */
final case class PageInfoView(
    hasNextPage: Boolean,
    hasPreviousPage: Boolean,
    startCursor: Option[String],
    endCursor: Option[String]
)

/** Represents a node with a cursor in a Connection (pagination) GraphQL response.
  *
  * @param cursor
  *   The cursor of the node.
  * @param node
  *   The node. (i.e. the item)
  */
final case class NodeWithCursorView[T](cursor: String, node: T)

/** Represents a Connection (pagination) GraphQL response.
  *
  * @param pageInfo
  *   The pagination information.
  * @param edges
  *   The nodes with their cursors.
  */
final case class ConnectionView[T](pageInfo: PageInfoView, edges: List[NodeWithCursorView[T]])

/** The pagination information used in a GraphQL query
  */
sealed abstract class Pagination {
  val limit: Int
}

/** Represents forward pagination.
  *
  * @param limit
  *   The maximum number of items to return.
  * @param after
  *   The cursor to start after, or None to start at the beginning.
  */
final case class ForwardPagination(limit: Int, after: Option[String] = None) extends Pagination

/** Represents backward pagination.
  *
  * @param limit
  *   The maximum number of items to return.
  * @param before
  *   The cursor to start before, or None to start at the end.
  */
final case class BackwardPagination(limit: Int, before: Option[String] = None) extends Pagination

/** Represents the result of a paginated query.
  *
  * @param items
  *   The items returned by the query.
  * @param continue
  *   The pagination information to continue the query, or None if there are no more items.
  */
final case class PaginationResult[T](items: List[T], continue: Option[Pagination])

/** Helper object to determine the pagination information to continue a query.
  */
object ContinuationPagination {

  /** Determines the pagination information to continue a query.
    *
    * @param requestPagination
    *   The original pagination request.
    * @param resultPageInfo
    *   The pagination information returned by the query.
    * @return
    *   The pagination information to continue the query, or None if there are no more items.
    */
  def apply(requestPagination: Pagination, resultPageInfo: PageInfoView): Option[Pagination] =
    requestPagination match {
      case ForwardPagination(limit, _) =>
        Option.when(resultPageInfo.hasNextPage)(
          ForwardPagination(limit = limit, after = resultPageInfo.endCursor)
        )
      case BackwardPagination(limit, _) =>
        Option.when(resultPageInfo.hasPreviousPage)(
          BackwardPagination(limit = limit, before = resultPageInfo.startCursor)
        )
    }
}
