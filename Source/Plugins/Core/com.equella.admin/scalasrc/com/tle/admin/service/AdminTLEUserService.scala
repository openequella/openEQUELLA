package com.tle.admin.service

import io.github.openequella.graphql.ClientConfiguration
import io.github.openequella.graphql.api.TleUserApi
import org.slf4j.{Logger, LoggerFactory}

/**
  * Service class for admin operations on TLEUser objects via the GraphQL library.
  */
class AdminTLEUserService(implicit val cfg: ClientConfiguration) {
  private val LOGGER: Logger = LoggerFactory.getLogger(classOf[AdminTLEUserService])

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
