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

package com.tle.admin.service

import com.tle.admin.rest.RestConfiguration
import com.tle.beans.entity.{BaseEntity, BaseEntityLabel}
import com.tle.common.EntityPack
import com.tle.common.beans.exception.NotFoundException
import com.tle.common.security.WorkflowTaskTarget
import com.tle.core.remoting.RemoteAbstractEntityService
import io.github.openequella.graphql.ClientConfiguration
import io.github.openequella.graphql.api.views.{
  BaseEntityReferenceView,
  BaseEntitySecurityView,
  OtherTargetListView,
  TargetListEntryView
}
import io.github.openequella.graphql.api.{ApiError, GraphQlError}
import io.github.openequella.graphql.client.OtherTargetListType
import org.mockito.Mockito.{mock, verify, verifyNoInteractions, when}
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

import java.util
import scala.jdk.CollectionConverters._
import scala.util.chaining.scalaUtilChainingOps

// Mockable wrapper base classes for Mockito — defined at package level (not as inner classes) so
// that Mockito 5's InlineMockMaker can instrument them without the byte-buddy agent.
// Direct Function1 mocking is unreliable due to type erasure of generic parameters.
private[service] abstract class MockableExportFn {
  def apply(id: Long): Either[List[ApiError], Option[Array[Byte]]]
}
private[service] abstract class MockableUnitFn {
  def apply(id: Long): Either[List[ApiError], Unit]
}
private[service] abstract class MockableSecurityFn {
  def apply(id: Long): Either[List[ApiError], BaseEntitySecurityView]
}

/** Tests for the helpers in AdminEntityService which wrap GraphQL API calls:
  *   - listAllFrom
  *   - idByUuid
  *   - exportWith
  *   - cancelEditWith
  *   - deleteWith
  *   - cloneWith
  *   - readOnlyPackWith
  *
  * The private helpers (handleEither, handleEitherUnit) are tested implicitly through their use by
  * the public helpers.
  */
class AdminEntityServiceSpec extends AnyFunSpec with Matchers {

  // Function type aliases to avoid repeating verbose signatures
  private type ExportFn   = Long => Either[List[ApiError], Option[Array[Byte]]]
  private type UnitFn     = Long => Either[List[ApiError], Unit]
  private type CloneFn    = Long => Either[List[ApiError], BaseEntityReferenceView]
  private type SecurityFn = Long => Either[List[ApiError], BaseEntitySecurityView]

  // Shared numeric constants used as test entity IDs
  private val TestEntityId   = 100L
  private val TestCloneSrcId = 50L
  private val TestClonedId   = 100L

  private val service = new TestService()

  describe("listAllFrom") {
    it("converts a Right(List) result to a Java list of BaseEntityLabel") {
      val uuid1 = "550e8400-e29b-41d4-a716-446655440000"
      val uuid2 = "6ba7b810-9dad-11d1-80b4-00c04fd430c8"
      val views = List(
        BaseEntityReferenceView(1L, uuid1, 100L, "owner-1", forCollection = false),
        BaseEntityReferenceView(2L, uuid2, 200L, "owner-2", forCollection = false)
      )
      val result = Right(views)

      val labels = service.testListAllFrom(result)

      labels.size() should equal(views.length)
      labels.asScala.map(_.getUuid) should contain theSameElementsInOrderAs views.map(_.uuid)
    }

    it("throws ClientRequestException when result is Left(errors)") {
      val result: Either[List[ApiError], List[BaseEntityReferenceView]] =
        Left(List(GraphQlError("Test error")))

      intercept[ClientRequestException] {
        service.testListAllFrom(result)
      }
    }

    it("returns empty Java list for empty Right list") {
      val result: Either[List[ApiError], List[BaseEntityReferenceView]] = Right(List())

      val labels = service.testListAllFrom(result)

      labels should be(empty)
    }
  }

