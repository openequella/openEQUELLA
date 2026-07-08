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

package io.github.openequella.graphql.test

import io.github.openequella.graphql.ClientConfiguration
import io.github.openequella.graphql.api.views.{
  BaseEntityReferenceView,
  EntityDetailsView,
  EntitySkeletonView,
  LanguageBundleView,
  LanguageStringView
}
import io.github.openequella.graphql.api.{ApiError, LockedError}
import org.scalatest.Assertions.fail
import org.scalatest.EitherValues._

import java.util.Locale

/** Shared utility helpers for mutation-level API tests.
  *
  * Provides generic, parameterized helpers that work with any entity API following the standard
  * list/clone/delete/edit-lifecycle patterns. Each API-specific test helper delegates here rather
  * than reimplementing these patterns.
  */
object BaseEntityApiTestHelper {
  val NEW_ENTITY_ID          = 0L
  val DEFAULT_PRIORITY       = 1
  val DEFAULT_LOCALE: String = Locale.ENGLISH.toString

  private def makeLanguageBundle(text: String): LanguageBundleView =
    LanguageBundleView(
      id = NEW_ENTITY_ID,
      strings = List(
        LanguageStringView(
          id = NEW_ENTITY_ID,
          priority = DEFAULT_PRIORITY,
          locale = DEFAULT_LOCALE,
          text = text
        )
      )
    )

  /** Builds the common `EntityDetailsView` for a new entity using the skeleton from a `startCreate`
    * operation. Entity-specific test helpers wrap this in their own view types.
    *
    * @param skeleton
    *   The skeleton returned from the entity API's `startCreate()`.
    * @param name
    *   The name to use for the entity.
    * @param description
    *   Optional description for the entity.
    * @return
    *   An `EntityDetailsView` populated for a new (not yet persisted) entity.
    */
  def buildNewEntityDetails(
      skeleton: EntitySkeletonView,
      name: String,
      description: Option[String]
  ): EntityDetailsView =
    EntityDetailsView(
      id = NEW_ENTITY_ID,
      uuid = skeleton.uuid,
      owner = skeleton.owner,
      dateCreated = None,
      dateModified = None,
      nameBundle = Some(makeLanguageBundle(name)),
      descriptionBundle = description.map(makeLanguageBundle),
      attributes = Map.empty,
      disabled = false
    )

  /** Extracts the default locale name text from an entity's details.
    *
    * @param details
    *   The entity details to extract the name from.
    * @return
    *   The name text if present, or None if the name bundle or strings are missing.
    */
  def getEntityName(details: EntityDetailsView): Option[String] =
    for {
      bundle      <- details.nameBundle
      firstString <- bundle.strings.headOption
    } yield firstString.text

  /** Loan-pattern helper that creates a test entity, runs the test body, and guarantees cleanup.
    *
    * Creates a new entity using the `startCreateFn` / `addFn` pair, and passes its reference to the
    * test body. In the `finally` block, any lingering edit lock is force-cancelled and the entity
    * is deleted — ensuring no resource leaks even when assertions fail. All function parameters
    * should have their ClientConfiguration captured at the call site.
    *
    * @param startCreateFn
    *   Function that starts creation of a new entity, returning its skeleton.
    * @param buildDetailsFn
    *   Function that builds the entity details to add from the skeleton.
    * @param addFn
    *   Function that adds the new entity, returning its reference.
    * @param cancelEditForcedFn
    *   Function that force-cancels an edit session for the entity.
    * @param deleteFn
    *   Function that deletes an entity by ID.
    * @param test
    *   The test body, receiving the newly created entity's reference.
    * @tparam D
    *   The entity-specific edit view type passed to `addFn`.
    */
  def withTestEntity[D](
      startCreateFn: () => Either[List[ApiError], EntitySkeletonView],
      buildDetailsFn: EntitySkeletonView => D,
      addFn: D => Either[List[ApiError], BaseEntityReferenceView],
      cancelEditForcedFn: Long => Either[List[ApiError], Unit],
      deleteFn: Long => Either[List[ApiError], Unit]
  )(test: BaseEntityReferenceView => Unit): Unit = {
    val skeleton  = startCreateFn().value
    val details   = buildDetailsFn(skeleton)
    val reference = addFn(details).value
    val entityId  = reference.id

    try {
      test(reference)
    } finally {
      // Best-effort cleanup: force-cancel any lingering edit lock, then delete.
      // Errors are ignored — the entity may already be unlocked or deleted by the test.
      cancelEditForcedFn(entityId)
      deleteFn(entityId)
    }
  }

