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

/** Helper object for CollectionDefinitionApi tests containing collection utilities. */
object CollectionDefinitionApiMutationsTestHelper {

  val LEGACY_WIZARD_COLLECTION_NAME = "Legacy Wizard Collection"
  val LEGACY_WIZARD_NAME            = "Legacy wizard"

  /** The element that marks a language bundle held as an immutable `Collections.singletonMap`. */
  val LEGACY_SINGLETON_MAP_ELEMENT = """<strings class="singleton-map">"""

  /** The Raw HTML control's text, as it appears XML-escaped inside the pages blob. */
  val LEGACY_CONTROL_TEXT = "&lt;hr&gt;"

  /** A wizard whose pages blob is in the format oEQ has always persisted.
    *
    * The `singleton-map` bundle with `k`/`v` children is what a Raw HTML control set to a
    * predefined element produces, and what older collections hold. It is not the shape a stock
    * XStream instance writes, so this fixture guards the server's ability to keep reading
    * real-world data.
    */
  def legacyWizard: CollectionWizardView =
    CollectionWizardView(
      name = Some(LEGACY_WIZARD_NAME),
      redraftScript = None,
      saveScript = None,
      allowNonSequentialNavigation = false,
      showPageTitlesNextPrev = false,
      additionalCssClass = None,
      pages = Some(legacyPagesXml),
      fixedMetadata = None
    )

  private def legacyPagesXml: String =
    """<list>
      |  <com.dytech.edge.wizard.beans.DefaultWizardPage>
      |    <controls>
      |      <com.dytech.edge.wizard.beans.control.Html>
      |        <include>true</include>
      |        <description>
      |          <id>0</id>
      |          <strings class="singleton-map">
      |            <k class="string">en</k>
      |            <v class="com.tle.beans.entity.LanguageString">
      |              <id>0</id>
      |              <locale>en</locale>
      |              <priority>1</priority>
      |              <text>&lt;hr&gt;</text>
      |              <bundle reference="../../.."/>
      |            </v>
      |          </strings>
      |        </description>
      |        <targetnodes/>
      |        <items/>
      |      </com.dytech.edge.wizard.beans.control.Html>
      |    </controls>
      |  </com.dytech.edge.wizard.beans.DefaultWizardPage>
      |</list>""".stripMargin

  /** Checks if a collection is locked for editing by attempting to start an edit session with a
    * different user. A collection is considered locked if another user cannot start an edit session
    * on it. If the current user created the lock, then they can still start an edit session, so we
    * need to test with a different user.
    */
  def isCollectionLockedForEditing(collectionId: Long)(implicit cfg: ClientConfiguration): Boolean =
    BaseEntityApiTestHelper.isEntityLockedForEditing(
      collectionId,
      (id, c) => CollectionDefinitionApi.startEdit(id)(c),
      (id, c) => CollectionDefinitionApi.cancelEditForced(id)(c)
    )

  /** Gets the ID of the first collection in the system for use in tests.
    *
    * @param cfg
    *   The client configuration.
    * @return
    *   The ID of the first collection.
    */
  def getFirstCollectionId()(implicit cfg: ClientConfiguration): Long =
    BaseEntityApiTestHelper.getFirstEntityId(CollectionDefinitionApi.listCollections _)

  /** Extracts the default locale name text from a CollectionDefinitionView.
    *
    * @param collection
    *   The collection definition view to extract the name from.
    * @return
    *   The name text if present, or None if the name bundle or strings are missing.
    */
  def getCollectionName(collection: CollectionDefinitionView): Option[String] =
    BaseEntityApiTestHelper.getEntityName(collection.details)

