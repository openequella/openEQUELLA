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

package com.tle.admin.service

import com.tle.admin.helper.GraphQLQueryHelper.{getAll, getEntity}
import com.tle.beans.user.GroupTreeNode
import io.github.openequella.graphql.ClientConfiguration
import io.github.openequella.graphql.api.{InternalGroupApi, InternalGroupView}
import org.slf4j.{Logger, LoggerFactory}

import java.util
import java.util.Optional
import javax.inject.{Inject, Singleton}
import scala.jdk.CollectionConverters._
import scala.jdk.OptionConverters._

/** Service class for admin operations on TLEGroup objects via the GraphQL library. Because this
  * class is intended for use primarily by the existing Java code, preference is given to Java types
  * over Scala types.
  */
@Singleton
class AdminTLEGroupServiceImpl @Inject() (implicit val cfg: ClientConfiguration)
    extends AdminTLEGroupService {
  private implicit val LOGGER: Logger = LoggerFactory.getLogger(classOf[AdminTLEGroupServiceImpl])

  override def add(parentID: String, name: String): String = {
    LOGGER.debug("Adding internal group: {}", name)
    InternalGroupApi.createGroup(name, Option(parentID)) match {
      case Right(InternalGroupView(uniqueId, _, newGroupName, _, _, _)) =>
        LOGGER.debug(s"Internal group [{}] added with UUID {}", newGroupName, uniqueId)
        uniqueId
      case Left(errors) =>
        throw new ClientRequestException(s"Error adding internal group: $name", errors)
    }
  }

  override def edit(group: BasicGroupDetails): String = {
    val uuid = group.getUuid
    LOGGER.debug("Editing internal group: {}", uuid)
    InternalGroupApi.updateGroup(
      uniqueId = uuid,
      name = Option(group.getName),
      description = group.getDescription.toScala,
      users = Some(group.getUsers.asScala.toList)
    ) match {
      case Right(InternalGroupView(uniqueId, _, newGroupName, _, _, _)) =>
        LOGGER.debug(s"Internal group '{}' [{}] updated", newGroupName, uniqueId)
        uniqueId
      case Left(errors) =>
        throw new ClientRequestException(s"Error editing internal group: $uuid", errors)
    }
  }

  override def delete(groupID: String, deleteChildren: Boolean): Unit = {
    LOGGER.debug("Deleting internal group: {}", groupID)
    val deleteResult =
      if (deleteChildren) InternalGroupApi.deleteGroup(groupID)
      else InternalGroupApi.deleteGroupOnly(groupID)

    deleteResult match {
      case Right(_) =>
        LOGGER.debug("Internal group [{}] deleted", groupID)
      case Left(errors) =>
        throw new ClientRequestException(s"Error deleting internal group: $groupID", errors)
    }
  }

  override def get(id: String): Optional[BasicGroupDetails] =
    getGroup(id).map(toBasicGroupDetails).toJava

  override def getByName(name: String): Optional[BasicGroupDetails] = {
    LOGGER.debug("Retrieving internal group by name: {}", name)
    getEntity("Internal group [by name]", name, InternalGroupApi.getByName)
      .map(toBasicGroupDetails)
      .toJava
  }

  override def getInformationForGroups(
      groups: util.Collection[String]
  ): util.List[BasicGroupDetails] =
    getAll() {
      InternalGroupApi.getGroupsByIds(_, groups.asScala.toSet)
    }.map(toBasicGroupDetails).asJava

  override def search(query: String): util.List[BasicGroupDetails] =
    getGroupsByQuery(query).map(toBasicGroupDetails).asJava

  override def searchTree(query: String): GroupTreeNode = {
    val builder = new GroupTreeBuilder(
      getGroupsByQuery = q => getGroupsByQuery(q),
      getListGroups = parentId => getAll() { InternalGroupApi.listGroups(_, parentId) },
      getGroup = id => getGroup(id)
    )
    builder.buildSearchTree(query)
  }

  private def getGroupsByQuery(query: String): List[InternalGroupView] = {
    LOGGER.debug("Searching for internal groups: [{}]", query)
    getAll() {
      InternalGroupApi.searchGroups(_, query)
    }
  }

  private def getGroup(id: String): Option[InternalGroupView] = {
    LOGGER.debug("Retrieving internal group by ID: {}", id)
    getEntity("Internal group [by UUID]", id, InternalGroupApi.getByUniqueId)
  }

  private def toBasicGroupDetails(view: InternalGroupView): BasicGroupDetails = {
    LOGGER.debug("Retrieving internal users for group: {}", view.uniqueId)
    val users = getAll() {
      InternalGroupApi.listGroupUsers(_, view.uniqueId)
    }
    new BasicGroupDetails(
      view.uniqueId,
      view.name,
      view.description,
      users.toSet
    )
  }
}
