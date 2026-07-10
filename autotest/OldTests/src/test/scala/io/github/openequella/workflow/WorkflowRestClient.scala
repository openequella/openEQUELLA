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

package io.github.openequella.workflow

import com.dytech.devlib.PropBagEx
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import com.tle.webtests.pageobject.viewitem.ItemId
import org.apache.commons.httpclient.methods.{GetMethod, PostMethod, PutMethod, StringRequestEntity}
import org.apache.commons.httpclient.{HttpClient, HttpMethod, HttpStatus, NameValuePair}

import java.io.IOException
import scala.jdk.CollectionConverters._

/** Minimal session-authenticated REST client for driving the item REST API from Selenium tests
  * against the "workflow" institution.
  */
private object WorkflowRestClient {
  object ApiPath {
    val authLogin  = "api/auth/login"
    val authLogout = "api/auth/logout"
    val item       = "api/item"
    val moderation = "moderation"
  }

  object QueryParam {
    val info     = "info"
    val password = "password"
    val username = "username"
  }

  object QueryValue {
    val metadata = "metadata"
  }

  object JsonKey {
    val assignedTo = "assignedTo"
    val children   = "children"
    val id         = "id"
    val metadata   = "metadata"
    val nodes      = "nodes"
    val uuid       = "uuid"
  }

  object RequestEntity {
    val jsonContentType = "application/json"
    val utf8Charset     = "UTF-8"
  }
}

class WorkflowRestClient(institutionUrl: String) {
  import WorkflowRestClient._

  private val httpClient = new HttpClient()
  private val mapper     = new ObjectMapper()

  private def itemEndpoint(itemId: ItemId): String =
    s"${institutionUrl}${ApiPath.item}/${itemId.getUuid}/${itemId.getVersion}"

  private def withMethod[M <: HttpMethod, T](method: M)(f: M => T): T =
    try f(method)
    finally method.releaseConnection()

  private def execute(method: HttpMethod, action: String): Unit = {
    val status = httpClient.executeMethod(method)
    if (status != HttpStatus.SC_OK) {
      throw new IOException(
        s"Failed to $action: HTTP $status - ${method.getResponseBodyAsString}"
      )
    }
  }

  def login(username: String, password: String): Unit = {
    withMethod(new PostMethod(s"${institutionUrl}${ApiPath.authLogin}")) { method =>
      method.setQueryString(
        Array(
          new NameValuePair(QueryParam.username, username),
          new NameValuePair(QueryParam.password, password)
        )
      )
      execute(method, s"log in as $username")
    }
  }

  def logout(): Unit =
    withMethod(new PutMethod(s"${institutionUrl}${ApiPath.authLogout}")) { method =>
      execute(method, "log out")
    }

  /** GET the item including its metadata XML (`?info=metadata`). */
  def getItemWithMetadata(itemId: ItemId): ObjectNode = {
    withMethod(new GetMethod(itemEndpoint(itemId))) { method =>
      method.setQueryString(Array(new NameValuePair(QueryParam.info, QueryValue.metadata)))
      execute(method, s"get item $itemId")
      mapper.readTree(method.getResponseBodyAsStream) match {
        case obj: ObjectNode => obj
        case node            =>
          throw new IllegalStateException(s"Expected ObjectNode but got ${node.getNodeType}")
      }
    }
  }

  /** Edit the item's metadata XML directly through the REST API (no wizard involved): GET the item,
    * apply `edit` to its metadata and PUT it back.
    */
  def editMetadata(itemId: ItemId)(edit: PropBagEx => Unit): Unit = {
    val item = getItemWithMetadata(itemId)
    val xml  = new PropBagEx(item.get(JsonKey.metadata).asText())
    edit(xml)
    item.put(JsonKey.metadata, xml.toString)

    withMethod(new PutMethod(itemEndpoint(itemId))) { method =>
      method.setRequestEntity(
        new StringRequestEntity(
          item.toString,
          RequestEntity.jsonContentType,
          RequestEntity.utf8Charset
        )
      )
      execute(method, s"edit metadata of item $itemId")
    }
  }

  /** Fetch the raw moderation status tree for the item (GET `/moderation`). */
  def getModerationStatus(itemId: ItemId): JsonNode =
    withMethod(new GetMethod(s"${itemEndpoint(itemId)}/${ApiPath.moderation}")) { method =>
      execute(method, s"get moderation status of item $itemId")
      mapper.readTree(method.getResponseBodyAsStream)
    }

  /** The user UUID assigned to the given workflow task, or None if the task is unassigned or
    * absent.
    */
  def getTaskAssignee(itemId: ItemId, taskUuid: String): Option[String] =
    findTaskAssignee(getModerationStatus(itemId), taskUuid)

  /** Extract, from a moderation status tree, the user UUID assigned to `taskUuid`. Pure: no I/O. */
  private def findTaskAssignee(status: JsonNode, taskUuid: String): Option[String] =
    status
      .findParents(JsonKey.uuid)
      .asScala
      .find(_.path(JsonKey.uuid).asText == taskUuid)
      .map(_.path(JsonKey.assignedTo).path(JsonKey.id).asText)
      .filter(_.nonEmpty)
}
