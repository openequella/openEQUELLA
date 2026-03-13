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
import io.github.openequella.graphql.test.TestHelper
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.{EitherValues, GivenWhenThen, OptionValues}

class BaseEntityApiTest
    extends AnyFunSpec
    with Matchers
    with GivenWhenThen
    with EitherValues
    with OptionValues {

  private implicit val cfg: ClientConfiguration = TestHelper.loginToRestInstitution()

  describe("getNameById") {
    it("retrieves name for a known base entity by ID") {
      Given("a valid base entity ID")
      val id = getBaseEntityId()

      When("calling getNameById with the valid ID")
      val result = BaseEntityApi.getNameById(id)

      Then("returns the name of the base entity")
      val languageBundle = result.value.value
      languageBundle.id should be > 0L
      languageBundle.strings shouldBe a[List[_]]
      languageBundle.strings should not be empty

      languageBundle.strings.foreach { s =>
        s.id should be > 0L
        s.priority shouldBe a[Int]
        s.locale.trim should not be empty
        s.text.trim should not be empty
      }
    }

    it("returns None for an unknown base entity") {
      Given("an invalid base entity ID")
      val invalidId = -1L

      When("calling getNameById with the invalid ID")
      val result = BaseEntityApi.getNameById(invalidId)

      Then("returns None")
      result shouldBe Right(None)
    }

    it("denies access when not authenticated") {
      val AnyEntityId = 1L

      When("an unauthenticated user calls getNameById")
      val response = TestHelper.asUnauthenticatedUser { unauthenticated =>
        BaseEntityApi.getNameById(AnyEntityId)(unauthenticated)
      }

      Then("returns an AccessDeniedError")
      TestHelper.checkApiError(response) shouldBe a[AccessDeniedError]
    }
  }

  private def getBaseEntityId(): Long = {
    val entities = MetadataSchemaApi.listSchemas().value
    entities should not be empty
    entities.head.id
  }
}
