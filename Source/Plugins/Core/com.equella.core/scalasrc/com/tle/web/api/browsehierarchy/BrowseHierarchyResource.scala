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
    response = classOf[HierarchyTopicSummary],
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
    response = classOf[HierarchyTopic],
  )
  def browseHierarchy(
      @ApiParam("The compound ID") @PathParam("compound-uuid") compoundUuids: String): Response = {
    val topicsCompoundUuids = compoundUuids.split(",")

    val currentTopicCompoundUuid = topicsCompoundUuids.headOption.getOrElse(compoundUuids)
    val (currentTopicUuid, currentVirtualTopicName) =
      browseHierarchyHelper.getUuidAndName(currentTopicCompoundUuid)
    val parentCompoundUuidMap =
      topicsCompoundUuids.tail.flatMap(browseHierarchyHelper.buildCompoundUuidMap).toMap

    Option(hierarchyService.getHierarchyTopicByUuid(currentTopicUuid)) match {
      case Some(topic) if !hierarchyService.hasViewAccess(topic) =>
        ApiErrorResponse.forbiddenRequest(s"Permission denied to access topic $currentTopicUuid")
      case Some(topicEntity) =>
        val topicSummary =
          browseHierarchyHelper.getTopicSummary(topicEntity,
                                                currentVirtualTopicName,
                                                parentCompoundUuidMap)
        val parents         = browseHierarchyHelper.getParents(topicEntity, parentCompoundUuidMap)
        val allKeyResources = browseHierarchyHelper.getAllKeyResources(topicEntity, compoundUuids)

        val result = HierarchyTopic(topicSummary, parents, allKeyResources)
        Response.ok(result).build
      case None => ApiErrorResponse.resourceNotFound(s"Topic $currentTopicUuid not found")
    }
  }
}
