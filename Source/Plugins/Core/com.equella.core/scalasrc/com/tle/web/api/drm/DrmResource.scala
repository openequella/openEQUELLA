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

package com.tle.web.api.drm

import com.dytech.edge.exceptions.{DRMException, ItemNotFoundException}
import com.tle.beans.item.{DrmSettings, Item, ItemId}
import com.tle.common.usermanagement.user.CurrentUser
import com.tle.exceptions.AccessDeniedException
import com.tle.legacy.LegacyGuice
import com.tle.web.api.ApiErrorResponse.{
  badRequest,
  forbiddenRequest,
  resourceNotFound,
  unauthorizedRequest
}
import io.swagger.annotations.{Api, ApiOperation, ApiParam}
import org.jboss.resteasy.annotations.cache.NoCache
import javax.ws.rs.core.Response
import javax.ws.rs.{BadRequestException, GET, NotFoundException, POST, Path, PathParam, Produces}
import scala.util.control.Exception.allCatch
import scala.util.{Failure, Success, Try}

/** Typically used to provide why a DRM Item is unauthorised to view.
  */
case class DrmViolation(violation: String)

@NoCache
@Path("item/{uuid}/{version}/drm")
@Produces(Array("application/json"))
@Api("Item DRM")
class DrmResource {
  val drmService = LegacyGuice.drmService

  @GET
  @ApiOperation(
    value = "List DRM terms",
    notes = "This endpoint is used to list an Item's DRM terms.",
    response = classOf[ItemDrmDetails]
  )
  def getDrmTerms(
      @ApiParam("Item UUID") @PathParam("uuid") uuid: String,
      @ApiParam("Item Version") @PathParam("version") version: Int
  ): Response = {
    val getTerms = (uuid: String, version: Int) =>
      Try {
        getItem.andThen(_.getDrmSettings)(new ItemId(uuid, version))
      }

    val mapToItemDrmDetails = (drm: DrmSettings) =>
      Try {
        Option(drm) match {
          case Some(drmSettings) => ItemDrmDetails(drmSettings)
          case None =>
            throw new NotFoundException(s"Failed to find DRM terms for item: $uuid/$version")
        }
      }

    val result = getTerms(uuid, version) flatMap mapToItemDrmDetails
    respond(result)
  }

  @POST
  @ApiOperation(
    value = "Accept DRM terms",
    notes = "This endpoint is used to accept an Item's DRM terms.",
    response = classOf[Unit]
  )
  def acceptDrm(
      @ApiParam("Item UUID") @PathParam("uuid") uuid: String,
      @ApiParam("Item Version") @PathParam("version") version: Int
  ): Response = {

    val acceptLicense: Item => Unit =
      drmService.acceptLicenseOrThrow // Explicitly discard the internal DB ID and use Unit instead.
    val result = allCatch withTry (getItem andThen acceptLicense)(new ItemId(uuid, version))
    respond(result)
  }

  @GET
  @Path("/violations")
  @ApiOperation(
    value = "List DRM violations",
    notes = "This endpoint is used to List why a DRM Item is unauthorised to view.",
    response = classOf[DrmViolation]
  )
  def getDrmViolations(
      @ApiParam("Item UUID") @PathParam("uuid") uuid: String,
      @ApiParam("Item Version") @PathParam("version") version: Int
  ): Response = {
    val isAuthorised: Item => Unit = (item: Item) =>
      drmService.isAuthorised(item, CurrentUser.getUserState.getIpAddress)

    Try {
      (getItem andThen isAuthorised)(new ItemId(uuid, version))
    } match {
      case Success(_)               => badRequest(s"Item ${uuid}/${version} is authorised.")
      case Failure(e: DRMException) => Response.ok().entity(DrmViolation(e.getMessage)).build()
      case Failure(e)               => mapException(e)(Seq(e.getMessage))
    }
  }

  // Take a subtype of Throwable and return a function which takes a sequence of string and returns a Response.
  private def mapException[T <: Throwable](e: T): Seq[String] => Response = {
    e match {
      case _: BadRequestException                               => badRequest
      case _: AccessDeniedException                             => forbiddenRequest
      case _: DRMException                                      => unauthorizedRequest
      case _ @(_: ItemNotFoundException | _: NotFoundException) => resourceNotFound
    }
  }

  private def respond[T](attempt: Try[T]): Response =
    attempt match {
      case Success(result) => Response.ok().entity(result).build()
      case Failure(e)      => mapException(e)(Seq(e.getMessage))
    }

  private val getItem: ItemId => Item = LegacyGuice.itemService.getUnsecure
}
