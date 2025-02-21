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

package com.tle.web.remoting.graphql.schema

import caliban._
import caliban.relay.{Base64Cursor, Pagination, PaginationArgs}
import caliban.schema.Annotations.GQLDescription
import caliban.schema.ArgBuilder.auto._
import caliban.schema.Schema.auto._
import com.tle.core.guice.Bind
import com.tle.web.remoting.graphql.provider.TLEUserProvider
import zio.IO

import javax.inject.{Inject, Singleton}

/** The schema for the TLE User GraphQL API. Because `TLEUser`s are effectively 'internal' users,
  * all operations in this schema using the namespace prefix of `internalUser`.
  */
@Bind
@Singleton
class TLEUserSchema extends SchemaProvider {
  private var tleUserProvider: TLEUserProvider = _

  /** Default constructor for Guice.
    */
  @Inject def this(tleUserProvider: TLEUserProvider) = {
    this()
    this.tleUserProvider = tleUserProvider
  }

  case class ListUsersArgs(
      @GQLDescription("A string to filter users by") query: Option[String],
      @GQLDescription(
        "Pagination - how many items to return from the start of the possible list of items"
      )
      first: Option[Int],
      @GQLDescription(
        "Pagination - how many items to return from the end of the possible list of items"
      )
      last: Option[Int],
      @GQLDescription(
        "Pagination - the cursor for a item before which all items should be returned"
      )
      before: Option[String],
      @GQLDescription("Pagination - the cursor for a item after which all items should be returned")
      after: Option[String]
  ) extends PaginationArgs[Base64Cursor]

  case class UserByUsernameArgs(
      @GQLDescription("Username of the user to retrieve") username: String
  )

  case class UserByIdArgs(@GQLDescription("Unique ID of the user to retrieve") id: String)

  case class Queries(
      @GQLDescription("List all internal users, optionally filtered by a query")
      internalUsers: ListUsersArgs => IO[CalibanError, UserConnection],
      @GQLDescription("Retrieve details of a user based on username")
      internalUserByUsername: UserByUsernameArgs => Option[User],
      @GQLDescription("Retrieve details of a user based on unique ID")
      internalUserById: UserByIdArgs => Option[User]
  )

  case class CreateUserArgs(
      @GQLDescription(
        "Username of the user to create which they'll use for authentication - must be unique"
      )
      username: String,
      @GQLDescription("Email address of the user")
      email: Option[String],
      @GQLDescription("First name of the user")
      firstName: String,
      @GQLDescription("Last name of the user")
      lastName: String,
      @GQLDescription("Password of the user - will be hashed on store")
      password: String
  )
  case class UpdateUserArgs(
      @GQLDescription("ID of existing user to update - used to find target user")
      id: String,
      @GQLDescription("New username of the user - should be unique")
      username: Option[String],
      @GQLDescription("New email address of the user")
      email: Option[String],
      @GQLDescription("New first name of the user")
      firstName: Option[String],
      @GQLDescription("New last name of the user")
      lastName: Option[String],
      @GQLDescription("New password of the user - will be hashed on store")
      password: Option[String]
  )
  case class DeleteUserArgs(@GQLDescription("The unique ID of the user to delete") id: String)
  case class Mutations(
      @GQLDescription("Create a new internal user")
      internalUserCreate: CreateUserArgs => ResultWithErrors[User],
      @GQLDescription("Update an existing internal user")
      internalUserUpdate: UpdateUserArgs => ResultWithErrors[User],
      @GQLDescription("Delete an existing internal user")
      internalUserDelete: DeleteUserArgs => ResultWithErrors[Unit]
  )

  private val queries = Queries(
    internalUsers = args =>
      for {
        pagination <- Pagination(args)
        users = tleUserProvider.listUsers(args.query, pagination)
      } yield users,
    internalUserByUsername = args => tleUserProvider.userByUsername(args.username),
    internalUserById = args => tleUserProvider.userById(args.id)
  )

  private val mutations = Mutations(
    internalUserCreate = args =>
      tleUserProvider
        .createUser(args.username, args.email, args.firstName, args.lastName, args.password),
    internalUserUpdate = args =>
      tleUserProvider.updateUser(
        args.id,
        args.username,
        args.email,
        args.firstName,
        args.lastName,
        args.password
      ),
    internalUserDelete = args => tleUserProvider.deleteUser(args.id)
  )

  /** Get the API for the TLE User GraphQL schema.
    */
  override def getApi: GraphQL[Any] =
    graphQL(
      RootResolver(
        queries,
        mutations
      )
    )
}
