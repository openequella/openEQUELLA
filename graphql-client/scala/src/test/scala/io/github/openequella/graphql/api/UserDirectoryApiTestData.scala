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

package io.github.openequella.graphql.api

import io.github.openequella.graphql.api.views.{
  UserDirectoryGroupView,
  UserDirectoryGroupWithIdView,
  UserDirectoryRoleView,
  UserDirectoryRoleWithIdView,
  UserDirectoryUserView,
  UserDirectoryUserWithIdView
}

/** Shared test data used by UserDirectory API tests. */
object UserDirectoryApiTestData {
  val autoTestUserView = UserDirectoryUserView(
    uniqueId = "adfcaf58-241b-4eca-9740-6a26d1c3dd58",
    username = "AutoTest",
    email = Some("auto@test.com"),
    firstName = "Auto",
    lastName = "Test"
  )
  val autoTestUserId: String   = autoTestUserView.uniqueId
  val autoTestUserName: String = autoTestUserView.username
  val autoTestUserWithIdView   = UserDirectoryUserWithIdView(autoTestUserId, autoTestUserView)

  val autoLoginUserView = UserDirectoryUserView(
    uniqueId = "45e552c2-e4e8-4061-a19b-22f4e0a7c608",
    username = "AutoLogin",
    email = None,
    firstName = "Auto",
    lastName = "Login"
  )
  val autoLoginUserId: String = autoLoginUserView.uniqueId
  val autoLoginUserWithIdView = UserDirectoryUserWithIdView(autoLoginUserId, autoLoginUserView)

  val tokenUserView = UserDirectoryUserView(
    uniqueId = "7e296f6f-8880-43a7-b00b-e42c6b816a1d",
    username = "tokenuser",
    email = None,
    firstName = "token",
    lastName = "user"
  )

  val workflowUserView = UserDirectoryUserView(
    uniqueId = "49a04e57-561f-45b6-8187-e0b893ac739c",
    username = "workflowhelper",
    email = None,
    firstName = "Other",
    lastName = "User"
  )

  val serverSideFlowUserView = UserDirectoryUserView(
    uniqueId = "3477e962-d9b4-4944-9da4-fe0dcd13a21c",
    username = "serverSideFlowUser",
    email = None,
    firstName = "serverSideFlowUser",
    lastName = "serverSideFlowUser"
  )

  val unknownUserId  = "unknown-user"
  val unknownGroupId = "unknown-group"
  val unknownRoleId  = "unknown-role"

  val AutoGroup1Id        = "d72eb802-0ea6-4384-907a-341ee60628c0"
  val AutoGroup1Name      = "AutoGroup1"
  val autoGroup1GroupView = UserDirectoryGroupView(AutoGroup1Id, AutoGroup1Name)
  val autoGroupIdView     = UserDirectoryGroupWithIdView(AutoGroup1Id, autoGroup1GroupView)

  val parentGroupId   = "dff74147-98b4-34d3-e193-d3eeada6d836"
  val parentGroupView = UserDirectoryGroupView(parentGroupId, "SelectGroup1")

  val childGroupId     = "f975419d-4d58-4848-b3a5-fd85d1d08c41"
  val childGroupName   = "SelectSubGroup1"
  val childGroupView   = UserDirectoryGroupView(childGroupId, childGroupName)
  val childGroupIdView = UserDirectoryGroupWithIdView(childGroupId, childGroupView)

  val QUERY_ALL     = ""
  val UNKNOWN_QUERY = "not-a-real-user-directory-query"

  val loggedInUserRoleView =
    UserDirectoryRoleView("TLE_LOGGED_IN_USER_ROLE", "Logged In User Role")
  val loggedInRoleWithIdView =
    UserDirectoryRoleWithIdView(loggedInUserRoleView.uniqueId, loggedInUserRoleView)

  val guestUserRoleView   = UserDirectoryRoleView("TLE_GUEST_USER_ROLE", "Guest User Role")
  val guestRoleWithIdView =
    UserDirectoryRoleWithIdView(guestUserRoleView.uniqueId, guestUserRoleView)
  val testRole1View =
    UserDirectoryRoleView("beeec3d7-94d1-4718-8e7e-d078d9fa469b", "test-role-1")
  val testRole2View =
    UserDirectoryRoleView("c98d8989-6497-4bd3-b468-76cfccbe81e3", "test-role-2")
}
