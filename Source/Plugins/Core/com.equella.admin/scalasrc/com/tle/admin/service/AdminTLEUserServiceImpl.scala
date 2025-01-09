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

import com.tle.beans.user.TLEUser
import com.tle.core.remoting.RemoteTLEUserService
import io.github.openequella.graphql.ClientConfiguration
import io.github.openequella.graphql.api.{ApiError, TleUserApi, TleUserView}
import org.slf4j.{Logger, LoggerFactory}

import java.util.Optional
import javax.inject.{Inject, Singleton}
import scala.language.implicitConversions

/** Service class for admin operations on TLEUser objects via the GraphQL library. Because this
  * class is intended for use primarily by the existing Java code, preference is given to Java types
  * over Scala types.
  */
@Singleton
class AdminTLEUserServiceImpl @Inject() (val delegate: RemoteTLEUserService)(implicit
    val cfg: ClientConfiguration
) extends AdminTLEUserService {
  private val LOGGER: Logger = LoggerFactory.getLogger(classOf[AdminTLEUserServiceImpl])

  private implicit def tleUserViewToTleUser(view: TleUserView): TLEUser = {
    val u = new TLEUser()
    u.setUuid(view.uniqueId)
    u.setUsername(view.username)
    u.setFirstName(view.firstName)
    u.setLastName(view.lastName)
    u.setEmailAddress(view.email.orNull)

    u
  }

  override def add(user: TLEUser): String = {
    LOGGER.debug("Adding user: " + user.getUsername)
    TleUserApi.createUser(
      user.getUsername,
      Option(user.getEmailAddress),
      user.getFirstName,
      user.getLastName,
      user.getPassword
    ) match {
      case Right(newUser) =>
        LOGGER.debug(s"User [${newUser.username}] added with UUID: ${newUser.uniqueId}")
        newUser.uniqueId
      case Left(errors) =>
        throw new ClientRequestException(s"Error adding user [${user.getUsername}]", errors)
    }
  }

  override def get(uniqueId: String): Optional[TLEUser] =
    getUser(uniqueId, TleUserApi.getByUniqueId)

  override def getByUsername(username: String): Optional[TLEUser] =
    getUser(username, TleUserApi.getByUsername)

  /** Delete a user by UUID.
    *
    * @param uuid
    *   the UUID of the user to delete
    * @throws ClientRequestException
    *   if there are any errors deleting the user
    */
  override def delete(uuid: String): Unit = {
    LOGGER.debug("Deleting user with UUID: " + uuid)
    TleUserApi.deleteUser(uuid) match {
      case Right(_)     => LOGGER.debug(s"User [$uuid] deleted")
      case Left(errors) => throw new ClientRequestException(s"Error deleting user [$uuid]", errors)
    }
  }

  /** Given an existing user's TLEUser entity which has been modified, update the user in the
    * database.
    *
    * @param user
    *   The user to update
    * @return
    *   The UUID of the updated user
    */
  override def edit(user: TLEUser): String = {
    val uuid = user.getUuid
    LOGGER.debug("Editing user: {}", uuid)
    TleUserApi.updateUser(
      uuid,
      Option(user.getUsername),
      Option(user.getEmailAddress),
      Option(user.getFirstName),
      Option(user.getLastName),
      Option(user.getPassword)
    ) match {
      case Right(updatedUser) =>
        LOGGER.debug("User {} [{}] updated", updatedUser.username, updatedUser.uniqueId)
        updatedUser.uniqueId
      case Left(errors) =>
        throw new ClientRequestException(s"Error updating user [${uuid}]", errors)
    }
  }

  override def searchUsers(query: String): java.util.List[TLEUser] =
    implementMe { d =>
      d.searchUsers(query, null, true)
    }

  private def getUser(
      identifier: String,
      f: String => Either[List[ApiError], Option[TleUserView]]
  ): Optional[TLEUser] = {
    f(identifier) match {
      case Right(user) =>
        user
          .fold[Optional[TLEUser]]({
            LOGGER.debug(s"User [$identifier] not found")
            Optional.empty()
          })(u => {
            LOGGER.debug(s"User [$identifier] found")
            Optional.of(u)
          })
      case Left(errors) =>
        throw new ClientRequestException(s"Error retrieving user [$identifier]", errors)
    }
  }

  private def implementMe[T](f: RemoteTLEUserService => T): T = {
    LOGGER.debug(
      "Still waiting on GraphQL implementation, will try delegate.",
      new NotImplementedError()
    )
    f(delegate)
  }
}
