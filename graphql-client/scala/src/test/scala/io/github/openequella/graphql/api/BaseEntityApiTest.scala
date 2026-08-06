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
import io.github.openequella.graphql.api.views.{BaseEntitySecurityView, LanguageBundleView}
import io.github.openequella.graphql.test.TestHelper.{
  INVALID_ENTITY_ID,
  assertAccessDeniedError,
  assertNotFoundError,
  loginToRestInstitution
}
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.{EitherValues, GivenWhenThen, OptionValues}

class BaseEntityApiTest
    extends AnyFunSpec
    with CrossInstitutionTestBehaviours
    with Matchers
    with GivenWhenThen
    with EitherValues
    with OptionValues {

  private implicit val cfg: ClientConfiguration = loginToRestInstitution()

  /** Any ID will do when the call is expected to be rejected before the entity is looked up. */
  private val AnyEntityId = 1L

  private val NoCollectionWithEntries =
    "No collection in this institution has any access control entries"
  private val NoWorkflowWithSubEntityLists =
    "No workflow in this institution has any sub-entity access control lists"
  private val NoSchemaResolved =
    "Every metadata schema in this institution vanished before its security could be retrieved"

  describe("getNameById") {
    it("retrieves name for a known base entity by ID") {
      Given("a valid base entity ID")
      val id = getBaseEntityId

      When("calling getNameById with the valid ID")
      val result = BaseEntityApi.getNameById(id)

      Then("returns the name of the base entity")
      val languageBundle = result.value.value
      languageBundle.id should be > 0L
      languageBundle.strings shouldBe a[List[_]]
      languageBundle.strings should not be empty

      languageBundle.strings.foreach { s =>
        s.id should be > 0L
        s.priority shouldBe a[Int]
        s.locale.trim should not be empty
        s.text.trim should not be empty
      }
    }

    it("returns None for an unknown base entity") {
      Given("an invalid base entity ID")
      val invalidId = INVALID_ENTITY_ID

      When("calling getNameById with the invalid ID")
      val result = BaseEntityApi.getNameById(invalidId)

      Then("returns None")
      result shouldBe Right(None)
    }

    it("denies access when not authenticated") {
      assertAccessDeniedError(BaseEntityApi.getNameById(AnyEntityId)(_))
    }

    // This query requires no privilege beyond being logged in, so without institution filtering any
    // user of any institution could enumerate IDs and harvest every entity name in the installation.
    crossInstitutionBehavior(
      CrossInstitutionBehaviorConfig[Option[LanguageBundleView]](
        entityName = "base entity's name",
        entityId = () => anEntityIdWithAName,
        lookup = (id, session) => BaseEntityApi.getNameById(id)(session),
        assertFound = _.value shouldBe defined,
        assertNotFound = _ shouldBe Right(None)
      )
    )
  }

  describe("getSecurityById") {
    it("retrieves the access control entries of an entity which has them") {
      Given("the collections of this institution")
      val collections = collectionIds

      When("retrieving the security details of each")
      val (id, security) = securityOfFirst(collections)(_.targetList.nonEmpty)
        .getOrElse(fail(NoCollectionWithEntries))

      Then(s"the entries of collection $id are returned")
      security.targetList.foreach { entry =>
        entry.privilege.trim should not be empty
        entry.who.trim should not be empty
      }
    }

    it("retrieves the sub-entity access control lists of an entity which has them") {
      Given("the workflows of this institution, reached via the collections which use them")
      // Also demonstrates that the query is entity type agnostic - workflows have no API of their
      // own in this client, yet their ACLs are still reachable.
      val workflows = workflowIds

      When("retrieving the security details of each")
      val (id, security) = securityOfFirst(workflows)(_.otherTargetLists.exists(_.entries.nonEmpty))
        .getOrElse(fail(NoWorkflowWithSubEntityLists))

      Then(s"the sub-entity lists of workflow $id are returned, keyed by workflow task")
      security.otherTargetLists.filter(_.entries.nonEmpty).foreach { otl =>
        otl.taskId.value.trim should not be empty
        otl.entries.foreach(_.privilege.trim should not be empty)
      }
    }

    it("succeeds with empty lists for an entity which has no access control entries") {
      Given("the metadata schemas of this institution, which carry no ACLs of their own")
      val schemas = MetadataSchemaApi.listSchemas().value.map(_.id)
      schemas should not be empty

      When("retrieving the security details of the first one still present")
      val (id, security) = securityOfFirst(schemas)(_ => true)
        .getOrElse(fail(NoSchemaResolved))

      Then(s"schema $id succeeds with empty lists rather than failing")
      // The distinction that matters: 'no access control entries' must never look like 'not found'.
      security shouldBe BaseEntitySecurityView(List.empty, List.empty)
    }

    it("returns a not found error for an unknown base entity") {
      Given("an invalid base entity ID")
      val invalidId = INVALID_ENTITY_ID

      When("calling getSecurityById with the invalid ID")
      val result = BaseEntityApi.getSecurityById(invalidId)

      Then("a not found error is returned - not an empty success")
      assertNotFoundError(result)
    }

    it("denies access when not authenticated") {
      assertAccessDeniedError(BaseEntityApi.getSecurityById(AnyEntityId)(_))
    }

    // Already institution filtered when this query was added - here as a regression guard. Sourced
    // from an entity which has entries, so a leak would show as data rather than an empty success.
    crossInstitutionBehavior(
      CrossInstitutionBehaviorConfig[BaseEntitySecurityView](
        entityName = "base entity's security details",
        entityId = () =>
          securityOfFirst(collectionIds)(_.targetList.nonEmpty)
            .getOrElse(fail(NoCollectionWithEntries))
            ._1,
        lookup = (id, session) => BaseEntityApi.getSecurityById(id)(session),
        assertFound = _.value.targetList should not be empty,
        assertNotFound = assertNotFoundError
      )
    )
  }

  /** The ID of a base entity whose name resolves, so that a cross-institution test can tell "not
    * found because of the behaviour under test" from "not found because there was never a name".
    *
    * Lowest ID first: entity IDs increase, so this settles on an entity imported with the
    * institution rather than one a concurrently running mutation suite is about to delete.
    */
  private def anEntityIdWithAName: Long =
    MetadataSchemaApi
      .listSchemas()
      .value
      .sortBy(_.id)
      .view
      .collectFirst { case ref if BaseEntityApi.getNameById(ref.id).value.isDefined => ref.id }
      .getOrElse(fail("No base entity in this institution has a resolvable name"))

  /** Lowest ID, so this is an entity imported with the institution rather than one a concurrently
    * running mutation suite is about to delete - the callers fetch it again after listing it.
    */
  private def getBaseEntityId: Long =
    MetadataSchemaApi
      .listSchemas()
      .value
      .minByOption(_.id)
      .getOrElse(fail("This institution has no metadata schemas"))
      .id

  private def collectionIds: List[Long] = CollectionDefinitionApi.listCollections().value.map(_.id)

  /** The IDs of the workflows in use by this institution's collections. */
  private def workflowIds: List[Long] =
    collectionIds
      .flatMap(id => CollectionDefinitionApi.getById(id).toOption.flatten)
      .flatMap(_.workflowId)
      .distinct

  /** The security details of the entity, or None if it has vanished - mutation suites run
    * concurrently, so entities listed a moment ago may already be deleted. Any other error is a
    * real failure and is reported as such rather than being mistaken for "no match".
    */
  private def securityIfPresent(id: Long): Option[BaseEntitySecurityView] =
    BaseEntityApi.getSecurityById(id) match {
      case Right(security)                                              => Some(security)
      case Left(errors) if errors.exists(_.isInstanceOf[NotFoundError]) => None
      case Left(errors) => fail(s"Retrieving the security details of entity $id failed: $errors")
    }

  /** The first of `ids` whose security details satisfy `predicate`, paired with its ID. Lazy, so it
    * stops calling the server once a match is found.
    */
  private def securityOfFirst(
      ids: List[Long]
  )(predicate: BaseEntitySecurityView => Boolean): Option[(Long, BaseEntitySecurityView)] =
    ids.view
      .flatMap(id => securityIfPresent(id).map(id -> _))
      .collectFirst { case (id, security) if predicate(security) => (id, security) }
}
