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
import com.tle.core.lti13.bean.LtiPlatformBean
import com.tle.core.lti13.bean.LtiPlatformBean.validateLtiPlatformBean
import com.tle.legacy.LegacyGuice
import com.tle.web.api.{ApiBatchOperationResponse, ApiErrorResponse}
import io.swagger.annotations.{Api, ApiOperation, ApiParam}
import org.jboss.resteasy.annotations.cache.NoCache
import com.tle.beans.lti.LtiPlatform
import javax.ws.rs.core.Response
import javax.ws.rs.{DELETE, GET, POST, PUT, Path, PathParam, Produces, QueryParam}
import com.tle.web.lti13.platforms.security.LTI13PlatformsSettingsPrivilegeTreeProvider
import org.slf4j.{Logger, LoggerFactory}
import java.net.URI

@NoCache
@Path("ltiplatform")
@Produces(Array("application/json"))
@Api("LTI 1.3 Platform")
class LtiPlatformResource {
  private val aclProvider: LTI13PlatformsSettingsPrivilegeTreeProvider = LegacyGuice.ltiPrivProvider
  private val ltiPlatformService                                       = LegacyGuice.ltiPlatformService
  private val logger: Logger                                           = LoggerFactory.getLogger(classOf[LtiPlatformResource])

  private def serverErrorResponse(error: Throwable, message: String) = {
    logger.error(message, error)
    ApiErrorResponse.serverError(s"$message : ${error.getMessage}")
  }

  private def platformNotFound(id: String) = s"No LTI platform matching ID: $id"

  @GET
  @Path("/{id}")
  @ApiOperation(
    value = "Get LTI Platform by ID",
    notes = "This endpoints retrieves a LTI Platform by Platform ID",
    response = classOf[LtiPlatformBean],
  )
  def getPlatform(@ApiParam("Platform ID") @PathParam("id") id: String): Response = {
    aclProvider.checkAuthorised()

    val result: Either[Throwable, Option[LtiPlatform]] = ltiPlatformService.getByPlatformID(id)
    result match {
      case Left(error) => serverErrorResponse(error, s"Failed to get LTI platform by ID $id")
      case Right(maybePlatform) =>
        maybePlatform
          .map(platform => Response.ok(LtiPlatformBean(platform)).build())
          .getOrElse(ApiErrorResponse.resourceNotFound(platformNotFound(id)))
    }
  }

  @GET
  @ApiOperation(
    value = "Get a list of LTI Platform",
    notes = "This endpoints retrieves a list of LTI Platform",
    response = classOf[LtiPlatformBean],
    responseContainer = "List"
  )
  def getPlatforms: Response = {
    aclProvider.checkAuthorised()

    val result: Either[Throwable, List[LtiPlatform]] = ltiPlatformService.getAll
    result match {
      case Left(error) => serverErrorResponse(error, s"Failed to get a list of LTI platform")
      case Right(platforms) =>
        val beans = platforms.map(LtiPlatformBean.apply)
        Response.ok.entity(beans).build()
    }
  }

  @POST
  @ApiOperation(
    value = "Create a new LTI platform",
    notes = "This endpoint creates a new LTI platform and returns ID of the created platform",
    response = classOf[Long],
  )
  def createPlatform(bean: LtiPlatformBean): Response = {
    aclProvider.checkAuthorised()

    def create(bean: LtiPlatformBean): Response = {
      val result = ltiPlatformService.create(bean)
      result match {
        case Left(error) => serverErrorResponse(error, s"Failed to create a new LTI platform")
        case Right(id) =>
          Response
            .created(new URI(s"/ltiplatform/$id"))
            .build()
      }
    }

    validateLtiPlatformBean(bean) match {
      case Invalid(error) => ApiErrorResponse.badRequest(error: _*)
      case Valid(bean)    => create(bean)
    }
  }

  @PUT
  @ApiOperation(
    value = "Update an existing LTI platform",
    notes = "This endpoint updates an existing LTI platform",
    response = classOf[Unit],
  )
  def updatePlatform(updates: LtiPlatformBean): Response = {
    aclProvider.checkAuthorised()

    def update: Response = {
      val result: Either[Throwable, Option[Unit]] = ltiPlatformService.update(updates)
      result match {
        case Left(error) => serverErrorResponse(error, s"Failed to update LTI platform")
        case Right(maybeUpdated) =>
          maybeUpdated
            .map(_ => Response.ok.build)
            .getOrElse(ApiErrorResponse.resourceNotFound(platformNotFound(updates.platformId)))
      }
    }

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
    response = classOf[Int],
  )
  def deletePlatform(@ApiParam("Platform ID") @PathParam("id") id: String): Response = {
    aclProvider.checkAuthorised()

    val result: Either[Throwable, Option[Unit]] = ltiPlatformService.delete(id)
    result match {
      case Left(error) => serverErrorResponse(error, s"Failed to delete LTI platform by ID $id")
      case Right(maybeDeleted) =>
        maybeDeleted
          .map(_ => Response.ok.build)
          .getOrElse(ApiErrorResponse.resourceNotFound(platformNotFound(id)))
    }
  }

  @DELETE
  @ApiOperation(
    value = "Delete multiple LTI platforms by a list of Platform ID",
    notes = "This endpoints deletes multiple LTI platforms by a list of platform ID",
    response = classOf[ApiBatchOperationResponse],
    responseContainer = "List"
  )
  def deletePlatforms(
      @ApiParam(value = "List of Platform ID") @QueryParam("ids") ids: Array[String]): Response = {
    aclProvider.checkAuthorised()

    def errorResponse(id: String, error: Throwable) =
      ApiBatchOperationResponse(id, 500, s"Failed to delete platform for $id : ${error.getMessage}")

    def delete(id: String): ApiBatchOperationResponse = {
      val result: Either[Throwable, Option[Unit]] = ltiPlatformService.delete(id)
      result match {
        case Left(error) =>
          errorResponse(id, error)
        case Right(_) => ApiBatchOperationResponse(id, 200, s"Platform $id has been deleted.")
      }
    }

    def deleteIfExists(id: String): ApiBatchOperationResponse = {
      val result: Either[Throwable, Option[LtiPlatform]] = ltiPlatformService.getByPlatformID(id)
      result match {
        case Left(error) =>
          errorResponse(id, error)
        case Right(maybePlatform) =>
          maybePlatform
            .map(p => delete(p.platformId))
            .getOrElse(ApiBatchOperationResponse(id, 404, s"No LTI Platform matching $id"))
      }
    }

    val responses = ids
      .map(deleteIfExists)
      .toList

    Response.status(207).entity(responses).build()
  }
}
