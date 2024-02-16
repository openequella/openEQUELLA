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
      users: ListUsersArgs => List[User],
      userByUsername: UserByUsernameArgs => Option[User],
      userById: UserByIdArgs => Option[User]
  )

  case class CreateUserArgs(
      username: String,
      email: String,
      firstName: String,
      lastName: String,
      password: String
  )
  case class Mutations(
      createUser: CreateUserArgs => IO[ExecutionError, User]
  )

  private val queries = Queries(
    users = args => tleUserProvider.listUsers(args.query),
    userByUsername = args => tleUserProvider.userByUsername(args.username),
    userById = args => tleUserProvider.userById(args.id)
  )

  private val mutations = Mutations(
    createUser = {
      case _ @CreateUserArgs(username, email, firstName, lastName, password) =>
        tleUserProvider.createUser(username, email, firstName, lastName, password) match {
          case Left(error)   => ZIO.fail(ExecutionError(msg = error.message))
          case Right(result) => ZIO.succeed(result)
        }
    }
  )

  def getApi() =
    graphQL(
      RootResolver(
        queries,
        mutations
      ))
}