  describe("idByUuid") {
    val TestUuid = "f47ac10b-58cc-4372-a567-0e02b2c3d479"

    it("returns the Long value from Some result") {
      val expectedId                                             = 42L
      val getter: String => Either[List[ApiError], Option[Long]] =
        _ => Right(Some(expectedId))

      val result = service.testIdByUuid(getter, TestUuid)

      result should equal(expectedId)
    }

    it("returns 0L when result is None") {
      val getter: String => Either[List[ApiError], Option[Long]] =
        _ => Right(None)

      val result = service.testIdByUuid(getter, TestUuid)

      result should equal(0L)
    }

    it("throws ClientRequestException when result is Left(errors)") {
      val getter: String => Either[List[ApiError], Option[Long]] =
        _ => Left(List(GraphQlError("some kind of error")))

      intercept[ClientRequestException] {
        service.testIdByUuid(getter, TestUuid)
      }
    }
  }

  describe("exportWith") {
    it("calls withoutSecurity when useSecurity is false") {
      val withoutSecurity = mock(classOf[MockableExportFn])
      val withSecurity    = mock(classOf[MockableExportFn])
      when(withoutSecurity.apply(TestEntityId)).thenReturn(Right(Some(Array(1, 2, 3))))

      val result =
        service.testExportWith(
          withoutSecurity.apply,
          withSecurity.apply,
          TestEntityId,
          useSecurity = false
        )

      verify(withoutSecurity).apply(TestEntityId)
      verifyNoInteractions(withSecurity)
      result.toList should equal(List(1, 2, 3))
    }

    it("calls withSecurity when useSecurity is true") {
      val withoutSecurity = mock(classOf[MockableExportFn])
      val withSecurity    = mock(classOf[MockableExportFn])
      when(withSecurity.apply(TestEntityId)).thenReturn(Right(Some(Array(4, 5, 6))))

      val result =
        service.testExportWith(
          withoutSecurity.apply,
          withSecurity.apply,
          TestEntityId,
          useSecurity = true
        )

      verifyNoInteractions(withoutSecurity)
      verify(withSecurity).apply(TestEntityId)
      result.toList should equal(List(4, 5, 6))
    }

    it("throws NotFoundException when result is Right(None)") {
      val withoutSecurity: ExportFn = _ => Right(None)
      val withSecurity: ExportFn    = _ => Right(None)

      intercept[NotFoundException] {
        service.testExportWith(withoutSecurity, withSecurity, TestEntityId, useSecurity = false)
      }
    }

    it("throws ClientRequestException when result is Left(errors)") {
      val withoutSecurity: ExportFn = _ => Left(List(GraphQlError("Export failed")))
      val withSecurity: ExportFn    = _ => Right(None)

      intercept[ClientRequestException] {
        service.testExportWith(withoutSecurity, withSecurity, TestEntityId, useSecurity = false)
      }
    }
  }

  describe("cancelEditWith") {
    it("calls normal when force is false") {
      val normal = mock(classOf[MockableUnitFn])
      val forced = mock(classOf[MockableUnitFn])
      when(normal.apply(TestEntityId)).thenReturn(Right(()))

      service.testCancelEditWith(normal.apply, forced.apply, TestEntityId, force = false)

      verify(normal).apply(TestEntityId)
      verifyNoInteractions(forced)
    }

    it("calls forced when force is true") {
      val normal = mock(classOf[MockableUnitFn])
      val forced = mock(classOf[MockableUnitFn])
      when(forced.apply(TestEntityId)).thenReturn(Right(()))

      service.testCancelEditWith(normal.apply, forced.apply, TestEntityId, force = true)

      verifyNoInteractions(normal)
      verify(forced).apply(TestEntityId)
    }

    it("throws ClientRequestException on Left(errors)") {
      val normal: UnitFn = _ => Left(List(GraphQlError("Cancel failed")))
      val forced: UnitFn = _ => Right(())

      intercept[ClientRequestException] {
        service.testCancelEditWith(normal, forced, TestEntityId, force = false)
      }
    }
  }

