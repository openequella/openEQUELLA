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
      val schemaId = MetadataSchemaApi.listSchemas().value.head.id

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
      val schemaId = MetadataSchemaApi.listSchemas().value.head.id

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
      val schemaId = MetadataSchemaApi.listSchemas().value.head.id

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

    def isSchemaLockedForEditing(schemaId: Long)(implicit cfg: ClientConfiguration): Boolean = {
      // A schema is considered locked for editing if another user cannot start an edit session on it.
      // If the current user created the lock, then they can still start an edit session,
      // so we need to test with a different user.
      TestHelper.withUser(TestHelper.CREDENTIALS_ADMIN) { implicit otherSession =>
        val editResultOtherUser = MetadataSchemaApi.startEdit(schemaId)(otherSession)
        val isLocked            = editResultOtherUser match {
          case Left(errors) =>
            if (errors.exists(_.isInstanceOf[LockedError])) true
            else
              fail(s"Expected a LockedError, but got: $errors")
          case Right(_) => false
        }
        // tidy-up by cancelling the edit session we just started (if it was successful)
        if (!isLocked) {
          MetadataSchemaApi.cancelEdit(schemaId)(otherSession)
        }
        isLocked
      }
    }
  }
}
