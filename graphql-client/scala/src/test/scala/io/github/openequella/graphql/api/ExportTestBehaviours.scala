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

package io.github.openequella.graphql.api

import io.github.openequella.graphql.ClientConfiguration
import io.github.openequella.graphql.test.TestHelper.{INVALID_ENTITY_ID, assertAccessDeniedError}
import io.github.openequella.graphql.test.ZipTestHelper
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatest.{EitherValues, GivenWhenThen, OptionValues}

import java.util.zip.ZipInputStream

/** Configuration for the [[ExportTestBehaviours#exportBehavior]] shared test.
  *
  * Groups parameters that would otherwise make `exportBehavior` violate the Clean Code guideline of
  * keeping function argument lists short.
  *
  * @param entityName
  *   Human-readable entity name used in test descriptions (e.g. `"metadata schema"`).
  * @param getFirstIdFn
  *   Returns the ID of the first available entity; the caller's implicit [[ClientConfiguration]] is
  *   captured at the call site.
  * @param exportFn
  *   Export function without security, taking the ID and the session to run it against. Called both
  *   as the authenticated user and, for the access check, as an unauthenticated one.
  * @param exportWithSecurityFn
  *   Export function with security ACLs included, taking the ID and the session to run it against.
  * @param expectedEntityClass
  *   Fully-qualified Java class name expected inside `_entity.xml` (e.g.
  *   `"com.tle.beans.entity.Schema"`).
  */
case class ExportBehaviorConfig(
    entityName: String,
    getFirstIdFn: () => Long,
    exportFn: (Long, ClientConfiguration) => Either[List[ApiError], Option[Array[Byte]]],
    exportWithSecurityFn: (Long, ClientConfiguration) => Either[List[ApiError], Option[
      Array[Byte]
    ]],
    expectedEntityClass: String
)

/** ScalaTest shared behaviour mixin for entity export tests.
  *
  * Mix this trait into a [[AnyFunSpec]]-based test class and call [[exportBehavior]] inside a
  * `describe` block to get four standard export test cases without any code duplication.
  *
  * ===Usage example===
  * {{{
  * class MyApiQueriesTest extends AnyFunSpec
  *     with ExportTestBehaviours
  *     with Matchers
  *     with GivenWhenThen
  *     with EitherValues
  *     with OptionValues
  *     with TableDrivenPropertyChecks {
  *
  *   implicit val cfg: ClientConfiguration = TestHelper.loginToRestInstitution()
  *
  *   describe("exportMyEntity") {
  *     exportBehavior(ExportBehaviorConfig(
  *       entityName            = "my entity",
  *       getFirstIdFn          = () => MyApi.listEntities().value.head.id,
  *       exportFn              = MyApi.exportEntity(_)(_),
  *       exportWithSecurityFn  = MyApi.exportEntityWithSecurity(_)(_),
  *       expectedEntityClass   = "com.example.MyEntity"
  *     ))
  *   }
  * }
  * }}}
  */
trait ExportTestBehaviours {
  self: AnyFunSpec
    with Matchers
    with GivenWhenThen
    with EitherValues
    with OptionValues
    with TableDrivenPropertyChecks =>

  /** Registers four `it` blocks covering the standard export scenarios.
    *
    * The tests are parameterised via [[ExportBehaviorConfig]] rather than through a long argument
    * list, keeping this method signature clean.
    *
    * @param config
    *   Entity-specific configuration for the export tests.
    * @param cfg
    *   Authenticated [[ClientConfiguration]] for the happy-path tests.
    */
  def exportBehavior(config: ExportBehaviorConfig)(implicit cfg: ClientConfiguration): Unit = {
    val securityElement = "<targetList>"

    def checkExportZip(
        result: Either[List[ApiError], Option[Array[Byte]]]
    )(checkSecurity: String => Unit): Unit = {
      result.isRight shouldBe true
      val zis = result.value.map(ZipTestHelper.bytesToZipInputStream).get
      zis shouldBe a[ZipInputStream]
      val entityXml = ZipTestHelper.extractEntityXml(zis).value
      entityXml should (startWith("<com.tle.common.ImportExportPack>") and include(
        s"""<entity class="${config.expectedEntityClass}">"""
      ))
      checkSecurity(entityXml)
      zis.close()
    }

    it(s"exports a ${config.entityName} as a ZIP file without security") {
      Given(s"a valid ${config.entityName} ID")
      val entityId = config.getFirstIdFn()

      When(s"calling export on the ${config.entityName} without security")
      val result = config.exportFn(entityId, cfg)

      Then("returns an Array[Byte] convertable to a ZipInputStream")
      checkExportZip(result) { xml =>
        And("the _entity.xml does not contain security information")
        xml should not include securityElement
      }
    }

    it(s"exports a ${config.entityName} as a ZIP file with security") {
      Given(s"a valid ${config.entityName} ID")
      val entityId = config.getFirstIdFn()

      When(s"calling export on the ${config.entityName} with security")
      val result = config.exportWithSecurityFn(entityId, cfg)

      Then("returns an Array[Byte] convertable to a ZipInputStream")
      checkExportZip(result) { xml =>
        And("the _entity.xml contains security information")
        xml should include(securityElement)
      }
    }

    it(s"returns None for an invalid ${config.entityName} ID") {
      Given(s"an invalid ${config.entityName} ID")
      val result = config.exportFn(INVALID_ENTITY_ID, cfg)

      Then("returns None")
      result shouldBe Right(None)
    }

    // The ID is irrelevant - an unauthenticated caller is turned away by the privilege check before
    // the entity is ever looked up - so use the invalid one rather than inventing a real-looking ID.
    it("denies access when not authenticated") {
      assertAccessDeniedError(config.exportFn(INVALID_ENTITY_ID, _))
    }
  }
}
