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

import com.tle.core.webkeyset.service.WebKeySetService
import io.circe.parser.{decode, parse}
import io.circe.{Decoder, Json}
import org.mockito.Mockito.mock
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.prop.TableDrivenPropertyChecks._
import org.scalatest.prop.{TableFor3, TableFor4}

import java.net.{URI, URL}

class ApiUserDirectoryTest extends AnyFunSpec with Matchers {
  val data: TableFor4[URL, Seq[String], String, String] = Table(
    ("apiUrl", "userPath", "id", "expected"),
    (
      new URI("https://idp.example.com/api").toURL,
      Seq("account", "users"),
      "google-oauth2|123456",
      "https://idp.example.com/api/account/users/google-oauth2%7C123456"
    ),
    (
      new URI("https://idp.example.com/api/").toURL,
      Seq("account", "users"),
      "john doe",
      "https://idp.example.com/api/account/users/john%20doe"
    ),
    (
      new URI("https://idp.example.com").toURL,
      Seq("users"),
      "a/b@c",
      "https://idp.example.com/users/a%2Fb@c"
    ),
    (
      new URI("https://idp.example.com/base").toURL,
      Seq("api", "v1", "users"),
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

  describe("Decoding") {
    val auth0Dir   = new Auth0UserDirectory
    val entraIdDir = new EntraIdUserDirectory
    val oktaDir    = new OktaUserDirectory(mock(classOf[WebKeySetService]))

    val firstName = "Test"
    val lastName  = "User"
    val email     = "test@test.com"

    val rawAuth0User =
      s"""{ "user_id": "auth0|abc", "name": "Auth0 testing user", "given_name": "$firstName", "family_name": "$lastName", "email": "$email" }"""
    val auth0User = IdPUser(
      id = "auth0|abc",
      username = Some("Auth0 testing user"),
      firstName = Some(firstName),
      lastName = Some(lastName),
      email = Some(email),
      raw = parse(rawAuth0User).getOrElse(Json.Null)
    )

    val rawEntraIdUser =
      s"""{ "id": "entraid_oid_abc", "displayName": "Entra ID testing user", "givenName": "$firstName", "surname": "$lastName", "mail": "$email" }"""
    val entraIdUser = IdPUser(
      id = "entraid_oid_abc",
      username = Some("Entra ID testing user"),
      firstName = Some(firstName),
      lastName = Some(lastName),
      email = Some(email),
      raw = parse(rawEntraIdUser).getOrElse(Json.Null)
    )

    val rawOktaUser =
      s"""{ "id": "okta_id_abc", "profile": { "username": "Okta testing user", "given_name": "$firstName", "family_name": "$lastName", "email": "$email"}}"""
    val oktaUser = IdPUser(
      id = "okta_id_abc",
      username = Some("Okta testing user"),
      firstName = Some(firstName),
      lastName = Some(lastName),
      email = Some(email),
      raw = parse(rawOktaUser).getOrElse(Json.Null)
    )

    it("transforms a single-user response structure into the common user structure") {
      val data: TableFor3[String, ApiUserDirectory, IdPUser] = Table(
        ("rawJson", "directory", "expected"),
        (rawAuth0User, auth0Dir, auth0User),
        (rawEntraIdUser, entraIdDir, entraIdUser),
        (rawOktaUser, oktaDir, oktaUser)
      )

      forAll(data) { (json, directory, expected) =>
        implicit val decoder: Decoder[IdPUser] = directory.userDecoder
        val result                             = decode(json)

        result shouldBe Right(expected)
      }
    }

    it("transforms a multi-user response structure into a list of the common user structure") {
      val data: TableFor3[String, ApiUserDirectory, List[IdPUser]] = Table(
        ("rawJson", "directory", "expected"),
        (s"""[$rawAuth0User]""", auth0Dir, List(auth0User)),
        (s"""{"value": [$rawEntraIdUser]}""", entraIdDir, List(entraIdUser)),
        (s"""[$rawOktaUser]""", oktaDir, List(oktaUser))
      )

      forAll(data) { (rawJson, directory, expected) =>
        implicit val decoder: Decoder[List[IdPUser]] = directory.usersDecoder
        val result                                   = decode(rawJson)

        result shouldBe Right(expected)
      }
    }

  }
}
