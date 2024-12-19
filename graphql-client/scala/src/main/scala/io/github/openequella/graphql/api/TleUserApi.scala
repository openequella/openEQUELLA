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

import io.github.openequella.graphql.client.{Mutations, Queries, User}
import io.github.openequella.graphql.{Client, ClientConfiguration}

/** Represents an internal openEQUELLA user.
  *
  * @param uniqueId
  *   The unique identifier of the user.
  * @param username
  *   The username of the user.
  * @param email
  *   The email address of the user.
  * @param firstName
  *   The first name of the user.
  * @param lastName
  *   The last name of the user.
  */
final case class TleUserView(
    uniqueId: String,
    username: String,
    email: Option[String],
    firstName: String,
    lastName: String
)

/** Provides access to the openEQUELLA internal user API.
  */
object TleUserApi {
  private val tleUser = (
    User.uniqueId ~ User.username ~ User.email ~ User.firstName ~ User.lastName
  ).mapN(TleUserView)

  /** Retrieves the details of an individual user by their unique identifier.
    *
    * @param uniqueId
    *   The unique identifier of the user.
    * @param cfg
    *   The client configuration.
    * @return
    *   Left containing a list of errors or Right if the operation was successful. Some if user was
    *   found, None if not.
    */
  def getByUniqueId(uniqueId: String)(implicit
      cfg: ClientConfiguration
  ): Either[List[ApiError], Option[TleUserView]] = {
    val query = Queries.internalUserById(uniqueId) {
      tleUser
    }

    Client.query(query)
  }

  /** Retrieves the details of an individual user by their username.
    *
    * @param username
    *   The username of the user.
    * @param cfg
    *   The client configuration.
    * @return
    *   Left containing a list of errors or Right if the operation was successful. Some if user was
    *   found, None if not.
    */
  def getByUsername(
      username: String
  )(implicit cfg: ClientConfiguration): Either[List[ApiError], Option[TleUserView]] = {
    val query = Queries.internalUserByUsername(username) {
      tleUser
    }

    Client.query(query)
  }

  /** Creates a new user.
    *
    * @param cfg
    *   The client configuration.
    * @return
    *   Left containing a list of errors or Right with the new user's details if the operation was
    *   successful.
    */
  def createUser(
      username: String,
      email: Option[String],
      firstName: String,
      lastName: String,
      password: String
  )(implicit cfg: ClientConfiguration): Either[List[ApiError], TleUserView] = {
    val query = Mutations.internalUserCreate(username, email, firstName, lastName, password) {
      tleUser
    }

    flattenResult {
      Client.mutate(query)
    }
  }

  /** Edits an existing user based on the unique identifier.
    *
    * @param uniqueId
    *   The unique identifier of the user.
    * @param username
    *   A new username for the user or `None` to keep the existing username.
    * @param email
    *   A new email address for the user or `None` to keep the existing email address.
    * @param firstName
    *   A new first name for the user or `None` to keep the existing first name.
    * @param lastName
    *   A new last name for the user or `None` to keep the existing last name.
    * @param password
    *   A new password for the user or `None` to keep the existing password.
    * @param cfg
    *   The client configuration.
    * @return
    *   Left containing a list of errors or Right with the updated user's details if the operation
    *   was successful.
    */
  def updateUser(
      uniqueId: String,
      username: Option[String],
      email: Option[String],
      firstName: Option[String],
      lastName: Option[String],
      password: Option[String]
  )(implicit cfg: ClientConfiguration): Either[List[ApiError], TleUserView] = {
    val query =
      Mutations.internalUserUpdate(uniqueId, username, email, firstName, lastName, password) {
        tleUser
      }

    flattenResult {
      Client.mutate(query)
    }
  }

  /** Deletes a user.
    *
    * @param uniqueId
    *   The unique identifier of the user.
    * @param cfg
    *   The client configuration.
    * @return
    *   Left containing a list of errors or Right if the operation was successful.
    */
  def deleteUser(
      uniqueId: String
  )(implicit cfg: ClientConfiguration): Either[List[ApiError], Unit] = {
    val query = Mutations.internalUserDelete(uniqueId)

    flattenResult {
      Client.mutate(query)
    }
  }
}
