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

package com.tle.web.api.browsehierarchy

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.prop.TableDrivenPropertyChecks._

class HierarchyCompoundUuidTest extends AnyFunSpec with Matchers {
  private val topicUuid  = "886aa61d-f8df-4e82-8984-c487849f80ff"
  private val parentUuid = "46249813-019d-4d14-b772-2a8ca0120c99"

  // Under the standard alphabet this name encodes to "TXVzaWM/IFRoZW9yeSA+IEpheno=", which covers
  // all three characters that make base64 unsafe in a URL: the '/' (from the '?'), the '+' (from
  // the '>') and the '=' padding.
  private val urlUnsafeName        = "Music? Theory > Jazz"
  private val urlUnsafeNameUrlSafe = "TXVzaWM_IFRoZW9yeSA-IEpheno"
  private val urlUnsafeNameLegacy  = "Music%3F+Theory+%3E+Jazz"

  describe("buildString") {
    val nonVirtual   = HierarchyCompoundUuid(topicUuid, None)
    val virtual      = HierarchyCompoundUuid(topicUuid, Some("D, David"))
    val urlUnsafe    = HierarchyCompoundUuid(topicUuid, Some(urlUnsafeName))
    val withAncestor = HierarchyCompoundUuid(
      topicUuid,
      Some("D, David"),
      Some(List(HierarchyCompoundUuid(parentUuid, Some("A James"))))
    )

    it("builds the string representation of a compound UUID") {
      val compoundUuids = Table(
        ("description", "compoundUuid", "expected"),
        ("a non virtual hierarchy", nonVirtual, topicUuid),
        ("a virtual hierarchy", virtual, s"$topicUuid:RCwgRGF2aWQ"),
        (
          "a virtual hierarchy whose name base64 is not URL safe",
          urlUnsafe,
          s"$topicUuid:$urlUnsafeNameUrlSafe"
        ),
        (
          "a virtual hierarchy with ancestor",
          withAncestor,
          s"$topicUuid:RCwgRGF2aWQ,$parentUuid:QSBKYW1lcw"
        )
      )

      forAll(compoundUuids) { (_, compoundUuid, expected) =>
        compoundUuid.buildString() shouldBe expected
      }
    }

    it("builds the legacy string representation of a compound UUID") {
      val compoundUuids = Table(
        ("description", "compoundUuid", "expected"),
        ("a non virtual hierarchy", nonVirtual, topicUuid),
        ("a virtual hierarchy", virtual, s"$topicUuid:D%2C+David"),
        (
          "a virtual hierarchy whose name base64 is not URL safe",
          urlUnsafe,
          s"$topicUuid:$urlUnsafeNameLegacy"
        ),
        (
          "a virtual hierarchy with ancestor",
          withAncestor,
          s"$topicUuid:D%2C+David,$parentUuid:A+James"
        )
      )

      forAll(compoundUuids) { (_, compoundUuid, expected) =>
        compoundUuid.buildLegacyFormatString() shouldBe expected
      }
    }

    it("never emits characters which would break a URL path segment") {
      val compoundUuid = urlUnsafe.buildString()

      compoundUuid should not include "/"
      compoundUuid should not include "+"
      compoundUuid should not include "="
    }
  }

  describe("apply") {
    it("parses both formats of a hierarchy to the same result") {
      val compoundUuids = Table(
        ("description", "compoundUuid", "legacyCompoundUuid"),
        ("a non virtual hierarchy", topicUuid, topicUuid),
        ("a virtual hierarchy", s"$topicUuid:RCwgRGF2aWQ", s"$topicUuid:D%2C+David"),
        (
          "a virtual hierarchy whose name base64 is not URL safe",
          s"$topicUuid:$urlUnsafeNameUrlSafe",
          s"$topicUuid:$urlUnsafeNameLegacy"
        ),
        (
          "a virtual hierarchy with ancestor",
          s"$topicUuid:RCwgRGF2aWQ,$parentUuid:QSBKYW1lcw",
          s"$topicUuid:D%2C+David,$parentUuid:A+James"
        )
      )

      forAll(compoundUuids) { (_, compoundUuid, legacyCompoundUuid) =>
        HierarchyCompoundUuid(compoundUuid) shouldBe Right(
          HierarchyCompoundUuid.applyWithLegacyFormat(legacyCompoundUuid)
        )
      }
    }

    it("round trips a compound UUID in both formats") {
      val original = HierarchyCompoundUuid(
        topicUuid,
        Some(urlUnsafeName),
        Some(List(HierarchyCompoundUuid(parentUuid, Some("Maths? Yes"))))
      )

      HierarchyCompoundUuid(original.buildString()) shouldBe Right(original)
      HierarchyCompoundUuid.applyWithLegacyFormat(
        original.buildLegacyFormatString()
      ) shouldBe original
    }
  }
}
