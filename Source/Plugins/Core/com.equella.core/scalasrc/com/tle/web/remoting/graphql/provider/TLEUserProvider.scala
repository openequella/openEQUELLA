package com.tle.web.remoting.graphql.provider

import com.tle.beans.user.TLEUser
import com.tle.core.guice.Bind
import com.tle.core.usermanagement.standard.service.TLEUserService
import com.tle.web.remoting.graphql.schema.User

import javax.inject.{Inject, Singleton}
import scala.jdk.CollectionConverters._
import scala.language.implicitConversions
import scala.util.{Failure, Success, Try}

@Bind
@Singleton
class TLEUserProvider {
  private var tleUserService: TLEUserService = _

  @Inject def this(tleUserService: TLEUserService) = {
    this()
    this.tleUserService = tleUserService
  }

  def listUsers(query: Option[String]): List[User] = {
    tleUserService
      .searchUsers(query.getOrElse(""), "", true)
      .asScala
      .map(User(_))
      .toList
  }

  def userByUsername(username: String): Option[User] = {
    tleUserService.getByUsername(username)
  }

  def userById(id: String): Option[User] = {
    tleUserService.get(id)
  }

  def createUser(username: String,
                 email: String,
                 firstName: String,
                 lastName: String,
                 password: String): Either[ProviderError, User] = {
    val u = new TLEUser()
    u.setUsername(username)
    u.setEmailAddress(email)
    u.setFirstName(firstName)
    u.setLastName(lastName)
    u.setPassword(password)

    for {
      id <- Try(tleUserService.add(u)).toEither.left.map(e =>
        ProviderError("Failed to add new user: " + e.getMessage, Some(e)))
      tleUser <- Try(tleUserService.get(id)).toEither.left.map(e =>
        ProviderError("Failed to retrieve new user:" + e.getMessage, Some(e)))
      newUser = User(tleUser)
    } yield newUser
  }

  def updateUser(username: String,
                 email: Option[String],
                 firstName: Option[String],
                 lastName: Option[String],
                 password: Option[String]): Either[ProviderError, User] =
    Option(tleUserService.getByUsername(username))
      .toRight(ProviderError(s"User with username $username not found"))
      .flatMap { u =>
        email.foreach(u.setEmailAddress)
        firstName.foreach(u.setFirstName)
        lastName.foreach(u.setLastName)
        password.foreach(u.setPassword)

        Try {
          val uuid = tleUserService.edit(u, password.isDefined)
          tleUserService.get(uuid)
        } match {
          case Success(u) => Right(User(u))
          case Failure(e) => Left(ProviderError("Failed to update user: " + e.getMessage, Some(e)))
        }
      }

  def deleteUser(id: String): Either[ProviderError, Unit] = {
    Try {
      tleUserService.delete(id)
    } match {
      case Success(_) => Right(())
      case Failure(e) => Left(ProviderError("Failed to delete user: " + e.getMessage, Some(e)))
    }
  }

  private implicit def optionalUser(u: TLEUser): Option[User] = Option(u).map(User(_))
}
