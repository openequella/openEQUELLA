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
import com.tle.beans.user.TLEUser
import com.tle.common.security.SecurityConstants
import com.tle.core.guice.Bind
import com.tle.core.security.impl.RequiresPrivilege
import com.tle.core.usermanagement.standard.service.TLEUserService
import com.tle.web.remoting.graphql.ErrorCode
import com.tle.web.remoting.graphql.schema.{Page, User, UserConnection, paginationOffsetLimit}

import javax.inject.{Inject, Singleton}
import scala.jdk.CollectionConverters._
import scala.language.implicitConversions
import scala.util.Try

/** A Provider for operations involving TLE User entities. Ultimately proxied to the
  * `TLEUserService`. Although some service methods have appropriate access control, this provider
  * enforces the `EDIT_USER_MANAGEMENT` privilege on most operations. This is done even for
  * operations using service methods which already enforce the privilege, to ensure that the
  * privilege is always checked.
  */
@Bind
@Singleton
class TLEUserProvider @Inject() (tleUserService: TLEUserService) {

  /** Retrieval of TLEUser objects often result in `null` values, so this helper `implicit`
    * conversion is used to convert `null` to `None`.
    */
  private implicit def optionalUser(u: TLEUser): Option[User] = Option(u).map(User(_))

  /** List all users in the system, filtered by `query`. If `query` is `None`, all users are
    * returned.
    *
    * @param query
    *   an optional query string to filter users by
    * @param pagination
    *   the pagination object to use for the query
    * @return
    *   a `UserConnection` object containing the users and pagination information
    */
  @RequiresPrivilege(priv = SecurityConstants.EDIT_USER_MANAGEMENT)
  def listUsers(query: Option[String], pagination: Pagination[Base64Cursor]): UserConnection = {
    val searchResult = new UserSearch().withQuery(query).searchSubGroups(pagination)
    UserConnection(searchResult)
  }

  /** Retrieve a user by their username, if the user can't be found `None` is returned.
    *
    * @param username
    *   the username of the user to retrieve
    */
  @RequiresPrivilege(priv = SecurityConstants.EDIT_USER_MANAGEMENT)
  def userByUsername(username: String): Option[User] = {
    tleUserService.getByUsername(username)
  }

  /** Retrieve a user by their ID, if the user can't be found `None` is returned.
    *
    * @param id
    *   The ID is the DB identifier for the user - typically a UUID, but can be anything.
    */
  @RequiresPrivilege(priv = SecurityConstants.EDIT_USER_MANAGEMENT)
  def userById(id: String): Option[User] = {
    tleUserService.get(id)
  }

  /** Create a new user with the provided details.
    *
    * @param username
    *   the username of the new user
    * @param email
    *   the email address of the new user
    * @param firstName
    *   the first name of the new user
    * @param lastName
    *   the last name of the new user
    * @param password
    *   the password of the new user - which will be hashed before storage
    * @return
    *   the new user, or an error if the user could not be created
    */
  @RequiresPrivilege(priv = SecurityConstants.EDIT_USER_MANAGEMENT)
  def createUser(
      username: String,
      email: Option[String],
      firstName: String,
      lastName: String,
      password: String
  ): Either[ProviderError, User] = {
    val u = new TLEUser()
    u.setUsername(username)
    u.setFirstName(firstName)
    u.setLastName(lastName)
    u.setPassword(password)

    // Optional fields
    email.foreach(u.setEmailAddress)

    for {
      id <- Try(tleUserService.add(u)).toEither.left.map(e =>
        ProviderError("Failed to add new user: " + e.getMessage, e)
      )
      tleUser <- Try(tleUserService.get(id)).toEither.left.map(e =>
        ProviderError("Failed to retrieve new user:" + e.getMessage, e)
      )
      newUser = User(tleUser)
    } yield newUser
  }

  /** Update a user with the provided details - any which are Some.
    *
    * @param id
    *   the database ID of the user to update - typically a UUID but can be anything.
    * @param username
    *   the username of the user to update
    * @param email
    *   a new email address for the user
    * @param firstName
    *   a new first name for the user
    * @param lastName
    *   a new last name for the user
    * @param password
    *   a new password for the user - which will be hashed before storage
    * @return
    *   the updated user, or an error if the user could not be updated
    */
  @RequiresPrivilege(priv = SecurityConstants.EDIT_USER_MANAGEMENT)
  def updateUser(
      id: String,
      username: Option[String],
      email: Option[String],
      firstName: Option[String],
      lastName: Option[String],
      password: Option[String]
  ): Either[ProviderError, User] =
    Option(tleUserService.get(id))
      .toRight(ProviderError(s"User with id of $id not found", ErrorCode.NOT_FOUND))
      .flatMap { u =>
        username.foreach(u.setUsername)
        email.foreach(u.setEmailAddress)
        firstName.foreach(u.setFirstName)
        lastName.foreach(u.setLastName)
        password.foreach(u.setPassword)

        ProviderError.Try("Failed to update user: ") {
          val uuid = tleUserService.edit(u, password.isDefined)
          User(tleUserService.get(uuid))
        }
      }

  /** Delete a user by their ID.
    *
    * @param id
    *   the database ID of the user to delete - typically a UUID but can be anything.
    */
  @RequiresPrivilege(priv = SecurityConstants.EDIT_USER_MANAGEMENT)
  def deleteUser(id: String): Either[ProviderError, Unit] =
    ProviderError.Try("Failed to delete user: ") {
      tleUserService.delete(id)
    }

  /** Helper class to encapsulate the search parameters for user searches.
    */
  private class UserSearch {
    private var query: Option[String]    = None
    private var parentId: Option[String] = None
    private var recursive: Boolean       = false

    /** Set the query string to search for.
      */
    def withQuery(query: Option[String]): UserSearch = {
      this.query = query
      this
    }

    /** Set the parent ID to search for users within.
      */
    def withParentId(parentId: String): UserSearch = {
      this.parentId = Some(parentId)
      this
    }

    /** Search for users with the provided criteria, walking the group hierarchy if required. If no
      * parent ID is provided, the search will be for all users.
      *
      * @param pagination
      *   the pagination parameters to limit the search
      * @return
      *   the search result
      */
    def searchSubGroups(pagination: Pagination[Base64Cursor]): Page[User] = {
      this.recursive = true
      search(pagination)
    }

    /** Search for users with the provided criteria. If no parent ID is provided, the search will be
      * for top level users only, otherwise only users within the provided group will be returned.
      *
      * @param pagination
      *   the pagination parameters to limit the search
      * @return
      *   the search result
      */
    def search(pagination: Pagination[Base64Cursor]): Page[User] = {
      // The arguments to this call should match those below for searchUsers.
      val userCount = tleUserService.countUsers(query.getOrElse(""), parentId.orNull, recursive)
      val (offset, limit) = paginationOffsetLimit(pagination, userCount)

      val users = if (userCount > 0) {
        tleUserService
          .searchUsers(query.getOrElse(""), parentId.orNull, recursive, limit, offset)
          .asScala
          .map(User(_))
          .toList
      } else {
        List.empty
      }

      Page(users, userCount, offset, limit)
    }
  }
}
