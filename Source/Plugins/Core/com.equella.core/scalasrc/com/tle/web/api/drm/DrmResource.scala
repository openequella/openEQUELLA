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
import com.tle.beans.item.ItemId
import com.tle.legacy.LegacyGuice
import com.tle.web.api.ApiErrorResponse.{
  badRequest,
  forbiddenRequest,
  resourceNotFound,
  unauthorizedRequest
}
import io.swagger.annotations.{Api, ApiParam}
import org.jboss.resteasy.annotations.cache.NoCache
import javax.ws.rs.core.Response
import javax.ws.rs.{BadRequestException, GET, POST, Path, PathParam, Produces}
import com.tle.exceptions.AccessDeniedException;

@NoCache
@Path("drm")
@Produces(Array("application/json"))
@Api("DRM")
class DrmResource {
  val drmService = LegacyGuice.drmService

  @GET
  @Path("/{uuid}/{version}")
  def getDRMTerms(@ApiParam("Item UUID") @PathParam("uuid") uuid: String,
                  @ApiParam("Item Version") @PathParam("version") version: Int): Response = {
    try {
      val item = LegacyGuice.itemService.getUnsecure(new ItemId(uuid, version))
      Option(item.getDrmSettings) match {
        case Some(drmSettings) => Response.ok().entity(ItemDrmDetails(drmSettings)).build()
        case None              => resourceNotFound(s"Failed to find DRM terms for item: $uuid/$version")
      }
    } catch {
      case e: ItemNotFoundException => resourceNotFound(e.getMessage)
    }
  }

  @POST
  @Path("/{uuid}/{version}")
  def acceptDRM(@ApiParam("Item UUID") @PathParam("uuid") uuid: String,
                @ApiParam("Item Version") @PathParam("version") version: Int): Response = {
    try {
      val item = LegacyGuice.itemService.getUnsecure(new ItemId(uuid, version))
      Response.ok().entity(drmService.acceptLicenseOrThrow(item)).build()
    } catch {
      case e: BadRequestException   => badRequest(e.getMessage)
      case e: AccessDeniedException => forbiddenRequest(e.getMessage)
      case e: DRMException          => unauthorizedRequest(e.getMessage)
      case e: ItemNotFoundException => resourceNotFound(e.getMessage)
    }
  }
}
