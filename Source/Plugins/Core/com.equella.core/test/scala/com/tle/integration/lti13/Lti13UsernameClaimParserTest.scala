package com.tle.integration.lti13

import org.scalatest.EitherValues._
import org.scalatest.GivenWhenThen
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class Lti13UsernameClaimParserTest extends AnyFunSpec with Matchers with GivenWhenThen {
  val FIRST_PATH  = "https://lti13.org"
  val SECOND_PATH = "username"
  val THIRD_PATH  = "ID"

  describe("parse") {
    it("should extract all the paths if the claim is valid") {
      Given("a claim constructed correctly in the bracket notation format")
      val claim = s"[$FIRST_PATH][$SECOND_PATH][$THIRD_PATH]"

      Then("Parsing the claim should return an Either of Right")
      val r = Lti13UsernameClaimParser.parse(claim)
      r.isRight shouldBe true

      And("The value of the Either should be an array of the paths")
      r.value should equal(Array(FIRST_PATH, SECOND_PATH, THIRD_PATH))
    }

    it("should return an error if the claim is invalid") {
      Given("a list of claims not properly constructed in the bracket notation format")
      val badClaims = Array(
        s"[[$FIRST_PATH][$SECOND_PATH][$THIRD_PATH]]",
        s"[$FIRST_PATH]$SECOND_PATH][$THIRD_PATH]",
        s"[$FIRST_PATH[$SECOND_PATH][$THIRD_PATH]",
        s"[$FIRST_PATH]$SECOND_PATH[$THIRD_PATH]",
        s"[$FIRST_PATH]]]]",
        s"[[[[$FIRST_PATH]",
        s"[$FIRST_PATH",
        s"$FIRST_PATH]",
        "[]",
        ""
      )

      Then("The result of parsing these claims should all be an Either of left")
      val result = badClaims.map(Lti13UsernameClaimParser.parse).forall(_.isLeft)
      result shouldBe (true)
    }
  }
}
