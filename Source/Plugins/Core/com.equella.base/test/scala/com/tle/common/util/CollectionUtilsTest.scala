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

import com.tle.common.util.CollectionUtils.{convertEmptyListToNone, intersect, intersectJava}
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.prop.TableDrivenPropertyChecks._

class CollectionUtilsTest extends AnyFunSpec with Matchers {
  describe("intersect") {
    val cases =
      Table(
        ("name", "originalCollection", "newCollection", "expected"),
        ("null ∩ [a,b] → [a,b]", None, Some(Iterable("a", "b")), Some(Iterable("a", "b"))),
        ("[a,b] ∩ null → [a,b]", Some(Iterable("a", "b")), None, Some(Iterable("a", "b"))),
        ("null ∩ null → null", None, None, None),
        (
          "[a,b,c,c] ∩ [b,b,c,d] → [b,c]",
          Some(Iterable("a", "b", "c", "c")),
          Some(Iterable("b", "b", "c", "d")),
          Some(Iterable("b", "c"))
        ),
        (
          "[a,b] ∩ [c,d] → []",
          Some(Iterable("a", "b")),
          Some(Iterable("c", "d")),
          Some(Iterable())
        )
      )

    it("behaves as expected for all cases") {
      forAll(cases) { (name, originalCollection, newCollection, expected) =>
        withClue(s"case: $name -> ") {
          val result = intersect(originalCollection, newCollection)
          result shouldBe expected
        }
      }
    }
  }

  describe("intersectJava") {
    val cases =
      Table(
        ("name", "originalCollection", "newCollection", "expected"),
        ("null ∩ [a,b] → [a,b]", null, java.util.List.of("a", "b"), java.util.List.of("a", "b")),
        ("[a,b] ∩ null → [a,b]", java.util.List.of("a", "b"), null, java.util.List.of("a", "b")),
        ("null ∩ null → null", null, null, null),
        (
          "[a,b,c,c] ∩ [b,b,c,d] → [b,c]",
          java.util.List.of("a", "b", "c", "c"),
          java.util.List.of("b", "b", "c", "d"),
          java.util.List.of("b", "c")
        ),
        (
          "[a,b] ∩ [c,d] → []",
          java.util.List.of("a", "b"),
          java.util.List.of("c", "d"),
          java.util.List.of()
        )
      )

    it("behaves as expected for all cases") {
      forAll(cases) { (name, originalCollection, newCollection, expected) =>
        withClue(s"case: $name -> ") {
          val result = intersectJava(originalCollection, newCollection)
          result shouldBe expected
        }
      }
    }
  }

  describe("convertEmptyListToNone (Java Collection)") {
    val javaCases =
      Table(
        ("name", "input", "expected"),
        ("null → None", null, None),
        ("[] → None", java.util.List.of(), None),
        ("[1] → Some(List(1))", java.util.List.of(1), Some(List(1))),
        ("[1,2,3] → Some(List(1,2,3))", java.util.List.of(1, 2, 3), Some(List(1, 2, 3)))
      )

    it("behaves as expected for Java collection") {
      forAll(javaCases) { (name, input, expected) =>
        withClue(s"case: $name → ") {
          val result = convertEmptyListToNone(input)
          result shouldBe expected
        }
      }
    }
  }

  describe("convertEmptyListToNone (Scala Iterable)") {
    val scalaCases =
      Table(
        ("name", "input", "expected"),
        ("null → None", null, None),
        ("[] → None", Iterable.empty[Int], None),
        ("[1] → Some(Iterable(1))", Iterable(1), Some(Iterable(1))),
        ("[1,2,3] → Some(Iterable(1,2,3))", Iterable(1, 2, 3), Some(Iterable(1, 2, 3)))
      )

    it("behaves as expected for Scala Iterable") {
      forAll(scalaCases) { (name, input, expected) =>
        withClue(s"case: $name → ") {
          val result = convertEmptyListToNone(input)
          result shouldBe expected
        }
      }
    }
  }
}
