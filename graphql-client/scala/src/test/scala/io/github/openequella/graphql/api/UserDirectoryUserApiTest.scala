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

import io.github.openequella.graphql.api.UserDirectoryApiTestData._
import io.github.openequella.graphql.test.PaginationTestHelper.{
  assertEmptyPage,
  assertSupportsPagination
}
import io.github.openequella.graphql.test.TestHelper.{assertAccessDeniedError, assertNotFoundError}
import io.github.openequella.graphql.test.UserDirectoryTestHelper.withTemporaryInternalUsers

class UserDirectoryUserApiTest extends UserDirectoryApiTest {
  describe("userById") {
    it("retrieves a known user by unique ID") {
      val response = UserDirectoryApi.userById(autoTestUserId)
      response.value shouldBe autoTestUserView
    }

    it("returns a NotFoundError for an unknown user") {
      assertNotFoundError(UserDirectoryApi.userById(unknownUserId))
    }

    it("denies access when not authenticated") {
      assertAccessDeniedError(UserDirectoryApi.userById(autoTestUserId)(_))
    }
  }

  describe("usersByIds") {
    it("returns resolved users") {
      val response      = UserDirectoryApi.usersByIds(List(autoTestUserId, autoLoginUserId))
      val expectedUsers = Set(autoTestUserWithIdView, autoLoginUserWithIdView)

      response.value.toSet shouldBe expectedUsers
      response.value.size shouldBe expectedUsers.size
    }

    it("returns resolved users and ignores unknown IDs") {
      val response      = UserDirectoryApi.usersByIds(List(autoTestUserId, unknownUserId))
      val expectedUsers = Set(autoTestUserWithIdView)

      response.value.toSet shouldBe expectedUsers
      response.value.size shouldBe expectedUsers.size
    }

    it("denies access when not authenticated") {
      assertAccessDeniedError(UserDirectoryApi.usersByIds(List(autoTestUserId))(_))
    }
  }

  describe("searchUsers") {
    it("finds exactly one known user by full username") {
      val response = UserDirectoryApi.searchUsers(defaultPagination, tokenUserView.username).value

      response.items shouldBe List(tokenUserView)
    }

    it("finds users by broad username query") {
      val query    = partialNameQuery(autoTestUserName)
      val response = UserDirectoryApi.searchUsers(defaultPagination, query)

      val users = response.value.items
      users should contain(autoTestUserView)
      users.size should be > 1
    }

    it("returns an empty page for an unknown user query") {
      val response = UserDirectoryApi.searchUsers(defaultPagination, UNKNOWN_QUERY).value

      assertEmptyPage(response)
    }

    it("supports forward and backward pagination") {
      val namePrefix = "UserDirectoryUserApiTest-searchUsers"
      withTemporaryInternalUsers(namePrefix) {
        assertSupportsPagination { pagination =>
          UserDirectoryApi.searchUsers(pagination, namePrefix)
        }
      }
    }

    it("denies access when not authenticated") {
      assertAccessDeniedError(UserDirectoryApi.searchUsers(defaultPagination, autoTestUserName)(_))
    }
  }

  describe("searchUsersInGroup") {
    it("returns exactly one known user in the specified group by full username") {
      val response =
        UserDirectoryApi
          .searchUsersInGroup(defaultPagination, autoTestUserName, AutoGroup1Id)
          .value

      response.items shouldBe List(autoTestUserView)
    }

    it("returns matching users in the specified group by broad username query") {
      val response =
        UserDirectoryApi
          .searchUsersInGroup(defaultPagination, partialNameQuery(autoTestUserName), AutoGroup1Id)
          .value

      val users = response.items
      users should contain(autoTestUserView)
      users.size should be > 1
    }

    it("returns an empty page for an unknown group-scoped user query") {
      val response =
        UserDirectoryApi.searchUsersInGroup(defaultPagination, UNKNOWN_QUERY, AutoGroup1Id).value

      assertEmptyPage(response)
    }

    it("returns a NotFoundError for an unknown group") {
      assertNotFoundError(
        UserDirectoryApi.searchUsersInGroup(defaultPagination, QUERY_ALL, unknownGroupId)
      )
    }

    it("supports forward and backward pagination when searching users in a group") {
      assertSupportsPagination { pagination =>
        UserDirectoryApi.searchUsersInGroup(pagination, QUERY_ALL, AutoGroup1Id)
      }
    }

    it("denies access when not authenticated") {
      assertAccessDeniedError(
        UserDirectoryApi.searchUsersInGroup(defaultPagination, autoTestUserName, AutoGroup1Id)(_)
      )
    }
  }

