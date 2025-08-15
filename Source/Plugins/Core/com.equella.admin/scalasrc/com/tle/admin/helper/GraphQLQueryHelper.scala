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

package com.tle.admin.helper

import com.tle.admin.service.ClientRequestException
import com.tle.common.beans.exception.NotFoundException
import io.github.openequella.graphql.api.{ApiError, ForwardPagination, PaginationResult}
import org.slf4j.Logger

import scala.annotation.tailrec

/** Helper object for common GraphQL query operations.
  */
object GraphQLQueryHelper {

  /** A sensible page size for GraphQL queries.
    */
  var PAGE_SIZE = 100

  /** Get an entity by its identifier, logging the result and throwing an exception if there are
    * errors.
    *
    * @param label
    *   the label for the entity type - useful for logging
    * @param identifier
    *   the identifier of the entity to use with the getter
    * @param getter
    *   the function to get the entity by its identifier
    * @param logger
    *   the logger to use for logging
    * @tparam A
    *   the type of the identifier
    * @tparam E
    *   the type of the entity optionally returned by the getter
    * @return
    *   the entity if it exists, or `None` if it does not
    * @throws ClientRequestException
    *   if there are errors getting the entity - i.e. if the getter returns `Left`
    */
  def getEntity[A, E](label: String, identifier: A, getter: A => Either[List[ApiError], Option[E]])(
      implicit logger: Logger
  ): Option[E] = getter(identifier) match {
    case Right(Some(entity)) =>
      logger.debug("Successfully retrieved {}: {}", label, identifier)
      Some(entity)
    case Right(None) =>
      logger.debug("No {} found for identifier: {}", label, identifier)
      None
    case Left(errors) =>
      throw new ClientRequestException(s"Error getting $label: $identifier", errors)
  }

  /** Get an entity by its identifier and throwing a NotFoundException if it does not exist. This is
    * an alternative to `getEntity` that is useful when providing implementations to match
    * `com.tle.core.entity.service.impl.AbstractEntityServiceImpl#get(long)`.
    *
    * @param label
    *   the label for the entity type - useful for logging
    * @param identifier
    *   the identifier of the entity to use with the getter
    * @param getter
    *   the function to get the entity by its identifier
    * @param logger
    *   the logger to use for logging
    * @tparam A
    *   the type of the identifier
    * @tparam E
    *   the type of the entity optionally returned by the getter
    * @return
    *   the entity if it exists, or throws NotFoundException if it does not exist
    * @throws NotFoundException
    *   if the entity does not exist for the given identifier
    * @throws ClientRequestException
    *   if there are errors getting the entity - i.e. if the getter returns `Left`
    */
  def getEntityOrNotFound[A, E](
      label: String,
      identifier: A,
      getter: A => Either[List[ApiError], Option[E]]
  )(implicit
      logger: Logger
  ): E =
    getEntity(label, identifier, getter).getOrElse {
      throw new NotFoundException(s"$label not found for identifier: $identifier")
    }

  /** Get all items from a paginated query, logging the result and throwing an exception if there
    * are errors.
    *
    * @param pageSize
    *   the page size to use for each paginated query
    * @param queryFn
    *   a query which returns a paginated results, and can be called to get further pages
    * @tparam T
    *   the type of item to retrieve
    * @return
    *   the list of items retrieved
    * @throws ClientRequestException
    *   if there are errors getting the items
    */
  def getAll[T](
      pageSize: Int = PAGE_SIZE
  )(queryFn: ForwardPagination => Either[List[ApiError], PaginationResult[T]]): List[T] = {
    @tailrec
    def retrieveItems(
        pagination: ForwardPagination,
        items: List[T] = List.empty
    ): List[T] = queryFn(pagination) match {
      case Left(errors) => throw new ClientRequestException("Failed to get items", errors)
      case Right(result) if result.continue.nonEmpty =>
        retrieveItems(result.continue.get.asInstanceOf[ForwardPagination], items ++ result.items)
      case Right(result) => items ++ result.items
    }

    retrieveItems(ForwardPagination(pageSize))
  }

  /** Get all items for a given identifier without pagination, logging the result and throwing an
    * exception if there are errors.
    *
    * @param label
    *   the label for the entity type - useful for logging
    * @param identifier
    *   the identifier of the entity to use with the getter
    * @param getter
    *   the function to get all items by their identifier
    * @tparam A
    *   the type of the identifier
    * @tparam E
    *   the type of the entity returned by the getter
    * @return
    *   a list of items retrieved for the given identifier
    * @throws ClientRequestException
    *   if there are errors getting the items - i.e. if the getter returns `Left`
    */
  def getAllUnpaginated[A, E](
      label: String,
      identifier: A,
      getter: A => Either[List[ApiError], List[E]]
  )(implicit
      logger: Logger
  ): List[E] =
    getter(identifier) match {
      case Right(items) =>
        logger.debug("Successfully retrieved all {} for identifier: {}", label, identifier)
        items
      case Left(errors) =>
        throw new ClientRequestException(
          s"Error getting all $label for identifier: $identifier",
          errors
        )
    }
}