  describe("deleteWith") {
    it("calls withoutCheck when checkReferences is false") {
      val withoutCheck = mock(classOf[MockableUnitFn])
      val withCheck    = mock(classOf[MockableUnitFn])
      when(withoutCheck.apply(TestEntityId)).thenReturn(Right(()))

      service.testDeleteWith(
        withoutCheck.apply,
        withCheck.apply,
        TestEntityId,
        checkReferences = false
      )

      verify(withoutCheck).apply(TestEntityId)
      verifyNoInteractions(withCheck)
    }

    it("calls withCheck when checkReferences is true") {
      val withoutCheck = mock(classOf[MockableUnitFn])
      val withCheck    = mock(classOf[MockableUnitFn])
      when(withCheck.apply(TestEntityId)).thenReturn(Right(()))

      service.testDeleteWith(
        withoutCheck.apply,
        withCheck.apply,
        TestEntityId,
        checkReferences = true
      )

      verifyNoInteractions(withoutCheck)
      verify(withCheck).apply(TestEntityId)
    }

    it("throws ClientRequestException on Left(errors)") {
      val withoutCheck: UnitFn = _ => Left(List(GraphQlError("Delete failed")))
      val withCheck: UnitFn    = _ => Right(())

      intercept[ClientRequestException] {
        service.testDeleteWith(withoutCheck, withCheck, TestEntityId, checkReferences = false)
      }
    }
  }

  describe("cloneWith") {
    it("converts Right(BaseEntityReferenceView) to BaseEntityLabel") {
      val clonedUuid      = "a8098c1a-f86e-11da-bd1a-00112444be1e"
      val cloner: CloneFn =
        _ =>
          Right(
            BaseEntityReferenceView(
              TestClonedId,
              clonedUuid,
              999L,
              "cloner-owner",
              forCollection = false
            )
          )

      val label = service.testCloneWith(cloner, TestCloneSrcId)

      label.getId should equal(TestClonedId)
      label.getUuid should equal(clonedUuid)
    }

    it("throws ClientRequestException when result is Left(errors)") {
      val cloner: CloneFn = _ => Left(List(GraphQlError("Clone failed")))

      intercept[ClientRequestException] {
        service.testCloneWith(cloner, TestCloneSrcId)
      }
    }
  }

  describe("readOnlyPackWith") {
    val SchemaPrivilege   = "EDIT_SCHEMA"
    val ModeratePrivilege = "MODERATE_ITEM"
    val TestWho           = "U:test-user"
    val TestTaskId        = "workflow-task-1"
    val NoPostfix         = ""

    def aclEntry(privilege: String): TargetListEntryView =
      TargetListEntryView(
        granted = true,
        overridden = false,
        privilege = privilege,
        who = TestWho,
        postfix = NoPostfix
      )

    /** A sub-entity list of the one shape which needs no owning collection for its key. */
    def workflowTaskList(privilege: String): OtherTargetListView =
      OtherTargetListView(
        targetType = OtherTargetListType.WORKFLOW_TASK,
        itemStatus = None,
        metadataRuleId = None,
        taskId = Some(TestTaskId),
        entries = List(aclEntry(privilege))
      )

    def anEntity: BaseEntity = new BaseEntity().tap(_.setId(TestEntityId))

    def serviceFor(entity: BaseEntity): TestService = new TestService(_ => entity)

    def packOf(entity: BaseEntity, security: BaseEntitySecurityView): EntityPack[BaseEntity] =
      serviceFor(entity).testReadOnlyPackWith(_ => Right(security), TestEntityId)

    it("assembles the pack from the entity and its access control entries") {
      val entity   = anEntity
      val security = BaseEntitySecurityView(List(aclEntry(SchemaPrivilege)), List.empty)

      val pack = packOf(entity, security)

      pack.getEntity should be theSameInstanceAs entity
      pack.getTargetList.getEntries.asScala.map(e =>
        (e.getPrivilege, e.getWho)
      ) should contain theSameElementsAs List((SchemaPrivilege, TestWho))
    }

    it("leaves the staging ID unset, as nothing is being edited") {
      packOf(anEntity, BaseEntitySecurityView(List.empty, List.empty)).getStagingID should be(null)
    }

    it("leaves the sub-entity lists unset when the entity has none") {
      val pack = packOf(anEntity, BaseEntitySecurityView(List.empty, List.empty))

      // Matching the legacy fillTargetLists, which only sets the map when it finds entries.
      pack.getOtherTargetLists should be(null)
    }

    it("converts the sub-entity lists when the entity has them") {
      val security =
        BaseEntitySecurityView(List.empty, List(workflowTaskList(ModeratePrivilege)))

      val pack = packOf(anEntity, security)

      pack.getOtherTargetLists.asScala.keys.map {
        case task: WorkflowTaskTarget => task.getTaskId
        case other                    => fail(s"Unexpected target list key: $other")
      } should contain theSameElementsAs List(TestTaskId)
    }

    it("does not look up the access control details when the entity is not found") {
      val securityFetcher = mock(classOf[MockableSecurityFn])
      val service         = new TestService(_ => throw new NotFoundException("No such entity"))

      intercept[NotFoundException] {
        service.testReadOnlyPackWith(securityFetcher.apply, TestEntityId)
      }

      verifyNoInteractions(securityFetcher)
    }

    it("throws ClientRequestException when the access control lookup fails") {
      val securityFetcher: SecurityFn = _ => Left(List(GraphQlError("Security lookup failed")))

      intercept[ClientRequestException] {
        serviceFor(anEntity).testReadOnlyPackWith(securityFetcher, TestEntityId)
      }
    }
  }

