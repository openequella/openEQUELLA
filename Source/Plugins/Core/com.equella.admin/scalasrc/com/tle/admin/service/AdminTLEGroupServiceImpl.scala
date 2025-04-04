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
import com.tle.core.remoting.RemoteTLEGroupService
import io.github.openequella.graphql.ClientConfiguration
import io.github.openequella.graphql.api.{TleGroupApi, TleGroupView}
import org.slf4j.{Logger, LoggerFactory}

import java.util
import java.util.Optional
import javax.inject.Inject
import scala.jdk.CollectionConverters._
import scala.jdk.OptionConverters._

/** Service class for admin operations on TLEGroup objects via the GraphQL library. Because this
  * class is intended for use primarily by the existing Java code, preference is given to Java types
  * over Scala types.
  */
class AdminTLEGroupServiceImpl @Inject() (
    val delegate: RemoteTLEGroupService
)(implicit val cfg: ClientConfiguration)
    extends AdminTLEGroupService {
  private implicit val LOGGER: Logger = LoggerFactory.getLogger(classOf[AdminTLEUserServiceImpl])

  override def add(parentID: String, name: String): String = {
    LOGGER.debug("Adding group: {}", name)
    TleGroupApi.createGroup(name, Option(parentID)) match {
      case Right(TleGroupView(uniqueId, _, newGroupName, _, _, _)) =>
        LOGGER.debug(s"Group [{}] added with UUID {}", newGroupName, uniqueId)
        uniqueId
      case Left(errors) =>
        throw new ClientRequestException(s"Error adding group: $name", errors)
    }
  }

  override def edit(group: BasicGroupDetails): String = {
    val uuid = group.getUuid
    LOGGER.debug("Editing group: {}", uuid)
    TleGroupApi.updateGroup(
      uniqueId = uuid,
      name = Option(group.getName),
      description = group.getDescription.toScala,
      users = Some(group.getUsers.asScala.toList)
    ) match {
      case Right(TleGroupView(uniqueId, _, newGroupName, _, _, _)) =>
        LOGGER.debug(s"Group '{}' [{}] updated", newGroupName, uniqueId)
        uniqueId
      case Left(errors) =>
        throw new ClientRequestException(s"Error editing group: $uuid", errors)
    }
  }

  override def delete(groupID: String, deleteChildren: Boolean): Unit = {
    LOGGER.debug("Deleting group: {}", groupID)
    TleGroupApi.deleteGroup(groupID, deleteChildren) match {
      case Right(_) =>
        LOGGER.debug("Group [{}] deleted", groupID)
      case Left(errors) =>
        throw new ClientRequestException(s"Error deleting group: $groupID", errors)
    }
  }

  override def get(id: String): Optional[BasicGroupDetails] = {
    LOGGER.debug("Retrieving group by ID: {}", id)
    getEntity("Group [by UUID]", id, TleGroupApi.getByUniqueId).map(toBasicGroupDetails).toJava
  }

  override def getByName(name: String): Optional[BasicGroupDetails] = {
    LOGGER.debug("Retrieving group by name: {}", name)
    getEntity("Group [by name]", name, TleGroupApi.getByName).map(toBasicGroupDetails).toJava
  }

  override def getInformationForGroups(
      groups: util.Collection[String]
  ): util.List[BasicGroupDetails] =
    getAll() {
      TleGroupApi.getGroupsByIds(_, groups.asScala.toSet)
    }.map(toBasicGroupDetails).asJava

  override def search(query: String): util.List[BasicGroupDetails] = {
    LOGGER.debug("Searching for groups: {}", query)
    getAll() {
      TleGroupApi.searchGroups(_, query)
    }.map(toBasicGroupDetails).asJava
  }

  override def searchTree(query: String): GroupTreeNode = implementMe {
    _.searchTree(query)
  }

  private def toBasicGroupDetails(view: TleGroupView): BasicGroupDetails = {
    LOGGER.debug("Retrieving users for group: {}", view.uniqueId)
    val users = getAll() {
      TleGroupApi.listGroupUsers(_, view.uniqueId)
    }
    new BasicGroupDetails(
      view.uniqueId,
      view.name,
      view.description,
      users.toSet
    )
  }

  private def implementMe[T](f: RemoteTLEGroupService => T): T = {
    LOGGER.warn(
      "Still waiting on GraphQL implementation, will try delegate.",
      new NotImplementedError()
    )
    f(delegate)
  }
}
