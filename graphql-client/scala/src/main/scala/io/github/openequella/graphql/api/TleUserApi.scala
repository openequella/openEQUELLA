package io.github.openequella.graphql.api

import io.github.openequella.graphql.client.{Mutations, Queries, User}
import io.github.openequella.graphql.{Client, ClientConfiguration}

/**
  * Represents an internal openEQUELLA user.
  *
  * @param uniqueId  The unique identifier of the user.
  * @param username  The username of the user.
  * @param email     The email address of the user.
  * @param firstName The first name of the user.
  * @param lastName  The last name of the user.
  */
case class TleUserView(
    uniqueId: String,
    username: String,
    email: Option[String],
    firstName: String,
    lastName: String
)

/**
  * Provides access to the openEQUELLA internal user API.
  */
object TleUserApi {
  private val tleUser = (
    User.uniqueId ~ User.username ~ User.email ~ User.firstName ~ User.lastName
  ).mapN(TleUserView)

  /**
    * Retrieves the details of an individual user by their unique identifier.
    *
    * @param uniqueId The unique identifier of the user.
    * @param cfg     The client configuration.
    * @return       Left containing a list of errors or Right if the operation was successful.
    *               Some if user was found, None if not.
    */
  def getByUniqueId(uniqueId: String)(
      implicit cfg: ClientConfiguration
  ): Either[List[ApiError], Option[TleUserView]] = {
    val query = Queries.internalUserById(uniqueId) {
      tleUser
    }

    Client.query(query)
  }

  /**
    * Retrieves the details of an individual user by their username.
    *
    * @param username The username of the user.
    * @param cfg The client configuration.
    * @return Left containing a list of errors or Right if the operation was successful.
    *         Some if user was found, None if not.
    */
  def getByUsername(username: String)(
      implicit cfg: ClientConfiguration): Either[List[ApiError], Option[TleUserView]] = {
    val query = Queries.internalUserByUsername(username) {
      tleUser
    }

    Client.query(query)
  }

  /**
    * Creates a new user.
    *
    * @param cfg The client configuration.
    * @return Left containing a list of errors or Right with the new user's details if the operation
    *         was successful.
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

    liftResult {
      Client.mutate(query)
    }
  }

  /**
    * Deletes a user.
    *
    * @param uniqueId The unique identifier of the user.
    * @param cfg The client configuration.
    * @return Left containing a list of errors or Right if the operation was successful.
    */
  def deleteUser(uniqueId: String)(
      implicit cfg: ClientConfiguration): Either[List[ApiError], Unit] = {
    val query = Mutations.internalUserDelete(uniqueId)

    liftResult {
      Client.mutate(query)
    }
  }
}