  /** Builds a CollectionDefinitionEditView for a new collection using the skeleton from
    * startCreate.
    *
    * @param skeleton
    *   The skeleton returned from `CollectionDefinitionApi.startCreate()`.
    * @param name
    *   The name to use for the collection. Defaults to "Test Collection".
    * @param description
    *   Optional description for the collection. Defaults to "A test collection".
    * @param wizard
    *   Optional contribution wizard, including its opaque serialised XML blobs.
    * @return
    *   A fully populated CollectionDefinitionEditView ready to be passed to
    *   `CollectionDefinitionApi.add()`.
    */
  def buildNewCollectionDetails(
      skeleton: EntitySkeletonView,
      name: String = "Test Collection",
      description: Option[String] = Some("A test collection"),
      wizard: Option[CollectionWizardView] = None
  ): CollectionDefinitionEditView = {
    val collectionView = CollectionDefinitionView(
      details = BaseEntityApiTestHelper.buildNewEntityDetails(skeleton, name, description),
      schemaId = None,
      wizardCategory = None,
      workflowId = None,
      reviewPeriod = None,
      scormPackagingTransformation = None,
      denyDirectContribution = false,
      wizard = wizard,
      searchDetails = None,
      metadataMapping = None,
      itemMetadataRules = List.empty,
      dynamicMetadataRules = List.empty,
      itemSummaryDisplayTemplate = None
    )

    CollectionDefinitionEditView(
      collection = collectionView,
      stagingId = skeleton.stagingId,
      version = None,
      targetList = List.empty,
      otherTargetLists = List.empty
    )
  }

  /** Loan-pattern helper that creates a test collection, runs the test body, and guarantees
    * cleanup.
    *
    * Creates a new collection using `startCreate` / `add`, and passes its reference to the test
    * body. In the `finally` block, any lingering edit lock is force-cancelled and the collection is
    * deleted — ensuring no resource leaks even when assertions fail.
    *
    * @param name
    *   The name for the test collection. Defaults to "Test Collection".
    * @param description
    *   Optional description. Defaults to Some("A test collection").
    * @param wizard
    *   Optional contribution wizard, including its opaque serialised XML blobs.
    * @param test
    *   The test body, receiving the newly created collection reference.
    * @param cfg
    *   The client configuration.
    */
  def withTestCollection(
      name: String = "Test Collection",
      description: Option[String] = Some("A test collection"),
      wizard: Option[CollectionWizardView] = None
  )(test: BaseEntityReferenceView => Unit)(implicit cfg: ClientConfiguration): Unit =
    withTestCollectionUsing(CollectionDefinitionApi.add)(name, description, wizard)(test)

  /** Loan-pattern helper that creates a locked test collection, runs the test body, and guarantees
    * cleanup.
    *
    * Like [[withTestCollection]] but keeps the collection locked for editing after creation. Use
    * this when the test needs to perform further edit operations without a separate `startEdit`
    * call.
    *
    * @param name
    *   The name for the test collection. Defaults to "Test Collection".
    * @param description
    *   Optional description. Defaults to Some("A test collection").
    * @param test
    *   The test body, receiving the newly created collection reference.
    * @param cfg
    *   The client configuration.
    */
  def withTestCollectionLocked(
      name: String = "Test Collection",
      description: Option[String] = Some("A test collection")
  )(test: BaseEntityReferenceView => Unit)(implicit cfg: ClientConfiguration): Unit =
    withTestCollectionUsing(CollectionDefinitionApi.addAndLock)(name, description, wizard = None)(
      test
    )

  private def withTestCollectionUsing(
      addFn: CollectionDefinitionEditView => Either[List[ApiError], BaseEntityReferenceView]
  )(
      name: String,
      description: Option[String],
      wizard: Option[CollectionWizardView]
  )(test: BaseEntityReferenceView => Unit)(implicit cfg: ClientConfiguration): Unit =
    BaseEntityApiTestHelper.withTestEntity(
      CollectionDefinitionApi.startCreate _,
      buildNewCollectionDetails(_, name = name, description = description, wizard = wizard),
      addFn,
      CollectionDefinitionApi.cancelEditForced,
      CollectionDefinitionApi.delete
    )(test)

  /** Loan-pattern helper for edit sessions that guarantees an edit session is cancelled and the
    * collection is unlocked even when assertions fail.
    *
    * @param collectionId
    *   The numeric ID of the collection to edit.
    * @param test
    *   The test body, receiving the `CollectionDefinitionEditView` from `startEdit`.
    * @param cfg
    *   The client configuration.
    */
  def withEditSession(collectionId: Long)(test: CollectionDefinitionEditView => Unit)(implicit
      cfg: ClientConfiguration
  ): Unit =
    BaseEntityApiTestHelper.withEditSession(
      collectionId,
      CollectionDefinitionApi.startEdit,
      CollectionDefinitionApi.cancelEditForced
    )(test)
}
