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

import cats.implicits.toBifunctorOps
import com.dytech.edge.exceptions.ItemNotFoundException
import com.tle.beans.hierarchy.{HierarchyTopic => HierarchyTopicEntity}
import com.tle.beans.item.ItemId
import com.tle.common.util.CollectionUtils.convertEmptyListToNone
import com.tle.core.guice.Bind
import com.tle.core.hierarchy.HierarchyService
import com.tle.core.item.service.ItemService
import com.tle.web.api.ApiErrorResponse
import com.tle.web.api.browsehierarchy.model.{HierarchyTopic, HierarchyTopicSummary}
import io.swagger.annotations.{Api, ApiOperation, ApiParam}
import org.jboss.resteasy.annotations.cache.NoCache

import javax.inject.{Inject, Singleton}
import javax.ws.rs._
import javax.ws.rs.core.Response
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
    value = "Browse root hierarchies",
    notes = "Retrieve all root Hierarchy topics for the current institution.",
    responseContainer = "List",
    response = classOf[HierarchyTopicSummary]
  )
  def browseRootHierarchies(
      @ApiParam("Collection UUID(s) to filter by")
      @QueryParam("collections") collectionUuids: java.util.List[String]
  ): Response = {
    // The default value of 'collection' is an empty list.
    // Convert empty collection list to None, since an empty list means filtering out all resources.
    val collectionsFilter = convertEmptyListToNone(collectionUuids)
    val rootTopics        = browseHierarchyHelper.getRootTopics(collectionsFilter)
    Response.ok(rootTopics).build
  }

  @GET
  @Path("/{compound-uuid}")
  @ApiOperation(
    value = "Browse sub hierarchies",
    notes = "Retrieve all sub Hierarchy topics for the provided parent topic.",
    responseContainer = "List",
    response = classOf[HierarchyTopicSummary]
  )
  def browseSubHierarchies(
      @ApiParam("The compound ID") @PathParam("compound-uuid") compoundUuid: String,
      @ApiParam("Collection UUID(s) to filter by") @QueryParam(
        "collections"
      ) collectionUuids: java.util.List[String]
  ): Response = {
    val collectionsFilter = convertEmptyListToNone(collectionUuids)
    withTopic(compoundUuid) { (topicEntity, currentVirtualTopicName, parentCompoundUuidList, _) =>
      {
        val children = browseHierarchyHelper.getChildren(
          topicEntity,
          currentVirtualTopicName,
          parentCompoundUuidList,
          collectionsFilter
        )
        Response.ok(children).build()
      }
    }
  }

  @GET
  @Path("/details/{compound-uuid}")
  @ApiOperation(
    value = "Get Hierarchy topic details",
    notes =
      "Retrieve a Hierarchy topic details for a given topic compound UUID. This compound UUID MUST include compound UUIDs of all the virtual parent topic, seperated by comma.",
    response = classOf[HierarchyTopic]
  )
  def browseHierarchyDetails(
      @ApiParam("The compound ID") @PathParam("compound-uuid") compoundUuid: String,
      @ApiParam("Collection UUID(s) to filter by") @QueryParam(
        "collections"
      ) collectionUuids: java.util.List[String]
  ): Response = {
    val collectionsFilter = convertEmptyListToNone(collectionUuids)

    withTopic(compoundUuid) {
      (topicEntity, currentVirtualTopicName, parentCompoundUuidList, hierarchyCompoundUuid) =>
        {
          val topicSummary =
            browseHierarchyHelper.getTopicSummary(
              topicEntity,
              currentVirtualTopicName,
              parentCompoundUuidList,
              collectionsFilter
            )
          val parents =
            browseHierarchyHelper.getParents(
              topicEntity,
              parentCompoundUuidList.getOrElse(List.empty)
            )
          val children =
            browseHierarchyHelper.getChildren(
              topicEntity,
              currentVirtualTopicName,
              parentCompoundUuidList,
              collectionsFilter
            )
          val allKeyResources =
            browseHierarchyHelper.getKeyResources(hierarchyCompoundUuid)

          val result = HierarchyTopic(topicSummary, parents, children, allKeyResources)
          Response.ok(result).build
        }
    }
  }

  @GET
  @Path("/key-resource/{item-uuid}/{version}")
  @ApiOperation(
    value = "Get all hierarchy IDs which have the provided key resource",
    responseContainer = "List",
    response = classOf[String]
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
            HierarchyCompoundUuid
              .applyWithLegacyFormat(legacyCompoundUuid)
              .buildString(inLegacyFormat = false)
          )

        Response.ok(ids).build
      case Failure(e: ItemNotFoundException) =>
        ApiErrorResponse.resourceNotFound(s"Failed to find key resource: ${e.getMessage}")
    }
  }

  /** Execute the provided function if the topic is found and the user has access permission.
    *
    * @param compoundUuid
    *   The compound UUID of the topic.
    * @param function
    *   The function to execute, which has the following parameters:
    *   1. [[HierarchyTopicEntity]]: The hierarchy topic entity;
    *   2. [[Option[String]]]: An optional virtual topic name;
    *   3. [[Option[List[HierarchyCompoundUuid]]: An optional list of parent HierarchyCompoundUuid;
    *   4. [[HierarchyCompoundUuid]]: The validated HierarchyCompoundUuid generated from
    *      `compoundUuid`.
    */
  private def withTopic(
      compoundUuid: String
  )(
      function: (
          HierarchyTopicEntity,
          Option[String],
          Option[List[HierarchyCompoundUuid]],
          HierarchyCompoundUuid
      ) => Response
  ): Response = {
    validateCompoundUuid(compoundUuid) match {
      case Left(errorResponse) => errorResponse
      case Right((validCompoundUuid, topicEntity)) =>
        val HierarchyCompoundUuid(_, currentVirtualTopicName, parentCompoundUuidList) =
          validCompoundUuid
        function(
          topicEntity,
          currentVirtualTopicName,
          parentCompoundUuidList,
          validCompoundUuid
        )
    }
  }

  // Validate the compound UUID string and return the HierarchyCompoundUuid object and topic entity if it is valid.
  private def validateCompoundUuid(compoundUuid: String) = {
    for {
      validCompoundUuid <- HierarchyCompoundUuid(compoundUuid).leftMap(e =>
        ApiErrorResponse.serverError(
          s"Failed to parse the compound UUID $compoundUuid: ${e.getMessage}"
        )
      )
      topicEntity <- browseHierarchyHelper
        .fetchHierarchyEntity(validCompoundUuid.uuid)
        .leftMap(e => ApiErrorResponse.apiErrorHandler(e))
    } yield (validCompoundUuid, topicEntity)
  }
}
