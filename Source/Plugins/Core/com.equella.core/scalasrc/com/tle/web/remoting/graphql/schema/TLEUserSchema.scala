package com.tle.web.remoting.graphql.schema

import caliban.CalibanError.ExecutionError
import caliban._
import caliban.schema.ArgBuilder.auto._
import caliban.schema.Schema.auto._
import com.tle.core.guice.Bind
import com.tle.web.remoting.graphql.provider.TLEUserProvider
import org.slf4j.LoggerFactory
import zio.{IO, ZIO}

import javax.inject.{Inject, Singleton}

@Bind
@Singleton
class TLEUserSchema {
  private val LOGGER = LoggerFactory.getLogger(classOf[TLEUserSchema])

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
      internalUserCreate: CreateUserArgs => IO[ExecutionError, User],
      internalUserUpdate: UpdateUserArgs => IO[ExecutionError, User],
      internalUserDelete: DeleteUserArgs => IO[ExecutionError, Unit]
  )

  private val queries = Queries(
    internalUsers = args => tleUserProvider.listUsers(args.query),
    internalUserByUsername = args => tleUserProvider.userByUsername(args.username),
    internalUserById = args => tleUserProvider.userById(args.id)
  )

  private val mutations = Mutations(
    internalUserCreate = {
      case _ @CreateUserArgs(username, email, firstName, lastName, password) =>
        tleUserProvider.createUser(username, email, firstName, lastName, password) match {
          case Left(error)   => ZIO.fail(ExecutionError(msg = error.message))
          case Right(result) => ZIO.succeed(result)
        }
    },
    internalUserUpdate = {
      case _ @UpdateUserArgs(username, email, firstName, lastName, password) =>
        tleUserProvider.updateUser(username, email, firstName, lastName, password) match {
          case Left(error)   => ZIO.fail(ExecutionError(msg = error.message))
          case Right(result) => ZIO.succeed(result)
        }
    },
    internalUserDelete = {
      case _ @DeleteUserArgs(id) =>
        tleUserProvider.deleteUser(id) match {
          case Left(error) => ZIO.fail(ExecutionError(msg = error.message))
          case Right(_)    => ZIO.succeed(())
        }
    }
  )

  def getApi: GraphQL[Any] =
    graphQL(
      RootResolver(
        queries,
        mutations
      ))
}
