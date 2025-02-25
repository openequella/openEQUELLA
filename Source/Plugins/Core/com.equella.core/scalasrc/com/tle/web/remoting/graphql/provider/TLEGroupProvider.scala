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

package com.tle.web.remoting.graphql.provider

import caliban.relay.{Base64Cursor, Pagination}
import com.tle.beans.user.TLEGroup
import com.tle.common.security.SecurityConstants
import com.tle.core.guice.Bind
import com.tle.core.security.impl.RequiresPrivilege
import com.tle.core.usermanagement.standard.service.TLEGroupService
import com.tle.web.remoting.graphql.ErrorCode
import com.tle.web.remoting.graphql.schema.{
  Group,
  GroupConnection,
  Page,
  StringConnection,
  paginationOffsetLimit
}
import org.springframework.transaction.annotation.Transactional

import javax.inject.{Inject, Singleton}
import scala.jdk.CollectionConverters._
import scala.language.implicitConversions
import scala.util.{Failure, Success, Try}

/** A Provider for operations involving TLE Group entities. Ultimately proxied to the
  * `TLEGroupService`.Although some service methods have appropriate access control, this provider
  * enforces the `EDIT_USER_MANAGEMENT` privilege on most operations. This is done even for
  * operations using service methods which already enforce the privilege, to ensure that the
  * privilege is always checked.
  */
@Bind
@Singleton
class TLEGroupProvider @Inject() (tleGroupService: TLEGroupService) {

  /** Retrieval of TLEGroup objects often result in `null` values, so this helper `implicit`
    * conversion is used to convert `null` to `None`.
    */
  private implicit def optionalGroup(g: TLEGroup): Option[Group] = Option(g).map(toGroup)

  /** Convert a `TLEGroup` to a `Group`. Done as a method rather than a Group companion object due
    * to the need for the `tleGroupService` to check for children and users.
    *
    * @param g
    *   the `TLEGroup` to convert
    * @return
    *   the `Group` representation
    */
  private def toGroup(g: TLEGroup): Group = {
    Group(
      uniqueId = g.getUuid,
      name = g.getName,
      parentId = Option(g.getParent).map(_.getUuid),
      hasGroups = tleGroupService.countGroupsInGroupById(g.getUuid) > 0,
      hasUsers = tleGroupService.countUsersInGroup(g.getUuid) > 0
    )
  }

  /** List groups in the system within the specified (via `parentId`) group for the provided
    * `pagination`.
    *
    * @param parentId
    *   the unique ID of the parent group to list groups within. If `None`, the root groups are
    *   listed.
    * @param pagination
    *   the pagination object to use for the query
    * @return
    *   a list of `Group` objects within the specified group (contained in a `GroupConnection`, but
    *   if the specified group doesn't exist, a `Left` containing a `ProviderError` is returned.
    */
  @RequiresPrivilege(priv = SecurityConstants.EDIT_USER_MANAGEMENT)
  def listGroups(
      parentId: Option[String],
      pagination: Pagination[Base64Cursor]
  ): Either[ProviderError, GroupConnection] = {
    def getSubGroups(
        group: Option[TLEGroup],
        limit: Int,
        offset: Int
    ): Either[ProviderError, List[Group]] = Try(
      tleGroupService.getGroupsInGroup(group.orNull, limit, offset)
    ) match {
      case Failure(exception) =>
        Left(ProviderError("Failed to retrieve subgroups", exception))
      case Success(groups) =>
        Right(groups.asScala.map(toGroup).toList)
    }

    for {
      parent <- parentId match {
        case Some(id) => getParentGroup(id).map(Some)
        case None     => Right(None)
      }
      groupCount      = tleGroupService.countGroupsInGroup(parent.orNull).toInt
      (offset, limit) = paginationOffsetLimit(pagination, groupCount)
      groups <- getSubGroups(parent, limit, offset)
      page = Page(groups, groupCount, offset, limit)
    } yield GroupConnection(page)
  }

  /** Search for groups by name using a wildcard query.
    *
    * @param query
    *   the query string to search for groups by
    * @return
    *   a list of `Group` objects matching the query
    */
  @RequiresPrivilege(priv = SecurityConstants.EDIT_USER_MANAGEMENT)
  def searchGroups(query: String): List[Group] = {
    val wildcardQuery = tleGroupService.prepareQuery(query)
    val searchResult  = tleGroupService.search(wildcardQuery)

    searchResult.asScala.map(toGroup).toList
  }

  /** Retrieve a group by its unique ID.
    *
    * @param uniqueId
    *   the unique ID of the group to retrieve
    * @return
    *   the `Group` object with the specified unique ID, or `None` if no group is found
    */
  @RequiresPrivilege(priv = SecurityConstants.EDIT_USER_MANAGEMENT)
  def groupById(uniqueId: String): Option[Group] =
    tleGroupService.get(uniqueId)

