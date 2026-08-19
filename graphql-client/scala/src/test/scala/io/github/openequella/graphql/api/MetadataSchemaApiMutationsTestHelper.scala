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
import io.github.openequella.graphql.test.BaseEntityApiTestHelper

/** Helper object for MetadataSchemaApiMutationsTest containing schema creation utilities. */
object MetadataSchemaApiMutationsTestHelper {

  /** Checks if a metadata schema is locked for editing by attempting to start an edit session with
    * a different user. A schema is considered locked for editing if another user cannot start an
    * edit session on it. If the current user created the lock, then they can still start an edit
    * session, so we need to test with a different user.
    */
  def isSchemaLockedForEditing(schemaId: Long)(implicit cfg: ClientConfiguration): Boolean =
    BaseEntityApiTestHelper.isEntityLockedForEditing(
      schemaId,
      (id, c) => MetadataSchemaApi.startEdit(id)(c),
      (id, c) => MetadataSchemaApi.cancelEditForced(id)(c)
    )

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
    BaseEntityApiTestHelper.getFirstEntityId(MetadataSchemaApi.listSchemas _)

  /** Extracts the default locale name text from a MetadataSchemaView.
    *
    * @param schema
    *   The metadata schema view to extract the name from.
    * @return
    *   The name text if present, or None if the name bundle or strings are missing.
    */
  def getSchemaName(schema: MetadataSchemaView): Option[String] =
    BaseEntityApiTestHelper.getEntityName(schema.details)

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
    val schemaView = MetadataSchemaView(
      details = BaseEntityApiTestHelper.buildNewEntityDetails(skeleton, name, description),
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
      targetList = List.empty,
      otherTargetLists = List.empty
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
    * @param test
    *   The test body, receiving the newly created schema reference.
    * @param cfg
    *   The client configuration.
    */
  def withTestSchema(
      name: String = "Test Schema",
      description: Option[String] = Some("A test schema")
  )(test: BaseEntityReferenceView => Unit)(implicit cfg: ClientConfiguration): Unit =
    withTestSchemaUsing(MetadataSchemaApi.add)(name, description)(test)

  /** Loan-pattern helper that creates a locked test schema, runs the test body, and guarantees
    * cleanup.
    *
    * Like [[withTestSchema]] but keeps the schema locked for editing after creation. Use this when
    * the test needs to perform further edit operations without a separate `startEdit` call.
    *
    * @param name
    *   The name for the test schema. Defaults to "Test Schema".
    * @param description
    *   Optional description. Defaults to Some("A test schema").
    * @param test
    *   The test body, receiving the newly created schema reference.
    * @param cfg
    *   The client configuration.
    */
  def withTestSchemaLocked(
      name: String = "Test Schema",
      description: Option[String] = Some("A test schema")
  )(test: BaseEntityReferenceView => Unit)(implicit cfg: ClientConfiguration): Unit =
    withTestSchemaUsing(MetadataSchemaApi.addAndLock)(name, description)(test)

  private def withTestSchemaUsing(
      addFn: MetadataSchemaEditView => Either[List[ApiError], BaseEntityReferenceView]
  )(
      name: String,
      description: Option[String]
  )(test: BaseEntityReferenceView => Unit)(implicit cfg: ClientConfiguration): Unit =
    BaseEntityApiTestHelper.withTestEntity(
      MetadataSchemaApi.startCreate _,
      buildNewSchemaDetails(_, name = name, description = description),
      addFn,
      MetadataSchemaApi.cancelEditForced,
      MetadataSchemaApi.delete
    )(test)

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
  ): Unit =
    BaseEntityApiTestHelper.withEditSession(
      schemaId,
      MetadataSchemaApi.startEdit,
      MetadataSchemaApi.cancelEditForced
    )(test)
}
