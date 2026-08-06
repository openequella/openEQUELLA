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

package com.tle.admin.graphql.conversion

import com.tle.common.usermanagement.user.valuebean.{
  DefaultGroupBean,
  DefaultRoleBean,
  DefaultUserBean,
  GroupBean,
  RoleBean,
  UserBean
}
import io.github.openequella.graphql.api.views.{
  UserDirectoryGroupView,
  UserDirectoryRoleView,
  UserDirectoryUserView
}

/** Converter for transforming GraphQL user directory views to Admin Console user management beans.
  */
object UserDirectoryViewConverter {

  /** Converts a GraphQL user directory user view to a [[UserBean]].
    *
    * @param user
    *   the GraphQL user view to convert
    * @return
    *   the corresponding [[UserBean]]
    */
  def toUserBean(user: UserDirectoryUserView): UserBean =
    new DefaultUserBean(
      user.uniqueId,
      user.username,
      user.firstName,
      user.lastName,
      user.email.orNull
    )

  /** Converts a GraphQL user directory group view to a [[GroupBean]].
    *
    * @param group
    *   the GraphQL group view to convert
    * @return
    *   the corresponding [[GroupBean]]
    */
  def toGroupBean(group: UserDirectoryGroupView): GroupBean =
    new DefaultGroupBean(group.uniqueId, group.name)

  /** Converts a GraphQL user directory role view to a [[RoleBean]].
    *
    * @param role
    *   the GraphQL role view to convert
    * @return
    *   the corresponding [[RoleBean]]
    */
  def toRoleBean(role: UserDirectoryRoleView): RoleBean =
    new DefaultRoleBean(role.uniqueId, role.name)
}
