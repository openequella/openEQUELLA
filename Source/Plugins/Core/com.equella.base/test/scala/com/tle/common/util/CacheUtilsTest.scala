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

package com.tle.common.util

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.prop.TableDrivenPropertyChecks._

class CacheUtilsTest extends AnyFunSpec with Matchers {
  describe("buildCacheKey") {
    it("encodes inputs to the expected key format") {
      val formatCases =
        Table(
          ("name", "parts", "expected"),
          ("empty input", Seq.empty, ""),
          ("single value", Seq("usersByQuery"), "|12:usersByQuery|"),
          ("two values", Seq("admin", "test"), "|5:admin|4:test|"),
          ("empty string value", Seq("", "x"), "|0:|1:x|"),
          ("null value", Seq("q", null), "|1:q|-1:|"),
          (
            "mixed string and boolean",
            Seq[AnyRef]("usersInGroup", java.lang.Boolean.TRUE),
            "|12:usersInGroup|4:true|"
          )
        )

      forAll(formatCases) { (name, parts, expected) =>
        withClue(s"case: $name -> ") {
          CacheUtils.buildCacheKey(parts: _*) shouldBe expected
        }
      }
    }

    it("avoids collisions from delimiter content across parts") {
      val keyA = CacheUtils.buildCacheKey("admin|", "test")
      val keyB = CacheUtils.buildCacheKey("admin", "|test")

      keyA should not be keyB
    }
  }
}
