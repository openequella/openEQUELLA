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
import io.github.openequella.graphql.api.views.BaseEntityReferenceView
import io.github.openequella.graphql.test.TestHelper.{
  INVALID_ENTITY_ID,
  assertAccessDeniedError,
  checkApiError,
  loginToRestInstitution
}
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatest.{EitherValues, GivenWhenThen, OptionValues}

/** Tests for the query operations in the MetadataSchemaApi.
  *
  * These tests cover read-only operations for metadata schemas, including listing schemas,
  * retrieving schema details, and exporting schemas. They also verify proper error handling for
  * invalid inputs and unauthenticated access.
  */
class MetadataSchemaApiQueriesTest
    extends AnyFunSpec
    with ExportTestBehaviours
    with Matchers
    with GivenWhenThen
    with EitherValues
    with OptionValues
    with TableDrivenPropertyChecks {
  private implicit val cfg: ClientConfiguration = loginToRestInstitution()

  describe("listSchemas") {
    it("returns all metadata schemas") {
      When("calling listSchemas")
      val result = MetadataSchemaApi.listSchemas()

      Then("returns a list of BaseEntityReferenceView")
      result.isRight shouldBe true
      result.value.length should be > 1
      result.value.head.uuid should not be empty
    }

    it("denies access when not authenticated") {
      assertAccessDeniedError(MetadataSchemaApi.listSchemas()(_))
    }
  }

  describe("getIdByUuid") {
    it("returns the ID for a valid schema UUID") {
      Given("a valid schema UUID")
      val schema = MetadataSchemaApi.listSchemas().value.head

      When("calling getIdByUuid with the schema UUID")
      val result = MetadataSchemaApi.getIdByUuid(schema.uuid)

      Then("returns the schema ID")
      result.isRight shouldBe true
      result.value.get shouldBe schema.id
    }

    it("returns None for an invalid schema UUID") {
      Given("an invalid schema UUID")
      val invalidUuid = "invalid-uuid"

      When("calling getIdByUuid with the invalid UUID")
      val result = MetadataSchemaApi.getIdByUuid(invalidUuid)

      Then("returns None")
      result.isRight shouldBe true
      result.value shouldBe None
    }

    it("denies access when not authenticated") {
      assertAccessDeniedError(MetadataSchemaApi.getIdByUuid("some-uuid")(_))
    }
  }

  describe("getUses") {
    it("returns all entities using a valid schema") {
      Given("a valid metadata schema ID")
      val schemaId = MetadataSchemaApi.listSchemas().value.head.id

      When("calling getUses with the schema ID")
      val result = MetadataSchemaApi.getUses(schemaId)

      Then("returns a list of BaseEntityReferenceView")
      result.isRight shouldBe true
      result.value shouldBe a[List[_]]
      all(result.value) shouldBe a[BaseEntityReferenceView]
    }

    it("returns an empty list for an invalid schema ID") {
      Given("an invalid schema ID")
      val invalidSchemaId = INVALID_ENTITY_ID

      When("calling getUses with the invalid schema ID")
      val result = MetadataSchemaApi.getUses(invalidSchemaId)

      Then("returns an empty list")
      result.value shouldBe empty
    }

    it("denies access when not authenticated") {
      assertAccessDeniedError(MetadataSchemaApi.getUses(1)(_))
    }
  }

  describe("hasReferences") {
    it("checks if a schema has references") {
      Given("a valid metadata schema ID")
      val schemaId = MetadataSchemaApi.listSchemas().value.head.id

      When("calling hasReferences with the schema ID")
      val result = MetadataSchemaApi.hasReferences(schemaId)

      Then("returns true or false")
      result.isRight shouldBe true
      result.value should (be(true) or be(false))
    }

    it("returns NotFoundError for an invalid schema ID") {
      Given("an invalid schema ID")
      val invalidSchemaId = INVALID_ENTITY_ID

      When("calling hasReferences with the invalid schema ID")
      val result = MetadataSchemaApi.hasReferences(invalidSchemaId)

      Then("returns a NotFoundError")
      checkApiError(result) shouldBe a[NotFoundError]
    }

    it("denies access when not authenticated") {
      assertAccessDeniedError(MetadataSchemaApi.hasReferences(1)(_))
    }
  }

  describe("getImportTypes") {
    it("returns available import types for a valid schema") {
      Given("a valid metadata schema ID")
      val schemaId = MetadataSchemaApi.listSchemas().value.head.id

      When("calling getImportTypes with the schema ID")
      val result = MetadataSchemaApi.getImportTypes(schemaId)

      Then("returns a list of strings")
      result.isRight shouldBe true
      result.value shouldBe a[List[_]]
      all(result.value) shouldBe a[String]
    }

    it("returns an empty list for an invalid schema ID") {
      Given("an invalid schema ID")
      val invalidSchemaId = INVALID_ENTITY_ID

      When("calling getImportTypes with the invalid schema ID")
      val result = MetadataSchemaApi.getImportTypes(invalidSchemaId)

      Then("returns an empty list")
      result.value shouldBe empty
    }

    it("denies access when not authenticated") {
      assertAccessDeniedError(MetadataSchemaApi.getImportTypes(1)(_))
    }
  }

  describe("exportSchema") {
    exportBehavior(
      ExportBehaviorConfig(
        entityName = "metadata schema",
        getFirstIdFn = () => MetadataSchemaApi.listSchemas().value.head.id,
        exportFn = MetadataSchemaApi.exportSchema,
        exportWithSecurityFn = MetadataSchemaApi.exportSchemaWithSecurity,
        expectedEntityClass = "com.tle.beans.entity.Schema",
        unauthExportFn = cfg => MetadataSchemaApi.exportSchema(1)(cfg)
      )
    )
  }

  describe("getById") {
    it("retrieves a schema by its ID") {
      Given("a valid schema ID")
      val schema = MetadataSchemaApi.listSchemas().value.head

      When("calling getById with the schema ID")
      val result = MetadataSchemaApi.getById(schema.id)

      Then("returns the schema")
      result.isRight shouldBe true
      val fetchedSchema = result.value.value
      fetchedSchema.details.id shouldBe schema.id
      fetchedSchema.details.uuid shouldBe schema.uuid
    }

    it("returns None for an invalid schema ID") {
      Given("an invalid schema ID")
      val invalidSchemaId = INVALID_ENTITY_ID

      When("calling getById with the invalid schema ID")
      val result = MetadataSchemaApi.getById(invalidSchemaId)

      Then("returns None")
      result.isRight shouldBe true
      result.value shouldBe None
    }

    it("denies access when not authenticated") {
      assertAccessDeniedError(MetadataSchemaApi.getById(1)(_))
    }
  }
}
