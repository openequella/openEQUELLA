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

package io.github.openequella.graphql.api.views

import caliban.client.SelectionBuilder
import io.github.openequella.graphql.client.{Group, GroupWithId, Role, RoleWithId, User, UserWithId}

/** View model for a user in the user directory.
  */
final case class UserDirectoryUserView(
    uniqueId: String,
    username: String,
    email: Option[String],
    firstName: String,
    lastName: String
)

object UserDirectoryUserView {
  val selector: SelectionBuilder[User, UserDirectoryUserView] = (
    User.uniqueId ~ User.username ~ User.email ~ User.firstName ~ User.lastName
  ).mapN(UserDirectoryUserView.apply _)
}

/** View model for a group in the user directory.
  */
final case class UserDirectoryGroupView(
    uniqueId: String,
    name: String
)

object UserDirectoryGroupView {
  val selector: SelectionBuilder[Group, UserDirectoryGroupView] = (
    Group.uniqueId ~ Group.name
  ).mapN(UserDirectoryGroupView.apply _)
}

/** View model for a role in the user directory.
  */
final case class UserDirectoryRoleView(
    uniqueId: String,
    name: String
)

object UserDirectoryRoleView {
  val selector: SelectionBuilder[Role, UserDirectoryRoleView] = (
    Role.uniqueId ~ Role.name
  ).mapN(UserDirectoryRoleView.apply _)
}

/** View model for a user resolved from a requested ID.
  */
final case class UserDirectoryUserWithIdView(
    id: String,
    user: UserDirectoryUserView
)

object UserDirectoryUserWithIdView {
  val selector: SelectionBuilder[UserWithId, UserDirectoryUserWithIdView] = (
    UserWithId.id ~ UserWithId.user { UserDirectoryUserView.selector }
  ).mapN(UserDirectoryUserWithIdView.apply _)
}

/** View model for a group resolved from a requested ID.
  */
final case class UserDirectoryGroupWithIdView(
    id: String,
    group: UserDirectoryGroupView
)

object UserDirectoryGroupWithIdView {
  val selector: SelectionBuilder[GroupWithId, UserDirectoryGroupWithIdView] = (
    GroupWithId.id ~ GroupWithId.group { UserDirectoryGroupView.selector }
  ).mapN(UserDirectoryGroupWithIdView.apply _)
}

/** View model for a role resolved from a requested ID.
  */
final case class UserDirectoryRoleWithIdView(
    id: String,
    role: UserDirectoryRoleView
)

object UserDirectoryRoleWithIdView {
  val selector: SelectionBuilder[RoleWithId, UserDirectoryRoleWithIdView] = (
    RoleWithId.id ~ RoleWithId.role { UserDirectoryRoleView.selector }
  ).mapN(UserDirectoryRoleWithIdView.apply _)
}
