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

import com.tle.beans.Institution
import com.tle.common.usermanagement.user.{CurrentUser, UserState}
import com.tle.core.institution.RunAsInstitution
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.GivenWhenThen

import java.util.concurrent.Callable
import java.util.function.ToIntFunction
import scala.jdk.CollectionConverters._
import org.mockito.Mockito.{mock, verifyNoInteractions}
import java.util

class ChainCounterTest extends AnyFunSpec with Matchers with GivenWhenThen with ChainFixtures {

  private def mockRunAs: RunAsInstitution = new RunAsInstitution {
    override def execute[V](userState: UserState, callable: Callable[V]): V = callable.call()

    override def executeAsSystem(institution: Institution, runnable: Runnable): Unit =
      runnable.run()

    override def executeAsSystem[V](institution: Institution, callable: Callable[V]): V =
      callable.call()
  }

  private def mockCountFunction(expectedCount: Int): ToIntFunction[UserDirectory] =
    (_: UserDirectory) => expectedCount

  describe("ChainCounter") {
    it("returns 0 for an empty chain and never invokes the count function") {
      Given("an empty plugin chain")
      val countFn = mock(classOf[ToIntFunction[UserDirectory]])

      When("counting items")
      val count = new ChainCounter(emptyChain, mockRunAs).count(countFn)

      Then("returns 0 and the count function is never called")
      count shouldBe 0
      verifyNoInteractions(countFn)
    }

    it("returns the count for a single-plugin chain") {
      val expectedCount = 7
      val count         =
        new ChainCounter(singlePluginChain, mockRunAs).count(mockCountFunction(expectedCount))
      count shouldBe expectedCount
    }

    it("sums counts across multiple plugins") {
      Given("a multi-plugin chain")
      val multiChain: util.List[UserDirectory] = List.fill(3)(mock(classOf[UserDirectory])).asJava
      val multiChainCounts: Seq[Int]           = List(3, 5, 2)
      // A count function that returns a distinct count per plugin based on its position in the chain.
      val countFunction: ToIntFunction[UserDirectory] =
        chainPlugin => multiChainCounts(multiChain.indexOf(chainPlugin))

      When("counting items")
      val count = new ChainCounter(multiChain, mockRunAs).count(countFunction)

      Then("returns the sum of all counts")
      count shouldBe multiChainCounts.sum
    }
  }
}
