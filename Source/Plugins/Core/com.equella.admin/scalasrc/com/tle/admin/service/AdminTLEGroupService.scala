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

import java.util
import java.util.Optional
import scala.jdk.CollectionConverters._
import scala.jdk.OptionConverters._

/** A basic representation of a group, with only the most essential details. Working as a data and
  * persistence layer agnostic representation of a group. Essentially a DTO. Preference is given to
  * Java types over Scala types for the external API. As this is primarily consumed on the Java
  * side.
  */
class BasicGroupDetails {
  private var uuid: String                = _
  private var name: String                = _
  private var description: Option[String] = None
  private var users: Set[String]          = Set.empty

  /** Default constructor to provide a more java-esque API.
    *
    * @param id
    *   The UUID of the group
    * @param name
    *   The name of the group
    * @param description
    *   The description of the group
    * @param users
    *   The users in the group
    */
  def this(
      id: String,
      name: String,
      description: Option[String],
      users: Set[String]
  ) = {
    this()
    this.uuid = id
    this.name = name
    this.description = description
    this.users = users
  }

  def getUuid: String = uuid

  def getName: String = name

  def getDescription: Optional[String] = description.toJava

  def getUsers: util.Set[String] = users.asJava

  def setUuid(id: String): Unit = this.uuid = id

  def setName(name: String): Unit = this.name = name

  /** Set the description of the group, if it is not blank or null. Otherwise the description will
    * be set to None.
    *
    * @param description
    *   The description of the group
    */
  def setDescription(description: String): Unit = this.description =
    Option(description).filterNot(_.isBlank)

  /** Set the users in the group. This will replace any existing users in the group.
    *
    * @param users
    *   The users in the group
    */
  def setUsers(users: util.Set[String]): Unit = this.users = users.asScala.toSet

  /** Add a user to the group. Appends the user to the existing list of users.
    *
    * @param user
    *   The user to add to the group
    */
  def addUser(user: String): Unit = this.users += user
}

trait AdminTLEGroupService {

  /** Create a new internal Group, under the optional specified parent, returning the UUID of the
    * new group.
    *
    * @param parentID
    *   The UUID of the parent group, or null if the group is to be a top-level group
    * @param name
    *   The name of the new group
    * @return
    *   The UUID of the new group
    * @throws ClientRequestException
    *   if there are any errors adding the group
    */
  def add(parentID: String, name: String): String

  /** Edit an existing group, returning the UUID of the group.
    *
    * @param group
    *   The updated details (in full) of the group
    * @return
    *   The UUID of the group
    * @throws ClientRequestException
    *   if there are any errors editing the group
    */
  def edit(group: BasicGroupDetails): String

  /** Delete a group and optionally all its children. If the children are to be kept, they will be
    * moved to the parent of the group being deleted.
    *
    * @param groupID
    *   The ID of the group to delete
    * @param deleteChildren
    *   Whether to delete all children of the group (true) or move them to the parent (false)
    * @throws ClientRequestException
    *   if there are any errors deleting the group
    */
  def delete(groupID: String, deleteChildren: Boolean): Unit

  /** Get a group by its UUID.
    *
    * @param id
    *   The UUID of the group to get
    * @return
    *   The group if it exists, or None if it does not
    * @throws ClientRequestException
    *   if there are any errors getting the group
    */
  def get(id: String): Optional[BasicGroupDetails]

  /** Get a group by its name.
    *
    * @param name
    *   The name of the group to get
    * @return
    *   The group if it exists, or None if it does not
    * @throws ClientRequestException
    *   if there are any errors getting the group
    */
  def getByName(name: String): Optional[BasicGroupDetails]

  def getInformationForGroups(groups: util.Collection[String]): util.List[TLEGroup]

  /** Searches for groups (anywhere within the group hierarchy) that match the query. No wildcards
    * are appended, so should be added as needed. (Asterisks are replaced with % in the query.)
    *
    * @param query
    *   The query to search for matching groups with
    * @return
    *   The list of groups that match the query - or an empty list if none are found
    */
  def search(query: String): util.List[TLEGroup]

  def searchTree(query: String): GroupTreeNode
}
