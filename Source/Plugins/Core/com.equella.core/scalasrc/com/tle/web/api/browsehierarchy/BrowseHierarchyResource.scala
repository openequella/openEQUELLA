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

package com.tle.web.api.browsehierarchy

import com.dytech.edge.exceptions.ItemNotFoundException
import com.tle.beans.item.ItemId
import com.tle.core.guice.Bind
import com.tle.core.hierarchy.HierarchyService
import com.tle.core.item.service.ItemService
import com.tle.web.api.ApiErrorResponse
import com.tle.web.api.browsehierarchy.model.{HierarchyTopic, HierarchyTopicSummary}
import io.swagger.annotations.{Api, ApiOperation, ApiParam}
import org.jboss.resteasy.annotations.cache.NoCache

import javax.inject.{Inject, Singleton}
import javax.ws.rs.core.Response
import javax.ws.rs.{GET, Path, PathParam, Produces}
import scala.jdk.CollectionConverters._
import scala.util.{Failure, Success, Try}

@Bind
@Singleton
@NoCache
@Path("browsehierarchy2")
@Produces(Array("application/json"))
@Api("Hierarchy browsing V2")
class BrowseHierarchyResource {
  @Inject private var browseHierarchyHelper: BrowseHierarchyHelper = _
  @Inject private var hierarchyService: HierarchyService           = _
  @Inject private var itemService: ItemService                     = _

  @GET
  @ApiOperation(
    value = "Browse hierarchies",
    notes = "Retrieve all Hierarchy topics for the current institution.",
    responseContainer = "List",
    response = classOf[HierarchyTopicSummary]
  )
  def browseHierarchies(): Response = {
    // Fetch all topics with permission check for `VIEW_HIERARCHY_TOPIC`
    val topLevelHierarchies = hierarchyService.getRootTopics
    val result =
      hierarchyService
        .expandVirtualisedTopics(topLevelHierarchies, null, null)
        .asScala
        .toList
        .map(browseHierarchyHelper.buildHierarchyTopicSummary(_))

    Response.ok(result).build
  }

  @GET
  @Path("/{compound-uuid}")
  @ApiOperation(
    value = "Browse a hierarchy",
    notes =
      "Retrieve a Hierarchy topic details for a given topic compound UUID. This compound UUID MUST include compound UUIDs of all the virtual parent topic, seperated by comma.",
    response = classOf[HierarchyTopic]
  )
  def browseHierarchy(
      @ApiParam("The compound ID") @PathParam("compound-uuid") compoundUuids: String
  ): Response = {
    val hierarchyCompoundUuid = HierarchyCompoundUuid(compoundUuids)
    val HierarchyCompoundUuid(topicUuid, currentVirtualTopicName, parentCompoundUuidList) =
      hierarchyCompoundUuid

    Option(hierarchyService.getHierarchyTopicByUuid(topicUuid)) match {
      case Some(topic) if !hierarchyService.hasViewAccess(topic) =>
        ApiErrorResponse.forbiddenRequest(s"Permission denied to access topic $topicUuid")
      case Some(topicEntity) =>
        val topicSummary =
          browseHierarchyHelper.getTopicSummary(
            topicEntity,
            currentVirtualTopicName,
            parentCompoundUuidList
          )
        val parents =
          browseHierarchyHelper.getParents(
            topicEntity,
            parentCompoundUuidList.getOrElse(List.empty)
          )
        val allKeyResources =
          browseHierarchyHelper.getKeyResources(hierarchyCompoundUuid)

        val result = HierarchyTopic(topicSummary, parents, allKeyResources)
        Response.ok(result).build
      case None => ApiErrorResponse.resourceNotFound(s"Topic $topicUuid not found")
    }
  }

  @GET
  @Path("/key-resource/{item-uuid}/{version}")
  @ApiOperation(
    value = "Get all hierarchy IDs which have the provided key resource",
    response = classOf[List[String]]
  )
  def getHierarchyIdsWithKeyResource(
      @ApiParam("The item UUID") @PathParam("item-uuid") itemUuid: String,
      @ApiParam("The item version") @PathParam("version") itemVersion: Int
  ): Response = {
    val version = itemService.getRealVersion(itemVersion, itemUuid)
    val itemId  = new ItemId(itemUuid, version)
    Try(itemService.getUnsecure(itemId)) match {
      case Success(item) =>
        val ids = hierarchyService
          .getTopicIdsWithKeyResource(item)
          .asScala
          .toList
          .map(legacyCompoundUuid =>
            HierarchyCompoundUuid(legacyCompoundUuid, inLegacyFormat = true).buildString(false)
          )
        Response.ok(ids).build()
      case Failure(e: ItemNotFoundException) =>
        ApiErrorResponse.resourceNotFound(s"Failed to find key resource: ${e.getMessage}")
    }
  }
}
