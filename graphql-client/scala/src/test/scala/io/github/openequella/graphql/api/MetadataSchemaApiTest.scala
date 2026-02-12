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
    it("returns all metadata schemas") {
      When("calling listSchemas")
      val result = MetadataSchemaApi.listSchemas()

      Then("returns a list of BaseEntityReferenceView")
      result.isRight shouldBe true
      result.value.length should be > 1
      result.value.head.uuid should not be empty
    }

    it("denies access when not authenticated") {
      When("an unauthenticated user calls listSchemas")
      val response = TestHelper.asUnauthenticatedUser { unauthenticated =>
        MetadataSchemaApi.listSchemas()(unauthenticated)
      }

      Then("returns an AccessDeniedError")
      TestHelper.checkApiError(response) shouldBe a[AccessDeniedError]
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
      When("an unauthenticated user calls getIdByUuid")
      val response = TestHelper.asUnauthenticatedUser { unauthenticated =>
        MetadataSchemaApi.getIdByUuid("some-uuid")(unauthenticated)
      }

      Then("returns an AccessDeniedError")
      TestHelper.checkApiError(response) shouldBe a[AccessDeniedError]
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
      val invalidSchemaId = -1L

      When("calling getUses with the invalid schema ID")
      val result = MetadataSchemaApi.getUses(invalidSchemaId)

      Then("returns an empty list")
      result.value shouldBe empty
    }

    it("denies access when not authenticated") {
      When("an unauthenticated user calls getUses")
      val response = TestHelper.asUnauthenticatedUser { unauthenticated =>
        MetadataSchemaApi.getUses(1)(unauthenticated)
      }

      Then("returns an AccessDeniedError")
      TestHelper.checkApiError(response) shouldBe a[AccessDeniedError]
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
      val invalidSchemaId = -1L

      When("calling hasReferences with the invalid schema ID")
      val result = MetadataSchemaApi.hasReferences(invalidSchemaId)

      Then("returns a NotFoundError")
      TestHelper.checkApiError(result) shouldBe a[NotFoundError]
    }

    it("denies access when not authenticated") {
      When("an unauthenticated user calls hasReferences")
      val response = TestHelper.asUnauthenticatedUser { unauthenticated =>
        MetadataSchemaApi.hasReferences(1)(unauthenticated)
      }

      Then("returns an AccessDeniedError")
      TestHelper.checkApiError(response) shouldBe a[AccessDeniedError]
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
      val invalidSchemaId = -1L

      When("calling getImportTypes with the invalid schema ID")
      val result = MetadataSchemaApi.getImportTypes(invalidSchemaId)

      Then("returns an empty list")
      result.value shouldBe empty
    }

    it("denies access when not authenticated") {
      When("an unauthenticated user calls getImportTypes")
      val response = TestHelper.asUnauthenticatedUser { unauthenticated =>
        MetadataSchemaApi.getImportTypes(1)(unauthenticated)
      }

      Then("returns an AccessDeniedError")
      TestHelper.checkApiError(response) shouldBe a[AccessDeniedError]
    }
  }

  describe("exportSchema") {
    it("exports a metadata schema as a ZIP file") {
      val withSecurityOptions = Table(
        "withSecurity",
        true,
        false
      )
      forAll(withSecurityOptions) { withSecurity =>
        Given(s"a valid metadata schema ID with withSecurity=$withSecurity")
        val schemaId = MetadataSchemaApi.listSchemas().value.head.id

        When("calling exportSchema with the schema ID and withSecurity")
        val result = MetadataSchemaApi.exportSchema(schemaId, withSecurity)

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
          """<entity class="com.tle.beans.entity.Schema">"""
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

    it("returns None for an invalid schema ID") {
      Given("an invalid schema ID")
      val invalidSchemaId = -1L

      When("calling exportSchema with the invalid schema ID")
      val result = MetadataSchemaApi.exportSchema(invalidSchemaId, withSecurity = false)

      Then("returns None")
      result shouldBe Right(None)
    }

    it("denies access when not authenticated") {
      When("an unauthenticated user calls exportSchema")
      val response = TestHelper.asUnauthenticatedUser { unauthenticated =>
        MetadataSchemaApi.exportSchema(1, withSecurity = false)(unauthenticated)
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
      val invalidSchemaId = -1L

      When("calling getById with the invalid schema ID")
      val result = MetadataSchemaApi.getById(invalidSchemaId)

      Then("returns None")
      result.isRight shouldBe true
      result.value shouldBe None
    }

    it("denies access when not authenticated") {
      When("an unauthenticated user calls getById")
      val response = TestHelper.asUnauthenticatedUser { unauthenticated =>
        MetadataSchemaApi.getById(1)(unauthenticated)
      }

      Then("returns an AccessDeniedError")
      TestHelper.checkApiError(response) shouldBe a[AccessDeniedError]
    }
  }

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
