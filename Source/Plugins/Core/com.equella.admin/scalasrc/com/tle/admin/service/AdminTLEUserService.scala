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

import com.tle.beans.user.TLEUser

import java.util.Optional

trait AdminTLEUserService {

  def add(user: TLEUser): String

  def get(uniqueId: String): Optional[TLEUser]

  def getByUsername(username: String): Optional[TLEUser]

  /**
    * Delete a user by UUID.
    *
    * @param uuid the UUID of the user to delete
    * @throws ClientRequestException if there are any errors deleting the user
    */
  def delete(uuid: String): Unit

  /**
    * Given an existing user's TLEUser entity which has been modified, update the user in the
    * database.
    *
    * @param user              The user to update
    * @param passwordNotHashed Whether the password is already hashed - if not, validate it meets
    *                          password requirements and hash it before updating the user.
    * @return The UUID of the updated user
    */
  def edit(user: TLEUser, passwordNotHashed: Boolean): String

  /**
    * Returns a list of all users matching the specified query. Note, there is no pagination limit on
    * how many users will be returned.
    */
  def searchUsers(query: String): java.util.List[TLEUser]

  /**
    * Fired when the list of suspended user accounts has been updated.
    *
    * @param uuids UUIDs of suspended user accounts
    */
  def onSuspension(uuids: java.util.Set[String]): Unit
}
