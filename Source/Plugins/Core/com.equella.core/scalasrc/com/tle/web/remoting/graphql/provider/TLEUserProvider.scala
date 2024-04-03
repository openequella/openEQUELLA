package com.tle.web.remoting.graphql.provider

import com.tle.beans.user.TLEUser
import com.tle.core.guice.Bind
import com.tle.core.security.impl.RequiresPrivilege
import com.tle.core.usermanagement.standard.service.TLEUserService
import com.tle.web.remoting.graphql.ErrorCodes
import com.tle.web.remoting.graphql.schema.User

import javax.inject.{Inject, Singleton}
import scala.jdk.CollectionConverters._
import scala.language.implicitConversions
import scala.util.Try

@Bind
@Singleton
class TLEUserProvider {
  private final val EDIT_USER_MANAGEMENT = "EDIT_USER_MANAGEMENT"

  private var tleUserService: TLEUserService = _

  @Inject def this(tleUserService: TLEUserService) = {
    this()
    this.tleUserService = tleUserService
  }

  @RequiresPrivilege(priv = EDIT_USER_MANAGEMENT)
  def listUsers(query: Option[String]): List[User] =
    tleUserService
      .searchUsers(query.getOrElse(""), "", true)
      .asScala
      .map(User(_))
      .toList

  @RequiresPrivilege(priv = EDIT_USER_MANAGEMENT)
  def userByUsername(username: String): Option[User] = {
    tleUserService.getByUsername(username)
  }

  @RequiresPrivilege(priv = EDIT_USER_MANAGEMENT)
  def userById(id: String): Option[User] = {
    tleUserService.get(id)
  }

  @RequiresPrivilege(priv = EDIT_USER_MANAGEMENT)
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
        ProviderError("Failed to add new user: " + e.getMessage, e))
      tleUser <- Try(tleUserService.get(id)).toEither.left.map(e =>
        ProviderError("Failed to retrieve new user:" + e.getMessage, e))
      newUser = User(tleUser)
    } yield newUser
  }

  @RequiresPrivilege(priv = EDIT_USER_MANAGEMENT)
  def updateUser(username: String,
                 email: Option[String],
                 firstName: Option[String],
                 lastName: Option[String],
                 password: Option[String]): Either[ProviderError, User] =
    Option(tleUserService.getByUsername(username))
      .toRight(ProviderError(s"User with username $username not found", ErrorCodes.NOT_FOUND))
      .flatMap { u =>
        email.foreach(u.setEmailAddress)
        firstName.foreach(u.setFirstName)
        lastName.foreach(u.setLastName)
        password.foreach(u.setPassword)

        ProviderError.Try("Failed to update user: ") {
          val uuid = tleUserService.edit(u, password.isDefined)
          User(tleUserService.get(uuid))
        }
      }

  @RequiresPrivilege(priv = EDIT_USER_MANAGEMENT)
  def deleteUser(id: String): Either[ProviderError, Unit] =
    ProviderError.Try("Failed to delete user: ") {
      tleUserService.delete(id)
    }

  private implicit def optionalUser(u: TLEUser): Option[User] = Option(u).map(User(_))
}
