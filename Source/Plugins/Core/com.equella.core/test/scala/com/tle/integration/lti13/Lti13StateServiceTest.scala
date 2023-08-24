package com.tle.integration.lti13

import com.tle.core.replicatedcache.TrieMapCache
import org.scalatest.GivenWhenThen
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should._

import java.net.URI

class Lti13StateServiceTest extends AnyFunSpec with Matchers with GivenWhenThen {
  private val testLtiDetails = Lti13StateDetails(platformId = "http://superlms.com/",
                                                 loginHint = "someid",
                                                 targetLinkUri =
                                                   new URI("http://someoeq.net/items/xyz"))

  class Fixture {
    val lti13StateService = new Lti13StateService(new TrieMapCache[Lti13StateDetails])
  }

  def fixture = new Fixture

  describe("createState") {
    it("should provide a state value") {
      val f = fixture
      f.lti13StateService.createState(testLtiDetails) should not be empty
    }
  }

  describe("getState") {
    it("returns the details of a valid state value - one or more times") {
      val f = fixture

      Given("a known state")
      val state = f.lti13StateService.createState(testLtiDetails)

      // Do it once
      Then("requesting the details for that state, should return the correct details")
      val getState = () => f.lti13StateService.getState(state) shouldBe Some(testLtiDetails)
      getState()

      // Do it again to illustrate persistence - one or more times
      And("requesting it again, should be identical")
      getState()
    }

    it("returns None for invalid state values") {
      val f = fixture
      f.lti13StateService.getState("nosuchstatevalue") shouldBe None
    }
  }

  describe("invalidateState") {
    it("makes a previous state value no longer usable") {
      val f = fixture

      Given("a new state value is created, and confirmed present")
      val state =
        f.lti13StateService.createState(testLtiDetails.copy(loginHint = "for-invalidate-state"))
      f.lti13StateService.getState(state) should not be None

      When("it is then invalidated")
      f.lti13StateService.invalidateState(state)

      Then("None will be returned on subsequent requests")
      f.lti13StateService.getState(state) shouldBe None
    }
  }
}
