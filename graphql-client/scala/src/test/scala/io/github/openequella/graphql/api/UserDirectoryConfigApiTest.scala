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
import io.github.openequella.graphql.test.TestHelper.{
  assertAccessDeniedError,
  assertBadRequestError,
  assertNotFoundError,
  loginToRestInstitution
}
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.{EitherValues, GivenWhenThen}
import upickle.default.{macroRW, read, write, ReadWriter}

// Mirrors only the top-level shape of com.tle.beans.usermanagement.standard.wrapper.
// SharedSecretSettings, used to exercise listPluginConfig/setPluginConfig round trips.
private case class SharedSecretConfig(
    sharedSecrets: List[Map[String, ujson.Value]],
    enabled: Boolean
)

// Provides the upickle ReadWriter so SharedSecretConfig can be (de)serialised with read/write.
private object SharedSecretConfig {
  implicit val rw: ReadWriter[SharedSecretConfig] = macroRW
}

class UserDirectoryConfigApiTest
    extends AnyFunSpec
    with Matchers
    with EitherValues
    with GivenWhenThen {

  private implicit val cfg: ClientConfiguration = loginToRestInstitution()

  private val sharedSecretSettingsClass =
    "com.tle.beans.usermanagement.standard.wrapper.SharedSecretSettings"

  private def readSharedSecretConfig(json: String): SharedSecretConfig =
    read[SharedSecretConfig](json)

  describe("listSharedSecretIds") {
    it("retrieves the shared secret IDs configured for the institution") {
      val response = UserDirectoryConfigApi.listSharedSecretIds
      val ids      = response.value

      ids should not be empty
      all(ids) shouldBe a[String]
    }

    it("denies access when not authenticated") {
      assertAccessDeniedError(UserDirectoryConfigApi.listSharedSecretIds(_))
    }
  }

  describe("getPluginConfigByClass") {
    it("retrieves the JSON configuration for a known settings class") {
      val response = UserDirectoryConfigApi.getPluginConfigByClass(sharedSecretSettingsClass)
      val json     = response.value

      val node = ujson.read(json)
      node("enabled").boolOpt shouldBe defined
      node("sharedSecrets").arrOpt shouldBe defined

      val config = readSharedSecretConfig(json)
      config.sharedSecrets.foreach { secret =>
        secret should contain key "id"
        secret should contain key "secret"
        secret should contain key "expression"
        secret should contain key "groups"
      }
    }

    it("returns a NotFoundError for an unknown settings class") {
      assertNotFoundError(
        UserDirectoryConfigApi.getPluginConfigByClass("com.example.NonExistentSettings")
      )
    }

    it("denies access when not authenticated") {
      assertAccessDeniedError(
        UserDirectoryConfigApi.getPluginConfigByClass(sharedSecretSettingsClass)(_)
      )
    }
  }

  describe("setPluginConfig") {
    val validSharedSecretConfigJson =
      write(SharedSecretConfig(sharedSecrets = List.empty, enabled = true))

    it("sets the configuration for a known settings class") {
      val originalJson =
        UserDirectoryConfigApi.getPluginConfigByClass(sharedSecretSettingsClass).value
      val originalConfig = readSharedSecretConfig(originalJson)
      val newEnabled     = !originalConfig.enabled
      Given("the new configuration for a known settings class, with new 'enabled' value")
      val updatedRequestJson =
        write(originalConfig.copy(enabled = newEnabled))

      try {
        When("the plugin configuration is updated with the new value")
        val response =
          UserDirectoryConfigApi.setPluginConfig(sharedSecretSettingsClass, updatedRequestJson)
        response shouldBe a[Right[_, _]]

        Then("the updated configuration is returned")
        val updatedConfigJson =
          UserDirectoryConfigApi.getPluginConfigByClass(sharedSecretSettingsClass).value
        val updatedConfig = readSharedSecretConfig(updatedConfigJson)
        updatedConfig.enabled shouldBe newEnabled
      } finally {
        // Always restore the original configuration to avoid impacting other tests.
        UserDirectoryConfigApi.setPluginConfig(sharedSecretSettingsClass, originalJson)
      }
    }

    it("returns a NotFoundError for an unknown settings class") {
      assertNotFoundError(
        UserDirectoryConfigApi.setPluginConfig(
          "com.example.NonExistentSettings",
          validSharedSecretConfigJson
        )
      )
    }

    it("returns a BadRequestError for malformed JSON content") {
      assertBadRequestError(
        UserDirectoryConfigApi.setPluginConfig(sharedSecretSettingsClass, "not valid json")
      )
    }

    it("denies access when not authenticated") {
      assertAccessDeniedError(
        UserDirectoryConfigApi
          .setPluginConfig(sharedSecretSettingsClass, validSharedSecretConfigJson)(_)
      )
    }
  }
}
