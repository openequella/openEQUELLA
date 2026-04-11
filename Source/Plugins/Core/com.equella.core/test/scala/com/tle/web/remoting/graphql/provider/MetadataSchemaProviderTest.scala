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
import org.mockito.Mockito.{mock, when}
import org.scalatest.GivenWhenThen
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class MetadataSchemaProviderTest
    extends AnyFunSpec
    with Matchers
    with GivenWhenThen
    with ImportBase64ZipValidationTests {

  private val mockSchemaService  = mock(classOf[SchemaService])
  private val mockStagingService = mock(classOf[StagingService])
  private val provider           = new MetadataSchemaProvider(mockSchemaService, mockStagingService)

  describe("MetadataSchemaProvider.importSchema") {

    validatesBase64ZipInput(
      provider.importSchema,
      "metadata schema",
      testData => {
        val mockSchema     = mock(classOf[Schema])
        val mockEntityPack = mock(classOf[EntityPack[Schema]])
        when(mockEntityPack.getEntity).thenReturn(mockSchema)
        when(mockEntityPack.getStagingID).thenReturn("test-staging-id")
        when(mockSchemaService.importEntity(testData)).thenReturn(mockEntityPack)
      }
    )
  }
}
