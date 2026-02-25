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
import org.scalatest.EitherValues._

import java.util.Locale

/** Helper object for MetadataSchemaApiMutationsTest containing schema creation utilities. */
object MetadataSchemaApiMutationsTestHelper {
  val NEW_ENTITY_ID          = 0L
  val DEFAULT_PRIORITY       = 1
  val DEFAULT_LOCALE: String = Locale.ENGLISH.toString

  /** Checks if a metadata schema is locked for editing by attempting to start an edit session with
    * a different user. A schema is considered locked for editing if another user cannot start an
    * edit session on it. If the current user created the lock, then they can still start an edit
    * session, so we need to test with a different user.
    */
  def isSchemaLockedForEditing(schemaId: Long)(implicit cfg: ClientConfiguration): Boolean =
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

  /** Gets the ID of the first schema in the system for use in tests.
    *
    * @param cfg
    *   The client configuration.
    * @return
    *   The ID of the first metadata schema.
    * @throws NoSuchElementException
    *   if no schemas exist in the system.
    */
  def getFirstSchemaId()(implicit cfg: ClientConfiguration): Long =
    MetadataSchemaApi.listSchemas() match {
      case Right(schemas) if schemas.nonEmpty => schemas.head.id
      case Right(_)                           =>
        fail("Test setup error: No schemas available in the system")
      case Left(errors) =>
        fail(s"Failed to retrieve schemas: ${errors.mkString(", ")}")
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
      id = NEW_ENTITY_ID,
      strings = List(
        LanguageStringView(
          id = NEW_ENTITY_ID,
          priority = DEFAULT_PRIORITY,
          locale = DEFAULT_LOCALE,
          text = name
        )
      )
    )

    val descriptionBundle = description.map { desc =>
      LanguageBundleView(
        id = NEW_ENTITY_ID,
        strings = List(
          LanguageStringView(
            id = NEW_ENTITY_ID,
            priority = DEFAULT_PRIORITY,
            locale = DEFAULT_LOCALE,
            text = desc
          )
        )
      )
    }

    val details = EntityDetailsView(
      id = NEW_ENTITY_ID,
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

  /** Loan-pattern helper that creates a test schema, runs the test body, and guarantees cleanup.
    *
    * Creates a new metadata schema using `startCreate` / `add`, resolves its ID, and passes it to
    * the test body. In the `finally` block, any lingering edit lock is force-cancelled and the
    * schema is deleted — ensuring no resource leaks even when assertions fail.
    *
    * @param name
    *   The name for the test schema. Defaults to "Test Schema".
    * @param description
    *   Optional description. Defaults to Some("A test schema").
    * @param lockAfterwards
    *   Whether to keep the schema locked after creation. Defaults to false.
    * @param test
    *   The test body, receiving the newly created schema's numeric ID.
    * @param cfg
    *   The client configuration.
    */
  def withTestSchema(
      name: String = "Test Schema",
      description: Option[String] = Some("A test schema"),
      lockAfterwards: Boolean = false
  )(test: BaseEntityReferenceView => Unit)(implicit cfg: ClientConfiguration): Unit = {
    val skeleton  = MetadataSchemaApi.startCreate().value
    val details   = buildNewSchemaDetails(skeleton, name = name, description = description)
    val reference = MetadataSchemaApi.add(details, lockAfterwards = lockAfterwards).value
    val schemaId  = reference.id

    try {
      test(reference)
    } finally {
      // Best-effort cleanup: force-cancel any lingering edit lock, then delete.
      // Errors are ignored — the schema may already be unlocked or deleted by the test.
      MetadataSchemaApi.cancelEdit(schemaId, Some(true))
      MetadataSchemaApi.delete(schemaId)
    }
  }

  /** Loan-pattern helper that starts an edit session on an existing schema and guarantees cleanup.
    *
    * Calls `startEdit` for the given schema, passes the resulting `MetadataSchemaEditView` to the
    * test body, and in the `finally` block force-cancels the edit session — ensuring the lock is
    * released even when assertions fail.
    *
    * @param schemaId
    *   The numeric ID of the schema to edit.
    * @param test
    *   The test body, receiving the `MetadataSchemaEditView` from `startEdit`.
    * @param cfg
    *   The client configuration.
    */
  def withEditSession(schemaId: Long)(test: MetadataSchemaEditView => Unit)(implicit
      cfg: ClientConfiguration
  ): Unit = {
    val editView = MetadataSchemaApi.startEdit(schemaId).value

    try {
      test(editView)
    } finally {
      // Best-effort cleanup: force-cancel to release any lingering lock.
      MetadataSchemaApi.cancelEdit(schemaId, Some(true))
    }
  }
}
