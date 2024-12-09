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

package com.tle.web.remoting.graphql

import caliban.CalibanError.ExecutionError
import caliban.relay.{Base64Cursor, Pagination, PaginationCount, PaginationCursor}
import com.tle.web.remoting.graphql.provider.ProviderError
import zio.{IO, ZIO}

package object schema {

  /** A common return type for Providers to enable error handling in GraphQL requests.
    * @tparam T
    *   the success result from a Provider
    */
  type ResultWithErrors[T] = IO[ExecutionError, T]

  /** Implicitly convert an `Either[ProviderError, T]` into a `ResultWithErrors[T]` to reduce the
    * repetition of error handling for Providers.
    *
    * @param eitherResult
    *   the result to convert - typically from a call to a Provider
    * @tparam T
    *   the success result from a Provider
    * @return
    *   a `ResultWithErrors[T]` that can be used in a GraphQL requests
    */
  implicit def errorHandler[T](eitherResult: Either[ProviderError, T]): ResultWithErrors[T] =
    eitherResult match {
      case Left(ProviderError(executionError: ExecutionError)) => ZIO.fail(executionError)
      case Right(data)                                         => ZIO.succeed(data)
    }

  /** Convert a `Pagination` object into an offset and limit pair for use in a database query, such
    * that the result will match [GraphQL Cursor Connections Specification - 4.4 Pagination
    * Algorithm](https://relay.dev/graphql/connections.htm#sec-Pagination-algorithm).
    *
    * Caliban provides a single implementation, but it only supports a `List` of entities. This
    * implementation is focused on use with traditional offset/max style DB access. The caliban
    * example can be found at `caliban.relay.Connection#fromList`.
    *
    * @param pagination
    *   the pagination object
    * @param max
    *   the maximum number of items in the database
    * @return
    *   a tuple of `(offset, limit)` where `limit` will be capped based on max (and if zero, the DB
    *   query should not be made)
    * @see
    *   [[caliban.relay.Connection#fromList]]
    */
  def paginationOffsetLimit(pagination: Pagination[Base64Cursor], max: Int): (Int, Int) = {
    val offset = pagination.cursor match {
      case PaginationCursor.NoCursor =>
        pagination.count match {
          case PaginationCount.First(_)    => 0
          case PaginationCount.Last(count) => math.max(0, max - count)
        }
      case PaginationCursor.After(cursor) =>
        val minOffset = math.min(cursor.value + 1, max)
        pagination.count match {
          case PaginationCount.First(_)    => minOffset
          case PaginationCount.Last(count) => math.max(minOffset, max - count)
        }
      case PaginationCursor.Before(cursor) =>
        pagination.count match {
          case PaginationCount.First(_)    => 0
          case PaginationCount.Last(count) => math.max(0, cursor.value - count)
        }
    }

    val rawLimit = pagination.count.count
    val cappedLimit = pagination.cursor match {
      // For after cursors, we can't go past the end of the list
      case PaginationCursor.After(_) => math.min(rawLimit, max - offset)
      // For before cursors, we have to make sure not to also include the cursor item in the
      // returned list - so the limit should be less than it.
      case PaginationCursor.Before(cursor) =>
        val cursorIndex = cursor.value
        math.min(rawLimit, cursorIndex)
      // For non-cursor based limits, make sure to limit the number of items returned to those
      // available in the database
      case PaginationCursor.NoCursor => math.min(rawLimit, max)
    }

    (offset, cappedLimit)
  }
}
