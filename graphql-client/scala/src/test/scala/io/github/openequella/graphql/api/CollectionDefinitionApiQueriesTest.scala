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
import org.scalatest.prop.TableDrivenPropertyChecks._
import org.scalatest.{EitherValues, GivenWhenThen, OptionValues}

import java.io.{BufferedReader, ByteArrayInputStream, InputStreamReader}
import java.util.zip.ZipInputStream

/** Tests for the query operations in the CollectionDefinitionApi.
  *
  * These tests cover read-only operations for collections, including listing collections,
  * retrieving IDs by UUID, and exporting collections. They also verify proper error handling for
  * invalid inputs and unauthenticated access.
  */
class CollectionDefinitionApiQueriesTest
    extends AnyFunSpec
    with Matchers
    with GivenWhenThen
    with EitherValues
    with OptionValues {
  private implicit val cfg: ClientConfiguration = TestHelper.loginToRestInstitution()

  describe("listCollections") {
    it("returns all collections") {
      When("calling listCollections")
      val result = CollectionDefinitionApi.listCollections()

      Then("returns a list of BaseEntityReferenceView")
      result.isRight shouldBe true
      result.value.length should be > 0
      result.value.head.uuid should not be empty
    }

    it("denies access when not authenticated") {
      When("an unauthenticated user calls listCollections")
      val response = TestHelper.asUnauthenticatedUser { unauthenticated =>
        CollectionDefinitionApi.listCollections()(unauthenticated)
      }

      Then("returns an AccessDeniedError")
      TestHelper.checkApiError(response) shouldBe a[AccessDeniedError]
    }
  }

  describe("getIdByUuid") {
    it("returns the ID for a valid collection UUID") {
      Given("a valid collection UUID")
      val collection = CollectionDefinitionApi.listCollections().value.head

      When("calling getIdByUuid with the collection UUID")
      val result = CollectionDefinitionApi.getIdByUuid(collection.uuid)

      Then("returns the collection ID")
      result.isRight shouldBe true
      result.value.value shouldBe collection.id
    }

    it("returns None for an invalid collection UUID") {
      Given("an invalid collection UUID")
      val invalidUuid = "invalid-uuid"

      When("calling getIdByUuid with the invalid UUID")
      val result = CollectionDefinitionApi.getIdByUuid(invalidUuid)

      Then("returns None")
      result.isRight shouldBe true
      result.value shouldBe None
    }

    it("denies access when not authenticated") {
      When("an unauthenticated user calls getIdByUuid")
      val response = TestHelper.asUnauthenticatedUser { unauthenticated =>
        CollectionDefinitionApi.getIdByUuid("some-uuid")(unauthenticated)
      }

      Then("returns an AccessDeniedError")
      TestHelper.checkApiError(response) shouldBe a[AccessDeniedError]
    }
  }

  describe("exportCollection") {
    it("exports a collection as a ZIP file") {
      val withSecurityOptions = Table(
        "withSecurity",
        true,
        false
      )
      forAll(withSecurityOptions) { withSecurity =>
        Given(s"a valid collection ID with withSecurity=$withSecurity")
        val collectionId = CollectionDefinitionApi.listCollections().value.head.id

        When("calling exportCollection with the collection ID and withSecurity")
        val result = CollectionDefinitionApi.exportCollection(collectionId, withSecurity)

        // We convert the result to a ZipInputStream for easier testing.
        // Most often in actual application the bytes are simply saved to a file.
        // Here we just want to ensure that the bytes can be interpreted as a zip file.
        Then("returns an Array[Byte] convertable to a ZipInputStream")
        result.isRight shouldBe true
        val zis = result.value.map(bytesToZipInputStream).get
        zis shouldBe a[ZipInputStream]

        And("the zip file includes a valid _entity.xml")
        val entityXml = extractEntityXml(zis).value
        entityXml should (startWith("<com.tle.common.ImportExportPack>") and include(
          """<entity class="com.tle.beans.entity.itemdef.ItemDefinition">"""
        ))

        And("the _entity.xml contains security information based on withSecurity")
        val securityElement = "<targetList>"
        if (withSecurity) {
          entityXml should include(securityElement)
        } else {
          entityXml should not include securityElement
        }

        // Ensure we close the stream to free resources
        zis.close()
      }
    }

    it("returns None for an invalid collection ID") {
      Given("an invalid collection ID")
      val invalidCollectionId = -1L

      When("calling exportCollection with the invalid collection ID")
      val result =
        CollectionDefinitionApi.exportCollection(invalidCollectionId, withSecurity = false)

      Then("returns None")
      result shouldBe Right(None)
    }

    it("denies access when not authenticated") {
      When("an unauthenticated user calls exportCollection")
      val response = TestHelper.asUnauthenticatedUser { unauthenticated =>
        CollectionDefinitionApi.exportCollection(1, withSecurity = false)(unauthenticated)
      }

      Then("returns an AccessDeniedError")
      TestHelper.checkApiError(response) shouldBe a[AccessDeniedError]
    }

    def bytesToZipInputStream(bytes: Array[Byte]): ZipInputStream =
      new ZipInputStream(new ByteArrayInputStream(bytes))

    def extractEntityXml(zis: ZipInputStream): Option[String] =
      Iterator
        .continually(zis.getNextEntry)
        .takeWhile(_ != null)
        .find(_.getName == "_entity.xml")
        .map(_ => readXmlFile(zis))

    def readXmlFile(zis: ZipInputStream): String = {
      val reader = new BufferedReader(new InputStreamReader(zis, "UTF-8"))
      Iterator
        .continually(reader.readLine())
        .takeWhile(_ != null)
        .mkString("\n")
    }
  }
}
