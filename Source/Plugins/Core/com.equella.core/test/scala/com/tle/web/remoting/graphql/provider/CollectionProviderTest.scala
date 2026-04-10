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

import com.tle.beans.entity.itemdef.ItemDefinition
import com.tle.beans.entity.LanguageBundle
import com.tle.common.EntityPack
import com.tle.core.collection.service.ItemDefinitionService
import com.tle.core.filesystem.staging.service.StagingService
import com.tle.web.remoting.graphql.ErrorCode
import org.mockito.Mockito.{mock, when}
import org.scalatest.GivenWhenThen
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

import java.util.Base64

class CollectionProviderTest extends AnyFunSpec with Matchers with GivenWhenThen {

  describe("CollectionProvider.importCollection") {

    it("rejects empty string input") {
      Given("a CollectionProvider with mocked services")
      val mockItemDefService = mock(classOf[ItemDefinitionService])
      val mockStagingService = mock(classOf[StagingService])
      val provider           = new CollectionProvider(mockItemDefService, mockStagingService)

      When("importCollection is called with an empty string")
      val result = provider.importCollection("")

      Then("it returns a Left with ProviderError")
      result should be(
        Left(
          ProviderError(
            "Import failed: empty zip data provided",
            ErrorCode.BAD_REQUEST.toString
          )
        )
      )
    }

    it("rejects whitespace-only input") {
      Given("a CollectionProvider with mocked services")
      val mockItemDefService = mock(classOf[ItemDefinitionService])
      val mockStagingService = mock(classOf[StagingService])
      val provider           = new CollectionProvider(mockItemDefService, mockStagingService)

      When("importCollection is called with whitespace only")
      val result = provider.importCollection("   \t\n   ")

      Then("it returns a Left with ProviderError")
      result should be(
        Left(
          ProviderError(
            "Import failed: empty zip data provided",
            ErrorCode.BAD_REQUEST.toString
          )
        )
      )
    }

    it("returns ProviderError for invalid base64") {
      Given("a CollectionProvider with mocked services")
      val mockItemDefService = mock(classOf[ItemDefinitionService])
      val mockStagingService = mock(classOf[StagingService])
      val provider           = new CollectionProvider(mockItemDefService, mockStagingService)

      When("importCollection is called with invalid base64 data")
      val result = provider.importCollection("!!!invalid_base64!!!")

      Then("it returns a Left with ProviderError")
      result.isLeft should be(true)
      result.left.get.message should include("Failed to import collection")
    }

    it("accepts valid base64 and decodes successfully") {
      Given("a CollectionProvider with mocked services")
      val mockItemDefService = mock(classOf[ItemDefinitionService])
      val mockStagingService = mock(classOf[StagingService])
      val provider           = new CollectionProvider(mockItemDefService, mockStagingService)

      And("a valid base64-encoded zip content")
      val testData    = "PK\u0003\u0004".getBytes() // Minimal zip file signature
      val validBase64 = Base64.getEncoder.encodeToString(testData)

      And("mocked item definition service returns an EntityPack")
      val mockItemDef = mock(classOf[ItemDefinition])
      when(mockItemDef.getId).thenReturn(1L)
      when(mockItemDef.getUuid).thenReturn("test-uuid-1234")
      when(mockItemDef.getOwner).thenReturn("test-owner")

      val mockNameBundle = mock(classOf[LanguageBundle])
      when(mockNameBundle.getId).thenReturn(100L)
      when(mockItemDef.getName).thenReturn(mockNameBundle)

      val mockEntityPack = mock(classOf[EntityPack[ItemDefinition]])
      when(mockEntityPack.getEntity).thenReturn(mockItemDef)
      when(mockEntityPack.getStagingID).thenReturn("test-staging-id")
      when(mockItemDefService.importEntity(testData)).thenReturn(mockEntityPack)

      When("importCollection is called with valid base64")
      val result = provider.importCollection(validBase64)

      Then("it returns a Right with BaseEntityReference")
      result.isRight should be(true)
      val ref = result.right.get
      ref.id should be(1L)
      ref.uuid should be("test-uuid-1234")
      ref.owner should be("test-owner")
      ref.forCollection should be(true)
    }
  }
}
