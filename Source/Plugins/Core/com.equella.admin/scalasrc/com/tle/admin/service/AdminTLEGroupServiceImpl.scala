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

import com.tle.beans.user.{GroupTreeNode, TLEGroup}
import com.tle.core.remoting.RemoteTLEGroupService
import io.github.openequella.graphql.ClientConfiguration
import io.github.openequella.graphql.api.{TleGroupApi, TleGroupView}
import org.slf4j.{Logger, LoggerFactory}

import java.util
import javax.inject.Inject
import scala.jdk.CollectionConverters._

/** Service class for admin operations on TLEGroup objects via the GraphQL library. Because this
  * class is intended for use primarily by the existing Java code, preference is given to Java types
  * over Scala types.
  */
class AdminTLEGroupServiceImpl @Inject() (
    val delegate: RemoteTLEGroupService
)(implicit val cfg: ClientConfiguration)
    extends AdminTLEGroupService {
  private val LOGGER: Logger = LoggerFactory.getLogger(classOf[AdminTLEUserServiceImpl])

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

  override def edit(group: TLEGroup): String = {
    val uuid = group.getUuid
    LOGGER.debug("Editing group: {}", uuid)
    TleGroupApi.updateGroup(
      uuid,
      Option(group.getName),
      Option(group.getDescription),
      Option(group.getParent).map(_.getUuid),
      Option(group.getUsers.asScala.toList)
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

  override def get(id: String): TLEGroup = implementMe {
    _.get(id)
  }

  override def getByName(name: String): TLEGroup = implementMe {
    _.getByName(name)
  }

  override def getInformationForGroups(groups: util.Collection[String]): util.List[TLEGroup] =
    implementMe {
      _.getInformationForGroups(groups)
    }

  override def search(query: String): util.List[TLEGroup] = implementMe {
    _.search(query)
  }

  override def searchTree(query: String): GroupTreeNode = implementMe {
    _.searchTree(query)
  }

  private def implementMe[T](f: RemoteTLEGroupService => T): T = {
    LOGGER.warn(
      "Still waiting on GraphQL implementation, will try delegate.",
      new NotImplementedError()
    )
    f(delegate)
  }
}
