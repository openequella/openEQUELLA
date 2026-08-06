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

class UserDirectoryRoleApiTest extends UserDirectoryApiTest {

  describe("searchRoles") {
    it("finds exactly one known role by full name") {
      val response =
        UserDirectoryApi.searchRoles(defaultPagination, loggedInUserRoleView.name).value

      val expectedRoles = List(loggedInUserRoleView)
      response.items shouldBe expectedRoles
    }

    it("finds a known role by broad name query") {
      val query    = partialNameQuery(loggedInUserRoleView.name)
      val response =
        UserDirectoryApi.searchRoles(defaultPagination, query).value

      response.items should contain(loggedInUserRoleView)
    }

    it("returns an empty page for an unknown role query") {
      val response = UserDirectoryApi.searchRoles(defaultPagination, UNKNOWN_QUERY).value

      assertEmptyPage(response)
    }

    it("supports forward and backward pagination when searching roles") {
      assertSupportsPagination { pagination =>
        UserDirectoryApi.searchRoles(pagination, QUERY_ALL)
      }
    }

    it("denies access when not authenticated") {
      assertAccessDeniedError(UserDirectoryApi.searchRoles(defaultPagination, QUERY_ALL)(_))
    }
  }

  describe("roleById") {
    it("retrieves a known role by unique ID") {
      UserDirectoryApi
        .roleById(loggedInUserRoleView.uniqueId)
        .value shouldBe loggedInUserRoleView
    }

    it("returns a NotFoundError for an unknown role") {
      assertNotFoundError(UserDirectoryApi.roleById(unknownRoleId))
    }

    it("denies access when not authenticated") {
      assertAccessDeniedError(UserDirectoryApi.roleById(unknownRoleId)(_))
    }
  }

  describe("rolesByIds") {
    it("returns resolved roles and ignores unknown IDs") {
      val response =
        UserDirectoryApi.rolesByIds(List(loggedInUserRoleView.uniqueId, unknownRoleId))

      response.value shouldBe List(loggedInRoleWithIdView)
    }

    it("returns multiple resolved roles") {
      val expectedRoles = Set(loggedInRoleWithIdView, guestRoleWithIdView)
      val roleIds       = expectedRoles.map(_.id).toList
      val response      = UserDirectoryApi.rolesByIds(roleIds)

      response.value.toSet shouldBe expectedRoles
      response.value.size shouldBe expectedRoles.size
    }

    it("denies access when not authenticated") {
      assertAccessDeniedError(UserDirectoryApi.rolesByIds(List(unknownRoleId))(_))
    }
  }
}
