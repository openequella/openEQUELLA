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

package com.tle.web.api.hierarchy
import com.dytech.edge.exceptions.ItemNotFoundException
import com.tle.beans.item.ItemId
import com.tle.core.guice.Bind
import com.tle.core.hierarchy.HierarchyService
import com.tle.core.item.service.ItemService
import com.tle.web.api.ApiErrorResponse
import com.tle.web.api.browsehierarchy.HierarchyCompoundUuid
import com.tle.web.api.hierarchy.model.HierarchyTopicAcl
import io.swagger.annotations.{Api, ApiOperation, ApiParam}
import org.jboss.resteasy.annotations.cache.NoCache
import org.slf4j.{Logger, LoggerFactory}

import javax.inject.{Inject, Singleton}
import javax.ws.rs._
import javax.ws.rs.core.Response
import scala.util.{Failure, Success, Try}

@Bind
@Singleton
@NoCache
@Path("hierarchy")
@Produces(Array("application/json"))
@Api("Hierarchy")
class HierarchyResource {
  val Logger: Logger = LoggerFactory.getLogger(getClass)

  @Inject private var hierarchyService: HierarchyService = _
  @Inject private var itemService: ItemService           = _

  @GET
  @Path("/{compound-uuid}/my-acls")
  @ApiOperation(
    value = "Get my ACLs for a hierarchy topic",
    response = classOf[HierarchyTopicAcl]
  )
  def getAcls(
      @ApiParam("The compound UUID") @PathParam("compound-uuid") compoundUuid: String
  ): Response = {
    withValidCompoundUuid(compoundUuid) { hierarchyCompoundUuid =>
      val currentTopicUuid = hierarchyCompoundUuid.uuid
      Option(hierarchyService.getHierarchyTopicByUuid(currentTopicUuid)) match {
        case Some(topicEntity) =>
          Response
            .ok(
              HierarchyTopicAcl(
                hierarchyService.hasViewAccess(topicEntity),
                hierarchyService.hasEditAccess(topicEntity),
                hierarchyService.hasModifyKeyResourceAccess(topicEntity)
              )
            )
            .build
        case None =>
          ApiErrorResponse.resourceNotFound(
            s"Failed to get ACLs: topic $currentTopicUuid not found"
          )
      }
    }
  }

  @POST
  @Path("/{compound-uuid}/keyresource/{item-uuid}/{version}")
  @ApiOperation(
    value = "Add an Item to a Hierarchy topic as a key resource"
  )
  def addKeyResource(
      @ApiParam("The compound UUID") @PathParam("compound-uuid") compoundUuid: String,
      @ApiParam("The item UUID") @PathParam("item-uuid") itemUuid: String,
      @ApiParam("The item version") @PathParam("version") version: Int
  ): Response =
    updateKeyResources(compoundUuid, itemUuid, version)

  @DELETE
  @Path("/{compound-uuid}/keyresource/{item-uuid}/{version}")
  @ApiOperation(
    value = "Delete a key resource from a Hierarchy topic"
  )
  def deleteKeyResource(
      @ApiParam("The compound UUID") @PathParam("compound-uuid") compoundUuid: String,
      @ApiParam("The item UUID") @PathParam("item-uuid") itemUuid: String,
      @ApiParam("The item version: 0 means always point to latest version") @PathParam(
        "version"
      ) version: Int
  ): Response =
    updateKeyResources(compoundUuid, itemUuid, version, addResource = false)

  // Update the list of key resources for a Hierarchy topic with ACL checks.
  private def updateKeyResources(
      compoundUuid: String,
      itemUuid: String,
      itemVersion: Int,
      addResource: Boolean = true
  ) = {
    val realVersion = itemService.getRealVersion(itemVersion, itemUuid)

    val itemId     = new ItemId(itemUuid, itemVersion)
    val realItemId = new ItemId(itemUuid, realVersion)

    withValidCompoundUuid(compoundUuid) { hierarchyCompoundUuid =>
      val currentTopicUuid = hierarchyCompoundUuid.uuid

      def update() =
        Try(itemService.getUnsecure(realItemId)) match {
          case Success(_) => {
            if (addResource) hierarchyService.addKeyResource(hierarchyCompoundUuid, itemId)
            else hierarchyService.deleteKeyResources(hierarchyCompoundUuid, itemId)
            Response.ok.build
          }
          case Failure(e: ItemNotFoundException) =>
            ApiErrorResponse.resourceNotFound(s"Failed to find key resource: ${e.getMessage}")
        }

      Option(hierarchyService.getHierarchyTopicByUuid(currentTopicUuid)) match {
        case Some(topic) if !hierarchyService.hasModifyKeyResourceAccess(topic) =>
          ApiErrorResponse.forbiddenRequest(
            s"Permission denied to update the key resources of topic $compoundUuid"
          )
        case Some(_)
            if addResource && hierarchyService.hasKeyResource(hierarchyCompoundUuid, itemId) =>
          ApiErrorResponse.conflictError(s"Item ${itemId.toString()} is already a key resource")
        case Some(_)
            if !addResource && !hierarchyService.hasKeyResource(hierarchyCompoundUuid, itemId) =>
          ApiErrorResponse.resourceNotFound(
            s"Item ${itemId.toString()} is not a key resource of topic $compoundUuid"
          )
        case Some(_) => update()
        case None =>
          ApiErrorResponse.resourceNotFound(s"Topic $currentTopicUuid not found")
      }
    }
  }

  /** Execute the provided function if the topic UUID is valid.
    *
    * @param compoundUuid
    *   The compound UUID of the topic.
    * @param function
    *   The function to execute.
    */
  private def withValidCompoundUuid(
      compoundUuid: String
  )(
      function: (HierarchyCompoundUuid) => Response
  ): Response = {
    HierarchyCompoundUuid(compoundUuid) match {
      case Left(e) =>
        ApiErrorResponse.serverError(
          s"Failed to parse the compound UUID $compoundUuid: ${e.getMessage}"
        )
      case Right((validCompoundUuid)) => function(validCompoundUuid)
    }
  }
}
