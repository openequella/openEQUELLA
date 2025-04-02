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
  def edit(group: TLEGroup): String

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

  def get(id: String): TLEGroup

  def getByName(name: String): TLEGroup

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
