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

  /** Add a new user to the database.
    *
    * @param user
    *   the user to add
    * @return
    *   the UUID of the new user
    * @throws ClientRequestException
    *   if there are any errors adding the user
    */
  def add(user: TLEUser): String

  /** Get a user by UUID.
    *
    * @param uniqueId
    *   the UUID of the user to get
    * @return
    *   the user, or an empty `Optional` if the user does not exist
    * @throws ClientRequestException
    *   if there are any errors getting the user
    */
  def get(uniqueId: String): Optional[TLEUser]

  /** Get a user by username.
    *
    * @param username
    *   the username of the user to get
    * @return
    *   the user, or an empty `Optional` if the user does not exist
    * @throws ClientRequestException
    *   if there are any errors getting the user
    */
  def getByUsername(username: String): Optional[TLEUser]

  /** Delete a user by UUID.
    *
    * @param uuid
    *   the UUID of the user to delete
    * @throws ClientRequestException
    *   if there are any errors deleting the user
    */
  def delete(uuid: String): Unit

  /** Given an existing user's TLEUser entity which has been modified, update the user in the
    * database.
    *
    * @param user
    *   The user to update
    * @param passwordNotHashed
    *   Whether the password is already hashed - if not, validate it meets password requirements and
    *   hash it before updating the user.
    * @return
    *   The UUID of the updated user
    */
  def edit(user: TLEUser, passwordNotHashed: Boolean): String

  /** Returns a list of all users matching the specified query. Note, there is no pagination limit
    * on how many users will be returned.
    */
  def searchUsers(query: String): java.util.List[TLEUser]
}
