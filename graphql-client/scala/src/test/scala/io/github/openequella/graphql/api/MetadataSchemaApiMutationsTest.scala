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
import io.github.openequella.graphql.api.MetadataSchemaApiMutationsTestHelper._
import io.github.openequella.graphql.api.views.{
  BaseEntityReferenceView,
  EntitySkeletonView,
  MetadataSchemaEditView,
  MetadataSchemaView
}
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
      withEditSession(schemaId) { editView =>
        Then("returns a MetadataSchemaEditView")
        editView shouldBe a[MetadataSchemaEditView]
        editView.schema.details.id shouldBe schemaId
        editView.stagingId should not be empty
        editView.targetList shouldBe a[List[_]]
      }
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

  describe("cancelEdit/cancelEditForced") {
    it("cancels an edit session and releases the lock") {
      Given("a valid metadata schema ID in edit mode")
      val schemaId = getFirstSchemaId()

      When("calling cancelEdit after startEdit")
      val editResult = MetadataSchemaApi.startEdit(schemaId)
      editResult.isRight shouldBe true

      val cancelResult = MetadataSchemaApi.cancelEditForced(schemaId)

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

    it("forcefully releases locks when cancelEditForced is used") {
      Given("a schema locked by another user")
      val schemaId = getFirstSchemaId()

      // Lock the schema as the ADMIN user
      TestHelper.withUser(TestHelper.CREDENTIALS_ADMIN) { implicit adminSession =>
        val adminEditResult = MetadataSchemaApi.startEdit(schemaId)(adminSession)
        adminEditResult.isRight shouldBe true
      }

      try {
        And("the current user cannot start an edit session due to the lock")
        val blockedEditResult = MetadataSchemaApi.startEdit(schemaId)
        blockedEditResult.isLeft shouldBe true
        blockedEditResult.swap.value.exists(_.isInstanceOf[LockedError]) shouldBe true

        When("calling cancelEditForced to release the other user's lock")
        val forceUnlockResult = MetadataSchemaApi.cancelEditForced(schemaId)

        Then("completes without errors")
        forceUnlockResult.isRight shouldBe true

        And("the current user can now start an edit session")
        val editResult = MetadataSchemaApi.startEdit(schemaId)
        editResult.isRight shouldBe true

        And("after cancelling the edit, the schema is no longer locked")
        val cancelResult = MetadataSchemaApi.cancelEditForced(schemaId)
        cancelResult.isRight shouldBe true
        isSchemaLockedForEditing(schemaId) shouldBe false
      } finally {
        // Best-effort cleanup: force-cancel any lingering lock
        MetadataSchemaApi.cancelEditForced(schemaId)
      }
    }

    it("denies access when not authenticated") {
      When("an unauthenticated user calls cancelEdit")
      val response = TestHelper.asUnauthenticatedUser { unauthenticated =>
        MetadataSchemaApi.cancelEditForced(1)(unauthenticated)
      }

      Then("returns an AccessDeniedError")
      TestHelper.checkApiError(response) shouldBe a[AccessDeniedError]
    }
  }

  describe("add") {
    it("creates a new metadata schema") {
      When("creating a new metadata schema with valid details")
      withTestSchema() { reference =>
        Then("the add result returns a valid BaseEntityReferenceView")
        reference shouldBe a[BaseEntityReferenceView]
        reference.id should be > 0L
        reference.uuid should not be empty
        reference.owner should not be empty

        And("the schema can be retrieved by its ID")
        val schema = MetadataSchemaApi.getById(reference.id).value.value
        getSchemaName(schema).value shouldBe "Test Schema"

        And("the schema is not locked for editing")
        isSchemaLockedForEditing(reference.id) shouldBe false
      }
    }

    it("can keep the schema locked after creation with addAndLock") {
      When("creating a new metadata schema with addAndLock")
      withTestSchemaLocked() { ref =>
        Then("the schema is locked for editing")
        isSchemaLockedForEditing(ref.id) shouldBe true
      }
    }

    it("denies access when not authenticated") {
      Given("a skeleton from startCreate (authenticated)")
      val skeleton = MetadataSchemaApi.startCreate().value

      And("a fully populated MetadataSchemaEditView")
      val newSchemaDetails = buildNewSchemaDetails(skeleton)

      When("an unauthenticated user calls add")
      val response = TestHelper.asUnauthenticatedUser { unauthenticated =>
        MetadataSchemaApi.add(newSchemaDetails)(unauthenticated)
      }

      Then("returns an AccessDeniedError")
      TestHelper.checkApiError(response) shouldBe a[AccessDeniedError]
    }
  }

  describe("stopEdit") {
    it("saves changes to a metadata schema and unlocks") {
      Given("a metadata schema in edit mode")
      withTestSchemaLocked() { reference =>
        val schemaId = reference.id

        When("calling stopEditAndUnlock")
        val editView   = MetadataSchemaApi.startEdit(schemaId).value
        val stopResult = MetadataSchemaApi.stopEditAndUnlock(editView)

        Then("completes without errors and returns the schema")
        stopResult.isRight shouldBe true
        val savedSchema = stopResult.value
        savedSchema shouldBe a[MetadataSchemaView]
        savedSchema.details.id shouldBe schemaId

        And("the schema is no longer locked for editing")
        isSchemaLockedForEditing(schemaId) shouldBe false
      }
    }

    it("saves changes and keeps the schema locked") {
      Given("a metadata schema in edit mode")
      withTestSchemaLocked() { reference =>
        val schemaId = reference.id

        When("calling stopEdit (keeping locked)")
        val editView   = MetadataSchemaApi.startEdit(schemaId).value
        val stopResult = MetadataSchemaApi.stopEdit(editView)

        Then("completes without errors and returns the schema")
        stopResult.isRight shouldBe true
        val savedSchema = stopResult.value
        savedSchema shouldBe a[MetadataSchemaView]
        savedSchema.details.id shouldBe schemaId

        And("the schema remains locked for editing")
        isSchemaLockedForEditing(schemaId) shouldBe true

        And("cleanup: cancel the edit session to release the lock")
        MetadataSchemaApi.cancelEdit(schemaId)
      }
    }

    it("returns a NotFoundError for an invalid schema ID") {
      Given("an invalid schema ID")
      val invalidSchemaId = -1L

      And("a valid MetadataSchemaEditView")
      val skeleton = MetadataSchemaApi.startCreate().value
      val editView = buildNewSchemaDetails(skeleton)

      When("calling stopEdit with the invalid schema ID")
      // Create a view with the invalid ID to test the mutation error handling
      val editViewWithBadId = editView.copy(
        schema = editView.schema.copy(
          details = editView.schema.details.copy(id = invalidSchemaId)
        )
      )
      val result = MetadataSchemaApi.stopEditAndUnlock(editViewWithBadId)

      Then("returns a NotFoundError")
      TestHelper.checkApiError(result) shouldBe a[NotFoundError]
    }

    it("denies access when not authenticated") {
      Given("a valid metadata schema ID")
      val schemaId = getFirstSchemaId()

      And("a valid MetadataSchemaEditView")
      val editView = MetadataSchemaApi.startEdit(schemaId).value

      try {
        When("an unauthenticated user calls stopEdit")
        val response = TestHelper.asUnauthenticatedUser { unauthenticated =>
          MetadataSchemaApi.stopEditAndUnlock(editView)(unauthenticated)
        }

        Then("returns an AccessDeniedError")
        TestHelper.checkApiError(response) shouldBe a[AccessDeniedError]
      } finally {
        // Best-effort cleanup: force-cancel to release any lingering lock.
        MetadataSchemaApi.cancelEditForced(schemaId)
      }
    }
  }

  describe("delete/deleteWithReferenceCheck") {
    it("deletes an existing metadata schema") {
      Given("a valid metadata schema ID")
      withTestSchema(name = "Schema to Delete") { reference =>
        val schemaId = reference.id

        When("calling delete with the schema ID")
        val deleteResult = MetadataSchemaApi.delete(schemaId)

        Then("completes without errors")
        deleteResult.isRight shouldBe true

        And("the schema no longer exists")
        val getResult = MetadataSchemaApi.getById(schemaId).value
        getResult shouldBe None
      }
    }

    it("returns a NotFoundError for an invalid schema ID") {
      Given("an invalid schema ID")
      val invalidSchemaId = -1L

      When("calling delete with the invalid schema ID")
      val result = MetadataSchemaApi.delete(invalidSchemaId)

      Then("returns a NotFoundError")
      TestHelper.checkApiError(result) shouldBe a[NotFoundError]
    }

    it("fails when deleteWithReferenceCheck is used and schema has references") {
      Given("a metadata schema that is in use")
      // Use a known schema that has references (typically the first one in the list)
      val schemaId = getFirstSchemaId()

      // Verify it has references
      val hasRefs = MetadataSchemaApi.hasReferences(schemaId).value
      assume(hasRefs, "Test requires a schema with references")

      When("calling deleteWithReferenceCheck")
      val result = MetadataSchemaApi.deleteWithReferenceCheck(schemaId)

      Then("returns an InUseError indicating references exist")
      TestHelper.checkApiError(result) shouldBe a[InUseError]
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

  describe("clone") {
    it("creates a copy of an existing metadata schema") {
      Given("an existing metadata schema")
      withTestSchema(name = "Original Schema") { original =>
        When("calling clone with the schema ID")
        val cloneResult = MetadataSchemaApi.clone(original.id)

        Then("returns a BaseEntityReferenceView for the new clone")
        cloneResult.isRight shouldBe true
        val cloneRef = cloneResult.value
        cloneRef shouldBe a[BaseEntityReferenceView]
        cloneRef.id should not be original.id
        cloneRef.uuid should not be original.uuid

        try {
          And("the cloned schema can be retrieved by its new ID")
          val clonedSchema = MetadataSchemaApi.getById(cloneRef.id).value.value
          val cloneName    = getSchemaName(clonedSchema)
          cloneName.value should startWith("Copy of ")
          cloneName.value should include("Original Schema")
        } finally {
          MetadataSchemaApi.delete(cloneRef.id)
        }
      }
    }

    it("returns a NotFoundError for an invalid schema ID") {
      Given("an invalid schema ID")
      val invalidSchemaId = -1L

      When("calling clone with the invalid schema ID")
      val result = MetadataSchemaApi.clone(invalidSchemaId)

      Then("returns a NotFoundError")
      TestHelper.checkApiError(result) shouldBe a[NotFoundError]
    }

    it("denies access when not authenticated") {
      When("an unauthenticated user calls clone")
      val response = TestHelper.asUnauthenticatedUser { unauthenticated =>
        MetadataSchemaApi.clone(1)(unauthenticated)
      }

      Then("returns an AccessDeniedError")
      TestHelper.checkApiError(response) shouldBe a[AccessDeniedError]
    }
  }

  describe("importSchema") {
    it(
      "imports a metadata schema from a zip file, returning an editable entity ready for stopEdit"
    ) {
      Given("a valid metadata schema export")
      val schemaId    = getFirstSchemaId()
      val exportBytes = MetadataSchemaApi.exportSchema(schemaId).value.value

      When("calling importSchema with the exported bytes")
      val importResult = MetadataSchemaApi.importSchema(exportBytes)

      Then("returns a MetadataSchemaEditView ready for editing")
      importResult.isRight shouldBe true
      val editView = importResult.value
      editView shouldBe a[MetadataSchemaEditView]
      editView.stagingId should not be empty

      var persistedId: Option[Long] = None
      try {
        And("calling stopEdit completes the import and persists the schema")
        val savedResult = MetadataSchemaApi.stopEditAndUnlock(editView)
        savedResult.isRight shouldBe true
        val savedSchema = savedResult.value
        savedSchema shouldBe a[MetadataSchemaView]
        savedSchema.details.id should be > 0L
        persistedId = Some(savedSchema.details.id)
      } finally {
        // If stopEdit succeeded, clean up the persisted schema. If it failed, the
        // staging area is left for server-side garbage collection — there is no
        // client-accessible cleanup path for unpersisted imports.
        persistedId.foreach(id => MetadataSchemaApi.delete(id))
      }
    }

    it("returns an error for invalid (non-zip) bytes") {
      Given("invalid bytes that are not a zip file")
      val invalidBytes = "this is not a zip file".getBytes("UTF-8")

      When("calling importSchema with the invalid bytes")
      val result = MetadataSchemaApi.importSchema(invalidBytes)

      Then("returns an InternalError indicating the import failed")
      TestHelper.checkApiError(result) shouldBe a[InternalError]
    }

    it("denies access when not authenticated") {
      When("an unauthenticated user calls importSchema")
      val dummyBytes = Array[Byte](0, 1, 2, 3)
      val response   = TestHelper.asUnauthenticatedUser { unauthenticated =>
        MetadataSchemaApi.importSchema(dummyBytes)(unauthenticated)
      }

      Then("returns an AccessDeniedError")
      TestHelper.checkApiError(response) shouldBe a[AccessDeniedError]
    }
  }
}
