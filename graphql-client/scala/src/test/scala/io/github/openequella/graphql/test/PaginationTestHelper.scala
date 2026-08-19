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

package io.github.openequella.graphql.test

import io.github.openequella.graphql.api._
import org.scalatest.Assertions.fail
import org.scalatest.matchers.should.Matchers.{convertToAnyShouldWrapper, empty}

import scala.annotation.tailrec

object PaginationTestHelper {

  /** Test pagination using the given query function. Both forward and backward pagination are
    * tested in the one test. This is because they should both result in the same order of items. So
    * the result from one can be used to validate the other.
    *
    * @param pageSize
    *   the number of items to retrieve per page
    * @param totalExpectedItems
    *   the total number of items expected to be retrieved
    * @param queryFn
    *   the function to call to retrieve the next page of items
    * @tparam T
    *   the type of item to retrieve
    */
  def testPagination[T](pageSize: Int, totalExpectedItems: Int)(
      queryFn: Pagination => Either[List[ApiError], PaginationResult[T]]
  ): Unit = {
    val itemsForward  = paginateForward(pageSize) { queryFn }
    val itemsBackward = paginateBackward(pageSize) { queryFn }

    itemsBackward shouldBe itemsForward
    itemsForward.size shouldBe totalExpectedItems
    itemsBackward.size shouldBe totalExpectedItems
  }

  /** Verify that a paginated API supports both forward and backward pagination.
    */
  def assertSupportsPagination[A](
      apiCall: Pagination => Either[List[ApiError], PaginationResult[A]]
  ): Unit = {
    // Use a large enough page to count all available items before checking cursor navigation.
    val maxPageSize = 100
    // Keep test pages small so both next-page and previous-page paths are exercised.
    val pageSize = 2

    val totalItems = apiCall(ForwardPagination(maxPageSize)) match {
      case Left(errors)  => fail(s"Failed to get items: $errors")
      case Right(result) => result.items.size
    }
    testPagination(pageSize = pageSize, totalExpectedItems = totalItems)(apiCall)
  }

  /** Verify that a paginated API response has no items and no continuation token.
    */
  def assertEmptyPage[A](result: PaginationResult[A]): Unit = {
    result.items shouldBe empty
    result.continue shouldBe empty
  }

  /** Paginate through a list of items using forward pagination via the given query function.
    *
    * @param pageSize
    *   the number of items to retrieve per page
    * @param queryFn
    *   the function to call to retrieve the next page of items
    * @tparam T
    *   the type of item to retrieve
    * @return
    *   the list of items retrieved
    */
  def paginateForward[T](
      pageSize: Int = Integer.MAX_VALUE
  )(queryFn: ForwardPagination => Either[List[ApiError], PaginationResult[T]]): List[T] = {
    @tailrec
    def retrieveItems(
        pagination: ForwardPagination,
        items: List[T] = List.empty
    ): List[T] = queryFn(pagination) match {
      case Left(errors)                              => fail(s"Failed to get items: $errors")
      case Right(result) if result.continue.nonEmpty =>
        retrieveItems(result.continue.get.asInstanceOf[ForwardPagination], items ++ result.items)
      case Right(result) => items ++ result.items
    }

    retrieveItems(ForwardPagination(pageSize))
  }

  /** Paginate through a list of items using backward pagination via the given query function.
    *
    * This pretty well identical to `paginateForward`, but with the way items are appended to the
    * list reversed. This is to ensure that the order of users is the same as the forward
    * pagination.
    *
    * There is value in having these two implementations stand-alone for reference purposes.
    *
    * @param pageSize
    *   the number of items to retrieve per page
    * @param queryFn
    *   the function to call to retrieve the next page of items
    * @tparam T
    *   the type of item to retrieve
    * @return
    *   the list of items retrieved
    */
  def paginateBackward[T](
      pageSize: Int = Integer.MAX_VALUE
  )(queryFn: BackwardPagination => Either[List[ApiError], PaginationResult[T]]): List[T] = {
    @tailrec
    def retrieveItems(
        pagination: BackwardPagination,
        items: List[T] = List.empty
    ): List[T] = queryFn(pagination) match {
      case Left(errors)                              => fail(s"Failed to get items: $errors")
      case Right(result) if result.continue.nonEmpty =>
        retrieveItems(result.continue.get.asInstanceOf[BackwardPagination], result.items ++ items)
      case Right(result) => result.items ++ items
    }

    retrieveItems(BackwardPagination(pageSize))
  }
}
