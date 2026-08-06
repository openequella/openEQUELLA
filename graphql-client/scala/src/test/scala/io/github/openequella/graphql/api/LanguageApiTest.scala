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
import io.github.openequella.graphql.api.views.{LanguageBundleNameView, LanguageView}
import io.github.openequella.graphql.test.TestHelper.{
  assertAccessDeniedError,
  loginToRestInstitution
}
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.{EitherValues, GivenWhenThen, OptionValues}

class LanguageApiTest
    extends AnyFunSpec
    with Matchers
    with GivenWhenThen
    with EitherValues
    with OptionValues {

  private implicit val cfg: ClientConfiguration = loginToRestInstitution()

  describe("listLanguages") {
    it("retrieves configured languages") {
      Given("an authenticated user")

      When("calling listLanguages")
      val languages = LanguageApi.listLanguages.value

      Then("returns languages with valid properties")
      languages should not be empty
      languages.foreach(assertLanguage)
    }

    it("denies access when not authenticated") {
      assertAccessDeniedError(LanguageApi.listLanguages(_))
    }
  }

  describe("namesByBundleIds") {
    it("retrieves language bundle names for known bundle IDs") {
      Given("known language bundle IDs")
      val bundleIds = getBundleIds

      When("calling namesByBundleIds")
      val names = LanguageApi.namesByBundleIds(bundleIds).value

      Then("returns entries including the requested IDs")
      names should not be empty
      names.foreach(assertLanguageBundleName)
      names.map(_.id).toSet shouldBe bundleIds.toSet
    }

    it("returns only valid entries when IDs include both valid and invalid values") {
      Given("a mix of valid and invalid language bundle IDs")
      val validBundleIds = getBundleIds
      val bundleIds      = validBundleIds ++ List(-1L, Long.MinValue)

      When("calling namesByBundleIds with mixed IDs")
      val names = LanguageApi.namesByBundleIds(bundleIds).value

      Then("returns only the valid bundle entry")
      names should not be empty
      names.foreach(assertLanguageBundleName)
      names.map(_.id).toSet shouldBe validBundleIds.toSet
    }

    it("returns an empty list for unknown bundle IDs") {
      Given("invalid language bundle IDs")
      val invalidBundleIds = List(Long.MinValue, -1L)

      When("calling namesByBundleIds with invalid IDs")
      val result = LanguageApi.namesByBundleIds(invalidBundleIds)

      Then("returns an empty list")
      result shouldBe Right(Nil)
    }

    it("denies access when not authenticated") {
      Given("known language bundle IDs")
      val bundleIds = getBundleIds
      assertAccessDeniedError(LanguageApi.namesByBundleIds(bundleIds)(_))
    }
  }

  private def getBundleIds: List[Long] = {
    val schemaIds = MetadataSchemaApi.listSchemas().value.map(_.id)
    schemaIds should not be empty
    schemaIds.tail should not be empty

    val firstNameBundleId  = BaseEntityApi.getNameById(schemaIds.head).value.value.id
    val secondNameBundleId = BaseEntityApi.getNameById(schemaIds.tail.head).value.value.id

    List(firstNameBundleId, secondNameBundleId)
  }

  private def assertLanguage(language: LanguageView): Unit = {
    language.id should be > 0L
    language.language.trim should not be empty
  }

  private def assertLanguageBundleName(entry: LanguageBundleNameView): Unit = {
    entry.id should be > 0L
    entry.string.trim should not be empty
  }
}
