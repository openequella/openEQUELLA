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

package com.tle.plugins.ump

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatest.GivenWhenThen

import java.util.function.ToIntFunction
import scala.jdk.CollectionConverters._
import org.mockito.Mockito.{mock, when, verifyNoInteractions}
import org.mockito.ArgumentMatchers.{any, anyInt}
import java.util

class ChainPagerTest
    extends AnyFunSpec
    with Matchers
    with TableDrivenPropertyChecks
    with GivenWhenThen
    with ChainFixtures {

  // Mocked user directories.
  private val ud1: UserDirectory = mock(classOf[UserDirectory])
  private val ud2: UserDirectory = mock(classOf[UserDirectory])
  private val ud3: UserDirectory = mock(classOf[UserDirectory])

  // A helper to create a simple PageFunction that always returns the same slice and ChainResult.CONTINUE.
  private def mockPageFunction(items: List[String]): PageFunction[String] =
    (_: UserDirectory, lim: Int, off: Int) =>
      ChainResult.continueWith(items.slice(off, off + lim).asJava)

  // A helper that returns a count function always yielding the same fixed value.
  private def constantCountFn(n: Int): ToIntFunction[UserDirectory] = _ => n

  // A helper to invoke ChainPager with the given parameters.
  private def getPage[T](
      chain: util.List[UserDirectory],
      pagingFunctions: PagingFunctions[T],
      pageRange: PageRange
  ): util.List[T] =
    new ChainPager[T](chain, pagingFunctions).getPage(pageRange)

  describe("ChainPager.getPage") {
    it("returns an empty list and never invokes the paging function when chain is empty") {
      Given("an empty plugin chain")
      val pageFn = mock(classOf[PageFunction[String]])

      When("getting a page")
      val result = getPage[String](
        emptyChain,
        PagingFunctions(constantCountFn(1), pageFn),
        PageRange(limit = 10, offset = 0)
      )

      Then("returns an empty list and the page function is never called")
      result.asScala shouldBe empty
      verifyNoInteractions(pageFn)
    }

    it(
      "returns the correct page slice for each combination of limit and offset with a single plugin"
    ) {
      val items: List[String]                = List("a", "b", "c", "d", "e")
      val total: Int                         = items.size
      val pageFunction: PageFunction[String] = mockPageFunction(items)
      val defaultLimit                       = 2

      val cases = Table(
        ("description", "limit", "offset", "expected"),
        ("limit is zero", 0, 0, List.empty),
        ("limit covers all items", total, 0, items),
        ("limit is less than total items", defaultLimit, 0, List("a", "b")),
        ("offset and limit are both within the total items", defaultLimit, 1, List("b", "c")),
        ("offset is set and limit exceeds the remaining items", total + 1, 3, List("d", "e")),
        ("offset exceeds total items", defaultLimit, total + 1, List.empty)
      )

      forAll(cases) { (description, limit, offset, expected) =>
        withClue(description) {
          val result = getPage[String](
            singlePluginChain,
            PagingFunctions(constantCountFn(total), pageFunction),
            PageRange(limit = limit, offset = offset)
          )
          result.asScala.toList shouldBe expected
        }
      }
    }

    it(
      "assembles the correct page across plugin boundaries for each combination of limit and offset"
    ) {
      val pluginItems: Map[UserDirectory, List[String]] = Map(
        ud1 -> List("a1", "a2"),
        ud2 -> List("b1", "b2"),
        ud3 -> List("c1", "c2")
      )

      val chain                                       = List(ud1, ud2, ud3).asJava
      val total: Int                                  = pluginItems.map(_._2.size).sum
      val countFunction: ToIntFunction[UserDirectory] = ud => pluginItems(ud).size
      val pageFunction: PageFunction[String]          =
        (ud, lim, off) => ChainResult.continueWith(pluginItems(ud).slice(off, off + lim).asJava)
      val defaultLimit = 3

      val cases = Table(
        ("description", "limit", "offset", "expected"),
        ("limit is zero", 0, 0, List.empty),
        (
          "limit covers all items across plugins",
          total,
          0,
          List("a1", "a2", "b1", "b2", "c1", "c2")
        ),
        ("limit stops mid-way through plugins", total / 2, 0, List("a1", "a2", "b1")),
        (
          "offset skips the first plugin entirely",
          defaultLimit,
          pluginItems(ud1).size,
          List("b1", "b2", "c1")
        ),
        (
          "offset falls within first plugin and spans to next",
          defaultLimit,
          1,
          List("a2", "b1", "b2")
        ),
        ("offset exceeds total items", defaultLimit, total + 1, List.empty)
      )

      forAll(cases) { (description, limit, offset, expected) =>
        withClue(description) {
          val result = getPage[String](
            chain,
            PagingFunctions(countFunction, pageFunction),
            PageRange(limit = limit, offset = offset)
          )
          result.asScala.toList shouldBe expected
        }
      }
    }

    it("stops querying plugins after one plugin signals STOP") {
      Given("a chain of two plugins where the first signals STOP")
      val chain    = List(ud1, ud2).asJava
      val ud1Items = List("1")
      val ud2Items = List("2")

      val ud1PageFunction = mock(classOf[PageFunction[String]])
      val ud2PageFunction = mock(classOf[PageFunction[String]])
      when(ud1PageFunction.fetch(any(classOf[UserDirectory]), anyInt(), anyInt()))
        .thenReturn(ChainResult.stopWith(ud1Items.asJava))
      when(ud2PageFunction.fetch(any(classOf[UserDirectory]), anyInt(), anyInt()))
        .thenReturn(ChainResult.stopWith(ud2Items.asJava))

      val pageFunction: PageFunction[String] = (ud, l, o) =>
        ud match {
          case `ud1` => ud1PageFunction.fetch(ud, l, o)
          case `ud2` => ud2PageFunction.fetch(ud, l, o)
          case _     => fail("Unexpected user directory")
        }
      val countFunction: ToIntFunction[UserDirectory] = {
        case `ud1` => ud1Items.size
        case `ud2` => ud2Items.size
        case _     => fail("Unexpected user directory")
      }

      When("getting a page")
      val result = getPage[String](
        chain,
        PagingFunctions(countFunction, pageFunction),
        PageRange(limit = ud1Items.size + ud2Items.size, offset = 0)
      )

      Then("returns only items from the first plugin and never queries the second")
      result.asScala.toList shouldBe List("1")
      verifyNoInteractions(ud2PageFunction)
    }
  }
}
