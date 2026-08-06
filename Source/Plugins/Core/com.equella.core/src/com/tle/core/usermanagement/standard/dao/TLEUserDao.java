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

package com.tle.core.usermanagement.standard.dao;

import com.tle.beans.user.TLEUser;
import com.tle.core.hibernate.dao.GenericDao;
import java.util.Collection;
import java.util.List;

public interface TLEUserDao extends GenericDao<TLEUser, Long> {
  /**
   * Count the number of users in the system.
   *
   * @return The number of users in the system
   */
  int totalExistingUsers();

  /**
   * Count the number of users in the system that match the query parameters.
   *
   * @param likeQuery a query string to search for in the user's first name, last name or username
   * @param parentGroupID the group to search within, or null to search all groups
   * @param recurse whether to search recursively within groups
   * @return The number of users in the system that match the query parameters
   */
  int countUsersInGroup(String likeQuery, String parentGroupID, boolean recurse);

  List<TLEUser> listAllUsers();

  /**
   * Search for users, optionally with a query string, optionally within a group, optionally
   * recursively.
   *
   * @param likeQuery a query string to search for in the user's first name, last name or username
   * @param parentGroupID the group to search within, or null to search all groups
   * @param recurse whether to search recursively within groups
   * @return a list of users matching the search criteria
   */
  List<TLEUser> searchUsersInGroup(String likeQuery, String parentGroupID, boolean recurse);

  /**
   * The same as {@link #searchUsersInGroup(String, String, boolean)} but with a limit on the number
   * of results returned.
   *
   * @param likeQuery a query string to search for in the user's first name, last name or username
   * @param parentGroupID the group to search within, or null to search all groups
   * @param recurse whether to search recursively within groups
   * @param limit the maximum number of results to return
   * @return a list of users matching the search criteria
   */
  List<TLEUser> searchUsersInGroup(
      String likeQuery, String parentGroupID, boolean recurse, Integer limit);

  /**
   * The same as {@link #searchUsersInGroup(String, String, boolean, Integer)} but with an offset
   * and limit on the number of results returned. Useful for pagination.
   *
   * @param likeQuery a query string to search for in the user's first name, last name or username
   * @param parentGroupID the group to search within, or null to search all groups
   * @param recurse whether to search recursively within groups
   * @param offset the number of results to skip before returning results
   * @param limit the maximum number of results to return
   * @return a list of users matching the search criteria
   */
  List<TLEUser> searchUsersInGroup(
      String likeQuery, String parentGroupID, boolean recurse, Integer limit, Integer offset);

  void deleteAll();

  TLEUser findByUuid(String uuid);

  /**
   * Warning - this method will return a user for any username case - upper/lower/mixed/whatever.
   */
  TLEUser findByUsername(String username);

  boolean doesOtherUsernameSameSpellingExist(String username, String userUuid);

  List<TLEUser> getInformationForUsers(Collection<String> ids);
}
