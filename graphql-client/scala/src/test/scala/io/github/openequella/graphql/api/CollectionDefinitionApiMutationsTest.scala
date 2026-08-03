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
import io.github.openequella.graphql.api.CollectionDefinitionApiMutationsTestHelper._
import io.github.openequella.graphql.api.views.{
  BaseEntityReferenceView,
  CollectionDefinitionEditView,
  CollectionDefinitionView,
  EntitySkeletonView
}
import io.github.openequella.graphql.test.TestHelper.{
  INVALID_ENTITY_ID,
  CREDENTIALS_ADMIN,
  assertAccessDeniedError,
  asUnauthenticatedUser,
  checkApiError,
  loginToRestInstitution,
  withUser
}
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.{EitherValues, GivenWhenThen, OptionValues}

/** Tests for the mutation operations in the CollectionDefinitionApi.
  *
  * These tests cover state-changing operations for collections, including creating, importing,
  * cloning, deleting, and cancelling edits. They also verify proper error handling and
  * unauthenticated access denial.
  *
  * Shared, entity-agnostic test utilities live in
  * [[io.github.openequella.graphql.test.BaseEntityApiTestHelper]]; the collection-specific wrappers
  * are in [[CollectionDefinitionApiMutationsTestHelper]].
  */
class CollectionDefinitionApiMutationsTest
    extends AnyFunSpec
    with Matchers
    with GivenWhenThen
    with EitherValues
    with OptionValues {
  private implicit val cfg: ClientConfiguration = loginToRestInstitution()

  describe("startEdit") {
    it("initiates an edit session for a valid collection") {
      Given("an existing collection ID")
      val collectionId = getFirstCollectionId()

      When("calling startEdit with the valid collection ID")
      withEditSession(collectionId) { editView =>
        Then("returns a CollectionDefinitionEditView for the collection")
        editView shouldBe a[CollectionDefinitionEditView]
        editView.collection.details.id shouldBe collectionId
        editView.stagingId should not be empty
        editView.targetList shouldBe a[List[_]]
      }
    }

    it("returns a NotFoundError for an invalid collection ID") {
      Given("an invalid collection ID")
      val invalidCollectionId = INVALID_ENTITY_ID

      When("calling startEdit with the invalid collection ID")
      val result = CollectionDefinitionApi.startEdit(invalidCollectionId)

      Then("returns a NotFoundError")
      checkApiError(result) shouldBe a[NotFoundError]
    }

    it("denies access when not authenticated") {
      assertAccessDeniedError(CollectionDefinitionApi.startEdit(1)(_))
    }
  }

  describe("startCreate") {
    it("initiates creation of a new collection") {
      When("calling startCreate")
      val result = CollectionDefinitionApi.startCreate()

      Then("returns an EntitySkeletonView")
      result.isRight shouldBe true
      val skeleton = result.value
      skeleton shouldBe a[EntitySkeletonView]
      skeleton.uuid should not be empty
      skeleton.owner should not be empty
      skeleton.stagingId should not be empty
    }

    it("denies access when not authenticated") {
      assertAccessDeniedError(CollectionDefinitionApi.startCreate()(_))
    }
  }

  describe("add") {
    it("creates a new collection") {
      When("creating a new collection with valid details")
      withTestCollection() { reference =>
        Then("the add result returns a valid BaseEntityReferenceView")
        reference shouldBe a[BaseEntityReferenceView]
        reference.id should be > 0L
        reference.uuid should not be empty
        reference.owner should not be empty
        reference.forCollection shouldBe true

        And("the collection can be resolved by its UUID")
        CollectionDefinitionApi.getIdByUuid(reference.uuid).value.value shouldBe reference.id

        And("the collection appears in the collection listing")
        CollectionDefinitionApi.listCollections().value.map(_.id) should contain(reference.id)

        And("the collection has the expected name")
        withEditSession(reference.id) { editView =>
          getCollectionName(editView.collection).value shouldBe "Test Collection"
        }

        And("the collection is not locked for editing")
        isCollectionLockedForEditing(reference.id) shouldBe false
      }
    }

    it("can keep the collection locked after creation with addAndLock") {
      When("creating a new collection with addAndLock")
      withTestCollectionLocked() { ref =>
        Then("the collection is locked for editing")
        isCollectionLockedForEditing(ref.id) shouldBe true
      }
    }

    it("denies access when not authenticated") {
      Given("a skeleton from startCreate (authenticated)")
      val skeleton = CollectionDefinitionApi.startCreate().value

      And("a fully populated CollectionDefinitionEditView")
      val newCollectionDetails = buildNewCollectionDetails(skeleton)
      assertAccessDeniedError(CollectionDefinitionApi.add(newCollectionDetails)(_))
    }

    it("round trips wizard pages written in the legacy singleton-map format") {
      Given("a collection whose wizard pages hold a language bundle as an immutable singleton map")
      withTestCollection(
        name = LEGACY_WIZARD_COLLECTION_NAME,
        wizard = Some(legacyWizard)
      ) { reference =>
        When("reading the collection back")
        val wizard = CollectionDefinitionApi.getById(reference.id).value.value.wizard.value

        Then("the pages blob is returned in the same legacy format it was sent in")
        val pages = wizard.pages.value
        pages should include(LEGACY_SINGLETON_MAP_ELEMENT)
        pages should include(LEGACY_CONTROL_TEXT)

        And("the rest of the wizard survived the round trip")
        wizard.name.value shouldBe LEGACY_WIZARD_NAME
      }
    }
  }

  describe("importCollection") {
    it(
      "imports a collection from a zip file, returning an editable entity ready for stopEdit"
    ) {
      Given("a valid collection export")
      val collectionId = getFirstCollectionId()
      val exportBytes  = CollectionDefinitionApi.exportCollection(collectionId).value.value

      When("calling importCollection with the exported bytes")
      val importResult = CollectionDefinitionApi.importCollection(exportBytes)

      Then("returns a CollectionDefinitionEditView ready for editing")
      importResult.isRight shouldBe true
      val editView = importResult.value
      editView shouldBe a[CollectionDefinitionEditView]
      editView.stagingId should not be empty

      var persistedId: Option[Long] = None
      try {
        And("calling stopEditAndUnlock completes the import and persists the collection")
        val savedResult = CollectionDefinitionApi.stopEditAndUnlock(editView)
        savedResult.isRight shouldBe true
        val savedCollection = savedResult.value
        savedCollection shouldBe a[CollectionDefinitionView]
        savedCollection.details.id should be > 0L
        persistedId = Some(savedCollection.details.id)
      } finally {
        persistedId.foreach(id => CollectionDefinitionApi.delete(id))
      }
    }

    it("returns an error for invalid (non-zip) bytes") {
      Given("invalid bytes that are not a zip file")
      val invalidBytes = "this is not a zip file".getBytes("UTF-8")

      When("calling importCollection with the invalid bytes")
      val result = CollectionDefinitionApi.importCollection(invalidBytes)

      Then("returns an InternalError indicating the import failed")
      checkApiError(result) shouldBe a[InternalError]
    }

    it("denies access when not authenticated") {
      val dummyBytes = Array[Byte](0, 1, 2, 3)
      assertAccessDeniedError(CollectionDefinitionApi.importCollection(dummyBytes)(_))
    }
  }

  describe("clone") {
    it("creates a copy of an existing collection") {
      Given("an existing collection")
      withTestCollection(name = "Original Collection") { original =>
        When("calling clone with the collection ID")
        val cloneResult = CollectionDefinitionApi.clone(original.id)

        Then("returns a BaseEntityReferenceView for the new clone")
        cloneResult.isRight shouldBe true
        val cloneRef = cloneResult.value
        cloneRef shouldBe a[BaseEntityReferenceView]
        cloneRef.id should not be original.id
        cloneRef.uuid should not be original.uuid

        And("the clone appears in the collection listing")
        CollectionDefinitionApi.listCollections().value.map(_.id) should contain(cloneRef.id)

        try {
          And("the cloned collection has a 'Copy of' name")
          withEditSession(cloneRef.id) { editView =>
            val cloneName = getCollectionName(editView.collection)
            cloneName.value should startWith("Copy of ")
            cloneName.value should include("Original Collection")
          }
        } finally {
          CollectionDefinitionApi.delete(cloneRef.id)
        }
      }
    }

    it("returns a NotFoundError for an invalid collection ID") {
      Given("an invalid collection ID")
      val invalidId = INVALID_ENTITY_ID

      When("calling clone with the invalid collection ID")
      val result = CollectionDefinitionApi.clone(invalidId)

      Then("returns a NotFoundError")
      checkApiError(result) shouldBe a[NotFoundError]
    }

    it("denies access when not authenticated") {
      assertAccessDeniedError(CollectionDefinitionApi.clone(1)(_))
    }
  }

  describe("delete") {
    it("deletes an existing collection") {
      Given("an existing collection")
      withTestCollection(name = "Collection to Delete") { reference =>
        When("calling delete with the collection ID")
        val deleteResult = CollectionDefinitionApi.delete(reference.id)

        Then("completes without errors")
        deleteResult.isRight shouldBe true

        And("the collection ID is no longer resolvable")
        val uuidResult = CollectionDefinitionApi.getIdByUuid(reference.uuid)
        uuidResult.value shouldBe None
      }
    }

    it("returns a NotFoundError for an invalid collection ID") {
      Given("an invalid collection ID")
      val invalidId = INVALID_ENTITY_ID

      When("calling delete with the invalid collection ID")
      val result = CollectionDefinitionApi.delete(invalidId)

      Then("returns a NotFoundError")
      checkApiError(result) shouldBe a[NotFoundError]
    }

    it("denies access when not authenticated") {
      assertAccessDeniedError(CollectionDefinitionApi.delete(1)(_))
    }
  }

  describe("stopEdit") {
    it("saves changes to a collection and unlocks") {
      Given("a collection in edit mode")
      withTestCollectionLocked() { reference =>
        val collectionId = reference.id

        When("calling stopEditAndUnlock")
        val editView   = CollectionDefinitionApi.startEdit(collectionId).value
        val stopResult = CollectionDefinitionApi.stopEditAndUnlock(editView)

        Then("completes without errors and returns the collection")
        stopResult.isRight shouldBe true
        val savedCollection = stopResult.value
        savedCollection shouldBe a[CollectionDefinitionView]
        savedCollection.details.id shouldBe collectionId

        And("the collection is no longer locked for editing")
        isCollectionLockedForEditing(collectionId) shouldBe false
      }
    }

    it("saves changes and keeps the collection locked") {
      Given("a collection in edit mode")
      withTestCollectionLocked() { reference =>
        val collectionId = reference.id

        When("calling stopEdit (keeping locked)")
        val editView   = CollectionDefinitionApi.startEdit(collectionId).value
        val stopResult = CollectionDefinitionApi.stopEdit(editView)

        Then("completes without errors and returns the collection")
        stopResult.isRight shouldBe true
        val savedCollection = stopResult.value
        savedCollection shouldBe a[CollectionDefinitionView]
        savedCollection.details.id shouldBe collectionId

        And("the collection remains locked for editing")
        isCollectionLockedForEditing(collectionId) shouldBe true

        And("cleanup: cancel the edit session to release the lock")
        CollectionDefinitionApi.cancelEdit(collectionId)
      }
    }

    it("returns a NotFoundError for an invalid collection ID") {
      Given("an invalid collection ID")
      val invalidCollectionId = INVALID_ENTITY_ID

      And("a valid CollectionDefinitionEditView")
      val skeleton = CollectionDefinitionApi.startCreate().value
      val editView = buildNewCollectionDetails(skeleton)

      When("calling stopEdit with the invalid collection ID")
      // Create a view with the invalid ID to test the mutation error handling
      val editViewWithBadId = editView.copy(
        collection = editView.collection.copy(
          details = editView.collection.details.copy(id = invalidCollectionId)
        )
      )
      val result = CollectionDefinitionApi.stopEditAndUnlock(editViewWithBadId)

      Then("returns a NotFoundError")
      checkApiError(result) shouldBe a[NotFoundError]
    }

    it("denies access when not authenticated") {
      Given("a valid collection ID in edit mode")
      val collectionId = getFirstCollectionId()
      val editView     = CollectionDefinitionApi.startEdit(collectionId).value

      try {
        When("an unauthenticated user calls stopEditAndUnlock")
        val response = asUnauthenticatedUser { unauthenticated =>
          CollectionDefinitionApi.stopEditAndUnlock(editView)(unauthenticated)
        }

        Then("returns an AccessDeniedError")
        checkApiError(response) shouldBe a[AccessDeniedError]
      } finally {
        // Best-effort cleanup: force-cancel to release any lingering lock.
        CollectionDefinitionApi.cancelEditForced(collectionId)
      }
    }
  }

  describe("cancelEdit/cancelEditForced") {
    it("cancels an edit session and releases the lock") {
      Given("a collection in edit mode")
      withTestCollection() { reference =>
        val collectionId = reference.id

        When("calling cancelEdit after startEdit")
        val editResult = CollectionDefinitionApi.startEdit(collectionId)
        editResult.isRight shouldBe true

        val cancelResult = CollectionDefinitionApi.cancelEdit(collectionId)

        Then("completes without errors")
        cancelResult.isRight shouldBe true

        And("the collection is no longer locked for editing")
        isCollectionLockedForEditing(collectionId) shouldBe false
      }
    }

    it("returns a NotFoundError for an invalid collection ID") {
      Given("an invalid collection ID")
      val invalidCollectionId = INVALID_ENTITY_ID

      When("calling cancelEdit with the invalid collection ID")
      val result = CollectionDefinitionApi.cancelEdit(invalidCollectionId)

      Then("returns a NotFoundError")
      checkApiError(result) shouldBe a[NotFoundError]
    }

    it("forcefully releases locks when cancelEditForced is used") {
      Given("a collection locked by another user")
      withTestCollection() { reference =>
        val collectionId = reference.id

        // Lock the collection as the ADMIN user
        withUser(CREDENTIALS_ADMIN) { implicit adminSession =>
          val adminEditResult = CollectionDefinitionApi.startEdit(collectionId)(adminSession)
          adminEditResult.isRight shouldBe true
        }

        // No inner try/finally needed: withTestCollection's cleanup force-cancels any
        // lingering lock (regardless of owner) before deleting the collection.
        And("the current user cannot start an edit session due to the lock")
        val blockedEditResult = CollectionDefinitionApi.startEdit(collectionId)
        blockedEditResult.isLeft shouldBe true
        blockedEditResult.swap.value.exists(_.isInstanceOf[LockedError]) shouldBe true

        When("calling cancelEditForced to release the other user's lock")
        val forceUnlockResult = CollectionDefinitionApi.cancelEditForced(collectionId)

        Then("completes without errors")
        forceUnlockResult.isRight shouldBe true

        And("the current user can now start an edit session")
        val editResult = CollectionDefinitionApi.startEdit(collectionId)
        editResult.isRight shouldBe true

        And("after cancelling the edit, the collection is no longer locked")
        val cancelResult = CollectionDefinitionApi.cancelEditForced(collectionId)
        cancelResult.isRight shouldBe true
        isCollectionLockedForEditing(collectionId) shouldBe false
      }
    }

    it("denies access when not authenticated") {
      assertAccessDeniedError(CollectionDefinitionApi.cancelEdit(1)(_))
    }
  }
}
