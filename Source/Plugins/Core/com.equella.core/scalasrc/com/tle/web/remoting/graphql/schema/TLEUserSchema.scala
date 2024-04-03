package com.tle.web.remoting.graphql.schema

import caliban._
import caliban.schema.ArgBuilder.auto._
import caliban.schema.Schema.auto._
import com.tle.core.guice.Bind
import com.tle.web.remoting.graphql.provider.TLEUserProvider

import javax.inject.{Inject, Singleton}

@Bind
@Singleton
class TLEUserSchema {
  private var tleUserProvider: TLEUserProvider = _

  @Inject def this(tleUserService: TLEUserProvider) = {
    this()
    this.tleUserProvider = tleUserService
  }

  case class ListUsersArgs(query: Option[String])
  case class UserByUsernameArgs(username: String)
  case class UserByIdArgs(id: String)
  case class Queries(
      internalUsers: ListUsersArgs => List[User],
      internalUserByUsername: UserByUsernameArgs => Option[User],
      internalUserById: UserByIdArgs => Option[User]
  )

  case class CreateUserArgs(
      username: String,
      email: String,
      firstName: String,
      lastName: String,
      password: String
  )
  case class UpdateUserArgs(
      username: String,
      email: Option[String],
      firstName: Option[String],
      lastName: Option[String],
      password: Option[String]
  )
  case class DeleteUserArgs(id: String)
  case class Mutations(
      internalUserCreate: CreateUserArgs => ResultWithErrors[User],
      internalUserUpdate: UpdateUserArgs => ResultWithErrors[User],
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

  def getApi: GraphQL[Any] =
    graphQL(
      RootResolver(
        queries,
        mutations
      ))
}
