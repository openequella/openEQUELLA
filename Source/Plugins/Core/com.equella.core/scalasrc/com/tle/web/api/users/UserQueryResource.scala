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

package com.tle.web.api.users

import com.tle.beans.usermanagement.standard.wrapper.SharedSecretSettings
import com.tle.common.security.SecurityConstants
import com.tle.common.usermanagement.user.valuebean.{GroupBean, RoleBean, UserBean}
import com.tle.legacy.LegacyGuice
import com.tle.web.api.ApiErrorResponse.resourceNotFound
import com.tle.core.security.ACLChecks.hasAclOrThrow
import io.swagger.annotations.{Api, ApiOperation, ApiParam}

import javax.ws.rs._
import javax.ws.rs.core.Response
import scala.jdk.CollectionConverters._

case class LookupQuery(users: Seq[String], groups: Seq[String], roles: Seq[String])

case class GroupDetails(id: String, name: String)

case class RoleDetails(id: String, name: String)

object GroupDetails {
  def apply(gb: GroupBean): GroupDetails =
    GroupDetails(gb.getUniqueID, gb.getName)
}

object RoleDetails {
  def apply(rb: RoleBean): RoleDetails = RoleDetails(rb.getUniqueID, rb.getName)
}

case class LookupQueryResult(
    users: Iterable[UserDetails],
    groups: Iterable[GroupDetails],
    roles: Iterable[RoleDetails]
)

@Path("userquery/")
@Produces(value = Array("application/json"))
@Api(value = "User queries")
class UserQueryResource {

  val exclude = Set(SecurityConstants.LOGGED_IN_USER_ROLE_ID, SecurityConstants.GUEST_USER_ROLE_ID)
  @POST
  @Path("lookup")
  def lookup(queries: LookupQuery): LookupQueryResult = {
    hasAclOrThrow(SecurityConstants.LIST_USERS)
    val us     = LegacyGuice.userService
    val users  = us.getInformationForUsers(queries.users.asJava)
    val groups = us.getInformationForGroups(queries.groups.asJava)
    val roles  = us.getInformationForRoles(queries.roles.asJava)
    LookupQueryResult(
      users.asScala.values.map(UserDetails.apply),
      groups.asScala.values.map(GroupDetails.apply),
      roles.asScala.values.map(RoleDetails.apply)
    )
  }

  @GET
  @Path("search")
  def search(
      @QueryParam(value = "q") q: String,
      @QueryParam("users") @DefaultValue("true") @ApiParam("Include users") susers: Boolean,
      @QueryParam("groups") @DefaultValue("true") @ApiParam("Include groups") sgroups: Boolean,
      @QueryParam("roles") @DefaultValue("true") @ApiParam("Include roles") sroles: Boolean
  ): LookupQueryResult = {
    hasAclOrThrow(SecurityConstants.LIST_USERS)
    val us     = LegacyGuice.userService
    val users  = if (susers) us.searchUsers(q).asScala else Iterable.empty
    val groups = if (sgroups) us.searchGroups(q).asScala else Iterable.empty
    val roles  = if (sroles) us.searchRoles(q).asScala else Iterable.empty
    LookupQueryResult(
      users.map(UserDetails.apply),
      groups.map(GroupDetails.apply),
      roles.filterNot(r => exclude(r.getUniqueID)).map(RoleDetails.apply)
    )
  }

  @GET
  @Path("filtered")
  @ApiOperation(
    value = "Search for users",
    notes = "Searches for users, but filters the results based on the byGroups parameter.",
    response = classOf[UserDetails],
    responseContainer = "List"
  )
  def filtered(
      @QueryParam(value = "q") @ApiParam("Query string") q: String,
      @QueryParam("byGroups") @ApiParam(
        "A list of group UUIDs to filter the search by"
      ) groups: Array[String]
  ): Response = {
    hasAclOrThrow(SecurityConstants.LIST_USERS)
    val us = LegacyGuice.userService
    val result: Iterable[UserBean] = groups match {
      case xs if xs.nonEmpty => xs.flatMap(g => us.searchUsers(q, g, true).asScala)
      case _                 => us.searchUsers(q).asScala
    }
    result match {
      case users if users.nonEmpty =>
        val uniqueUsers = users.toSet
        val details     = uniqueUsers.map(UserDetails.apply)
        Response.ok.entity(details).build()
      case _ => resourceNotFound("No users were found matching the specified criteria")
    }
  }

  @GET
  @Path("filtered-groups")
  @ApiOperation(
    value = "Search for groups",
    notes = "Searches for groups, but filters the results based on the byGroups parameter.",
    response = classOf[GroupDetails],
    responseContainer = "List"
  )
  def filteredGroups(
      @QueryParam("q") @ApiParam("Query string") q: String,
      @QueryParam("byGroups") @ApiParam(
        "A list of group UUIDs to filter the search by"
      ) groups: Array[String]
  ): Response = {
    val us = LegacyGuice.userService

    Option
      .when(groups.nonEmpty)(groups.flatMap(us.searchGroups(q, _).asScala))
      .orElse(Option(us.searchGroups(q).asScala.toArray))
      .filter(_.nonEmpty)
      .map(groups => Response.ok.entity(groups.toSet.map(GroupDetails.apply)).build())
      .getOrElse(resourceNotFound("No groups were found matching the specified criteria"))
  }

  @GET
  @Path("tokens")
  def listTokens = {
    Option(
      LegacyGuice.userService.getReadOnlyPluginConfig(
        "com.tle.beans.usermanagement.standard.wrapper.SharedSecretSettings"
      )
    ) match {
      case Some(s: SharedSecretSettings) => s.getSharedSecrets.asScala.map(_.getId)
      case _                             => Iterable()
    }
  }

  @GET
  @Path("userinfobackup")
  def getUserInfoBackup(@QueryParam("uniqueId") uniqueId: String) = {
    val userService = LegacyGuice.userService
    userService.findUserInfoBackup(uniqueId)
  }
}
