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

import com.tle.beans.entity.Schema
import com.tle.common.EntityPack
import com.tle.core.filesystem.staging.service.StagingService
import com.tle.core.schema.service.SchemaService
import com.tle.web.remoting.graphql.ErrorCode
import org.mockito.Mockito.{mock, when}
import org.scalatest.GivenWhenThen
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

import java.util.Base64

class MetadataSchemaProviderTest extends AnyFunSpec with Matchers with GivenWhenThen {

  describe("MetadataSchemaProvider.importSchema") {

    it("rejects empty string input") {
      Given("a MetadataSchemaProvider with mocked services")
      val mockSchemaService  = mock(classOf[SchemaService])
      val mockStagingService = mock(classOf[StagingService])
      val provider           = new MetadataSchemaProvider(mockSchemaService, mockStagingService)

      When("importSchema is called with an empty string")
      val result = provider.importSchema("")

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
      Given("a MetadataSchemaProvider with mocked services")
      val mockSchemaService  = mock(classOf[SchemaService])
      val mockStagingService = mock(classOf[StagingService])
      val provider           = new MetadataSchemaProvider(mockSchemaService, mockStagingService)

      When("importSchema is called with whitespace only")
      val result = provider.importSchema("   \t\n   ")

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
      Given("a MetadataSchemaProvider with mocked services")
      val mockSchemaService  = mock(classOf[SchemaService])
      val mockStagingService = mock(classOf[StagingService])
      val provider           = new MetadataSchemaProvider(mockSchemaService, mockStagingService)

      When("importSchema is called with invalid base64 data")
      val result = provider.importSchema("!!!invalid_base64!!!")

      Then("it returns a Left with ProviderError")
      result.isLeft should be(true)
      result.left.get.message should include("Failed to import metadata schema")
    }

    it("accepts valid base64 and decodes successfully") {
      Given("a MetadataSchemaProvider with mocked services")
      val mockSchemaService  = mock(classOf[SchemaService])
      val mockStagingService = mock(classOf[StagingService])
      val provider           = new MetadataSchemaProvider(mockSchemaService, mockStagingService)

      And("a valid base64-encoded zip content")
      val testData    = "PK\u0003\u0004".getBytes() // Minimal zip file signature
      val validBase64 = Base64.getEncoder.encodeToString(testData)

      And("mocked schema service returns an EntityPack")
      val mockSchema     = mock(classOf[Schema])
      val mockEntityPack = mock(classOf[EntityPack[Schema]])
      when(mockEntityPack.getEntity).thenReturn(mockSchema)
      when(mockEntityPack.getStagingID).thenReturn("test-staging-id")
      when(mockSchemaService.importEntity(testData)).thenReturn(mockEntityPack)

      When("importSchema is called with valid base64")
      val result = provider.importSchema(validBase64)

      Then("it returns a Right with EditableEntity")
      result.isRight should be(true)
    }
  }
}
