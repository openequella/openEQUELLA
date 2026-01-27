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
import io.github.openequella.graphql.api.views.{
  BaseEntityReferenceView,
  EntitySkeletonView,
  MetadataSchemaEditView
}
import io.github.openequella.graphql.test.TestHelper
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.prop.TableDrivenPropertyChecks._
import org.scalatest.{EitherValues, GivenWhenThen, OptionValues}

import java.io.{BufferedReader, ByteArrayInputStream, InputStreamReader}
import java.util.zip.ZipInputStream

class MetadataSchemaApiTest
    extends AnyFunSpec
    with Matchers
    with GivenWhenThen
    with EitherValues
    with OptionValues {
  private implicit val cfg: ClientConfiguration = TestHelper.loginToRestInstitution()

  describe("listSchemas") {
    it("should return a list of metadata schemas") {
      When("listSchemas is called")
      val result = MetadataSchemaApi.listSchemas()

      Then("it should return a list of BaseEntityReferenceView")
      result.isRight shouldBe true
      result.value.length should be > 1
      result.value.head.uuid should not be empty
    }

    it("should return an AccessDeniedError if not authenticated") {
      When("an unauthenticated user tries to list schemas")
      val response = TestHelper.asUnauthenticatedUser { unauthenticated =>
        MetadataSchemaApi.listSchemas()(unauthenticated)
      }

      Then("it should return an AccessDeniedError")
      TestHelper.checkApiError(response) shouldBe a[AccessDeniedError]
    }
  }

  describe("getIdByUuid") {
    it("should return the ID for a valid schema UUID") {
      Given("A valid schema UUID")
      val schema = MetadataSchemaApi.listSchemas().value.head

      When("getIdByUuid is called with the schema UUID")
      val result = MetadataSchemaApi.getIdByUuid(schema.uuid)

      Then("it should return the schema ID")
      result.isRight shouldBe true
      result.value.get shouldBe schema.id
    }

    it("should return None for an invalid schema UUID") {
      Given("An invalid schema UUID")
      val invalidUuid = "invalid-uuid"

      When("getIdByUuid is called with the invalid UUID")
      val result = MetadataSchemaApi.getIdByUuid(invalidUuid)

      Then("it should return None")
      result.isRight shouldBe true
      result.value shouldBe None
    }

    it("should return an AccessDeniedError if not authenticated") {
      When("an unauthenticated user tries to get a schema ID by UUID")
      val response = TestHelper.asUnauthenticatedUser { unauthenticated =>
        MetadataSchemaApi.getIdByUuid("some-uuid")(unauthenticated)
      }

      Then("it should return an AccessDeniedError")
      TestHelper.checkApiError(response) shouldBe a[AccessDeniedError]
    }
  }

  describe("getUses") {
    it("should return a list of uses for a valid schema ID") {
      Given("A valid metadata schema ID")
      val schemaId = MetadataSchemaApi.listSchemas().value.head.id

      When("getUses is called with the schema ID")
      val result = MetadataSchemaApi.getUses(schemaId)

      Then("it should return a list of BaseEntityReferenceView")
      result.isRight shouldBe true
      result.value shouldBe a[List[_]]
      all(result.value) shouldBe a[BaseEntityReferenceView]
    }

    it("should return an empty list for an invalid schema ID") {
      Given("An invalid schema ID")
      val invalidSchemaId = -1L

      When("getUses is called with the invalid schema ID")
      val result = MetadataSchemaApi.getUses(invalidSchemaId)

      Then("it should return an empty list")
      result.value shouldBe empty
    }

    it("should return an AccessDeniedError if not authenticated") {
      When("an unauthenticated user tries to get uses")
      val response = TestHelper.asUnauthenticatedUser { unauthenticated =>
        MetadataSchemaApi.getUses(1)(unauthenticated)
      }

      Then("it should return an AccessDeniedError")
      TestHelper.checkApiError(response) shouldBe a[AccessDeniedError]
    }
  }

  describe("hasReferences") {
    it("should return true or false for a valid schema ID") {
      Given("A valid metadata schema ID")
      val schemaId = MetadataSchemaApi.listSchemas().value.head.id

      When("hasReferences is called with the schema ID")
      val result = MetadataSchemaApi.hasReferences(schemaId)

      Then("it should return true or false")
      result.isRight shouldBe true
      result.value should (be(true) or be(false))
    }

    it("should return NotFoundError for an invalid schema ID") {
      Given("An invalid schema ID")
      val invalidSchemaId = -1L

      When("hasReferences is called with the invalid schema ID")
      val result = MetadataSchemaApi.hasReferences(invalidSchemaId)

      Then("it should return a NotFoundError")
      TestHelper.checkApiError(result) shouldBe a[NotFoundError]
    }

    it("should return an AccessDeniedError if not authenticated") {
      When("an unauthenticated user tries to check for references")
      val response = TestHelper.asUnauthenticatedUser { unauthenticated =>
        MetadataSchemaApi.hasReferences(1)(unauthenticated)
      }

      Then("it should return an AccessDeniedError")
      TestHelper.checkApiError(response) shouldBe a[AccessDeniedError]
    }
  }

  describe("getImportTypes") {
    it("should return a list of import types for a valid schema ID") {
      Given("A valid metadata schema ID")
      val schemaId = MetadataSchemaApi.listSchemas().value.head.id

      When("getImportTypes is called with the schema ID")
      val result = MetadataSchemaApi.getImportTypes(schemaId)

      Then("it should return a list of strings")
      result.isRight shouldBe true
      result.value shouldBe a[List[_]]
      all(result.value) shouldBe a[String]
    }

    it("should return an empty list for an invalid schema ID") {
      Given("An invalid schema ID")
      val invalidSchemaId = -1L

      When("getImportTypes is called with the invalid schema ID")
      val result = MetadataSchemaApi.getImportTypes(invalidSchemaId)

      Then("it should return an empty list")
      result.value shouldBe empty
    }

    it("should return an AccessDeniedError if not authenticated") {
      When("an unauthenticated user tries to get import types")
      val response = TestHelper.asUnauthenticatedUser { unauthenticated =>
        MetadataSchemaApi.getImportTypes(1)(unauthenticated)
      }

      Then("it should return an AccessDeniedError")
      TestHelper.checkApiError(response) shouldBe a[AccessDeniedError]
    }
  }

  describe("exportSchema") {
    it("should export a metadata schema as a ZIP file") {
      val withSecurityOptions = Table(
        "withSecurity",
        true,
        false
      )
      forAll(withSecurityOptions) { withSecurity =>
        Given(s"A valid metadata schema ID and withSecurity set to $withSecurity")
        val schemaId = MetadataSchemaApi.listSchemas().value.head.id

        When("exportSchema is called with the schema ID and withSecurity")
        val result = MetadataSchemaApi.exportSchema(schemaId, withSecurity)

        // We convert the result to a ZipInputStream for easier testing.
        // Most often in actual application the bytes are simply saved to a file.
        // Here we just want to ensure that the bytes can be interpreted as a zip file.
        Then("it should return a Array[Byte] convertable to a ZipInputStream")
        result.isRight shouldBe true
        val zis = result.value.map(bytesToZipInputStream).get
        zis shouldBe a[ZipInputStream]

        And("the zip file should include a valid _entity.xml")
        val entityXml = extractEntityXml(zis).value
        entityXml should (startWith("<com.tle.common.ImportExportPack>") and include(
          """<entity class="com.tle.beans.entity.Schema">"""
        ))

        And("the _entity.xml should contain the security information depending on withSecurity")
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

    it("should return a not found error for an invalid schema ID") {
      Given("An invalid schema ID")
      val invalidSchemaId = -1L

      When("exportSchema is called with the invalid schema ID")
      val result = MetadataSchemaApi.exportSchema(invalidSchemaId, withSecurity = false)

      Then("it should return None")
      result shouldBe Right(None)
    }

    it("should return an AccessDeniedError if not authenticated") {
      When("an unauthenticated user tries to export a schema")
      val response = TestHelper.asUnauthenticatedUser { unauthenticated =>
        MetadataSchemaApi.exportSchema(1, withSecurity = false)(unauthenticated)
      }

      Then("it should return an AccessDeniedError")
      TestHelper.checkApiError(response) shouldBe a[AccessDeniedError]
    }

    def bytesToZipInputStream(bytes: Array[Byte]): ZipInputStream =
      new ZipInputStream(new ByteArrayInputStream(bytes))

    def extractEntityXml(zis: ZipInputStream): Option[String] =
      Iterator
        .continually(zis.getNextEntry)
        .takeWhile(_ != null)
        .find(_.getName == "_entity.xml")
        .map { _ =>
          readXmlFile(zis)
        }

    def readXmlFile(zis: ZipInputStream): String = {
      val reader = new BufferedReader(new InputStreamReader(zis, "UTF-8"))
      Iterator
        .continually(reader.readLine())
        .takeWhile(_ != null)
        .mkString("\n")
    }
  }

  describe("getById") {
    it("should return the schema for a valid schema ID") {
      Given("A valid schema ID")
      val schema = MetadataSchemaApi.listSchemas().value.head

      When("getById is called with the schema ID")
      val result = MetadataSchemaApi.getById(schema.id)

      Then("it should return the schema")
      result.isRight shouldBe true
      val fetchedSchema = result.value.value
      fetchedSchema.details.id shouldBe schema.id
      fetchedSchema.details.uuid shouldBe schema.uuid
    }

    it("should return None for an invalid schema ID") {
      Given("An invalid schema ID")
      val invalidSchemaId = -1L

      When("getById is called with the invalid schema ID")
      val result = MetadataSchemaApi.getById(invalidSchemaId)

      Then("it should return None")
      result.isRight shouldBe true
      result.value shouldBe None
    }

    it("should return an AccessDeniedError if not authenticated") {
      When("an unauthenticated user tries to get a schema by ID")
      val response = TestHelper.asUnauthenticatedUser { unauthenticated =>
        MetadataSchemaApi.getById(1)(unauthenticated)
      }

      Then("it should return an AccessDeniedError")
      TestHelper.checkApiError(response) shouldBe a[AccessDeniedError]
    }
  }

  describe("startEdit") {
    it("should start editing a metadata schema for a valid schema ID") {
      Given("A valid metadata schema ID")
      val schemaId = MetadataSchemaApi.listSchemas().value.head.id

      When("startEdit is called with the schema ID")
      val result = MetadataSchemaApi.startEdit(schemaId)

      Then("it should return a MetadataSchemaEditView")
      result.isRight shouldBe true
      val editView = result.value
      editView shouldBe a[MetadataSchemaEditView]
      editView.schema.details.id shouldBe schemaId
      editView.stagingId should not be empty
      editView.targetList shouldBe a[List[_]]
    }

    it("should return a NotFoundError for an invalid schema ID") {
      Given("An invalid schema ID")
      val invalidSchemaId = -1L

      When("startEdit is called with the invalid schema ID")
      val result = MetadataSchemaApi.startEdit(invalidSchemaId)

      Then("it should return a NotFoundError")
      TestHelper.checkApiError(result) shouldBe a[NotFoundError]
    }

    it("should return an AccessDeniedError if not authenticated") {
      When("an unauthenticated user tries to start editing a schema")
      val response = TestHelper.asUnauthenticatedUser { unauthenticated =>
        MetadataSchemaApi.startEdit(1)(unauthenticated)
      }

      Then("it should return an AccessDeniedError")
      TestHelper.checkApiError(response) shouldBe a[AccessDeniedError]
    }
  }

  describe("startCreate") {
    it("should start creating a new metadata schema") {
      When("startCreate is called")
      val result = MetadataSchemaApi.startCreate()

      Then("it should return an EntitySkeletonView")
      result.isRight shouldBe true
      val skeleton = result.value
      skeleton shouldBe a[EntitySkeletonView]
      skeleton.uuid should not be empty
      skeleton.owner should not be empty
      skeleton.stagingId should not be empty
    }

    it("should return an AccessDeniedError if not authenticated") {
      When("an unauthenticated user tries to start creating a schema")
      val response = TestHelper.asUnauthenticatedUser { unauthenticated =>
        MetadataSchemaApi.startCreate()(unauthenticated)
      }

      Then("it should return an AccessDeniedError")
      TestHelper.checkApiError(response) shouldBe a[AccessDeniedError]
    }
  }
}
