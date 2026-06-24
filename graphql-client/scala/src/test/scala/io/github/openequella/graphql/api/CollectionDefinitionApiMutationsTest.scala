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
import io.github.openequella.graphql.api.CollectionDefinitionApiMutationsTestHelper.{
  getFirstCollectionId,
  isCollectionLockedForEditing,
  withClonedCollection,
  withEditSession
}
import io.github.openequella.graphql.api.views.{
  BaseEntityReferenceView,
  CollectionDefinitionEditView,
  CollectionDefinitionView
}
import io.github.openequella.graphql.test.TestHelper
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.{EitherValues, GivenWhenThen, OptionValues}

/** Tests for the mutation operations in the CollectionDefinitionApi.
  *
  * These tests cover state-changing operations for collections, including importing, cloning,
  * deleting, and cancelling edits. They also verify proper error handling and unauthenticated
  * access denial.
  *
  * TODO: Once this test suite is fully implemented, consider refactoring to use a shared set of
  * test utilities for collection-related tests, as there will likely be significant overlap with
  * tests for other collection-related APIs (e.g. MetadataSchemaApi). Maybe look to what was done on
  * the server side with `com.tle.web.remoting.graphql.provider.ImportBase64ZipValidationTests` but
  * taking it further.
  */
class CollectionDefinitionApiMutationsTest
    extends AnyFunSpec
    with Matchers
    with GivenWhenThen
    with EitherValues
    with OptionValues {
  private implicit val cfg: ClientConfiguration = TestHelper.loginToRestInstitution()

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
      val invalidCollectionId = -1L

      When("calling startEdit with the invalid collection ID")
      val result = CollectionDefinitionApi.startEdit(invalidCollectionId)

      Then("returns a NotFoundError")
      TestHelper.checkApiError(result) shouldBe a[NotFoundError]
    }

    it("denies access when not authenticated") {
      When("an unauthenticated user calls startEdit")
      val response = TestHelper.asUnauthenticatedUser { unauthenticated =>
        CollectionDefinitionApi.startEdit(1)(unauthenticated)
      }

      Then("returns an AccessDeniedError")
      TestHelper.checkApiError(response) shouldBe a[AccessDeniedError]
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
      TestHelper.checkApiError(result) shouldBe a[InternalError]
    }

    it("denies access when not authenticated") {
      When("an unauthenticated user calls importCollection")
      val dummyBytes = Array[Byte](0, 1, 2, 3)
      val response   = TestHelper.asUnauthenticatedUser { unauthenticated =>
        CollectionDefinitionApi.importCollection(dummyBytes)(unauthenticated)
      }

      Then("returns an AccessDeniedError")
      TestHelper.checkApiError(response) shouldBe a[AccessDeniedError]
    }
  }

  describe("clone") {
    it("creates a copy of an existing collection") {
      // TODO: This needs to be reworked to be the same as the clone tests for MetadataSchemaApi,
      //       however this is not possible until we have an `add` method to create new collections.
      //       Once that's available, we can create a withTestCollection helper.
      Given("an existing collection")
      withClonedCollection { cloneRef =>
        Then("returns a BaseEntityReferenceView for the new clone")
        cloneRef shouldBe a[BaseEntityReferenceView]

        And("the clone has a different ID and UUID from any known collection")
        val allCollections = CollectionDefinitionApi.listCollections().value
        allCollections.map(_.id) should contain(cloneRef.id)

        val original = allCollections.find(c => c.id != cloneRef.id && c.forCollection)
        original should not be empty
        cloneRef.id should not be original.get.id
        cloneRef.uuid should not be original.get.uuid
      }
    }

    it("returns a NotFoundError for an invalid collection ID") {
      Given("an invalid collection ID")
      val invalidId = -1L

      When("calling clone with the invalid collection ID")
      val result = CollectionDefinitionApi.clone(invalidId)

      Then("returns a NotFoundError")
      TestHelper.checkApiError(result) shouldBe a[NotFoundError]
    }

    it("denies access when not authenticated") {
      When("an unauthenticated user calls clone")
      val response = TestHelper.asUnauthenticatedUser { unauthenticated =>
        CollectionDefinitionApi.clone(1)(unauthenticated)
      }

      Then("returns an AccessDeniedError")
      TestHelper.checkApiError(response) shouldBe a[AccessDeniedError]
    }
  }

  describe("delete") {
    it("deletes a cloned collection") {
      Given("a cloned collection")
      val collectionId = getFirstCollectionId()
      val cloneRef     = CollectionDefinitionApi.clone(collectionId).value

      When("calling delete with the cloned collection ID")
      val deleteResult = CollectionDefinitionApi.delete(cloneRef.id)

      Then("completes without errors")
      deleteResult.isRight shouldBe true

      And("the collection ID is no longer resolvable")
      val uuidResult = CollectionDefinitionApi.getIdByUuid(cloneRef.uuid)
      uuidResult.value shouldBe None
    }

    it("returns a NotFoundError for an invalid collection ID") {
      Given("an invalid collection ID")
      val invalidId = -1L

      When("calling delete with the invalid collection ID")
      val result = CollectionDefinitionApi.delete(invalidId)

      Then("returns a NotFoundError")
      TestHelper.checkApiError(result) shouldBe a[NotFoundError]
    }

    it("denies access when not authenticated") {
      When("an unauthenticated user calls delete")
      val response = TestHelper.asUnauthenticatedUser { unauthenticated =>
        CollectionDefinitionApi.delete(1)(unauthenticated)
      }

      Then("returns an AccessDeniedError")
      TestHelper.checkApiError(response) shouldBe a[AccessDeniedError]
    }
  }

  describe("stopEdit") {
    it("saves changes to a collection and unlocks") {
      Given("a cloned collection to use for testing")
      withClonedCollection { cloneRef =>
        val collectionId = cloneRef.id

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
      Given("a cloned collection")
      withClonedCollection { cloneRef =>
        When("calling stopEdit (keeping locked)")
        withEditSession(cloneRef.id) { editView =>
          val stopResult = CollectionDefinitionApi.stopEdit(editView)

          Then("completes without errors and returns the collection")
          stopResult.isRight shouldBe true
          val savedCollection = stopResult.value
          savedCollection shouldBe a[CollectionDefinitionView]
          savedCollection.details.id shouldBe cloneRef.id

          And("the collection remains locked for editing")
          isCollectionLockedForEditing(cloneRef.id) shouldBe true
          // withEditSession's finally calls cancelEditForced automatically
        }
      }
    }

    it("returns a NotFoundError for an invalid collection ID") {
      Given("an existing collection in an edit session")
      val collectionId = getFirstCollectionId()

      withEditSession(collectionId) { editView =>
        And("the edit view is modified to have an invalid collection ID")
        val invalidCollectionId = -1L
        val editViewWithBadId   = editView.copy(
          collection = editView.collection.copy(
            details = editView.collection.details.copy(id = invalidCollectionId)
          )
        )

        When("calling stopEditAndUnlock with the invalid collection ID")
        val result = CollectionDefinitionApi.stopEditAndUnlock(editViewWithBadId)

        Then("returns a NotFoundError")
        TestHelper.checkApiError(result) shouldBe a[NotFoundError]
      }
    }

    it("denies access when not authenticated") {
      Given("a valid collection ID in edit mode")
      val collectionId = getFirstCollectionId()
      val editView     = CollectionDefinitionApi.startEdit(collectionId).value

      try {
        When("an unauthenticated user calls stopEditAndUnlock")
        val response = TestHelper.asUnauthenticatedUser { unauthenticated =>
          CollectionDefinitionApi.stopEditAndUnlock(editView)(unauthenticated)
        }

        Then("returns an AccessDeniedError")
        TestHelper.checkApiError(response) shouldBe a[AccessDeniedError]
      } finally {
        // Best-effort cleanup: force-cancel to release any lingering lock.
        CollectionDefinitionApi.cancelEditForced(collectionId)
      }
    }
  }

  describe("cancelEdit") {
    // TODO: Once `add` method is implemented, add a test to verify that `cancelEdit` properly
    //  cancels an edit session and unlocks the collection. This will require the ability to
    //  start editing and then cancel without saving.

    it("denies access when not authenticated") {
      When("an unauthenticated user calls cancelEdit")
      val response = TestHelper.asUnauthenticatedUser { unauthenticated =>
        CollectionDefinitionApi.cancelEdit(1)(unauthenticated)
      }

      Then("returns an AccessDeniedError")
      TestHelper.checkApiError(response) shouldBe a[AccessDeniedError]
    }
  }
}
