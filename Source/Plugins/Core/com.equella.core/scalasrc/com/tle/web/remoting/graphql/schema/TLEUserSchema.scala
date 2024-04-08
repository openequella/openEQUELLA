package com.tle.web.remoting.graphql.schema

import caliban._
import caliban.schema.Annotations.GQLDescription
import caliban.schema.ArgBuilder.auto._
import caliban.schema.Schema.auto._
import com.tle.core.guice.Bind
import com.tle.web.remoting.graphql.provider.TLEUserProvider

import javax.inject.{Inject, Singleton}

/**
  * The schema for the TLE User GraphQL API. Because `TLEUser`s are effectively 'internal' users, all
  * operations in this schema using the namespace prefix of `internalUser`.
  */
@Bind
@Singleton
class TLEUserSchema {
  private var tleUserProvider: TLEUserProvider = _

  /**
    * Default constructor for Guice.
    */
  @Inject def this(tleUserService: TLEUserProvider) = {
    this()
    this.tleUserProvider = tleUserService
  }

  case class ListUsersArgs(@GQLDescription("A string to filter users by") query: Option[String])
  case class UserByUsernameArgs(
      @GQLDescription("Username of the user to retrieve") username: String)
  case class UserByIdArgs(@GQLDescription("Unique ID of the user to retrieve") id: String)
  case class Queries(
      @GQLDescription("List all internal users, optionally filtered by a query")
      internalUsers: ListUsersArgs => List[User],
      @GQLDescription("Retrieve details of a user based on username")
      internalUserByUsername: UserByUsernameArgs => Option[User],
      @GQLDescription("Retrieve details of a user based on unique ID")
      internalUserById: UserByIdArgs => Option[User]
  )

  case class CreateUserArgs(
      @GQLDescription(
        "Username of the user to create which they'll use for authentication - must be unique")
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
      @GQLDescription("Username of existing user to update")
      username: String,
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
    internalUsers = args => tleUserProvider.listUsers(args.query),
    internalUserByUsername = args => tleUserProvider.userByUsername(args.username),
    internalUserById = args => tleUserProvider.userById(args.id)
  )

  private val mutations = Mutations(
    internalUserCreate = args =>
      tleUserProvider.createUser(args.username,
                                 args.email,
                                 args.firstName,
                                 args.lastName,
                                 args.password),
    internalUserUpdate = args =>
      tleUserProvider.updateUser(args.username,
                                 args.email,
                                 args.firstName,
                                 args.lastName,
                                 args.password),
    internalUserDelete = args => tleUserProvider.deleteUser(args.id)
  )

  /**
    * Get the API for the TLE User GraphQL schema.
    */
  def getApi: GraphQL[Any] =
    graphQL(
      RootResolver(
        queries,
        mutations
      ))
}