  // A test subclass that can access protected methods.
  //
  // `getEntity` stands in for the type specific `get` a real subclass provides; it defaults to
  // throwing so that the helpers which never call it are unaffected.
  private class TestService(
      getEntity: Long => BaseEntity = _ => throw new NotImplementedError("Test implementation only")
  ) extends AdminEntityService[BaseEntity] {
    override def entityDescription: String = "test entity"

    // Neither config is exercised by these tests — the helpers under test are handed their API
    // functions, so they never construct a client of their own.
    override protected implicit def cfg: ClientConfiguration =
      throw new NotImplementedError("Test implementation only")

    override protected implicit def restCfg: RestConfiguration =
      throw new NotImplementedError("Test implementation only")

    override def get(id: Long): BaseEntity = getEntity(id)

    override def implementMe[T](f: RemoteAbstractEntityService[BaseEntity] => T): T =
      throw new NotImplementedError("Test implementation only")

    // Public test methods that call protected helpers
    def testListAllFrom(
        result: Either[List[ApiError], List[BaseEntityReferenceView]]
    ): util.List[BaseEntityLabel] = listAllFrom(result)

    def testIdByUuid(
        getter: String => Either[List[ApiError], Option[Long]],
        uuid: String
    ): Long =
      idByUuid(getter)(uuid)

    def testExportWith(
        withoutSecurity: ExportFn,
        withSecurity: ExportFn,
        id: Long,
        useSecurity: Boolean
    ): Array[Byte] = exportWith(withoutSecurity, withSecurity)(id, useSecurity)

    def testCancelEditWith(
        normal: UnitFn,
        forced: UnitFn,
        id: Long,
        force: Boolean
    ): Unit = cancelEditWith(normal, forced)(id, force)

    def testDeleteWith(
        withoutCheck: UnitFn,
        withCheck: UnitFn,
        id: Long,
        checkReferences: Boolean
    ): Unit = deleteWith(withoutCheck, withCheck)(id, checkReferences)

    def testCloneWith(
        cloner: CloneFn,
        id: Long
    ): BaseEntityLabel =
      cloneWith(cloner)(id)

    def testReadOnlyPackWith(
        securityFetcher: SecurityFn,
        id: Long
    ): EntityPack[BaseEntity] =
      readOnlyPackWith(securityFetcher)(id)
  }

}