  /** Retrieve a group by its name.
    *
    * @param name
    *   the name of the group to retrieve
    * @return
    *   the `Group` object with the specified name, or `None` if no group is found
    */
  @RequiresPrivilege(priv = SecurityConstants.EDIT_USER_MANAGEMENT)
  def groupByName(name: String): Option[Group] =
    tleGroupService.getByName(name)

  /** List user IDs for all users in the specified group.
    *
    * @param uniqueId
    *   the unique ID of the group to list users for
    * @return
    *   a list of user IDs for all users in the specified group - note that these are not limited to
    *   TLE Users and may include other user types (LDAP, LTI, etc).
    */
  @RequiresPrivilege(priv = SecurityConstants.EDIT_USER_MANAGEMENT)
  def listGroupUsers(uniqueId: String, pagination: Pagination[Base64Cursor]): StringConnection = {
    val userCount       = tleGroupService.countUsersInGroup(uniqueId).toInt
    val (offset, limit) = paginationOffsetLimit(pagination, userCount)

    val users = tleGroupService.getUsersInGroup(uniqueId, false, limit, offset).asScala.toList

    StringConnection(Page(users, userCount, offset, limit))
  }

  /** Create a new group with the specified name and parent group.
    *
    * @param name
    *   the name of the new group
    * @param parentId
    *   the unique ID of the parent group for the new group, or `None` if the new group is a root
    *   group
    * @return
    *   the newly created `Group` object, or a `Left` containing a `ProviderError` if the group
    *   could not be created
    */
  @RequiresPrivilege(priv = SecurityConstants.EDIT_USER_MANAGEMENT)
  def createGroup(name: String, parentId: Option[String]): Either[ProviderError, Group] = {
    val newGroupId = tleGroupService.add(parentId.orNull, name)
    Option(tleGroupService.get(newGroupId))
      .map(toGroup)
      .toRight(
        ProviderError(
          "Failed to retrieve newly created group with id: " + newGroupId,
          ErrorCode.NOT_FOUND
        )
      )
  }

  /** Delete a group by its unique ID.
    *
    * @param uniqueId
    *   the unique ID of the group to delete
    * @param deleteChildren
    *   if `true` all child groups and users will also be deleted, otherwise they will be kept but
    *   moved to the parent group
    * @return
    *   `Unit` if the group was deleted successfully, or a `Left` containing a `ProviderError` if
    *   the group could not be deleted
    */
  @RequiresPrivilege(priv = SecurityConstants.EDIT_USER_MANAGEMENT)
  def deleteGroup(uniqueId: String, deleteChildren: Boolean): Either[ProviderError, Unit] =
    ProviderError.Try("Failed to delete group: ") {
      tleGroupService.delete(uniqueId, deleteChildren)
    }

  /** Update a group with the provided details - any which are `Some`.
    *
    * @param uniqueId
    *   the unique ID of the group to update
    * @param name
    *   the new name of the group
    * @param parentId
    *   the new parent ID of the group - used to move the group within the hierarchy
    * @return
    *   the updated group, or a `Left` containing a `ProviderError` if the group could not be
    *   updated
    */
  @RequiresPrivilege(priv = SecurityConstants.EDIT_USER_MANAGEMENT)
  @Transactional
  def updateGroup(
      uniqueId: String,
      name: Option[String],
      parentId: Option[String],
      users: Option[Set[String]]
  ): Either[ProviderError, Group] = {
    def updateParent(g: TLEGroup, parentId: Option[String]) = parentId match {
      case Some(pid) =>
        getParentGroup(pid).map(parent => {
          g.setParent(parent)
          g
        })
      case None => Right(g)
    }

    Option(tleGroupService.get(uniqueId))
      .toRight(ProviderError(s"Group with id of $uniqueId not found", ErrorCode.NOT_FOUND))
      .flatMap(updateParent(_, parentId))
      .flatMap { g =>
        name.foreach(g.setName)
        users.map(_.asJava).foreach(g.setUsers)

        ProviderError.Try("Failed to update group: ") {
          val uuid = tleGroupService.edit(g)
          toGroup(tleGroupService.get(uuid))
        }
      }
  }

  private def getParentGroup(id: String): Either[ProviderError, TLEGroup] = Try(
    tleGroupService.get(id)
  ) match {
    case Failure(exception) =>
      Left(ProviderError("Failed to retrieve parent group with id: " + id, exception))
    case Success(group) =>
      Option(group).toRight(
        ProviderError("Parent group not found with id: " + id, ErrorCode.NOT_FOUND)
      )
  }
}
