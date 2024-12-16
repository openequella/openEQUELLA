package com.tle.core.ltiplatform

import cats.data.Validated.{Invalid, Valid}
import com.tle.core.lti13.bean.LtiPlatformBean
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class LtiPlatformBeanValidationTest extends AnyFunSpec with Matchers {
  val validBean: LtiPlatformBean = new LtiPlatformBean(
    platformId = "moodle123",
    name = "moodle",
    clientId = "moodle-user",
    authUrl = "https://test",
    keysetUrl = "https://test",
    usernamePrefix = Option("hello"),
    usernameSuffix = None,
    usernameClaim = None,
    unknownUserHandling = "CREATE",
    unknownUserDefaultGroups = None,
    instructorRoles = Set("tutor"),
    unknownRoles = Set("builder"),
    customRoles = Map("moodle role" -> Set("oeq role 1", "oeq role 2")),
    allowExpression = None,
    kid = None,
    enabled = true
  )

  describe("validate LTI Platform Bean") {
    it("accumulates all the errors found from the provided bean") {
      val invalidBean = validBean.copy(
        authUrl = "abc",
        clientId = "",
        unknownUserHandling = "RUN",
        allowExpression = Option(
          "Everyone OR Administrative User [admin] OR Alistair Oliver [al] OR Carl Hoffman [carl] OR Andrew Hunter [ahunter] OR Demonstration Student [demostudent] OR Demonstration Sysadmin [demosysadmin] OR Bob Carter [bcarter] OR Cath Fitzgerald [cfitzgerald] OR Damian McShane [dmcshane] OR Demonstration Content Admin [democontentadmin] OR Demonstration Librarian [demolibrarian] OR Demonstration Teacher [demoteacher] OR Documentation Administrator [DocAdmin] OR EQUELLA Documentation [Documentation] OR Greg Brown [gbrown] OR Jane Jones [jjones] OR Jennifer Seidenwurm [ilikedrawing@gmail.com] OR Joe Bloggs [jbloggs] OR John Smith [jsmith] OR Sue Smith [ssmith] OR q q [test] OR whitespace whitespace [whitespace]")
      )
      val result = LtiPlatformBean.validateLtiPlatformBean(invalidBean)

      result shouldBe Invalid(
        List(
          "Missing value for required field client ID",
          "Invalid value for Auth URL : no protocol: abc",
          "Unknown handling for unknown users: No value found for 'RUN'",
          "ACL expression is too long (maximum 255 characters allowed)"
        ))
    }

    it("returns the original bean if it's valid") {
      val result = LtiPlatformBean.validateLtiPlatformBean(validBean)
      result shouldBe Valid(validBean)
    }
  }
}
