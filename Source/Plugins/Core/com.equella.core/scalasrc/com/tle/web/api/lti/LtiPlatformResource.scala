package com.tle.web.api.lti

import com.tle.legacy.LegacyGuice
import com.tle.web.api.{ApiBatchOperationResponse, ApiErrorResponse}
import io.swagger.annotations.{Api, ApiOperation, ApiParam}
import org.jboss.resteasy.annotations.cache.NoCache
import javax.ws.rs.core.Response
import javax.ws.rs.core.Response.Status
import javax.ws.rs.{DELETE, GET, POST, PUT, Path, PathParam, Produces, QueryParam}
import com.tle.web.api.ApiErrorResponse.badRequest
import com.tle.web.api.lti.LtiPlatformBean.{
  buildLtiPlatformFromParams,
  updateLtiPlatformWithParams,
  validate
}
import com.tle.web.lti13.platforms.security.LTI13PlatformsSettingsPrivilegeTreeProvider

@NoCache
@Path("ltiplatform")
@Produces(Array("application/json"))
@Api("LTI 1.3 Platform")
class LtiPlatformResource {
  private val aclProvider: LTI13PlatformsSettingsPrivilegeTreeProvider = LegacyGuice.ltiPrivProvider
  private val lti13Service                                             = LegacyGuice.lti13Service

  private def platformNotFound(id: String) = s"No LTI Platform matching $id"

  @GET
  @Path("/{id}")
  @ApiOperation(
    value = "Get LTI Platform by ID",
    notes = "This endpoints retrieves a LTI Platform configuration by Platform ID",
    response = classOf[LtiPlatformBean],
  )
  def getPlatform(@ApiParam("Platform ID") @PathParam("id") id: String): Response = {
    aclProvider.checkAuthorised()
    lti13Service.getByPlatformID(id) match {
      case Some(platform) => Response.ok.entity(LtiPlatformBean(platform)).build()
      case None           => ApiErrorResponse.resourceNotFound(platformNotFound(id))
    }
  }

  @GET
  @ApiOperation(
    value = "Get a list of LTI Platform",
    notes = "This endpoints retrieves a list of LTI Platform configuration",
    response = classOf[LtiPlatformBean],
    responseContainer = "List"
  )
  def getPlatform: Response = {
    aclProvider.checkAuthorised()
    Response.ok.entity(lti13Service.getAll.map(LtiPlatformBean.apply)).build()
  }

  @POST
  @ApiOperation(
    value = "Create a new LTI Platform configuration",
    notes =
      "This endpoint creates a new LTI Platform configuration and returns ID of the created entity",
    response = classOf[Long],
  )
  def createPlatform(params: LtiPlatformBean): Response = {
    aclProvider.checkAuthorised()
    validate(params)
      .fold(badRequest(_: _*),
            buildLtiPlatformFromParams
              andThen (lti13Service.create)
              andThen (Response.status(Status.CREATED).entity(_).build()))
  }

  @PUT
  @ApiOperation(
    value = "Update an existing LTI Platform configuration",
    notes = "This endpoint updates an existing LTI Platform configuration",
    response = classOf[Unit],
  )
  def updatePlatform(params: LtiPlatformBean): Response = {
    aclProvider.checkAuthorised()
    val platformId = params.platformId

    lti13Service.getByPlatformID(platformId) match {
      case Some(platform) =>
        validate(params)
          .fold(badRequest(_: _*),
                updateLtiPlatformWithParams(platform)
                  andThen (lti13Service.update)
                  andThen (_ => Response.ok().build()))
      case None => ApiErrorResponse.resourceNotFound(platformNotFound(platformId))
    }
  }

  @DELETE
  @Path("/{id}")
  @ApiOperation(
    value = "Delete a LTI Platform by ID",
    notes = "This endpoints deletes an existing LTI Platform configuration by Platform ID",
    response = classOf[Int],
  )
  def deletePlatform(@ApiParam("Platform ID") @PathParam("id") id: String): Response = {
    aclProvider.checkAuthorised()
    lti13Service.getByPlatformID(id) match {
      case Some(platform) =>
        lti13Service.delete(platform)
        Response.ok.build()
      case None => ApiErrorResponse.resourceNotFound(platformNotFound(id))
    }
  }

  @DELETE
  @ApiOperation(
    value = "Delete multiple LTI Platforms by a list of Platform ID",
    notes = "This endpoints deletes multiple LTI Platform configuration by a list of Platform ID",
    response = classOf[ApiBatchOperationResponse],
    responseContainer = "List"
  )
  def deletePlatforms(
      @ApiParam(value = "List of Platform ID") @QueryParam("ids") ids: Array[String]): Response = {
    aclProvider.checkAuthorised()
    val responses = ids
      .map(id =>
        lti13Service.getByPlatformID(id) match {
          case Some(platform) =>
            lti13Service.delete(platform)
            ApiBatchOperationResponse(id, 200, s"Platform $id has been deleted.")
          case None => ApiBatchOperationResponse(id, 404, platformNotFound(id))
      })
      .toList
    Response.status(207).entity(responses).build()
  }
}
