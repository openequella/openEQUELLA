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
import io.github.openequella.graphql.api.MetadataSchemaApiMutationsTestHelper.{
  buildNewSchemaDetails,
  getFirstSchemaId,
  getSchemaName,
  isSchemaLockedForEditing
}
import io.github.openequella.graphql.api.views.{EntitySkeletonView, MetadataSchemaEditView}
import io.github.openequella.graphql.test.TestHelper
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.{EitherValues, GivenWhenThen, OptionValues}

/** Tests for the mutation operations in the MetadataSchemaApi.
  *
  * These tests cover state-changing operations for metadata schemas, including starting edit
  * sessions, creating new schemas, and cancelling edits.
  */
class MetadataSchemaApiMutationsTest
    extends AnyFunSpec
    with Matchers
    with GivenWhenThen
    with EitherValues
    with OptionValues {
  private implicit val cfg: ClientConfiguration = TestHelper.loginToRestInstitution()

  describe("startEdit") {
    it("initiates an edit session for a valid schema") {
      Given("a valid metadata schema ID")
      val schemaId = getFirstSchemaId()

      When("calling startEdit with the schema ID")
      val result = MetadataSchemaApi.startEdit(schemaId)

      Then("returns a MetadataSchemaEditView")
      result.isRight shouldBe true
      val editView = result.value
      editView shouldBe a[MetadataSchemaEditView]
      editView.schema.details.id shouldBe schemaId
      editView.stagingId should not be empty
      editView.targetList shouldBe a[List[_]]

      // tidy-up by cancelling the edit session we just started
      MetadataSchemaApi.cancelEdit(schemaId)
    }

    it("returns a NotFoundError for an invalid schema ID") {
      Given("an invalid schema ID")
      val invalidSchemaId = -1L

      When("calling startEdit with the invalid schema ID")
      val result = MetadataSchemaApi.startEdit(invalidSchemaId)

      Then("returns a NotFoundError")
      TestHelper.checkApiError(result) shouldBe a[NotFoundError]
    }

    it("denies access when not authenticated") {
      When("an unauthenticated user calls startEdit")
      val response = TestHelper.asUnauthenticatedUser { unauthenticated =>
        MetadataSchemaApi.startEdit(1)(unauthenticated)
      }

      Then("returns an AccessDeniedError")
      TestHelper.checkApiError(response) shouldBe a[AccessDeniedError]
    }
  }

  describe("startCreate") {
    it("initiates creation of a new metadata schema") {
      When("calling startCreate")
      val result = MetadataSchemaApi.startCreate()

      Then("returns an EntitySkeletonView")
      result.isRight shouldBe true
      val skeleton = result.value
      skeleton shouldBe a[EntitySkeletonView]
      skeleton.uuid should not be empty
      skeleton.owner should not be empty
      skeleton.stagingId should not be empty
    }

    it("denies access when not authenticated") {
      When("an unauthenticated user calls startCreate")
      val response = TestHelper.asUnauthenticatedUser { unauthenticated =>
        MetadataSchemaApi.startCreate()(unauthenticated)
      }

      Then("returns an AccessDeniedError")
      TestHelper.checkApiError(response) shouldBe a[AccessDeniedError]
    }
  }

  describe("cancelEdit") {
    it("cancels an edit session and releases the lock") {
      Given("a valid metadata schema ID in edit mode")
      val schemaId = getFirstSchemaId()

      When("calling cancelEdit after startEdit")
      val editResult = MetadataSchemaApi.startEdit(schemaId)
      editResult.isRight shouldBe true

      val cancelResult = MetadataSchemaApi.cancelEdit(schemaId)

      Then("completes without errors")
      cancelResult.isRight shouldBe true

      And("the schema is no longer locked for editing")
      isSchemaLockedForEditing(schemaId) shouldBe false
    }

    it("returns a NotFoundError for an invalid schema ID") {
      Given("an invalid schema ID")
      val invalidSchemaId = -1L

      When("calling cancelEdit with the invalid schema ID")
      val result = MetadataSchemaApi.cancelEdit(invalidSchemaId)

      Then("returns a NotFoundError")
      TestHelper.checkApiError(result) shouldBe a[NotFoundError]
    }

    it("forcefully releases locks when force parameter is true") {
      Given("a schema locked by another user")
      val schemaId = getFirstSchemaId()

      // Lock the schema as the ADMIN user
      TestHelper.withUser(TestHelper.CREDENTIALS_ADMIN) { implicit adminSession =>
        val adminEditResult = MetadataSchemaApi.startEdit(schemaId)(adminSession)
        adminEditResult.isRight shouldBe true
      }

      And("the current user cannot start an edit session due to the lock")
      val blockedEditResult = MetadataSchemaApi.startEdit(schemaId)
      blockedEditResult.isLeft shouldBe true
      blockedEditResult.swap.value.exists(_.isInstanceOf[LockedError]) shouldBe true

      When("calling cancelEdit with force=true to release the other user's lock")
      val forceUnlockResult = MetadataSchemaApi.cancelEdit(schemaId, Some(true))

      Then("completes without errors")
      forceUnlockResult.isRight shouldBe true

      And("the current user can now start an edit session")
      val editResult = MetadataSchemaApi.startEdit(schemaId)
      editResult.isRight shouldBe true

      And("after cancelling the edit, the schema is no longer locked")
      val cancelResult = MetadataSchemaApi.cancelEdit(schemaId)
      cancelResult.isRight shouldBe true
      isSchemaLockedForEditing(schemaId) shouldBe false
    }

    it("denies access when not authenticated") {
      When("an unauthenticated user calls cancelEdit")
      val response = TestHelper.asUnauthenticatedUser { unauthenticated =>
        MetadataSchemaApi.cancelEdit(1)(unauthenticated)
      }

      Then("returns an AccessDeniedError")
      TestHelper.checkApiError(response) shouldBe a[AccessDeniedError]
    }
  }

  describe("add") {
    it("creates a new metadata schema") {
      Given("a skeleton from startCreate")
      val skeleton = MetadataSchemaApi.startCreate().value

      And("a fully populated MetadataSchemaEditView")
      val newSchemaDetails = buildNewSchemaDetails(skeleton)

      When("calling add with the new schema details")
      val addResult = MetadataSchemaApi.add(newSchemaDetails, lockAfterwards = false)

      Then("completes without errors")
      addResult.isRight shouldBe true

      And("the schema can be retrieved by its UUID")
      val schemaId = MetadataSchemaApi.getIdByUuid(skeleton.uuid).value.value
      val schema   = MetadataSchemaApi.getById(schemaId).value.value
      schema.details.uuid shouldBe skeleton.uuid
      getSchemaName(schema).value shouldBe "Test Schema"

      // Clean up: delete the created schema
      val deleteResult = MetadataSchemaApi.delete(schemaId)
      deleteResult.isRight shouldBe true
    }

    it("can keep the schema locked after creation when lockAfterwards is true") {
      Given("a skeleton from startCreate")
      val skeleton = MetadataSchemaApi.startCreate().value

      And("a fully populated MetadataSchemaEditView")
      val newSchemaDetails = buildNewSchemaDetails(skeleton)

      When("calling add with lockAfterwards = true")
      val addResult = MetadataSchemaApi.add(newSchemaDetails, lockAfterwards = true)

      Then("completes without errors")
      addResult.isRight shouldBe true

      And("the schema is locked for editing by the current user")
      val schemaId = MetadataSchemaApi.getIdByUuid(skeleton.uuid).value.value

      // Another user cannot start an edit session
      TestHelper.withUser(TestHelper.CREDENTIALS_ADMIN) { implicit adminSession =>
        val editResult = MetadataSchemaApi.startEdit(schemaId)(adminSession)
        editResult.isLeft shouldBe true
        editResult.swap.value.exists(_.isInstanceOf[LockedError]) shouldBe true
      }

      // Clean up: cancel the edit and delete the schema
      MetadataSchemaApi.cancelEdit(schemaId)
      val deleteResult = MetadataSchemaApi.delete(schemaId)
      deleteResult.isRight shouldBe true
    }

    it("denies access when not authenticated") {
      Given("a skeleton from startCreate (authenticated)")
      val skeleton = MetadataSchemaApi.startCreate().value

      And("a fully populated MetadataSchemaEditView")
      val newSchemaDetails = buildNewSchemaDetails(skeleton)

      When("an unauthenticated user calls add")
      val response = TestHelper.asUnauthenticatedUser { unauthenticated =>
        MetadataSchemaApi.add(newSchemaDetails, lockAfterwards = false)(unauthenticated)
      }

      Then("returns an AccessDeniedError")
      TestHelper.checkApiError(response) shouldBe a[AccessDeniedError]
    }
  }

  describe("delete") {
    it("deletes an existing metadata schema") {
      Given("a new metadata schema")
      val skeleton         = MetadataSchemaApi.startCreate().value
      val newSchemaDetails = buildNewSchemaDetails(skeleton, name = "Schema to Delete")
      MetadataSchemaApi.add(newSchemaDetails, lockAfterwards = false).value

      val schemaId = MetadataSchemaApi.getIdByUuid(skeleton.uuid).value.value

      When("calling delete with the schema ID")
      val deleteResult = MetadataSchemaApi.delete(schemaId)

      Then("completes without errors")
      deleteResult.isRight shouldBe true

      And("the schema no longer exists")
      val getResult = MetadataSchemaApi.getById(schemaId).value
      getResult shouldBe None
    }

    it("returns a NotFoundError for an invalid schema ID") {
      Given("an invalid schema ID")
      val invalidSchemaId = -1L

      When("calling delete with the invalid schema ID")
      val result = MetadataSchemaApi.delete(invalidSchemaId)

      Then("returns a NotFoundError")
      TestHelper.checkApiError(result) shouldBe a[NotFoundError]
    }

    it("fails when checkReferences is true and schema has references") {
      Given("a metadata schema that is in use")
      // Use a known schema that has references (typically the first one in the list)
      val schemaId = getFirstSchemaId()

      // Verify it has references
      val hasRefs = MetadataSchemaApi.hasReferences(schemaId).value
      assume(hasRefs, "Test requires a schema with references")

      When("calling delete with checkReferences = true")
      val result = MetadataSchemaApi.delete(schemaId, checkReferences = Some(true))

      Then("returns an error indicating references exist")
      result.isLeft shouldBe true
    }

    it("denies access when not authenticated") {
      When("an unauthenticated user calls delete")
      val response = TestHelper.asUnauthenticatedUser { unauthenticated =>
        MetadataSchemaApi.delete(1)(unauthenticated)
      }

      Then("returns an AccessDeniedError")
      TestHelper.checkApiError(response) shouldBe a[AccessDeniedError]
    }
  }
}
