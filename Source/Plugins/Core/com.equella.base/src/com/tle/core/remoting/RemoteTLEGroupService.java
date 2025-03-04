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

package com.tle.core.remoting;

import com.tle.beans.user.GroupTreeNode;
import com.tle.beans.user.TLEGroup;
import java.util.Collection;
import java.util.List;

public interface RemoteTLEGroupService {
  String add(String parentID, String name);

  TLEGroup get(String id);

  TLEGroup getByName(String name);

  String edit(final TLEGroup group);

  /**
   * Delete a group and optionally all its children. If the children are to be kept, they will be
   * moved to the parent of the group being deleted.
   *
   * @param groupID The ID of the group to delete
   * @param deleteChildren Whether to delete all children of the group (true) or move them to the
   *     parent (false)
   */
  void delete(String groupID, boolean deleteChildren);

  /**
   * Searches for groups (anywhere within the group hierarchy) that match the query. No wildcards
   * are appended, so should be added as needed. (Asterisks are replaced with % in the query.)
   *
   * @param query The query to search for matching groups with
   * @return The list of groups that match the query - or an empty list if none are found
   */
  List<TLEGroup> search(String query);

  /**
   * Searches for groups (anywhere within the group hierarchy) that match the query. No wildcards
   * are appended, so should be added as needed. (Asterisks are replaced with % in the query.)
   *
   * @param query The query to search for matching groups with
   * @param limit The maximum number of results to return
   * @param offset The number of results to skip before returning results
   * @return The list of groups that match the query - or an empty list if none are found
   */
  List<TLEGroup> search(String query, Integer limit, Integer offset);

  List<TLEGroup> search(String query, String parentId);

  List<TLEGroup> search(String query, String userId, boolean allParents);

  GroupTreeNode searchTree(String query);

  List<TLEGroup> getInformationForGroups(Collection<String> groups);
}
