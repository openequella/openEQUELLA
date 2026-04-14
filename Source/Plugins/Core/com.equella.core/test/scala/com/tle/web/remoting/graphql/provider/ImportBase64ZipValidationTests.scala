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

import com.tle.web.remoting.graphql.ErrorCode
import org.scalatest.GivenWhenThen
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

import java.util.Base64

/** Shared ScalaTest behaviours for testing Base64 zip input validation in entity provider import
  * methods.
  *
  * Mix this trait into any provider test that exercises an import method accepting a Base64-encoded
  * zip string. Call the shared behavior function directly inside a `describe` block to apply all
  * shared test cases automatically.
  *
  * Example usage:
  * {{{
  * class MyProviderTest extends AnyFunSpec with Matchers with GivenWhenThen
  *     with ImportBase64ZipValidationTests {
  *
  *   val mockService = mock(classOf[MyService])
  *   val provider    = new MyProvider(mockService)
  *
  *   describe("MyProvider.importEntity") {
  *     validatesBase64ZipInput(
  *       provider.importEntity,
  *       "my entity",
  *       testData => when(mockService.importEntity(testData)).thenReturn(mockEntityPack)
  *     )
  *   }
  * }
  * }}}
  */
trait ImportBase64ZipValidationTests { self: AnyFunSpec with Matchers with GivenWhenThen =>

  /** Defines shared test cases for Base64 zip input in import methods:
    *   - Rejects empty string input
    *   - Rejects whitespace-only input
    *   - Returns `ProviderError` for invalid (non-decodable) Base64
    *   - Accepts valid Base64 and returns a `Right`
    *
    * @param importFn
    *   the import function under test, accepting a `String` and returning an `Either`
    * @param entityName
    *   a human-readable entity name used in failure message assertions (e.g., "metadata schema")
    * @param setupMockForValidImport
    *   a function that configures the service mock(s) for the success case; receives the raw bytes
    *   that will be passed to the service
    * @tparam T
    *   the success type of the `Either` returned by the import function
    */
  def validatesBase64ZipInput[T](
      importFn: String => Either[ProviderError, T],
      entityName: String,
      setupMockForValidImport: Array[Byte] => Unit
  ): Unit = {

    it("rejects empty string input") {
      Given(s"an import function for $entityName")

      When("it is called with an empty string")
      val result = importFn("")

      Then("it returns a Left with ProviderError indicating empty zip data")
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
      Given(s"an import function for $entityName")

      When("it is called with whitespace-only input")
      val result = importFn("   \t\n   ")

      Then("it returns a Left with ProviderError indicating empty zip data")
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
      Given(s"an import function for $entityName")

      When("it is called with invalid (non-decodable) base64 data")
      val result = importFn("!!!invalid_base64!!!")

      Then("it returns a Left with ProviderError describing the failure")
      result.isLeft should be(true)
      result.left.get.message should include(s"Failed to import $entityName")
    }

    it("accepts valid base64 and returns a Right") {
      Given("a valid base64-encoded zip content")
      val testData    = "PK\u0003\u0004".getBytes() // Minimal zip file signature
      val validBase64 = Base64.getEncoder.encodeToString(testData)

      And(s"the $entityName service is mocked to return a result")
      setupMockForValidImport(testData)

      When(s"the import function is called with valid base64 input")
      val result = importFn(validBase64)

      Then("it returns a Right")
      result.isRight should be(true)
    }
  }
}
