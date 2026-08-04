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
import io.github.openequella.graphql.api.ApiError
import io.github.openequella.graphql.api.InternalGroupApi
import io.github.openequella.graphql.api.InternalUserApi
import io.github.openequella.graphql.api.Pagination
import io.github.openequella.graphql.api.PaginationResult
import io.github.openequella.graphql.test.PaginationTestHelper.paginateForward

import scala.language.reflectiveCalls

object UserDirectoryTestHelper {

  /** Default number of temporary entries to create for user directory pagination tests. */
  val DEFAULT_ENTRY_COUNT = 5

  /** Structural type for user directory search results that expose the ID needed for cleanup. */
  private type UniqueIdEntry = { def uniqueId: String }

  /** Creates temporary internal users with the supplied name prefix, runs the supplied test, and
    * cleans them up.
    *
    * @param namePrefix
    *   Prefix for temporary usernames in one test.
    * @param entryCount
    *   Number of temporary users to create. Must be at least 1.
    * @param test
    *   The test body to run.
    * @param cfg
    *   Client configuration used by the internal user API calls.
    */
  def withTemporaryInternalUsers(
      namePrefix: String,
      entryCount: Int = DEFAULT_ENTRY_COUNT
  )(test: => Unit)(implicit cfg: ClientConfiguration): Unit =
    withTemporaryEntries(
      namePrefix,
      name => {
        val user = TestUserGenerator.next()
        InternalUserApi.createUser(name, None, user.firstName, user.lastName, user.password)
      },
      InternalUserApi.deleteUser,
      (pagination, prefix) => InternalUserApi.searchUsers(pagination, Some(prefix))(cfg),
      entryCount
    )(test)

  /** Creates temporary internal groups with the supplied name prefix, runs the supplied test, and
    * cleans them up.
    *
    * @param namePrefix
    *   Prefix for temporary group names in one test.
    * @param entryCount
    *   Number of temporary groups to create. Must be at least 1.
    * @param test
    *   The test body to run.
    * @param cfg
    *   Client configuration used by the internal group API calls.
    */
  def withTemporaryInternalGroups(
      namePrefix: String,
      entryCount: Int = DEFAULT_ENTRY_COUNT
  )(test: => Unit)(implicit cfg: ClientConfiguration): Unit =
    withTemporaryEntries(
      namePrefix,
      name => InternalGroupApi.createGroup(name),
      InternalGroupApi.deleteGroup,
      InternalGroupApi.searchGroups,
      entryCount
    )(test)

  /** Creates temporary entries with the supplied name prefix, runs the supplied test, and cleans
    * them up.
    *
    * This helper only manages test data. It removes old entries returned by `searchEntries`,
    * creates a fixed number of new temporary entries, runs `test`, and searches/deletes matching
    * entries again in a `finally` block.
    *
    * @param namePrefix
    *   Prefix for temporary entry names in one test.
    * @param createEntry
    *   Creates one temporary entry for the supplied name.
    * @param deleteEntry
    *   Deletes a temporary entry by unique ID.
    * @param searchEntries
    *   Finds temporary entries created with the supplied name prefix so they can be deleted during
    *   cleanup.
    * @param entryCount
    *   Number of temporary entries to create. Must be at least 1.
    * @param test
    *   The test body to run.
    */
  def withTemporaryEntries[A <: UniqueIdEntry, B](
      namePrefix: String,
      createEntry: String => Either[List[ApiError], B],
      deleteEntry: String => Either[List[ApiError], Unit],
      searchEntries: (Pagination, String) => Either[List[ApiError], PaginationResult[A]],
      entryCount: Int = DEFAULT_ENTRY_COUNT
  )(test: => Unit): Unit = {
    def cleanupEntries(): Unit =
      paginateForward()(pagination => searchEntries(pagination, namePrefix))
        .map(_.uniqueId)
        .foreach(deleteEntry)

    def createEntries(): Unit = {
      require(
        entryCount >= 1,
        s"entryCount must be at least 1, but was $entryCount"
      )

      (1 to entryCount).foreach { index =>
        createEntry(s"$namePrefix-$index").left
          .foreach { errors =>
            throw new RuntimeException(s"Failed to create temporary entry: $errors")
          }
      }
    }

    try {
      cleanupEntries()
      createEntries()
      test
    } finally {
      cleanupEntries()
    }
  }
}
