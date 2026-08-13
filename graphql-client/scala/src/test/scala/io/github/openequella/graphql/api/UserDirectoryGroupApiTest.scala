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
import io.github.openequella.graphql.test.UserDirectoryTestHelper.withTemporaryInternalGroups

class UserDirectoryGroupApiTest extends UserDirectoryApiTest {
  describe("groupById") {
    it("retrieves a known group by unique ID") {
      UserDirectoryApi.groupById(AutoGroup1Id).value shouldBe autoGroup1GroupView
    }

    it("returns a NotFoundError for an unknown group") {
      assertNotFoundError(UserDirectoryApi.groupById(unknownGroupId))
    }

    it("denies access when not authenticated") {
      assertAccessDeniedError(UserDirectoryApi.groupById(AutoGroup1Id)(_))
    }
  }

  describe("groupsByIds") {
    it("returns multiple resolved groups") {
      val response       = UserDirectoryApi.groupsByIds(List(AutoGroup1Id, childGroupId))
      val expectedGroups = Set(
        autoGroupIdView,
        childGroupIdView
      )

      response.value.toSet shouldBe expectedGroups
      response.value.size shouldBe expectedGroups.size
    }

    it("returns resolved groups and ignores unknown IDs") {
      val response = UserDirectoryApi.groupsByIds(List(AutoGroup1Id, unknownGroupId))

      response.value shouldBe List(autoGroupIdView)
    }

    it("denies access when not authenticated") {
      assertAccessDeniedError(UserDirectoryApi.groupsByIds(List(AutoGroup1Id))(_))
    }
  }

  describe("searchGroups") {
    it("finds exactly one known group globally by full name") {
      val response =
        UserDirectoryApi.searchGroups(defaultPagination, autoGroup1GroupView.name).value

      response.items shouldBe List(autoGroup1GroupView)
    }

    it("finds a known group globally by broad name query") {
      val response =
        UserDirectoryApi.searchGroups(defaultPagination, partialNameQuery(AutoGroup1Name)).value

      response.items should contain(autoGroup1GroupView)
    }

    it("returns an empty page for an unknown group query") {
      val response = UserDirectoryApi.searchGroups(defaultPagination, UNKNOWN_QUERY).value

      assertEmptyPage(response)
    }

    it("supports forward and backward pagination when searching groups") {
      val namePrefix = "UserDirectoryGroupApiTest-searchGroups"
      withTemporaryInternalGroups(namePrefix) {
        assertSupportsPagination { pagination =>
          UserDirectoryApi.searchGroups(pagination, namePrefix)
        }
      }
    }

    it("denies access when not authenticated") {
      assertAccessDeniedError(UserDirectoryApi.searchGroups(defaultPagination, childGroupName)(_))
    }
  }

  describe("searchGroupsInParent") {
    it("finds exactly one known group in a parent by full name") {
      val response =
        UserDirectoryApi
          .searchGroupsInParent(defaultPagination, childGroupName, parentGroupId)
          .value

      response.items shouldBe List(childGroupView)
    }

    it("finds a known group in a parent by broad name query") {
      val query    = partialNameQuery(childGroupView.name)
      val response =
        UserDirectoryApi
          .searchGroupsInParent(defaultPagination, query, parentGroupId)
          .value

      response.items should contain(childGroupView)
    }

    it("returns an empty page for an unknown parent-scoped group query") {
      val response =
        UserDirectoryApi
          .searchGroupsInParent(defaultPagination, UNKNOWN_QUERY, parentGroupId)
          .value

      assertEmptyPage(response)
    }

    it("returns a NotFoundError for an unknown parent group") {
      assertNotFoundError(
        UserDirectoryApi.searchGroupsInParent(defaultPagination, QUERY_ALL, unknownGroupId)
      )
    }

    it("supports forward and backward pagination when searching groups in a parent") {
      assertSupportsPagination { pagination =>
        UserDirectoryApi.searchGroupsInParent(pagination, QUERY_ALL, AutoGroup1Id)
      }
    }

    it("denies access when not authenticated") {
      assertAccessDeniedError(
        UserDirectoryApi
          .searchGroupsInParent(defaultPagination, autoGroup1GroupView.name, AutoGroup1Id)(_)
      )
    }
  }

  describe("parentGroup") {
    it("retrieves the parent group of a group") {
      UserDirectoryApi
        .parentGroup(childGroupId)
        .value shouldBe Some(parentGroupView)
    }

    it("returns a NotFoundError for an unknown group") {
      assertNotFoundError(UserDirectoryApi.parentGroup(unknownGroupId))
    }

    it("denies access when not authenticated") {
      assertAccessDeniedError(UserDirectoryApi.parentGroup(AutoGroup1Id)(_))
    }
  }

}
