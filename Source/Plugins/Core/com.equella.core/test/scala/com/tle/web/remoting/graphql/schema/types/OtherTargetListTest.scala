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

class OtherTargetListTest extends AnyFunSpec with Matchers with GivenWhenThen {

  private val UnsupportedKey = "unsupported-key"

  /** Converts a single key/targetList pair through `fromMap` and returns the sole result. */
  private def convertSingle(key: Object, targetList: TargetList): OtherTargetList = {
    val result = OtherTargetList.fromMap(otherTargetListsOf(key -> targetList))
    result should have size 1
    result.head
  }

  describe("OtherTargetList.fromMap") {

    it("returns an empty list when the map is null") {
      OtherTargetList.fromMap(null) shouldBe List.empty
    }

    it("returns an empty list when the map is empty") {
      OtherTargetList.fromMap(otherTargetListsOf()) shouldBe List.empty
    }

    it("converts ItemStatusTarget keys to OtherTargetList with ITEM_STATUS type") {
      Given("an item status target with a granted entry")
      val target = itemStatusTarget(ItemStatus.DRAFT)

      When("converting it")
      val result = convertSingle(target, targetList(ViewItemPrivilege, TestUser))

      Then("it is discriminated as an item status list")
      result.targetType shouldBe OtherTargetListType.ITEM_STATUS
      result.itemStatus shouldBe Some(ItemStatus.DRAFT.name)
      result.metadataRuleId shouldBe None
      result.taskId shouldBe None

      And("the entry is converted")
      result.entries should have size 1
      result.entries.head.granted shouldBe true
      result.entries.head.privilege shouldBe ViewItemPrivilege
    }

    it("converts ItemMetadataTarget keys to OtherTargetList with ITEM_METADATA type") {
      Given("a metadata rule target with a revoked, overriding entry")
      val target = itemMetadataTarget

      When("converting it")
      val result = convertSingle(
        target,
        targetList(EditItemPrivilege, EditorsGroup, granted = false, overridden = true)
      )

      Then("it is discriminated as a metadata rule list")
      result.targetType shouldBe OtherTargetListType.ITEM_METADATA
      result.metadataRuleId shouldBe Some(MetadataRuleId)
      result.itemStatus shouldBe None
      result.taskId shouldBe None

      And("the entry retains its granted and override flags")
      result.entries should have size 1
      result.entries.head.granted shouldBe false
      result.entries.head.overridden shouldBe true
      result.entries.head.privilege shouldBe EditItemPrivilege
    }

    it("converts WorkflowTaskTarget keys to OtherTargetList with WORKFLOW_TASK type") {
      Given("a workflow task target with a granted entry")
      val target = workflowTaskTarget

      When("converting it")
      val result = convertSingle(target, targetList(ModerateItemPrivilege, ModeratorsRole))

      Then("it is discriminated as a workflow task list")
      result.targetType shouldBe OtherTargetListType.WORKFLOW_TASK
      result.taskId shouldBe Some(WorkflowTaskId)
      result.itemStatus shouldBe None
      result.metadataRuleId shouldBe None

      And("the entry is converted")
      result.entries should have size 1
      result.entries.head.privilege shouldBe ModerateItemPrivilege
    }

    it("handles a map with multiple target types") {
      Given("a map holding one target of each type")
      val targets = otherTargetListsOf(
        itemStatusTarget(ItemStatus.LIVE) -> emptyTargetList,
        itemMetadataTarget                -> emptyTargetList,
        workflowTaskTarget                -> emptyTargetList
      )

      When("converting it")
      val result = OtherTargetList.fromMap(targets)

      Then("each is converted under its own discriminator")
      result should have size 3
      result.count(_.targetType == OtherTargetListType.ITEM_STATUS) shouldBe 1
      result.count(_.targetType == OtherTargetListType.ITEM_METADATA) shouldBe 1
      result.count(_.targetType == OtherTargetListType.WORKFLOW_TASK) shouldBe 1
    }

    it("handles entries with null TargetList values") {
      Given("a target whose target list is null")
      val target = itemStatusTarget(ItemStatus.ARCHIVED)

      When("converting it")
      val result = convertSingle(target, null)

      Then("the target is still converted, with no entries")
      result.targetType shouldBe OtherTargetListType.ITEM_STATUS
      result.itemStatus shouldBe Some(ItemStatus.ARCHIVED.name)
      result.entries shouldBe List.empty
    }

    it("throws an exception for unsupported key types") {
      // Rather than silently dropping the entry.
      val targets = otherTargetListsOf(UnsupportedKey -> emptyTargetList)

      an[IllegalArgumentException] should be thrownBy {
        OtherTargetList.fromMap(targets)
      }
    }
  }
}
