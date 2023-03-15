package com.tle.integration.lti13

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should._

class Lti13NonceServiceTest extends AnyFunSpec with Matchers {
  describe("createNonce") {
    it("creates a non-empty unique nonce") {
      val testStateValue = "someStateValue"
      val nonce          = Lti13NonceService.createNonce(testStateValue)
      nonce.isBlank shouldBe false

      // If we create another nonce with the same state value we should still get a unique nonce
      val anotherNonce = Lti13NonceService.createNonce(testStateValue)
      anotherNonce should not be nonce
    }
  }

  describe("validateNonce") {

    it("does not 'validate' an invalid nonce") {
      val invalidNonceError = Left("Provided nonce does not exist")

      Lti13NonceService.validateNonce("nonsense", "nostate!") shouldBe invalidNonceError

      // What about for a a valid state
      val testState = "validstate"
      Lti13NonceService.createNonce(testState)
      Lti13NonceService.validateNonce("pretender", testState) shouldBe invalidNonceError
    }

    it("ensures nounces are used within a reasonable timeframe") {
      val expiredNonceError = Left("Provided nonce has expired")

      val state = "expirytest"
      val nonce = Lti13NonceService.createNonce(state)

      // Sleep for a period long enough for a nonce to expire
      Thread.sleep(15000)
      Lti13NonceService.validateNonce(nonce, state) shouldBe expiredNonceError
    }

    it("correctly validates nounces which match the requirements") {
      val state = "avalidstatevalue"
      val nonce = Lti13NonceService.createNonce(state)
      Lti13NonceService.validateNonce(nonce, state) shouldBe Right(true)
    }
  }
}
