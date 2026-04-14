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
import io.github.openequella.graphql.api.{ApiError, LockedError}
import io.github.openequella.graphql.api.views.BaseEntityReferenceView
import org.scalatest.Assertions.fail
import org.scalatest.EitherValues._

/** Shared utility helpers for mutation-level API tests.
  *
  * Provides generic, parameterized helpers that work with any entity API following the standard
  * list/clone/delete/edit-lifecycle patterns. Each API-specific test helper delegates here rather
  * than reimplementing these patterns.
  */
object BaseEntityApiTestHelper {

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
    *   Function that deletes an entity by ID (with optional checkReferences flag).
    * @param test
    *   The test body, receiving the cloned entity's BaseEntityReferenceView.
    */
  def withClonedEntity(
      listFn: () => Either[List[ApiError], List[BaseEntityReferenceView]],
      cloneFn: Long => Either[List[ApiError], BaseEntityReferenceView],
      deleteFn: (Long, Option[Boolean]) => Either[List[ApiError], Unit]
  )(test: BaseEntityReferenceView => Unit): Unit = {
    val sourceId = getFirstEntityId(listFn)
    val cloneRef = cloneFn(sourceId).value

    try {
      test(cloneRef)
    } finally {
      // Best-effort cleanup: ignore errors — clone may already be deleted by test.
      deleteFn(cloneRef.id, None)
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
    * @param cancelEditFn
    *   Function that cancels an edit session; takes (id, force, cfg).
    * @param cfg
    *   The current user's client configuration (used to obtain the institution URL).
    * @return
    *   True if the entity is locked, false if it is not.
    */
  def isEntityLockedForEditing[E](
      entityId: Long,
      startEditFn: (Long, ClientConfiguration) => Either[List[ApiError], E],
      cancelEditFn: (Long, Option[Boolean], ClientConfiguration) => Either[List[ApiError], Unit]
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
        cancelEditFn(entityId, None, otherSession)
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
    * @param cancelEditFn
    *   Function that cancels an edit session for the entity (with optional force flag).
    * @param test
    *   The test body, receiving the edit view from `startEditFn`.
    */
  def withEditSession[E](
      entityId: Long,
      startEditFn: Long => Either[List[ApiError], E],
      cancelEditFn: (Long, Option[Boolean]) => Either[List[ApiError], Unit]
  )(test: E => Unit): Unit = {
    val editView = startEditFn(entityId).value

    try {
      test(editView)
    } finally {
      // Best-effort cleanup: force-cancel to release any lingering lock.
      cancelEditFn(entityId, Some(true))
    }
  }
}
