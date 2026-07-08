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

import scala.jdk.CollectionConverters._

/** Minimal session-authenticated REST client for driving the item REST API from Selenium tests
  * against the "workflow" institution.
  */
class WorkflowRestClient(institutionUrl: String) {
  private val httpClient = new HttpClient()
  private val mapper     = new ObjectMapper()

  private def itemEndpoint(itemId: ItemId): String =
    s"${institutionUrl}api/item/${itemId.getUuid}/${itemId.getVersion}"

  private def execute(method: HttpMethod, action: String): Unit = {
    val status = httpClient.executeMethod(method)
    if (status != HttpStatus.SC_OK) {
      throw new IllegalStateException(
        s"Failed to $action: HTTP $status - ${method.getResponseBodyAsString}"
      )
    }
  }

  def login(username: String, password: String): Unit = {
    val method = new PostMethod(s"${institutionUrl}api/auth/login")
    method.setQueryString(
      Array(new NameValuePair("username", username), new NameValuePair("password", password))
    )
    execute(method, s"log in as $username")
  }

  def logout(): Unit =
    execute(new PutMethod(s"${institutionUrl}api/auth/logout"), "log out")

  /** GET the item including its metadata XML (`?info=metadata`). */
  def getItemWithMetadata(itemId: ItemId): ObjectNode = {
    val method = new GetMethod(itemEndpoint(itemId))
    method.setQueryString(Array(new NameValuePair("info", "metadata")))
    execute(method, s"get item $itemId")
    mapper.readTree(method.getResponseBodyAsStream).asInstanceOf[ObjectNode]
  }

  /** Edit the item's metadata XML directly through the REST API (no wizard involved): GET the item,
    * apply `edit` to its metadata and PUT it back.
    */
  def editMetadata(itemId: ItemId)(edit: PropBagEx => Unit): Unit = {
    val item = getItemWithMetadata(itemId)
    val xml  = new PropBagEx(item.get("metadata").asText())
    edit(xml)
    item.put("metadata", xml.toString)

    val method = new PutMethod(itemEndpoint(itemId))
    method.setRequestEntity(new StringRequestEntity(item.toString, "application/json", "UTF-8"))
    execute(method, s"edit metadata of item $itemId")
  }

  /** Query `/moderation` for the user currently assigned to the given workflow task, or None if the
    * task is unassigned (or the task node is absent).
    */
  def getTaskAssignee(itemId: ItemId, taskUuid: String): Option[String] = {
    val method = new GetMethod(s"${itemEndpoint(itemId)}/moderation")
    execute(method, s"get moderation status of item $itemId")
    val status = mapper.readTree(method.getResponseBodyAsStream)

    findNode(Option(status.get("nodes")), taskUuid)
      .flatMap(task => Option(task.get("assignedTo")))
      .flatMap(assignedTo => Option(assignedTo.get("id")))
      .filterNot(_.isNull)
      .map(_.asText)
      .filter(_.nonEmpty)
  }

  /** Searching workflow node status tree for the node with the given UUID. */
  private def findNode(node: Option[JsonNode], uuid: String): Option[JsonNode] =
    node.flatMap { n =>
      if (Option(n.get("uuid")).exists(_.asText == uuid)) Some(n)
      else {
        Option(n.get("children")).toSeq
          .flatMap(_.elements().asScala)
          .flatMap(child => findNode(Some(child), uuid))
          .headOption
      }
    }
}
