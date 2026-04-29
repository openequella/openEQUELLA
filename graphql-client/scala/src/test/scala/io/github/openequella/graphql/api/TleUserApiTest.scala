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

import io.github.openequella.graphql.ClientConfiguration
import io.github.openequella.graphql.test.TestHelper
import io.github.openequella.graphql.test.TestHelper.specialCharacters
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should._
import org.scalatest.prop.TableDrivenPropertyChecks._

class TleUserApiTest extends AnyFunSpec with Matchers {
  // A known user in the Rest institution to test with. The values are those found in the
  // institution export - and imported at test time.
  private val knownUser = TleUserView(
    uniqueId = "adfcaf58-241b-4eca-9740-6a26d1c3dd58",
    username = "AutoTest",
    email = Some("auto@test.com"),
    firstName = "Auto",
    lastName = "Test"
  )

  private implicit val cfg: ClientConfiguration = TestHelper.loginToRestInstitution()

  describe("getByUsername") {
    it("should be able to retrieve a known user by username") {
      val u = TleUserApi.getByUsername(knownUser.username)
      u shouldBe Right(Some(knownUser))
    }

    it("should return None for an unknown user") {
      val u = TleUserApi.getByUsername("unknown")
      u shouldBe Right(None)
    }

    it("should return an AccessDeniedError if not authenticated") {
      val response = TestHelper.asUnauthenticatedUser { unauthenticated =>
        TleUserApi.getByUsername(knownUser.username)(unauthenticated)
      }

      TestHelper.checkApiError(response) shouldBe a[AccessDeniedError]
    }
  }

  describe("getByUniqueId") {
    it("should be able to retrieve a known user by uniqueId") {
      val u = TleUserApi.getByUniqueId(knownUser.uniqueId)
      u shouldBe Right(Some(knownUser))
    }

    it("should return None for an unknown user") {
      val u = TleUserApi.getByUniqueId("unknown")
      u shouldBe Right(None)
    }

    it("should return an AccessDeniedError if not authenticated") {
      val response = TestHelper.asUnauthenticatedUser { unauthenticated =>
        TleUserApi.getByUniqueId(knownUser.uniqueId)(unauthenticated)
      }

      TestHelper.checkApiError(response) shouldBe a[AccessDeniedError]
    }
  }

  describe("deleteUser") {
    it("should be able to delete a known user") {
      val testUsername = "deleteMe"
      val response     = for {
        user <- TleUserApi.createUser(testUsername, None, "Delete", "Me", "password")
        uniqueId = user.uniqueId
      } yield TleUserApi.deleteUser(uniqueId)

      response shouldBe a[Right[_, _]]
      TleUserApi.getByUsername(testUsername) shouldBe Right(None)
    }

    it("should return None for an unknown user") {
      val response = TleUserApi.deleteUser("no such user")
      TestHelper.checkApiError(response) shouldBe a[NotFoundError]
    }

    it("should return an AccessDeniedError if not authenticated") {
      val response = TestHelper.asUnauthenticatedUser { unauthenticated =>
        TleUserApi.deleteUser(knownUser.uniqueId)(unauthenticated)
      }

      TestHelper.checkApiError(response) shouldBe a[AccessDeniedError]
    }
  }

  describe("updateUser") {
    it("should be able to update a known user") {
      val updated = "updated"
      val results = for {
        user <- TleUserApi.createUser("updateMe", None, "Update", "Me", "password")
        uniqueId = user.uniqueId
        updatedUser <- TleUserApi.updateUser(
          uniqueId,
          Some(updated),
          Some("test@email.example"),
          Some(updated),
          Some(updated),
          None
        )
        retrievedUser <- TleUserApi.getByUniqueId(uniqueId).map {
          case Some(u) => u
          case _       => fail("Failed to retrieve updated user")
        }
        _ <- TleUserApi.deleteUser(uniqueId)
      } yield (updatedUser, retrievedUser)

      results match {
        case Right((updatedUser, retrievedUser)) =>
          updatedUser shouldBe retrievedUser
          retrievedUser.username shouldBe updated
          retrievedUser.firstName shouldBe updated
          retrievedUser.lastName shouldBe updated
        case Left(errors) => fail("Failed to update user: " + errors)
      }
    }

    it("should return an error for an unknown user") {
      val response = TleUserApi.updateUser("no such user", None, None, None, None, None)
      TestHelper.checkApiError(response) shouldBe a[NotFoundError]
    }

    it("should return an AccessDeniedError if not authenticated") {
      val response = TestHelper.asUnauthenticatedUser { unauthenticated =>
        TleUserApi.updateUser(knownUser.uniqueId, None, None, None, None, None)(unauthenticated)
      }

      TestHelper.checkApiError(response) shouldBe a[AccessDeniedError]
    }
  }

  describe("searchUsers") {
    // There are 12 users in the target institution
    val TOTAL_USERS = 12
    val BIG_LIMIT   = 100

    it("supports searching for all users") {
      // I know there's less than 100 users in the test institution
      val response = TleUserApi.searchUsers(ForwardPagination(BIG_LIMIT))
      response match {
        case Right(users) =>
          assert(users.items.nonEmpty)
          assert(users.continue.isEmpty) // We should have all the users
        case Left(errors) => fail("Failed to search for users: " + errors)
      }
    }

    it("supports forward and backward pagination") {
      TestHelper.testPagination(pageSize = 4, totalExpectedItems = TOTAL_USERS) {
        (pagination: Pagination) => TleUserApi.searchUsers(pagination)
      }
    }

    it("supports searching for a specific user") {
      val response = TleUserApi.searchUsers(ForwardPagination(1), Some(knownUser.username))
      response match {
        case Right(users) =>
          assert(users.items.size == 1)
          assert(users.items.head == knownUser)
        case Left(errors) => fail("Failed to search for user: " + errors)
      }
    }

    it("returns all partially matching users") {
      val response = TleUserApi.searchUsers(ForwardPagination(BIG_LIMIT), Some("test"))
      response match {
        case Right(users) =>
          // There are five known users with 'test' in their details
          assert(users.items.size == 5)
        case Left(errors) => fail("Failed to search for users: " + errors)
      }
    }

    it("should return an empty list if the query matches no users") {
      val response = TleUserApi.searchUsers(ForwardPagination(BIG_LIMIT), Some("gobbledygook"))
      response match {
        case Right(users) =>
          assert(users.items.isEmpty)
          assert(users.continue.isEmpty)
        case Left(errors) => fail("Failed to search for users: " + errors)
      }
    }

    it("handles special characters in the query string") {
      forAll(specialCharacters) { char =>
        val response = TleUserApi.searchUsers(ForwardPagination(1), Some(s"test$char"))
        // Basically, the server doesn't blow up - not testing search validity
        response shouldBe a[Right[_, _]]
      }
    }

    it("should return an AccessDeniedError if not authenticated") {
      val response = TestHelper.asUnauthenticatedUser { unauthenticated =>
        TleUserApi.searchUsers(ForwardPagination(BIG_LIMIT))(unauthenticated)
      }

      TestHelper.checkApiError(response) shouldBe a[AccessDeniedError]
    }
  }
}
