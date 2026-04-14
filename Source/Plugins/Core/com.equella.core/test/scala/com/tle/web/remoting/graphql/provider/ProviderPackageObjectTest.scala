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

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class ProviderPackageObjectTest extends AnyFunSpec with Matchers {

  describe("provider.idForUuid") {
    it("returns Some(id) when identify returns a non-zero id") {
      val expectedId               = 123L
      val identify: String => Long = _ => expectedId

      idForUuid("some-uuid", identify) shouldBe Some(expectedId)
    }

    it("returns None when identify returns 0") {
      val identify: String => Long = _ => 0L

      idForUuid("missing-uuid", identify) shouldBe None
    }
  }
}
