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
import io.github.openequella.graphql.api.views._
import io.github.openequella.graphql.test.TestHelper
import org.scalatest.Assertions.fail

/** Helper object for MetadataSchemaApiMutationsTest containing schema creation utilities. */
object MetadataSchemaApiMutationsTestHelper {

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

  /** Gets the ID of the first schema in the system for use in tests.
    *
    * @param cfg
    *   The client configuration.
    * @return
    *   The ID of the first metadata schema.
    * @throws NoSuchElementException
    *   if no schemas exist in the system.
    */
  def getFirstSchemaId()(implicit cfg: ClientConfiguration): Long = {
    MetadataSchemaApi.listSchemas() match {
      case Right(schemas) if schemas.nonEmpty => schemas.head.id
      case Right(_)                           =>
        fail("Test setup error: No schemas available in the system")
      case Left(errors) =>
        fail(s"Failed to retrieve schemas: ${errors.mkString(", ")}")
    }
  }

  /** Extracts the default locale name text from a MetadataSchemaView.
    *
    * @param schema
    *   The metadata schema view to extract the name from.
    * @return
    *   The name text if present, or None if the name bundle or strings are missing.
    */
  def getSchemaName(schema: MetadataSchemaView): Option[String] =
    for {
      bundle      <- schema.details.nameBundle
      firstString <- bundle.strings.headOption
    } yield firstString.text

  /** Builds a MetadataSchemaEditView for a new schema using the skeleton from startCreate.
    *
    * @param skeleton
    *   The skeleton returned from `MetadataSchemaApi.startCreate()`.
    * @param name
    *   The name to use for the schema. Defaults to "Test Schema".
    * @param description
    *   Optional description for the schema. Defaults to "A test schema".
    * @return
    *   A fully populated MetadataSchemaEditView ready to be passed to `MetadataSchemaApi.add()`.
    */
  def buildNewSchemaDetails(
      skeleton: EntitySkeletonView,
      name: String = "Test Schema",
      description: Option[String] = Some("A test schema")
  ): MetadataSchemaEditView = {
    val nameBundle = LanguageBundleView(
      id = 0,
      strings = List(LanguageStringView(id = 0, priority = 1, locale = "en", text = name))
    )

    val descriptionBundle = description.map { desc =>
      LanguageBundleView(
        id = 0,
        strings = List(LanguageStringView(id = 0, priority = 1, locale = "en", text = desc))
      )
    }

    val details = EntityDetailsView(
      id = 0,
      uuid = skeleton.uuid,
      owner = skeleton.owner,
      dateCreated = None,
      dateModified = None,
      nameBundle = Some(nameBundle),
      descriptionBundle = descriptionBundle,
      attributes = Map.empty,
      disabled = false
    )

    val schemaView = MetadataSchemaView(
      details = details,
      exportTransforms = List.empty,
      importTransforms = List.empty,
      itemNamePath = "/item/name",
      itemDescriptionPath = "/item/description",
      definition = "<xml><item><name/><description/></item></xml>",
      citations = List.empty
    )

    MetadataSchemaEditView(
      schema = schemaView,
      stagingId = skeleton.stagingId,
      version = None,
      targetList = List.empty
    )
  }
}
