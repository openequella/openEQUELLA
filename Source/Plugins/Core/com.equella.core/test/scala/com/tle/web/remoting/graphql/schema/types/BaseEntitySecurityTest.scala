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

package com.tle.web.remoting.graphql.schema.types

import com.tle.beans.item.ItemStatus
import com.tle.common.security.TargetList
import com.tle.web.remoting.graphql.SecurityTestFixtures._
import org.scalatest.GivenWhenThen
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class BaseEntitySecurityTest extends AnyFunSpec with Matchers with GivenWhenThen {

  /** The sub-entity target lists a collection produces - one per item status, one per metadata
    * rule.
    */
  private def collectionOtherTargetLists: java.util.Map[Object, TargetList] =
    otherTargetListsOf(
      itemStatusTarget(ItemStatus.DRAFT) -> targetList(ViewItemPrivilege, TestUser),
      itemMetadataTarget                 -> targetList(EditItemPrivilege, EditorsGroup)
    )

  /** The sub-entity target lists a workflow produces - one per task. */
  private def workflowOtherTargetLists: java.util.Map[Object, TargetList] =
    otherTargetListsOf(workflowTaskTarget -> targetList(ModerateItemPrivilege, ModeratorsRole))

  /** Indexes the converted sub-entity lists by their discriminator. */
  private def byTargetType(
      security: BaseEntitySecurity
  ): Map[OtherTargetListType, OtherTargetList] =
    security.otherTargetLists.map(otl => otl.targetType -> otl).toMap

  describe("BaseEntitySecurity.apply(pack)") {

    it("yields empty lists when the pack has no target lists at all") {
      Given("a pack whose target lists are both null")
      val pack = entityPack()

      When("converting it")
      val result = BaseEntitySecurity(pack)

      Then("both lists are empty")
      result.targetList shouldBe List.empty
      result.otherTargetLists shouldBe List.empty
    }

    it("yields empty lists - not an absent value - for an entity with no ACLs") {
      Given("a pack with target lists which are present but empty")
      val pack = entityPack(emptyTargetList, otherTargetListsOf())

      When("converting it")
      val result = BaseEntitySecurity(pack)

      Then("both lists are empty")
      // This is the case the query's error semantics exist to keep distinct from 'not found'.
      result.targetList shouldBe List.empty
      result.otherTargetLists shouldBe List.empty
    }

    it("converts the entity's own target list") {
      Given("a pack with an entry on the entity itself")
      val pack = entityPack(targetList = targetList(EditSchemaPrivilege, TestUser))

      When("converting it")
      val result = BaseEntitySecurity(pack)

      Then("the entry is converted")
      result.targetList should have size 1
      result.targetList.head.privilege shouldBe EditSchemaPrivilege
      result.targetList.head.who shouldBe TestUser
      result.targetList.head.granted shouldBe true

      And("there are no sub-entity lists")
      result.otherTargetLists shouldBe List.empty
    }

    it("converts the collection shaped sub-entity target lists") {
      Given("a pack with item status and metadata rule target lists")
      val pack = entityPack(otherTargetLists = collectionOtherTargetLists)

      When("converting it")
      val result = BaseEntitySecurity(pack)

      Then("each is converted under its own discriminator")
      result.otherTargetLists should have size 2
      val byType = byTargetType(result)
      byType(OtherTargetListType.ITEM_STATUS).itemStatus shouldBe Some(ItemStatus.DRAFT.name)
      byType(OtherTargetListType.ITEM_METADATA).metadataRuleId shouldBe Some(MetadataRuleId)

      And("the entity's own target list is empty")
      result.targetList shouldBe List.empty
    }

    it("converts the workflow shaped sub-entity target lists") {
      Given("a pack with a workflow task target list")
      val pack = entityPack(otherTargetLists = workflowOtherTargetLists)

      When("converting it")
      val result = BaseEntitySecurity(pack)

      Then("it is converted under the workflow task discriminator")
      result.otherTargetLists should have size 1
      val workflowTasks = byTargetType(result)(OtherTargetListType.WORKFLOW_TASK)
      workflowTasks.taskId shouldBe Some(WorkflowTaskId)
      workflowTasks.entries.head.privilege shouldBe ModerateItemPrivilege
    }
  }
}
