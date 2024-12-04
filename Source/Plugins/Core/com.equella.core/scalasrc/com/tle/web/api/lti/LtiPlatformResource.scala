/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0, (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.web.api.lti

import cats.data.Validated.{Invalid, Valid}
import cats.implicits._
import com.tle.core.lti13.bean.LtiPlatformBean
import com.tle.core.lti13.bean.LtiPlatformBean.validateLtiPlatformBean
import com.tle.legacy.LegacyGuice
import com.tle.web.api.{ApiBatchOperationResponse, ApiErrorResponse}
import com.tle.web.lti13.platforms.security.LTI13PlatformsSettingsPrivilegeTreeProvider
import io.swagger.annotations.{Api, ApiOperation, ApiParam}
import org.jboss.resteasy.annotations.cache.NoCache
import org.slf4j.{Logger, LoggerFactory}
import java.net.{URI, URLDecoder, URLEncoder}
import java.nio.charset.StandardCharsets
import javax.ws.rs.core.{MediaType, Response}
import javax.ws.rs._

class UpdateEnabledStatusRequest {
  var platformId: String = _
  var enabled: Boolean   = _

  def getPlatformId(): String = platformId

  def setPlatformId(value: String): Unit = {
    platformId = value
  }

  def getEnabled(): Boolean = enabled

  def setEnabled(value: Boolean): Unit = {
    enabled = value
  }
}

@NoCache
@Path("ltiplatform")
@Produces(Array("application/json"))
@Api("LTI 1.3 Platform")
class LtiPlatformResource {
  private val aclProvider: LTI13PlatformsSettingsPrivilegeTreeProvider = LegacyGuice.ltiPrivProvider
  private val ltiPlatformService = LegacyGuice.ltiPlatformService
  private val logger: Logger     = LoggerFactory.getLogger(classOf[LtiPlatformResource])

  // Lazily evaluated the result and capture the potential exceptions in an Either. Use the provided handler to build a response for success, or
  // return a server error response if any exception is triggered.
  private def processResult[A](result: => A, onSuccess: A => Response, onFailureMessage: String) =
    Either.catchNonFatal(result) match {
      case Left(error) =>
        logger.error(onFailureMessage, error)
        ApiErrorResponse.serverError(s"$onFailureMessage : ${error.getMessage}")
      case Right(result) => onSuccess(result)
    }

  // Similar to `processResult` but handle the situation where the target LTI platform may not exist.
  private def processOptionResult[A](
      result: => Option[A],
      onSome: A => Response,
      onNone: () => Response = () =>
        ApiErrorResponse.resourceNotFound("Target LTI platform is not found."),
      onFailureMessage: String
  ) = {
    def processOption(maybeResult: Option[A]): Response =
      maybeResult.map(onSome).getOrElse(onNone())

    processResult[Option[A]](result, processOption, onFailureMessage)
  }

  private def decodeId(id: String): String = URLDecoder.decode(id, StandardCharsets.UTF_8.name())

  @GET
  @Path("/{id}")
  @ApiOperation(
    value = "Get LTI Platform by ID",
    notes = "This endpoints retrieves a LTI Platform by Platform ID",
    response = classOf[LtiPlatformBean]
  )
  def getPlatform(
      @ApiParam(
        "The Platform ID has to be double URL encoded to protect against premature decoding."
      ) @PathParam("id") id: String
  ): Response = {
    aclProvider.checkAuthorised()

    processOptionResult[LtiPlatformBean](
      ltiPlatformService.getByPlatformID(decodeId(id)),
      Response.ok(_).build,
      onFailureMessage = s"Failed to get LTI platform by ID $id"
    )

  }

  @GET
  @ApiOperation(
    value = "Get a list of LTI Platforms",
    notes = "This endpoints retrieves a list of enabled or disabled LTI Platforms",
    response = classOf[LtiPlatformBean],
    responseContainer = "List"
  )
  def getPlatforms: Response = {
    aclProvider.checkAuthorised()

    processResult[List[LtiPlatformBean]](
      ltiPlatformService.getAll,
      Response.ok.entity(_).build,
      "Failed to get a list of LTI platform"
    )
  }

  @POST
  @ApiOperation(
    value = "Create a new LTI platform",
    notes =
      "This endpoint creates a new LTI platform and includes ID of the created platform in the response header. " +
        "The ID has to be double URL encoded to protect against premature decoding.",
    response = classOf[String]
  )
  def createPlatform(bean: LtiPlatformBean): Response = {
    aclProvider.checkAuthorised()

    def urlEncode(s: String): String = URLEncoder.encode(s, StandardCharsets.UTF_8.name())
    def create(bean: LtiPlatformBean): Response =
      processResult[String](
        ltiPlatformService.create(bean),
        id =>
          Response
            // Double encoding the ID to make sure any forward slash is fully escaped due to the Tomcat 'no slash' issue.
            .created(new URI(s"/ltiplatform/${urlEncode(urlEncode(id))}"))
            .build(),
        "Failed to create a new LTI platform"
      )

    validateLtiPlatformBean(bean) match {
      case Invalid(error) => ApiErrorResponse.badRequest(error: _*)
      case Valid(bean)    => create(bean)
    }
  }

  @PUT
  @ApiOperation(
    value = "Update an existing LTI platform",
    notes = "This endpoint updates an existing LTI platform",
    response = classOf[Unit]
  )
  def updatePlatform(updates: LtiPlatformBean): Response = {
    aclProvider.checkAuthorised()

    def update: Response =
      processOptionResult[String](
        ltiPlatformService.update(updates),
        Response.ok(_).build,
        onFailureMessage = "Failed to update LTI platform"
      )

    validateLtiPlatformBean(updates) match {
      case Invalid(error) => ApiErrorResponse.badRequest(error: _*)
      case Valid(_)       => update
    }
  }

  @DELETE
  @Path("/{id}")
  @ApiOperation(
    value = "Delete a LTI platform by ID",
    notes = "This endpoints deletes an existing LTI platform by platform ID",
    response = classOf[Int]
  )
  def deletePlatform(
      @ApiParam(
        "The Platform ID has to be double URL encoded to protect against premature decoding."
      ) @PathParam("id") id: String
  ): Response = {
    aclProvider.checkAuthorised()

    processOptionResult[Unit](
      ltiPlatformService.delete(decodeId(id)),
      Response.ok(_).build,
      onFailureMessage = s"Failed to delete LTI platform by ID $id"
    )
  }

  @DELETE
  @ApiOperation(
    value = "Delete multiple LTI platforms by a list of Platform ID",
    notes = "This endpoints deletes multiple LTI platforms by a list of platform ID",
    response = classOf[ApiBatchOperationResponse],
    responseContainer = "List"
  )
  def deletePlatforms(
      @ApiParam(value = "List of Platform ID") @QueryParam("ids") ids: Array[String]
  ): Response = {
    aclProvider.checkAuthorised()

    def delete(id: String): ApiBatchOperationResponse = {
      Either.catchNonFatal(ltiPlatformService.delete(id)) match {
        case Left(error) =>
          ApiBatchOperationResponse(
            id,
            500,
            s"Failed to delete platform for $id : ${error.getMessage}"
          )
        case Right(maybeDeleted) =>
          maybeDeleted
            .map(_ => ApiBatchOperationResponse(id, 200, s"Platform $id has been deleted."))
            .getOrElse(ApiBatchOperationResponse(id, 404, s"No LTI Platform matching $id"))
      }
    }

    val responses = ids
      .map(delete)
      .toList

    Response.status(207).entity(responses).build()
  }

  @PUT
  @Path("/enabled")
  @Consumes(Array(MediaType.APPLICATION_JSON))
  @ApiOperation(
    value = "Enable or disable multiple LTI platforms by a list of Platform ID with boolean flag",
    notes =
      "This endpoints enables/disables multiple LTI platforms by a list of platform ID with boolean flag",
    response = classOf[ApiBatchOperationResponse],
    responseContainer = "List"
  )
  def enabledPlatforms(idWithStatus: Array[UpdateEnabledStatusRequest]): Response = {
    aclProvider.checkAuthorised()

    def updateEnabledStatus(idWithStatus: UpdateEnabledStatusRequest): ApiBatchOperationResponse = {
      val id = idWithStatus.platformId

      ltiPlatformService
        .getByPlatformID(id)
        .toRight(ApiBatchOperationResponse(id, 404, s"No LTI Platform matching $id"))
        .flatMap { p =>
          {
            val updatedPlatform = p.copy(enabled = idWithStatus.enabled)
            ltiPlatformService
              .update(updatedPlatform)
              .toRight(ApiBatchOperationResponse(id, 500, s"Failed to update platform $id"))
          }
        }
        .fold(
          e => identity(e),
          r => ApiBatchOperationResponse(r, 200, s"Platform $id has been updated.")
        )
    }

    val responses = idWithStatus.map(updateEnabledStatus).toList

    Response.status(207).entity(responses).build()
  }

  @GET
  @Path("/{id}/rotated-keys")
  @ApiOperation(
    value = "Rotate Key Pair For the specified Platform",
    notes = "This endpoint will rotate the activated key pair for an LTI platform.",
    response = classOf[String]
  )
  def rotateKeyPair(
      @ApiParam(
        "The Platform ID has to be double URL encoded to protect against premature decoding."
      ) @PathParam("id") id: String
  ): Response = {
    aclProvider.checkAuthorised()

    val result = ltiPlatformService.rotateKeyPairForPlatform(decodeId(id));

    result match {
      case Right(keySetId) =>
        Response.ok(keySetId).build()
      case Left(errorMessage) =>
        logger.error(errorMessage)
        ApiErrorResponse.serverError(s"Failed to rotate key pair : ${errorMessage}")
    }
  }
}
