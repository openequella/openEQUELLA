package com.tle.integration.oidc

import com.tle.core.replicatedcache.TrieMapCache
import com.tle.integration.lti13.{Lti13StateDetails, Lti13StateService}
import com.tle.integration.oidc.service.{OidcStateService, StateConfig}
import org.scalatest.GivenWhenThen
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should._

import java.net.URI

class OidcStateServiceTest extends AnyFunSpec with Matchers with GivenWhenThen {
  case class TestData(value: String)

  private val testData = TestData("http://superlms.com/")

  class Fixture {
    val oidcStateService = new OidcStateService(new TrieMapCache[TestData])
  }

  def fixture = new Fixture

  describe("createState") {
    it("should provide a state value") {
      val f = fixture
      f.oidcStateService.createState(testData) should not be empty
    }
  }

  describe("getState") {
    it("returns the details of a valid state value - one or more times") {
      val f = fixture

      Given("a known state")
      val state = f.oidcStateService.createState(testData)

      // Do it once
      Then("requesting the details for that state, should return the correct details")
      val getState = () => f.oidcStateService.getState(state) shouldBe Some(testData)
      getState()

      // Do it again to illustrate persistence - one or more times
      And("requesting it again, should be identical")
      getState()
    }

    it("returns None for invalid state values") {
      val f = fixture
      f.oidcStateService.getState("nosuchstatevalue") shouldBe None
    }
  }

  describe("invalidateState") {
    it("makes a previous state value no longer usable") {
      val f = fixture

      Given("a new state value is created, and confirmed present")
      val state =
        f.oidcStateService.createState(testData.copy(value = "for-invalidate-state"))
      f.oidcStateService.getState(state) should not be None

      When("it is then invalidated")
      f.oidcStateService.invalidateState(state)

      Then("None will be returned on subsequent requests")
      f.oidcStateService.getState(state) shouldBe None
    }
  }
}