  describe("searchUsersInGroupRecursively") {
    it("returns exactly one known user from subgroups by full username") {
      val recursiveSearch =
        UserDirectoryApi
          .searchUsersInGroupRecursively(defaultPagination, autoTestUserName, parentGroupId)
          .value

      recursiveSearch.items shouldBe List(autoTestUserView)
    }

    it("returns matching users from subgroups by broad username query") {
      val recursiveSearch =
        UserDirectoryApi
          .searchUsersInGroupRecursively(
            defaultPagination,
            partialNameQuery(autoTestUserName),
            parentGroupId
          )
          .value

      recursiveSearch.items should contain(autoTestUserView)
    }

    it("returns an empty page for an unknown recursive group-scoped user query") {
      val response =
        UserDirectoryApi
          .searchUsersInGroupRecursively(defaultPagination, UNKNOWN_QUERY, parentGroupId)
          .value

      assertEmptyPage(response)
    }

    it("returns a NotFoundError for an unknown group when searching recursively") {
      assertNotFoundError(
        UserDirectoryApi.searchUsersInGroupRecursively(defaultPagination, QUERY_ALL, unknownGroupId)
      )
    }

    it("supports forward and backward pagination when searching users in subgroups") {
      assertSupportsPagination { pagination =>
        UserDirectoryApi.searchUsersInGroupRecursively(pagination, QUERY_ALL, AutoGroup1Id)
      }
    }

    it("denies access when not authenticated") {
      assertAccessDeniedError(
        UserDirectoryApi
          .searchUsersInGroupRecursively(defaultPagination, autoTestUserName, AutoGroup1Id)(_)
      )
    }
  }

  describe("usersInGroup") {
    it("returns users that belong to the specified group") {
      val usersInGroup  = UserDirectoryApi.usersInGroup(defaultPagination, AutoGroup1Id).value
      val expectedUsers = Set(autoTestUserView, autoLoginUserView)

      usersInGroup.items.toSet shouldBe expectedUsers
      usersInGroup.items.size shouldBe expectedUsers.size
    }

    it("denies access when not authenticated") {
      assertAccessDeniedError(UserDirectoryApi.usersInGroup(defaultPagination, AutoGroup1Id)(_))
    }

    it("returns a NotFoundError for an unknown group") {
      assertNotFoundError(UserDirectoryApi.usersInGroup(defaultPagination, unknownGroupId))
    }
  }

  describe("usersInGroupRecursively") {
    it("returns users from subgroups recursively") {
      val response =
        UserDirectoryApi.usersInGroupRecursively(defaultPagination, parentGroupId).value

      val users = response.items

      val expectedUsers =
        Set(autoTestUserView, tokenUserView, workflowUserView, serverSideFlowUserView)
      users.toSet shouldBe expectedUsers
      users.size shouldBe expectedUsers.size
    }

    it("denies access when not authenticated") {
      assertAccessDeniedError(
        UserDirectoryApi.usersInGroupRecursively(defaultPagination, AutoGroup1Id)(_)
      )
    }

    it("returns a NotFoundError for an unknown group") {
      assertNotFoundError(
        UserDirectoryApi.usersInGroupRecursively(defaultPagination, unknownGroupId)
      )
    }
  }

  describe("rolesForUser") {
    it("returns roles for a user without failing") {
      val response = UserDirectoryApi.rolesForUser(autoTestUserId)

      val roles         = response.value
      val expectedRoles = Set(testRole1View, testRole2View)
      roles.toSet shouldBe expectedRoles
      roles.size shouldBe expectedRoles.size
    }

    it("returns a NotFoundError for an unknown user") {
      assertNotFoundError(UserDirectoryApi.rolesForUser(unknownUserId))
    }

    it("denies access when not authenticated") {
      assertAccessDeniedError(UserDirectoryApi.rolesForUser(autoTestUserId)(_))
    }
  }

  describe("groupIdsForUser") {
    it("returns group IDs containing a user") {
      val groupIdsForUser  = UserDirectoryApi.groupIdsForUser(autoTestUserId)
      val expectedGroupIds = Set(AutoGroup1Id, parentGroupId, childGroupId)

      groupIdsForUser.value.toSet shouldBe expectedGroupIds
      groupIdsForUser.value.size shouldBe expectedGroupIds.size
    }

    it("returns a NotFoundError for an unknown user") {
      assertNotFoundError(UserDirectoryApi.groupIdsForUser(unknownUserId))
    }

    it("denies access when not authenticated") {
      assertAccessDeniedError(UserDirectoryApi.groupIdsForUser(autoTestUserId)(_))
    }
  }

  describe("groupsForUser") {
    it("returns groups containing a user") {
      val groupsForUser  = UserDirectoryApi.groupsForUser(autoTestUserId)
      val expectedGroups = Set(autoGroup1GroupView, parentGroupView, childGroupView)

      groupsForUser.value.toSet shouldBe expectedGroups
      groupsForUser.value.size shouldBe expectedGroups.size
    }

    it("returns a NotFoundError for an unknown user") {
      assertNotFoundError(UserDirectoryApi.groupsForUser(unknownUserId))
    }

    it("denies access when not authenticated") {
      assertAccessDeniedError(UserDirectoryApi.groupsForUser(autoTestUserId)(_))
    }
  }
}
