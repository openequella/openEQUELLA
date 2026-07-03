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

package com.tle.admin.service

import com.tle.admin.graphql.conversion.UserDirectoryViewConverter.{
  toGroupBean,
  toRoleBean,
  toUserBean
}
import com.tle.admin.graphql.conversion.{
  ArrayListConverter,
  Converter,
  asArrayList,
  asHashMap,
  asOptional
}
import com.tle.admin.helper.GraphQLQueryHelper.{
  getAll,
  getAllUnpaginated,
  getEntityOrNoneOnNotFound,
  getOptionalEntityOrNoneOnNotFound
}
import com.tle.common.usermanagement.user.valuebean.{GroupBean, RoleBean, UserBean}
import io.github.openequella.graphql.ClientConfiguration
import io.github.openequella.graphql.api.UserDirectoryApi
import org.slf4j.{Logger, LoggerFactory}

import java.util
import java.util.Optional
import javax.inject.{Inject, Singleton}
import scala.jdk.CollectionConverters._

/** GraphQL-backed implementation of the user directory lookups for the Admin Console.
  */
@Singleton
class AdminUserDirectoryServiceImpl @Inject() (implicit
    val cfg: ClientConfiguration
) extends AdminUserDirectoryService {

  private implicit val LOGGER: Logger =
    LoggerFactory.getLogger(classOf[AdminUserDirectoryServiceImpl])

  override def getInformationForUser(userId: String): Optional[UserBean] = {
    LOGGER.debug("Retrieving user directory user by ID: {}", userId)
    getEntityOrNoneOnNotFound(
      "user directory user [by ID]",
      userId,
      UserDirectoryApi.userById
    ) convert asOptional(toUserBean)
  }

  override def getInformationForUsers(
      userIds: util.Collection[String]
  ): util.Map[String, UserBean] = {
    LOGGER.debug("Retrieving user directory users by IDs: {}", userIds)
    getAllUnpaginated(
      "user directory users [by IDs]",
      userIds.asScala.toList,
      UserDirectoryApi.usersByIds
    ) convert asHashMap(record => record.id -> toUserBean(record.user))
  }

  override def getRolesForUser(userId: String): util.List[RoleBean] = {
    LOGGER.debug("Retrieving user directory roles for user: {}", userId)
    getAllUnpaginated(
      "user directory roles for user",
      userId,
      UserDirectoryApi.rolesForUser
    ) convert asArrayList(toRoleBean)
  }

  override def getGroupIdsContainingUser(userId: String): util.List[String] = {
    LOGGER.debug("Retrieving user directory group IDs containing user: {}", userId)
    getAllUnpaginated(
      "user directory group IDs containing user",
      userId,
      UserDirectoryApi.groupIdsForUser
    ).asArrayList
  }

  override def getGroupsContainingUser(userId: String): util.List[GroupBean] = {
    LOGGER.debug("Retrieving user directory groups containing user: {}", userId)
    getAllUnpaginated(
      "user directory groups containing user",
      userId,
      UserDirectoryApi.groupsForUser
    ) convert asArrayList(toGroupBean)
  }

  override def getUsersInGroup(groupId: String, recursive: Boolean): util.List[UserBean] = {
    LOGGER.debug("Retrieving user directory users in group: {}, recursive: {}", groupId, recursive)
    val users =
      if (recursive) getAll("user directory users in group recursively") {
        UserDirectoryApi.usersInGroupRecursively(_, groupId)
      }
      else
        getAll("user directory users in group") {
          UserDirectoryApi.usersInGroup(_, groupId)
        }
    users convert asArrayList(toUserBean)
  }

  override def searchUsers(query: String): util.List[UserBean] = {
    LOGGER.debug("Searching user directory users with query: {}", query)
    getAll("user directory users matching query") {
      UserDirectoryApi.searchUsers(_, query)
    } convert asArrayList(toUserBean)
  }

  override def searchUsersInGroup(
      query: String,
      parentGroupId: String,
      recurse: Boolean
  ): util.List[UserBean] = {
    LOGGER.debug(
      "Searching user directory users in group {} with query: {}, recurse: {}",
      parentGroupId,
      query,
      recurse
    )
    val users =
      if (recurse) {
        getAll("user directory users in group matching query recursively") {
          UserDirectoryApi.searchUsersInGroupRecursively(_, query, parentGroupId)
        }
      } else
        getAll("user directory users in group matching query") {
          UserDirectoryApi.searchUsersInGroup(_, query, parentGroupId)
        }
    users convert asArrayList(toUserBean)
  }

  override def getInformationForGroup(groupId: String): Optional[GroupBean] = {
    LOGGER.debug("Retrieving user directory group by ID: {}", groupId)
    getEntityOrNoneOnNotFound(
      "user directory group [by ID]",
      groupId,
      UserDirectoryApi.groupById
    ) convert asOptional(toGroupBean)
  }

  override def getInformationForGroups(
      groupIds: util.Collection[String]
  ): util.Map[String, GroupBean] = {
    LOGGER.debug("Retrieving user directory groups by IDs: {}", groupIds)
    getAllUnpaginated(
      "user directory groups by IDs",
      groupIds.asScala.toList,
      UserDirectoryApi.groupsByIds
    ) convert asHashMap(group => group.id -> toGroupBean(group.group))
  }

  override def getParentGroupForGroup(groupId: String): Optional[GroupBean] = {
    LOGGER.debug("Retrieving user directory parent group for group: {}", groupId)
    getOptionalEntityOrNoneOnNotFound(
      "user directory parent group",
      groupId,
      UserDirectoryApi.parentGroup
    ) convert asOptional(toGroupBean)
  }

  override def searchGroups(query: String): util.List[GroupBean] = {
    LOGGER.debug("Searching user directory groups with query: {}", query)
    getAll("user directory groups matching query") {
      UserDirectoryApi.searchGroups(_, query)
    } convert asArrayList(toGroupBean)
  }

  override def searchGroupsInParent(
      query: String,
      parentGroupId: String
  ): util.List[GroupBean] = {
    LOGGER.debug(
      "Searching user directory groups in parent {} with query: {}",
      parentGroupId,
      query
    )
    getAll("user directory groups in parent matching query") {
      UserDirectoryApi.searchGroupsInParent(_, query, parentGroupId)
    } convert asArrayList(toGroupBean)
  }

  override def getInformationForRole(roleId: String): Optional[RoleBean] = {
    LOGGER.debug("Retrieving user directory role by ID: {}", roleId)
    getEntityOrNoneOnNotFound(
      "user directory role [by ID]",
      roleId,
      UserDirectoryApi.roleById
    ) convert asOptional(toRoleBean)
  }

  override def getInformationForRoles(
      roleIds: util.Collection[String]
  ): util.Map[String, RoleBean] = {
    LOGGER.debug("Retrieving user directory roles by IDs: {}", roleIds)
    getAllUnpaginated(
      "user directory roles by IDs",
      roleIds.asScala.toList,
      UserDirectoryApi.rolesByIds
    ) convert asHashMap(role => role.id -> toRoleBean(role.role))
  }

  override def searchRoles(query: String): util.List[RoleBean] = {
    LOGGER.debug("Searching user directory roles with query: {}", query)
    getAll("user directory roles matching query") {
      UserDirectoryApi.searchRoles(_, query)
    } convert asArrayList(toRoleBean)
  }
}
