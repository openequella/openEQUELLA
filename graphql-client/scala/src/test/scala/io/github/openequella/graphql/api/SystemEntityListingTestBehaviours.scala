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
import io.github.openequella.graphql.api.views.BaseEntityReferenceView
import io.github.openequella.graphql.test.TestHelper.assertAccessDeniedError
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.{EitherValues, GivenWhenThen}

/** Configuration for the shared tests in [[SystemEntityListingTestBehaviours]].
  *
  * @param entityName
  *   Human-readable entity name used in test descriptions (e.g. `"collection"`).
  * @param systemEntityUuid
  *   UUID of the institution's one 'system type' entity of this type - the "My Content" entity
  *   backing the Scrapbook.
  * @param listFn
  *   The listing which excludes system entities, taking the session to run it against.
  * @param listIncludingSystemFn
  *   The listing which includes them, taking the session to run it against. Called both as the
  *   authenticated user and, for the access check, as an unauthenticated one.
  */
case class SystemEntityListingBehaviorConfig(
    entityName: String,
    systemEntityUuid: String,
    listFn: ClientConfiguration => Either[List[ApiError], List[BaseEntityReferenceView]],
    listIncludingSystemFn: ClientConfiguration => Either[List[
      ApiError
    ], List[BaseEntityReferenceView]]
)

/** ScalaTest shared behaviour mixin for the pair of entity listings which differ only by whether
  * 'system type' entities are included.
  *
  * The two halves are registered separately so that each sits under the `describe` block for the
  * method it exercises. Together they pin down the distinction between the two listings - in
  * particular [[systemEntityExcludedBehavior]], without which both halves would pass whether or not
  * the flag is honoured at all.
  *
  * ===Usage example===
  * {{{
  * private val systemListingConfig = SystemEntityListingBehaviorConfig(...)
  *
  * private val systemListingConfig = SystemEntityListingBehaviorConfig(
  *   entityName = "my entity",
  *   systemEntityUuid = MY_ENTITY_SYSTEM_UUID,
  *   listFn = MyApi.listEntities()(_),
  *   listIncludingSystemFn = MyApi.listEntitiesIncludingSystem()(_)
  * )
  *
  * describe("listEntities") {
  *   systemEntityExcludedBehavior(systemListingConfig)
  * }
  *
  * describe("listEntitiesIncludingSystem") {
  *   systemEntityIncludedBehavior(systemListingConfig)
  * }
  * }}}
  */
trait SystemEntityListingTestBehaviours {
  self: AnyFunSpec with Matchers with GivenWhenThen with EitherValues =>

  /** Registers the one `it` block asserting the plain listing hides system entities.
    *
    * @param config
    *   Entity-specific configuration for the tests.
    * @param cfg
    *   Authenticated [[ClientConfiguration]] to list against.
    */
  def systemEntityExcludedBehavior(
      config: SystemEntityListingBehaviorConfig
  )(implicit cfg: ClientConfiguration): Unit = {
    import config._

    it(s"excludes the system $entityName") {
      When(s"listing ${entityName}s without system ${entityName}s")
      val result = listFn(cfg)

      Then(s"the system $entityName is absent")
      result.value.map(_.uuid) should not contain systemEntityUuid
    }
  }

  /** Registers the `it` blocks covering the listing which includes system entities.
    *
    * @param config
    *   Entity-specific configuration for the tests.
    * @param cfg
    *   Authenticated [[ClientConfiguration]] for the happy-path tests.
    */
  def systemEntityIncludedBehavior(
      config: SystemEntityListingBehaviorConfig
  )(implicit cfg: ClientConfiguration): Unit = {
    import config._

    it(s"includes the system $entityName") {
      When(s"listing ${entityName}s including system ${entityName}s")
      val result = listIncludingSystemFn(cfg)

      Then(s"the system $entityName is present")
      result.value.map(_.uuid) should contain(systemEntityUuid)
    }

    // Guards against the flag being read as a filter *for* system entities rather than an addition
    // to the normal listing. A cardinality comparison against listFn would be flaky here: the
    // mutation suites run concurrently and create and delete their own entities between the calls.
    it(s"also returns ordinary ${entityName}s") {
      When(s"listing ${entityName}s including system ${entityName}s")
      val result = listIncludingSystemFn(cfg)

      Then(s"non-system ${entityName}s are present too")
      result.value.filterNot(_.uuid == systemEntityUuid) should not be empty
    }

    it("denies access when not authenticated") {
      assertAccessDeniedError(listIncludingSystemFn)
    }
  }
}
