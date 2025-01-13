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
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should._
import org.scalatest.prop.TableDrivenPropertyChecks._

import scala.annotation.tailrec

class TleUserApiTest extends AnyFunSpec with Matchers {
  private val autotest = TleUserView(
    uniqueId = "adfcaf58-241b-4eca-9740-6a26d1c3dd58",
    username = "AutoTest",
    email = Some("auto@test.com"),
    firstName = "Auto",
    lastName = "Test"
  )

  implicit val cfg: ClientConfiguration =
    TestHelper.login("rest", TestHelper.CREDENTIALS_AUTOTEST)

  describe("getByUsername") {
    it("should be able to retrieve a known user by username") {
      val u = TleUserApi.getByUsername(autotest.username)
      u shouldBe Right(Some(autotest))
    }

    it("should return None for an unknown user") {
      val u = TleUserApi.getByUsername("unknown")
      u shouldBe Right(None)
    }

    it("should return an AccessDeniedError if not authenticated") {
      val response = TleUserApi.getByUsername(autotest.username)(
        cfg.copy(cookies = scala.collection.mutable.Set.empty)
      )

      TestHelper.checkApiError(response) shouldBe a[AccessDeniedError]
    }
  }

  describe("getByUniqueId") {
    it("should be able to retrieve a known user by uniqueId") {
      val u = TleUserApi.getByUniqueId(autotest.uniqueId)
      u shouldBe Right(Some(autotest))
    }

    it("should return None for an unknown user") {
      val u = TleUserApi.getByUniqueId("unknown")
      u shouldBe Right(None)
    }
  }

  describe("deleteUser") {
    it("should be able to delete a known user") {
      val response = for {
        user <- TleUserApi.createUser("deleteMe", None, "Delete", "Me", "password")
        uniqueId = user.uniqueId
      } yield TleUserApi.deleteUser(uniqueId)

      response shouldBe a[Right[_, _]]
    }

    it("should return None for an unknown user") {
      val response = TleUserApi.deleteUser("no such user")
      TestHelper.checkApiError(response) shouldBe a[NotFoundError]
    }

    it("should return an AccessDeniedError if not authenticated") {
      val response = TleUserApi.deleteUser(autotest.uniqueId)(
        cfg.copy(cookies = scala.collection.mutable.Set.empty)
      )

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
      val response = TleUserApi.updateUser(autotest.uniqueId, None, None, None, None, None)(
        cfg.copy(cookies = scala.collection.mutable.Set.empty)
      )

      TestHelper.checkApiError(response) shouldBe a[AccessDeniedError]
    }
  }

  describe("searchUsers") {
    // There are 10 users in the target institution
    val TOTAL_USERS = 10
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

    // Both are tested in the one test, as they should both result in the same order of users. So
    // the call from one can be used to validate the other.
    it("supports forward and backward pagination") {
      @tailrec
      def getUsersForward(
          pagination: Pagination,
          users: List[TleUserView] = List.empty
      ): List[TleUserView] = {
        assert(pagination.isInstanceOf[ForwardPagination])

        TleUserApi.searchUsers(pagination) match {
          case Left(errors) => fail("Failed to search for users: " + errors)
          case Right(result) if result.continue.nonEmpty =>
            getUsersForward(result.continue.get, users ++ result.items)
          case Right(result) => users ++ result.items
        }
      }

      // This pretty well identical to getUsersForward, but with the way users are appended to the
      // list reversed. This is to ensure that the order of users is the same as the forward
      // pagination.
      //
      // There is value in having these two implementations stand-alone for reference purposes.
      @tailrec
      def getUsersBackward(
          pagination: Pagination,
          users: List[TleUserView] = List.empty
      ): List[TleUserView] = {
        assert(pagination.isInstanceOf[BackwardPagination])

        TleUserApi.searchUsers(pagination) match {
          case Left(errors) => fail("Failed to search for users: " + errors)
          case Right(result) if result.continue.nonEmpty =>
            getUsersBackward(result.continue.get, result.items ++ users)
          case Right(result) => result.items ++ users
        }
      }

      val pageSize      = 4
      val usersForward  = getUsersForward(ForwardPagination(pageSize))
      val usersBackward = getUsersBackward(BackwardPagination(pageSize))
      usersBackward shouldBe usersForward
      usersForward.size shouldBe TOTAL_USERS
      usersBackward.size shouldBe TOTAL_USERS
    }

    it("supports searching for a specific user") {
      val response = TleUserApi.searchUsers(ForwardPagination(1), Some(autotest.username))
      response match {
        case Right(users) =>
          assert(users.items.size == 1)
          assert(users.items.head == autotest)
        case Left(errors) => fail("Failed to search for user: " + errors)
      }
    }

    it("returns all partially matching users") {
      val response = TleUserApi.searchUsers(ForwardPagination(BIG_LIMIT), Some("test"))
      response match {
        case Right(users) =>
          // There are three known users with 'test' in their details
          assert(users.items.size == 3)
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
      val specialCharacters = Table(
        "char",
        "!",
        "@",
        "#",
        "$",
        "%",
        "^",
        "&",
        "*",
        "(",
        ")",
        "-",
        "_",
        "=",
        "+",
        "[",
        "]",
        "{",
        "}",
        "|",
        "\\",
        ":",
        ";",
        "\"",
        "'",
        "<",
        ">",
        ",",
        ".",
        "?",
        "/"
      )

      forAll(specialCharacters) { char =>
        val response = TleUserApi.searchUsers(ForwardPagination(1), Some(s"test$char"))
        response shouldBe a[Right[_, _]]
      }
    }

    it("should return an AccessDeniedError if not authenticated") {
      val response = TleUserApi.searchUsers(ForwardPagination(BIG_LIMIT))(
        cfg.copy(cookies = scala.collection.mutable.Set.empty)
      )

      TestHelper.checkApiError(response) shouldBe a[AccessDeniedError]
    }
  }
}
