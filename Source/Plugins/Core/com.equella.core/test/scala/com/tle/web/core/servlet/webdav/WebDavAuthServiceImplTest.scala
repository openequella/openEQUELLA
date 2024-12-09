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

package com.tle.web.core.servlet.webdav

import com.tle.core.replicatedcache.TrieMapCache
import org.scalatest.GivenWhenThen
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should._

import java.util.{Base64, UUID}

class WebDavAuthServiceImplTest extends AnyFunSpec with Matchers with GivenWhenThen {
  def generateAuthHeaderPayload(username: String, password: String): String =
    Base64.getEncoder.encodeToString(s"$username:$password".getBytes)

  class Fixture {
    val webDavAuthService = new WebDavAuthServiceImpl(new TrieMapCache[WebDavCredentials])
  }

  def fixture = new Fixture

  describe("createCredentials") {
    it("creates some unique credentials") {
      val f = fixture

      Given("an ID for a WebDav context - e.g. a staging ID")
      val id = UUID.randomUUID().toString

      When("some credentials are created")
      val (username, password) =
        f.webDavAuthService.createCredentials(id, UUID.randomUUID().toString, "oequser1")

      Then("each part should be non-blank")
      username.isBlank shouldBe false
      password.isBlank shouldBe false

      And(
        "if another set of credentials are requested for the same id, the same current ones are returned"
      )
      val (newCredsUsername, newCredsPassword) =
        f.webDavAuthService.createCredentials(id, UUID.randomUUID().toString, "oequser2")
      newCredsUsername shouldBe username
      newCredsPassword shouldBe password
    }
  }

  describe("validateCredentials") {
    it("should return an error if incorrect credentials are provided") {
      val f = fixture

      Given("an ID for a WebDav context - e.g. a staging ID")
      val id = UUID.randomUUID().toString

      And("some random credentials in a properly structured authorization payload")
      val invalidUsername = "invalidusername"
      val invalidPassword = "invalidpassword"
      val authHeader      = generateAuthHeaderPayload(invalidUsername, invalidPassword)

      Then("authentication should fail if there's not even a context created with that ID")
      f.webDavAuthService.validateCredentials(id, authHeader) shouldBe Left(InvalidContext())

      And("should also fail if there is a valid ID - but wrong credentials")
      f.webDavAuthService.createCredentials(id, UUID.randomUUID().toString, "oequser3")
      f.webDavAuthService.validateCredentials(id, authHeader) shouldBe Left(InvalidCredentials())
    }

    it("correctly validates valid credentials") {
      val f = fixture

      Given("an ID for a WebDav context - e.g. a staging ID")
      val id = UUID.randomUUID().toString

      And("some registered credentials")
      val (validUsername, validPassword) =
        f.webDavAuthService.createCredentials(id, UUID.randomUUID().toString, "oequser4")

      When("those credentials are formed into a valid payload")
      val authHeader = generateAuthHeaderPayload(validUsername, validPassword)

      Then("authentication should succeed")
      f.webDavAuthService.validateCredentials(id, authHeader) shouldBe Right(true)

    }
  }

  describe("removeCredentials") {
    it("ensures no further authentication is possible after removal") {
      val f = fixture

      Given("an ID for a WebDav context - e.g. a staging ID")
      val id = UUID.randomUUID().toString

      And("some registered credentials")
      val (validUsername, validPassword) =
        f.webDavAuthService.createCredentials(id, UUID.randomUUID().toString, "oequser5")

      When("those credentials are removed")
      f.webDavAuthService.removeCredentials(id)

      Then("no further authentication is possible as the ID/context is invalid")
      f.webDavAuthService
        .validateCredentials(
          id,
          generateAuthHeaderPayload(validUsername, validPassword)
        ) shouldBe Left(InvalidContext())
    }
  }

  describe("whois") {
    it("tells you the details of the oEQ user who setup the credentials") {
      val f = fixture

      Given("an ID for a WebDav context - e.g. a staging ID")
      val id = UUID.randomUUID().toString

      And("some registered credentials")
      val oeqUserId   = UUID.randomUUID().toString
      val oeqUserName = "oequser6"
      f.webDavAuthService.createCredentials(id, oeqUserId, oeqUserName)

      When("it is requested who the credentials belong to")
      val creator = f.webDavAuthService.whois(id)

      Then("the details of the oEQ user who setup the credentials are returned")
      creator shouldBe Some(WebUserDetails(oeqUserId, oeqUserName))
    }

    it("returns None if it can't find user details for the specified context") {
      val f = fixture

      When("a request is made for the user details of a none-extent context")
      val creator = f.webDavAuthService.whois(UUID.randomUUID().toString)

      Then("None is returned")
      creator shouldBe None
    }
  }
}
