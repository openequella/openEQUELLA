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

package com.tle.core.entity.service

import com.tle.beans.entity.{BaseEntity, Schema}
import com.tle.common.EntityPack
import com.tle.core.entity.dao.BaseEntityDao
import com.tle.core.entity.registry.EntityRegistry
import com.tle.core.entity.service.impl.BaseEntityServiceImpl
import org.mockito.Mockito.{doReturn, mock, when}
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.{GivenWhenThen, OptionValues}

import java.util.Optional
import scala.jdk.OptionConverters._
import scala.util.chaining.scalaUtilChainingOps

/** Covers the dispatch logic of the entity type agnostic `getReadOnlyPack` - resolving the concrete
  * entity service, and what happens when there isn't one. Institution scoping is enforced by the
  * DAO query rather than here, so it is covered end to end against a live server instead.
  */
class BaseEntityServiceImplTest
    extends AnyFunSpec
    with Matchers
    with GivenWhenThen
    with OptionValues {

  private val EntityId = 123L

  private def schema: Schema = new Schema().tap(_.setId(EntityId))

  private def anEntityService = mock(classOf[AbstractEntityService[_, BaseEntity]])

  /** Builds the service under test over mocks returning the given entity and entity service. Uses
    * `doReturn` for the registry because the wildcard in its `AbstractEntityService[_, BaseEntity]`
    * return type makes `when(...).thenReturn(...)` fail to typecheck in Scala.
    */
  private def serviceFor(entity: BaseEntity, entityService: AnyRef): BaseEntityServiceImpl = {
    val mockDao = mock(classOf[BaseEntityDao])
    when(mockDao.getEntityInCurrentInstitution(EntityId)).thenReturn(Optional.ofNullable(entity))

    val mockRegistry = mock(classOf[EntityRegistry])
    doReturn(entityService, Nil: _*).when(mockRegistry).getServiceForClass(classOf[Schema])

    new BaseEntityServiceImpl(mockDao, mockRegistry)
  }

  describe("BaseEntityServiceImpl.getReadOnlyPack") {

    it("delegates to the entity service registered for the entity's concrete class") {
      Given("an entity with a registered service")
      val pack              = new EntityPack[BaseEntity]()
      val mockEntityService = anEntityService.tap { s =>
        when(s.getReadOnlyPack(EntityId)).thenReturn(pack)
      }

      When("retrieving the read only pack")
      val result = serviceFor(schema, mockEntityService).getReadOnlyPack(EntityId)

      Then("the pack from the delegate is returned")
      result.toScala.value should be theSameInstanceAs pack
    }

    it("returns empty when the current institution has no entity with that ID") {
      Given("a DAO which finds nothing")
      val service = serviceFor(null, anEntityService)

      When("retrieving the read only pack")
      val result = service.getReadOnlyPack(EntityId)

      Then("nothing is returned")
      result.toScala shouldBe None
    }

    it("fails rather than reporting 'no ACLs' when the entity type has no registered service") {
      Given("an entity whose class the registry does not know")
      val service = serviceFor(schema, null)

      Then("retrieving the read only pack fails, because the ACLs cannot be determined")
      // Which is not the same as the entity having no ACLs.
      an[UnsupportedOperationException] should be thrownBy service.getReadOnlyPack(EntityId)
    }
  }
}
