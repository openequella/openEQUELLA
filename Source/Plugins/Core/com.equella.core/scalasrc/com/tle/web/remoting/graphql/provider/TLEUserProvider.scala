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

/**
  * A Provider for operations involving TLE User entities. Ultimately proxied to the `TLEUserService`.
  * Although some service methods have appropriate access control, this provider enforces the `EDIT_USER_MANAGEMENT`
  * privilege on most operations. This is done even for operations using service methods which already enforce
  * the privilege, to ensure that the privilege is always checked.
  */
@Bind
@Singleton
class TLEUserProvider {
  private final val EDIT_USER_MANAGEMENT = "EDIT_USER_MANAGEMENT"

  private var tleUserService: TLEUserService = _

  /**
    * Default constructor for Guice.
    *
    * @param tleUserService the `TLEUserService` to use for operations
    */
  @Inject def this(tleUserService: TLEUserService) = {
    this()
    this.tleUserService = tleUserService
  }

  /**
    * List all users in the system, filtered by `query`. If `query` is `None`, all users are returned.
    *
    * @param query an optional query string to filter users by
    */
  @RequiresPrivilege(priv = EDIT_USER_MANAGEMENT)
  def listUsers(query: Option[String]): List[User] =
    tleUserService
      .searchUsers(query.getOrElse(""), "", true)
      .asScala
      .map(User(_))
      .toList

  /**
    * Retrieve a user by their username, if the user can't be found `None` is returned.
    *
    * @param username the username of the user to retrieve
    */
  @RequiresPrivilege(priv = EDIT_USER_MANAGEMENT)
  def userByUsername(username: String): Option[User] = {
    tleUserService.getByUsername(username)
  }

  /**
    * Retrieve a user by their ID, if the user can't be found `None` is returned.
    *
    * @param id The ID is the DB identifier for the user - typically a UUID, but can be anything.
    */
  @RequiresPrivilege(priv = EDIT_USER_MANAGEMENT)
  def userById(id: String): Option[User] = {
    tleUserService.get(id)
  }

  /**
    * Create a new user with the provided details.
    *
    * @param username the username of the new user
    * @param email the email address of the new user
    * @param firstName the first name of the new user
    * @param lastName the last name of the new user
    * @param password the password of the new user - which will be hashed before storage
    * @return the new user, or an error if the user could not be created
    */
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

  /**
    * Update a user with the provided details - any which are Some.
    *
    * @param username the username of the user to update
    * @param email a new email address for the user
    * @param firstName a new first name for the user
    * @param lastName a new last name for the user
    * @param password a new password for the user - which will be hashed before storage
    * @return the updated user, or an error if the user could not be updated
    */
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

  /**
    * Delete a user by their ID.
    *
    * @param id the database ID of the user to delete - typically a UUID but can be anything.
    */
  @RequiresPrivilege(priv = EDIT_USER_MANAGEMENT)
  def deleteUser(id: String): Either[ProviderError, Unit] =
    ProviderError.Try("Failed to delete user: ") {
      tleUserService.delete(id)
    }

  private implicit def optionalUser(u: TLEUser): Option[User] = Option(u).map(User(_))
}
