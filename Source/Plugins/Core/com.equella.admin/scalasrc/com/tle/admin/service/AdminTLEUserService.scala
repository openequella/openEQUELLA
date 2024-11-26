package com.tle.admin.service

import com.tle.beans.user.TLEUser
import io.github.openequella.graphql.ClientConfiguration
import io.github.openequella.graphql.api.{TleUserApi, TleUserView}
import org.slf4j.{Logger, LoggerFactory}

import java.util.Optional

/**
  * Service class for admin operations on TLEUser objects via the GraphQL library.
  */
class AdminTLEUserService(implicit val cfg: ClientConfiguration) {
  private val LOGGER: Logger = LoggerFactory.getLogger(classOf[AdminTLEUserService])

  private implicit def tleUserViewToTleUser(view: TleUserView): TLEUser = {
    val u = new TLEUser()
    u.setUuid(view.uniqueId)
    u.setUsername(view.username)
    u.setFirstName(view.firstName)
    u.setLastName(view.lastName)
    u.setEmailAddress(view.email.orNull)

    u
  }

  // Turns out this is not called in any of the admin console code.
  def getByUsername(username: String): Optional[TLEUser] = {
    LOGGER.debug("Getting user by username: " + username)
    TleUserApi.getByUsername(username) match {
      case Right(user) =>
        user
          .fold[Optional[TLEUser]]({
            LOGGER.debug(s"User [$username] not found")
            Optional.empty()
          })(u => {
            LOGGER.debug(s"User [$username] found")
            Optional.of(u)
          })
      case Left(errors) =>
        throw new ClientRequestException(s"Error retrieving user [$username]", errors)
    }
  }

  /**
    * Delete a user by UUID.
    *
    * @param uuid the UUID of the user to delete
    * @throws ClientRequestException if there are any errors deleting the user
    */
  def delete(uuid: String): Unit = {
    LOGGER.debug("Deleting user with UUID: " + uuid)
    TleUserApi.deleteUser(uuid) match {
      case Right(_)     => LOGGER.debug(s"User [$uuid] deleted")
      case Left(errors) => throw new ClientRequestException(s"Error deleting user [$uuid]", errors)
    }
  }
}
