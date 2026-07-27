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
  INVALID_ENTITY_ID,
  INVALID_ENTITY_UUID,
  assertAccessDeniedError,
  loginToRestInstitution
}
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatest.{EitherValues, GivenWhenThen, OptionValues}

/** Tests for the query operations in the CollectionDefinitionApi.
  *
  * These tests cover read-only operations for collections, including listing collections,
  * retrieving IDs by UUID, and exporting collections. They also verify proper error handling for
  * invalid inputs and unauthenticated access.
  */
class CollectionDefinitionApiQueriesTest
    extends AnyFunSpec
    with ExportTestBehaviours
    with Matchers
    with GivenWhenThen
    with EitherValues
    with OptionValues
    with TableDrivenPropertyChecks {
  private implicit val cfg: ClientConfiguration = loginToRestInstitution()

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
      assertAccessDeniedError(CollectionDefinitionApi.listCollections()(_))
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
      val invalidUuid = INVALID_ENTITY_UUID

      When("calling getIdByUuid with the invalid UUID")
      val result = CollectionDefinitionApi.getIdByUuid(invalidUuid)

      Then("returns None")
      result.isRight shouldBe true
      result.value shouldBe None
    }

    it("denies access when not authenticated") {
      assertAccessDeniedError(CollectionDefinitionApi.getIdByUuid("some-uuid")(_))
    }
  }

  describe("exportCollection") {
    exportBehavior(
      ExportBehaviorConfig(
        entityName = "collection",
        getFirstIdFn = () => CollectionDefinitionApi.listCollections().value.head.id,
        exportFn = CollectionDefinitionApi.exportCollection,
        exportWithSecurityFn = CollectionDefinitionApi.exportCollectionWithSecurity,
        expectedEntityClass = "com.tle.beans.entity.itemdef.ItemDefinition",
        unauthExportFn = cfg => CollectionDefinitionApi.exportCollection(1)(cfg)
      )
    )
  }

  describe("getById") {
    it("retrieves a collection by its ID") {
      Given("a valid collection ID")
      val collection = CollectionDefinitionApi.listCollections().value.head

      When("calling getById with the collection ID")
      val result = CollectionDefinitionApi.getById(collection.id)

      Then("returns the collection")
      result.isRight shouldBe true
      val fetchedCollection = result.value.value
      fetchedCollection.details.id shouldBe collection.id
      fetchedCollection.details.uuid shouldBe collection.uuid
    }

    it("returns None for an invalid collection ID") {
      Given("an invalid collection ID")
      val invalidCollectionId = INVALID_ENTITY_ID

      When("calling getById with the invalid collection ID")
      val result = CollectionDefinitionApi.getById(invalidCollectionId)

      Then("returns None")
      result.isRight shouldBe true
      result.value shouldBe None
    }

    it("denies access when not authenticated") {
      assertAccessDeniedError(CollectionDefinitionApi.getById(1)(_))
    }
  }

  describe("listCategories") {
    it("returns the distinct sorted wizard categories across all collections") {
      Given("the wizard categories of all collections")
      val expectedCategories = CollectionDefinitionApi
        .listCollections()
        .value
        .flatMap(collection =>
          CollectionDefinitionApi.getById(collection.id).value.value.wizardCategory
        )
        .distinct
        .sorted

      When("calling listCategories")
      val result = CollectionDefinitionApi.listCategories()

      Then("returns the distinct sorted wizard categories")
      result.isRight shouldBe true
      result.value shouldBe expectedCategories
    }

    it("denies access when not authenticated") {
      assertAccessDeniedError(CollectionDefinitionApi.listCategories()(_))
    }
  }

  describe("listForSchema") {
    it("returns the collections which use the specified schema") {
      Given("a schema ID sourced from a collection which has one")
      val (collection, schemaId) = CollectionDefinitionApi
        .listCollections()
        .value
        .iterator
        .map(c => (c, CollectionDefinitionApi.getById(c.id).value.value.schemaId))
        .collectFirst { case (c, Some(id)) => (c, id) }
        .value

      When("calling listForSchema with the schema ID")
      val result = CollectionDefinitionApi.listForSchema(schemaId)

      Then("returns a list of collections including the source collection")
      result.isRight shouldBe true
      result.value.map(_.uuid) should contain(collection.uuid)
    }

    it("returns an empty list for an invalid schema ID") {
      Given("an invalid schema ID")
      val invalidSchemaId = INVALID_ENTITY_ID

      When("calling listForSchema with the invalid schema ID")
      val result = CollectionDefinitionApi.listForSchema(invalidSchemaId)

      Then("returns an empty list")
      result.isRight shouldBe true
      result.value shouldBe empty
    }

    it("denies access when not authenticated") {
      assertAccessDeniedError(CollectionDefinitionApi.listForSchema(1)(_))
    }
  }

  describe("getByUuid") {
    it("retrieves a collection by its UUID") {
      Given("a valid collection UUID")
      val collection = CollectionDefinitionApi.listCollections().value.head

      When("calling getByUuid with the collection UUID")
      val result = CollectionDefinitionApi.getByUuid(collection.uuid)

      Then("returns the collection")
      result.isRight shouldBe true
      val fetchedCollection = result.value.value
      fetchedCollection.details.id shouldBe collection.id
      fetchedCollection.details.uuid shouldBe collection.uuid
    }

    it("returns None for an invalid collection UUID") {
      Given("an invalid collection UUID")
      val invalidUuid = INVALID_ENTITY_UUID

      When("calling getByUuid with the invalid UUID")
      val result = CollectionDefinitionApi.getByUuid(invalidUuid)

      Then("returns None")
      result.isRight shouldBe true
      result.value shouldBe None
    }

    it("denies access when not authenticated") {
      assertAccessDeniedError(CollectionDefinitionApi.getByUuid("some-uuid")(_))
    }
  }
}
