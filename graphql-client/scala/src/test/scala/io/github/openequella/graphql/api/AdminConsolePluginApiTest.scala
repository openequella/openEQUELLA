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
import io.github.openequella.graphql.api.views.PluginDetailsView
import io.github.openequella.graphql.test.TestHelper.{
  assertAccessDeniedError,
  loginToRestInstitution
}
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.{EitherValues, GivenWhenThen, OptionValues}

class AdminConsolePluginApiTest
    extends AnyFunSpec
    with Matchers
    with GivenWhenThen
    with EitherValues
    with OptionValues {

  private implicit val cfg: ClientConfiguration = loginToRestInstitution()

  describe("listPlugins") {
    it("retrieves admin console plugin details") {
      Given("an authenticated user")

      When("calling listPlugins")
      val plugins = AdminConsolePluginApi.listPlugins.value

      Then("returns plugin details with valid properties")
      plugins should not be empty
      plugins.foreach(assertPluginDetails)
    }

    it("denies access when not authenticated") {
      assertAccessDeniedError(AdminConsolePluginApi.listPlugins(_))
    }
  }

  private def assertPluginDetails(details: PluginDetailsView): Unit = {
    details.baseUrl.trim should not be empty
    details.manifestXml.trim should not be empty
  }
}
