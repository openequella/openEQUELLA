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

import com.tle.admin.helper.GraphQLQueryHelper.{executeOrThrow, getAll, getOptionalEntity}
import com.tle.beans.user.TLEUser
import io.github.openequella.graphql.ClientConfiguration
import io.github.openequella.graphql.api._
import org.slf4j.{Logger, LoggerFactory}

import java.util.Optional
import javax.inject.{Inject, Singleton}
import scala.collection.mutable.ListBuffer
import scala.jdk.CollectionConverters._
import scala.language.implicitConversions

/** Service class for admin operations on TLEUser objects via the GraphQL library. Because this
  * class is intended for use primarily by the existing Java code, preference is given to Java types
  * over Scala types.
  */
@Singleton
class AdminTLEUserServiceImpl @Inject() (implicit
    val cfg: ClientConfiguration
) extends AdminTLEUserService {
  private implicit val LOGGER: Logger = LoggerFactory.getLogger(classOf[AdminTLEUserServiceImpl])

  private implicit def InternalUserViewToTleUser(view: InternalUserView): TLEUser = {
    val u = new TLEUser()
    u.setUuid(view.uniqueId)
    u.setUsername(view.username)
    u.setFirstName(view.firstName)
    u.setLastName(view.lastName)
    u.setEmailAddress(view.email.orNull)

    u
  }

  private implicit def optionalViewToUser(view: Option[InternalUserView]): Optional[TLEUser] =
    view.fold[Optional[TLEUser]](Optional.empty())(u => Optional.of(u))

  override def add(user: TLEUser): String = {
    val created = executeOrThrow(s"adding internal user: ${user.getUsername}")(
      InternalUserApi.createUser(
        user.getUsername,
        Option(user.getEmailAddress),
        user.getFirstName,
        user.getLastName,
        user.getPassword
      )
    )
    LOGGER.debug("Internal user [{}] added with UUID: {}", created.username, created.uniqueId)
    created.uniqueId
  }

  override def get(uniqueId: String): Optional[TLEUser] =
    getOptionalEntity("Internal user [by UUID]", uniqueId, InternalUserApi.getByUniqueId)

  override def getByUsername(username: String): Optional[TLEUser] =
    getOptionalEntity("Internal user [by username]", username, InternalUserApi.getByUsername)

  /** Delete a user by UUID.
    *
    * @param uuid
    *   the UUID of the user to delete
    * @throws ClientRequestException
    *   if there are any errors deleting the user
    */
  override def delete(uuid: String): Unit =
    executeOrThrow(s"deleting internal user: $uuid")(InternalUserApi.deleteUser(uuid))

  /** Given an existing user's TLEUser entity which has been modified, update the user in the
    * database.
    *
    * @param user
    *   The user to update
    * @return
    *   The UUID of the updated user
    */
  override def edit(user: TLEUser): String =
    executeOrThrow(s"updating internal user: ${user.getUuid}")(
      InternalUserApi.updateUser(
        user.getUuid,
        Option(user.getUsername),
        Option(user.getEmailAddress),
        Option(user.getFirstName),
        Option(user.getLastName),
        Option(user.getPassword)
      )
    ).uniqueId

  override def searchUsers(query: String): java.util.List[TLEUser] = {
    LOGGER.debug("Searching for internal users with query: {}", query)
    val users = getAll("internal users matching query") {
      InternalUserApi.searchUsers(_, Option(query))
    }
    LOGGER.debug("Found {} users", users.size)

    // We convert the following to a ListBuffer to ensure a proper mutable list is returned on the Java
    // side. This is especially important, as the main caller of this method in the Admin Console
    // calls java.util.List.sort() on the result.
    users.map(InternalUserViewToTleUser).to(ListBuffer).asJava
  }
}