  /** Returns the ID of the first entity in the list, or fails the test if the list is empty.
    *
    * @param listFn
    *   Function that lists entities, returning a reference view for each. The ClientConfiguration
    *   should be captured in the function value at the call site.
    * @return
    *   The ID of the first entity.
    */
  def getFirstEntityId(
      listFn: () => Either[List[ApiError], List[BaseEntityReferenceView]]
  ): Long =
    listFn() match {
      case Right(entities) if entities.nonEmpty => entities.head.id
      case Right(_)                             =>
        fail("Test setup error: No entities available in the system")
      case Left(errors) =>
        fail(s"Failed to retrieve entities: ${errors.mkString(", ")}")
    }

  /** Loan-pattern helper that clones an entity, runs the test body, and guarantees cleanup by
    * deleting the clone.
    *
    * The entity to clone is determined by calling `listFn` and taking the first result. All
    * function parameters should have their ClientConfiguration captured at the call site.
    *
    * @param listFn
    *   Function that lists entities to find one to clone.
    * @param cloneFn
    *   Function that clones an entity by ID.
    * @param deleteFn
    *   Function that deletes an entity by ID.
    * @param test
    *   The test body, receiving the cloned entity's BaseEntityReferenceView.
    */
  def withClonedEntity(
      listFn: () => Either[List[ApiError], List[BaseEntityReferenceView]],
      cloneFn: Long => Either[List[ApiError], BaseEntityReferenceView],
      deleteFn: Long => Either[List[ApiError], Unit]
  )(test: BaseEntityReferenceView => Unit): Unit = {
    val sourceId = getFirstEntityId(listFn)
    val cloneRef = cloneFn(sourceId).value

    try {
      test(cloneRef)
    } finally {
      // Best-effort cleanup: ignore errors — clone may already be deleted by test.
      deleteFn(cloneRef.id)
    }
  }

  /** Checks if an entity is locked for editing by attempting to start an edit session with a
    * different user (the admin user). A locked entity will return a LockedError; an unlocked one
    * will allow the session to start, which is then immediately cancelled.
    *
    * The `startEditFn` and `cancelEditFn` take an explicit ClientConfiguration so that the admin
    * user's session can be passed when probing for the lock.
    *
    * @param entityId
    *   The ID of the entity to check.
    * @param startEditFn
    *   Function that starts an edit session; takes (id, cfg).
    * @param cancelEditForcedFn
    *   Function that cancels an edit session; takes (id, cfg).
    * @param cfg
    *   The current user's client configuration (used to obtain the institution URL).
    * @return
    *   True if the entity is locked, false if it is not.
    */
  def isEntityLockedForEditing[E](
      entityId: Long,
      startEditFn: (Long, ClientConfiguration) => Either[List[ApiError], E],
      cancelEditForcedFn: (Long, ClientConfiguration) => Either[List[ApiError], Unit]
  )(implicit cfg: ClientConfiguration): Boolean =
    TestHelper.withUser(TestHelper.CREDENTIALS_ADMIN) { otherSession =>
      val editResult = startEditFn(entityId, otherSession)
      val isLocked   = editResult match {
        case Left(errors) =>
          if (errors.exists(_.isInstanceOf[LockedError])) true
          else fail(s"Expected a LockedError, but got: $errors")
        case Right(_) => false
      }
      // Tidy up by cancelling the edit session we just started (if it was successful).
      if (!isLocked) {
        cancelEditForcedFn(entityId, otherSession)
      }
      isLocked
    }

  /** Loan-pattern helper that starts an edit session on an existing entity and guarantees cleanup.
    *
    * Calls `startEditFn` for the given entity, passes the resulting edit view to the test body, and
    * in the `finally` block force-cancels the edit session — ensuring the lock is released even
    * when assertions fail. All function parameters should have their ClientConfiguration captured
    * at the call site.
    *
    * @param entityId
    *   The numeric ID of the entity to edit.
    * @param startEditFn
    *   Function that starts an edit session for the entity.
    * @param cancelEditForcedFn
    *   Function that force-cancels an edit session for the entity.
    * @param test
    *   The test body, receiving the edit view from `startEditFn`.
    */
  def withEditSession[E](
      entityId: Long,
      startEditFn: Long => Either[List[ApiError], E],
      cancelEditForcedFn: Long => Either[List[ApiError], Unit]
  )(test: E => Unit): Unit = {
    val editView = startEditFn(entityId).value

    try {
      test(editView)
    } finally {
      // Best-effort cleanup: force-cancel to release any lingering lock.
      cancelEditForcedFn(entityId)
    }
  }
}
