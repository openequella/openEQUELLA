package com.tle.integration.lti13

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should._

import java.net.URI

class Lti13StateServiceTest extends AnyFunSpec with Matchers {
  private val testLtiDetails = Lti13StateDetails(platformId = "http://superlms.com/",
                                                 loginHint = "someid",
                                                 targetLinkUri =
                                                   new URI("http://someoeq.net/items/xyz"))

  describe("createState") {
    it("should provide a state value") {
      Lti13StateService.createState(testLtiDetails) should not be empty
    }
  }

  describe("getState") {
    it("returns the details of a valid state value - one or more times") {
      val state = Lti13StateService.createState(testLtiDetails)

      // Do it once
      val getState = () => Lti13StateService.getState(state) shouldBe Some(testLtiDetails)
      getState()

      // Do it again to illustrate persistence - one or more times
      getState()
    }

    it("returns None for invalid state values") {
      Lti13StateService.getState("nosuchstatevalue") shouldBe None
    }
  }

  describe("invalidateState") {
    it("makes a previous state value no longer usable") {
      val state =
        Lti13StateService.createState(testLtiDetails.copy(loginHint = "for-invalidate-state"))

      Lti13StateService.getState(state) should not be None

      // However if we invalidate it, then it will be None
      Lti13StateService.invalidateState(state)
      Lti13StateService.getState(state) shouldBe None
    }
  }
}
