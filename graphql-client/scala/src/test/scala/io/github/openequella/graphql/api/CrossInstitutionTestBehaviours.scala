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
import io.github.openequella.graphql.test.TestHelper.sessionInOtherInstitution
import org.scalatest.GivenWhenThen
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

/** Configuration for the [[CrossInstitutionTestBehaviours#crossInstitutionBehavior]] shared test.
  *
  * Groups parameters that would otherwise make `crossInstitutionBehavior` violate the Clean Code
  * guideline of keeping function argument lists short.
  *
  * @param entityName
  *   Human-readable entity name used in the test description (e.g. `"metadata schema"`).
  * @param entityId
  *   Returns the ID to look up. Should be sourced the same way the happy-path test sources it, and
  *   should be stable: the mutation suites run concurrently, so an ID belonging to one of their
  *   short-lived entities makes this test flaky rather than meaningful.
  * @param lookup
  *   The operation under test, taking the ID and the session to run it against. Called twice - once
  *   as the other institution, once as this one.
  * @param assertFound
  *   What a successful lookup looks like for this operation. Asserted against the caller's own
  *   institution, so a foreign not-found cannot be mistaken for the entity having vanished.
  * @param assertNotFound
  *   What "found nothing" means for this operation - `None`, an empty list, or a not-found error.
  */
case class CrossInstitutionBehaviorConfig[T](
    entityName: String,
    entityId: () => Long,
    lookup: (Long, ClientConfiguration) => Either[List[ApiError], T],
    assertFound: Either[List[ApiError], T] => Unit,
    assertNotFound: Either[List[ApiError], T] => Unit
)

/** ScalaTest shared behaviour mixin for cross-institution security tests.
  *
  * Entity IDs are globally unique across institutions, so a by-ID lookup which is not institution
  * filtered hands one institution's entity to a caller of another. The ACL check behind the query
  * does not prevent that: an institution wide grant (e.g. EDIT_COLLECTION at "All collections") is
  * held against the target "*", which matches an entity of any institution. Every query which
  * reaches an entity by ID therefore wants this test.
  *
  * Two things make it a real test rather than a vacuous one, and both are the reason this is a
  * shared behaviour rather than something each suite writes by hand:
  *
  *   - The foreign call is made as `TLE_ADMINISTRATOR` - see
  *     [[io.github.openequella.graphql.test.TestHelper.sessionInOtherInstitution]] for why an
  *     ordinary user of that institution would be a weaker test.
  *   - The happy path is re-asserted afterwards. The mutation suites run concurrently against this
  *     institution, so without that an entity which vanished mid-test would look exactly like the
  *     institution filter working.
  *
  * ===Usage example===
  * {{{
  * describe("getById") {
  *   crossInstitutionBehavior(
  *     CrossInstitutionBehaviorConfig[Option[MyEntityView]](
  *       entityName = "my entity",
  *       entityId = () => aStableEntityId,
  *       lookup = (id, session) => MyApi.getById(id)(session),
  *       assertFound = _.value shouldBe defined,
  *       assertNotFound = _.value shouldBe None
  *     )
  *   )
  * }
  * }}}
  */
trait CrossInstitutionTestBehaviours {
  self: AnyFunSpec with Matchers with GivenWhenThen =>

  /** Registers an `it` block asserting that the operation finds nothing when called from another
    * institution, and still finds the entity when called from its own.
    *
    * @param config
    *   Entity-specific configuration for the test.
    * @param cfg
    *   Authenticated [[ClientConfiguration]] for the institution under test.
    */
  def crossInstitutionBehavior[T](
      config: CrossInstitutionBehaviorConfig[T]
  )(implicit cfg: ClientConfiguration): Unit =
    it(s"does not expose a ${config.entityName} to a caller in another institution") {
      Given(s"the ID of a ${config.entityName} of this institution")
      val entityId = config.entityId()

      When("an administrator of another institution asks for it by that ID")
      val result = config.lookup(entityId, sessionInOtherInstitution())

      Then("nothing is returned")
      config.assertNotFound(result)

      And(s"the ${config.entityName} still resolves in its own institution")
      config.assertFound(config.lookup(entityId, cfg))
    }
}
