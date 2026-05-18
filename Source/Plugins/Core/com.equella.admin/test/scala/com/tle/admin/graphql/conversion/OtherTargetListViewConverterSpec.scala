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

package com.tle.admin.graphql.conversion

import com.tle.beans.entity.itemdef.ItemDefinition
import com.tle.beans.item.ItemStatus
import com.tle.common.security.{ItemMetadataTarget, ItemStatusTarget, WorkflowTaskTarget}
import io.github.openequella.graphql.api.views.{OtherTargetListView, TargetListEntryView}
import io.github.openequella.graphql.client.OtherTargetListType
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class OtherTargetListViewConverterSpec extends AnyFunSpec with Matchers {

  private val itemDef = new ItemDefinition()
  itemDef.setId(42L)

  private val sampleEntry = TargetListEntryView(
    granted = true,
    overridden = false,
    privilege = "VIEW_ITEM",
    who = "U:testuser",
    postfix = ""
  )

  describe("OtherTargetListViewConverter.toTargetListMap") {

    it("passes the ItemDefinition to ItemStatusTarget keys") {
      val view = OtherTargetListView(
        targetType = OtherTargetListType.ITEM_STATUS,
        itemStatus = Some("DRAFT"),
        metadataRuleId = None,
        taskId = None,
        entries = List(sampleEntry)
      )

      val result = OtherTargetListViewConverter.toTargetListMap(List(view), itemDef)

      val key = result.keySet().iterator().next()
      key shouldBe a[ItemStatusTarget]
      val target = key.asInstanceOf[ItemStatusTarget]
      target.getItemStatus shouldBe ItemStatus.DRAFT
      target.getItemDefinition shouldBe itemDef
    }

    it("passes the ItemDefinition to ItemMetadataTarget keys") {
      val view = OtherTargetListView(
        targetType = OtherTargetListType.ITEM_METADATA,
        itemStatus = None,
        metadataRuleId = Some("rule-123"),
        taskId = None,
        entries = List(sampleEntry)
      )

      val result = OtherTargetListViewConverter.toTargetListMap(List(view), itemDef)

      val key = result.keySet().iterator().next()
      key shouldBe a[ItemMetadataTarget]
      val target = key.asInstanceOf[ItemMetadataTarget]
      target.getId shouldBe "rule-123"
      target.getItemDefinition shouldBe itemDef
    }

    it("handles WorkflowTaskTarget keys without requiring ItemDefinition") {
      val view = OtherTargetListView(
        targetType = OtherTargetListType.WORKFLOW_TASK,
        itemStatus = None,
        metadataRuleId = None,
        taskId = Some("task-456"),
        entries = List(sampleEntry)
      )

      val result = OtherTargetListViewConverter.toTargetListMap(List(view), itemDef)

      val key = result.keySet().iterator().next()
      key shouldBe a[WorkflowTaskTarget]
      key.asInstanceOf[WorkflowTaskTarget].getTaskId shouldBe "task-456"
    }

    it("returns null for an empty list") {
      val result = OtherTargetListViewConverter.toTargetListMap(List.empty, itemDef)
      result shouldBe null
    }

    it("produces keys that match equals() with targets created using the same ItemDefinition") {
      val view = OtherTargetListView(
        targetType = OtherTargetListType.ITEM_STATUS,
        itemStatus = Some("LIVE"),
        metadataRuleId = None,
        taskId = None,
        entries = List(sampleEntry)
      )

      val result    = OtherTargetListViewConverter.toTargetListMap(List(view), itemDef)
      val lookupKey = new ItemStatusTarget(ItemStatus.LIVE, itemDef)

      result.get(lookupKey) should not be null
    }
  }
}
