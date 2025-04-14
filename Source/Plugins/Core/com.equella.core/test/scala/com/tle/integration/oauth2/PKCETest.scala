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

package com.tle.integration.oauth2

import org.scalatest.GivenWhenThen
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import java.security.MessageDigest
import java.util.Base64

class PKCETest extends AnyFunSpec with Matchers with GivenWhenThen {
  describe("generatePKCEPair") {
    it("should return a verifier and a correctly encoded challenge") {

      Given("a pair of code challenge and code verifier")
      val (verifier, challenge) = generatePKCEPair

      Then("the verifier has a length within the expected PKCE limits (43-128 characters)")
      verifier.length should (be >= 43 and be <= 128)

      Then("Challenge should be a SHA-256 hash of the verifier")
      val digest = MessageDigest.getInstance("SHA-256").digest(verifier.getBytes("US-ASCII"))
      val expectedChallenge = Base64.getUrlEncoder.withoutPadding.encodeToString(digest)
      challenge shouldBe expectedChallenge
    }
  }
}
