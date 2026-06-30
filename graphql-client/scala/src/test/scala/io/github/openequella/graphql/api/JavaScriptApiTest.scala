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

package io.github.openequella.graphql.api

import io.github.openequella.graphql.ClientConfiguration
import io.github.openequella.graphql.api.views.NameValueView
import io.github.openequella.graphql.test.TestHelper.{
  assertAccessDeniedError,
  loginToRestInstitution
}
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.{EitherValues, GivenWhenThen, OptionValues}

class JavaScriptApiTest
    extends AnyFunSpec
    with Matchers
    with GivenWhenThen
    with EitherValues
    with OptionValues {

  private implicit val cfg: ClientConfiguration = loginToRestInstitution()

  describe("listLibraries") {
    it("retrieves JavaScript library names and IDs") {
      Given("an authenticated user")

      When("calling listLibraries")
      val libraries = JavaScriptApi.listLibraries.value

      Then("returns library entries with non-blank name and value")
      libraries should not be empty
      libraries.foreach(assertNameValue)
    }

    it("denies access when not authenticated") {
      assertAccessDeniedError(JavaScriptApi.listLibraries(_))
    }
  }

  describe("modulesByLibraryId") {
    it("retrieves JavaScript module names for a known library ID") {
      Given("a JavaScript library ID")
      val libraryId = getFirstLibraryId

      When("calling modulesByLibraryId with the ID")
      val modules = JavaScriptApi.modulesByLibraryId(libraryId).value.value

      Then("returns module entries with non-blank name and value")

      modules.foreach(assertNameValue)
    }

    it("returns None for an unknown library ID") {
      Given("an invalid JavaScript library ID")
      val invalidLibraryId = "invalid-library-id"

      When("calling modulesByLibraryId with the invalid ID")
      val result = JavaScriptApi.modulesByLibraryId(invalidLibraryId)

      Then("returns None")
      result shouldBe Right(None)
    }

    it("denies access when not authenticated") {
      Given("a JavaScript library ID")
      val libraryId = getFirstLibraryId
      assertAccessDeniedError(JavaScriptApi.modulesByLibraryId(libraryId)(_))
    }
  }

  private def getFirstLibraryId: String = {
    val libraries = JavaScriptApi.listLibraries.value
    libraries should not be empty
    libraries.head.value
  }

  private def assertNameValue(entry: NameValueView): Unit = {
    entry.name.trim should not be empty
    entry.value.trim should not be empty
  }
}
