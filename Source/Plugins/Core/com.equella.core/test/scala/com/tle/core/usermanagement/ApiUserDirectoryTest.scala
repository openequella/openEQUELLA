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

package com.tle.core.usermanagement

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.prop.TableDrivenPropertyChecks._
import org.scalatest.prop.TableFor4

class ApiUserDirectoryTest extends AnyFunSpec with Matchers {
  val data: TableFor4[String, String, String, String] = Table(
    ("apiUrl", "userPath", "id", "expected"),
    (
      "https://idp.example.com/api",
      "account/users",
      "google-oauth2|123456",
      "https://idp.example.com/api/account/users/google-oauth2%7C123456"
    ),
    (
      "https://idp.example.com/api/",
      "/account/users/",
      "john doe",
      "https://idp.example.com/api/account/users/john%20doe"
    ),
    (
      "https://idp.example.com",
      "users",
      "a/b@c",
      "https://idp.example.com/users/a%2Fb@c"
    ),
    (
      "https://idp.example.com/base",
      "api/v1/users",
      "accountId",
      "https://idp.example.com/base/api/v1/users/accountId"
    )
  )

  describe("Test ApiUserDirectory.buildCommonUserEndpoint") {
    it("should build correct URIs for various inputs") {
      forAll(data) { (apiUrl, userPath, id, expected) =>
        val uri = ApiUserDirectory.buildCommonUserEndpoint(apiUrl, userPath, id)
        uri.toString shouldBe expected
      }
    }
  }
}
