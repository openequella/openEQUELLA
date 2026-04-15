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
import io.github.openequella.graphql.api.{AccessDeniedError, ApiError}
import io.github.openequella.graphql.test.{TestHelper, ZipTestHelper}
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.prop.{TableDrivenPropertyChecks, Tables}
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
  *   The export function under test — takes `(id, withSecurity)`; cfg captured at call site.
  * @param expectedEntityClass
  *   Fully-qualified Java class name expected inside `_entity.xml` (e.g.
  *   `"com.tle.beans.entity.Schema"`).
  * @param unauthExportFn
  *   Export function that accepts an explicit [[ClientConfiguration]], used to simulate an
  *   unauthenticated caller.
  */
case class ExportBehaviorConfig(
    entityName: String,
    getFirstIdFn: () => Long,
    exportFn: (Long, Boolean) => Either[List[ApiError], Option[Array[Byte]]],
    expectedEntityClass: String,
    unauthExportFn: ClientConfiguration => Either[List[ApiError], Option[Array[Byte]]]
)

/** ScalaTest shared behaviour mixin for entity export tests.
  *
  * Mix this trait into a [[AnyFunSpec]]-based test class and call [[exportBehavior]] inside a
  * `describe` block to get three standard export test cases without any code duplication.
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
  *       entityName        = "my entity",
  *       getFirstIdFn      = () => MyApi.listEntities().value.head.id,
  *       exportFn          = MyApi.exportEntity,
  *       expectedEntityClass = "com.example.MyEntity",
  *       unauthExportFn    = cfg => MyApi.exportEntity(1, withSecurity = false)(cfg)
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

  /** Registers three `it` blocks covering the standard export scenarios.
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

    it(s"exports a ${config.entityName} as a ZIP file") {
      val withSecurityOptions = Tables.Table("withSecurity", true, false)
      forAll(withSecurityOptions) { withSecurity =>
        Given(s"a valid ${config.entityName} ID with withSecurity=$withSecurity")
        val entityId = config.getFirstIdFn()

        When(s"calling export with the ${config.entityName} ID and withSecurity")
        val result = config.exportFn(entityId, withSecurity)

        // We convert the result to a ZipInputStream for easier testing.
        // Most often in actual application the bytes are simply saved to a file.
        // Here we just want to ensure that the bytes can be interpreted as a zip file.
        Then("returns an Array[Byte] convertable to a ZipInputStream")
        result.isRight shouldBe true
        val zis = result.value.map(ZipTestHelper.bytesToZipInputStream).get
        zis shouldBe a[ZipInputStream]

        And("the zip file includes a valid _entity.xml")
        val entityXml = ZipTestHelper.extractEntityXml(zis).value
        entityXml should (startWith("<com.tle.common.ImportExportPack>") and include(
          s"""<entity class="${config.expectedEntityClass}">"""
        ))

        And("the _entity.xml contains security information based on withSecurity")
        val securityElement = "<targetList>"
        if (withSecurity) entityXml should include(securityElement)
        else entityXml should not include securityElement

        // Ensure we close the stream to free resources
        zis.close()
      }
    }

    it(s"returns None for an invalid ${config.entityName} ID") {
      Given(s"an invalid ${config.entityName} ID")
      val result = config.exportFn(-1L, false)

      Then("returns None")
      result shouldBe Right(None)
    }

    it("denies access when not authenticated") {
      When(s"an unauthenticated user calls export ${config.entityName}")
      val response = TestHelper.asUnauthenticatedUser(config.unauthExportFn)

      Then("returns an AccessDeniedError")
      TestHelper.checkApiError(response) shouldBe a[AccessDeniedError]
    }
  }
}
