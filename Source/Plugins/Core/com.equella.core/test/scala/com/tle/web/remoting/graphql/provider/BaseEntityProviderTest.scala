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

package com.tle.web.remoting.graphql.provider

import com.tle.beans.entity.BaseEntity
import com.tle.common.EntityPack
import com.tle.common.beans.exception.NotFoundException
import com.tle.core.entity.service.BaseEntityService
import com.tle.web.remoting.graphql.ErrorCode
import com.tle.web.remoting.graphql.SecurityTestFixtures._
import org.mockito.Mockito.{mock, when}
import org.scalatest.EitherValues
import org.scalatest.GivenWhenThen
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

import java.util.Optional

class BaseEntityProviderTest extends AnyFunSpec with Matchers with GivenWhenThen with EitherValues {

  private val EntityId = 123L

  /** A provider over a service which answers with `pack` - which may be null, or may throw. The
    * answer is by-name so that a throwing `pack` is thrown from the call rather than from the stub
    * setup.
    */
  private def providerReturning(pack: => EntityPack[BaseEntity]): BaseEntityProvider = {
    val mockService = mock(classOf[BaseEntityService])
    when(mockService.getReadOnlyPack(EntityId)).thenAnswer(_ => Optional.ofNullable(pack))
    new BaseEntityProvider(mockService)
  }

  describe("BaseEntityProvider.securityById") {

    it("returns the security details of an entity which has ACLs") {
      Given("a service which returns a pack with a target list")
      val provider = providerReturning(entityPack(targetList(EditSchemaPrivilege, TestUser)))

      When("retrieving the security details")
      val result = provider.securityById(EntityId)

      Then("the entries are returned")
      result.value.targetList should have size 1
      result.value.targetList.head.privilege shouldBe EditSchemaPrivilege
      result.value.targetList.head.who shouldBe TestUser
    }

    it("succeeds with empty lists for an entity which has no ACLs") {
      Given("a service which returns a pack with no target lists")
      val provider = providerReturning(entityPack())

      When("retrieving the security details")
      val result = provider.securityById(EntityId)

      Then("it is a success with empty lists, not a not-found error")
      result.value.targetList shouldBe List.empty
      result.value.otherTargetLists shouldBe List.empty
    }

    it("returns a NOT_FOUND error when the entity does not exist") {
      Given("a service which returns null")
      val provider = providerReturning(null)

      When("retrieving the security details")
      val result = provider.securityById(EntityId)

      Then("a NOT_FOUND error naming the entity is returned")
      result.left.value.cause shouldBe ErrorCode.NOT_FOUND.toString
      result.left.value.message should include(EntityId.toString)
    }

    it("returns a NOT_FOUND error when the entity vanishes mid-lookup") {
      Given("a service which throws NotFoundException")
      val provider = providerReturning(throw new NotFoundException("gone"))

      When("retrieving the security details")
      val result = provider.securityById(EntityId)

      Then("a NOT_FOUND error is returned")
      result.left.value.cause shouldBe ErrorCode.NOT_FOUND.toString
    }
  }
}
