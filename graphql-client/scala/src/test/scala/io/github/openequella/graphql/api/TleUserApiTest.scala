package io.github.openequella.graphql.api

import io.github.openequella.graphql.ClientConfiguration
import io.github.openequella.graphql.test.TestHelper
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should._

class TleUserApiTest extends AnyFunSpec with Matchers {
  private val autotest = TleUserView(uniqueId = "adfcaf58-241b-4eca-9740-6a26d1c3dd58",
                                     username = "AutoTest",
                                     email = Some("auto@test.com"),
                                     firstName = "Auto",
                                     lastName = "Test")

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
        cfg.copy(cookies = scala.collection.mutable.Set.empty))

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
  }
}
