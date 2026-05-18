package com.tle.web.remoting.graphql.schema.types

import com.tle.beans.entity.itemdef.ItemDefinition
import com.tle.beans.item.ItemStatus
import com.tle.common.security.{
  ItemMetadataTarget,
  ItemStatusTarget,
  TargetList,
  WorkflowTaskTarget
}
import org.scalatest.GivenWhenThen
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class OtherTargetListTest extends AnyFunSpec with Matchers with GivenWhenThen {

  /** Shared ItemDefinition for tests that require one (ItemStatusTarget, ItemMetadataTarget). */
  private val itemDef = new ItemDefinition()

  /** Creates a TargetList containing a single entry with the given properties. */
  private def buildTargetList(
      granted: Boolean,
      overridden: Boolean,
      privilege: String,
      who: String
  ): TargetList = {
    val entry = new com.tle.common.security.TargetListEntry()
    entry.setGranted(granted)
    entry.setOverride(overridden)
    entry.setPrivilege(privilege)
    entry.setWho(who)

    val tl = new TargetList()
    tl.setEntries(java.util.List.of(entry))
    tl
  }

  /** Converts a single key/targetList pair through `fromMap` and returns the sole result. */
  private def convertSingle(key: Object, targetList: TargetList): OtherTargetList = {
    val map = new java.util.HashMap[Object, TargetList]()
    map.put(key, targetList)
    val result = OtherTargetList.fromMap(map)
    result should have size 1
    result.head
  }

  describe("OtherTargetList.fromMap") {

    it("returns an empty list when the map is null") {
      OtherTargetList.fromMap(null) shouldBe List.empty
    }

    it("returns an empty list when the map is empty") {
      OtherTargetList.fromMap(new java.util.HashMap()) shouldBe List.empty
    }

    it("converts ItemStatusTarget keys to OtherTargetList with ITEM_STATUS type") {
      val target = new ItemStatusTarget(ItemStatus.DRAFT, itemDef)
      val result = convertSingle(
        target,
        buildTargetList(
          granted = true,
          overridden = false,
          privilege = "VIEW_ITEM",
          who = "U:testuser"
        )
      )

      result.targetType shouldBe OtherTargetListType.ITEM_STATUS
      result.itemStatus shouldBe Some("DRAFT")
      result.metadataRuleId shouldBe None
      result.taskId shouldBe None
      result.entries should have size 1
      result.entries.head.granted shouldBe true
      result.entries.head.privilege shouldBe "VIEW_ITEM"
    }

    it("converts ItemMetadataTarget keys to OtherTargetList with ITEM_METADATA type") {
      val target = new ItemMetadataTarget("rule-uuid-123", itemDef)
      val result = convertSingle(
        target,
        buildTargetList(
          granted = false,
          overridden = true,
          privilege = "EDIT_ITEM",
          who = "G:editors"
        )
      )

      result.targetType shouldBe OtherTargetListType.ITEM_METADATA
      result.metadataRuleId shouldBe Some("rule-uuid-123")
      result.itemStatus shouldBe None
      result.taskId shouldBe None
      result.entries should have size 1
      result.entries.head.granted shouldBe false
      result.entries.head.privilege shouldBe "EDIT_ITEM"
    }

    it("converts WorkflowTaskTarget keys to OtherTargetList with WORKFLOW_TASK type") {
      val target = new WorkflowTaskTarget(42L, "task-uuid-456")
      val result = convertSingle(
        target,
        buildTargetList(
          granted = true,
          overridden = false,
          privilege = "MODERATE_ITEM",
          who = "U:moderator"
        )
      )

      result.targetType shouldBe OtherTargetListType.WORKFLOW_TASK
      result.taskId shouldBe Some("task-uuid-456")
      result.itemStatus shouldBe None
      result.metadataRuleId shouldBe None
      result.entries should have size 1
      result.entries.head.privilege shouldBe "MODERATE_ITEM"
    }

    it("handles a map with multiple target types") {
      val statusTarget   = new ItemStatusTarget(ItemStatus.LIVE, itemDef)
      val metadataTarget = new ItemMetadataTarget("rule-1", itemDef)

      val emptyTargetList = new TargetList()
      emptyTargetList.setEntries(java.util.Collections.emptyList())

      val map = new java.util.HashMap[Object, TargetList]()
      map.put(statusTarget, emptyTargetList)
      map.put(metadataTarget, emptyTargetList)

      val result = OtherTargetList.fromMap(map)

      result should have size 2
      result.count(_.targetType == OtherTargetListType.ITEM_STATUS) shouldBe 1
      result.count(_.targetType == OtherTargetListType.ITEM_METADATA) shouldBe 1
    }

    it("handles entries with null TargetList values") {
      val target = new ItemStatusTarget(ItemStatus.ARCHIVED, itemDef)
      val result = convertSingle(target, null)

      result.targetType shouldBe OtherTargetListType.ITEM_STATUS
      result.itemStatus shouldBe Some("ARCHIVED")
      result.entries shouldBe List.empty
    }

    it("throws an exception for unsupported key types") {
      val map = new java.util.HashMap[Object, TargetList]()
      map.put("unsupported-key", new TargetList())

      an[IllegalArgumentException] should be thrownBy {
        OtherTargetList.fromMap(map)
      }
    }
  }
}
