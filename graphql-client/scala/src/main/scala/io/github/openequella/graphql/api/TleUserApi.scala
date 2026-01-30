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

import caliban.client.Operations.RootQuery
import caliban.client.SelectionBuilder
import io.github.openequella.graphql.ClientConfiguration
import io.github.openequella.graphql.client._

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
object TleUserApi extends NestedApi[InternalUserQueries, InternalUserMutations] {

  override protected def queryWrapper[A]
      : SelectionBuilder[InternalUserQueries, A] => SelectionBuilder[RootQuery, A] =
    Queries.internalUsers

  override protected def mutationWrapper[A]
      : SelectionBuilder[InternalUserMutations, A] => SelectionBuilder[
        _root_.caliban.client.Operations.RootMutation,
        A
      ] = Mutations.internalUser

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
    val q = InternalUserQueries.byId(uniqueId) {
      tleUser
    }

    query(q)
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
    val q = InternalUserQueries.byUsername(username) {
      tleUser
    }

    query(q)
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
    val m = InternalUserMutations.create(username, email, firstName, lastName, password) {
      tleUser
    }

    flattenResult {
      mutate(m)
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
    val m =
      InternalUserMutations.update(uniqueId, username, email, firstName, lastName, password) {
        tleUser
      }

    flattenResult {
      mutate(m)
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
    val m = InternalUserMutations.delete(uniqueId)

    flattenResult {
      mutate(m)
    }
  }

  /** Searches for users based on the provided query.
    *
    * @param pagination
    *   Detail the number of items to return, and whether to page through forward or backwards.
    *   Especially useful for paging through large result sets.
    * @param query
    *   The query to search for. If None, all users will be returned.
    * @param cfg
    *   The client configuration.
    * @return
    *   Left containing a list of errors or Right with the list of users if the operation was
    *   successful.
    */
  def searchUsers(
      pagination: Pagination,
      query: Option[String] = None
  )(implicit cfg: ClientConfiguration): Either[List[ApiError], PaginationResult[TleUserView]] = {
    // Set up the various selectors
    val userEdge =
      (UserEdge.cursor ~ UserEdge.node { tleUser }).mapN(NodeWithCursorView[TleUserView](_, _))
    val userConnection =
      (UserConnection.pageInfo { PageInfoView.selector } ~ UserConnection.edges { userEdge })
        .mapN(ConnectionView[TleUserView](_, _))

    queryWithPagination(pagination) { (first, last, before, after) =>
      queryWrapper(InternalUserQueries.list(query, first, last, before, after) {
        userConnection
      })
    }
  }
}
