package com.tle.integration.lti13

import com.tle.core.replicatedcache.TrieMapCache
import org.scalatest.GivenWhenThen
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should._

class Lti13NonceServiceTest extends AnyFunSpec with Matchers with GivenWhenThen {
  class Fixture {
    val lti13NonceService = new Lti13NonceService(new TrieMapCache[Lti13NonceDetails])
  }

  def fixture = new Fixture

  describe("createNonce") {
    it("creates a non-empty unique nonce") {
      val f = fixture

      Given("A state value")
      val testStateValue = "someStateValue"

      When("a nonce is created for that state")
      val nonce = f.lti13NonceService.createNonce(testStateValue)

      Then("it should be non-empty")
      nonce.isBlank shouldBe false

      And("if another nonce was created with the same state, it should be different - i.e. unique")
      val anotherNonce = f.lti13NonceService.createNonce(testStateValue)
      anotherNonce should not be nonce
    }
  }

  describe("validateNonce") {
    it("does not 'validate' an invalid nonce") {
      val f                 = fixture
      val invalidNonceError = Left("Provided nonce does not exist")

      Given("An attempt to validate a nonce for a non-existent state")
      val result = f.lti13NonceService.validateNonce("nonsense", "nostate!")

      Then("an invalid nonce error should be returned")
      result shouldBe invalidNonceError

      When("an attempt is made to validate a non-existent nonce for a valid state")
      val testState = "validstate"
      f.lti13NonceService.createNonce(testState)

      Then("the same invalid nonce error should also be returned")
      f.lti13NonceService.validateNonce("pretender", testState) shouldBe invalidNonceError
    }

    it("ensures nounces are used within a reasonable timeframe") {
      val f                 = fixture
      val expiredNonceError = Left("Provided nonce has expired")

      Given("a valid nonce value")
      val state = "expirytest"
      val nonce = f.lti13NonceService.createNonce(state)

      When("we wait long enough for the nonce to expire")
      Thread.sleep(15000)

      Then("the expired nonce error should be returned on attempts to validate")
      f.lti13NonceService.validateNonce(nonce, state) shouldBe expiredNonceError
    }

    it("correctly validates nounces which match the requirements") {
      val f = fixture

      Given("a valid nonce")
      val state = "avalidstatevalue"
      val nonce = f.lti13NonceService.createNonce(state)

      Then("attempts to validate it should succeed")
      f.lti13NonceService.validateNonce(nonce, state) shouldBe Right(true)
    }
  }
}
