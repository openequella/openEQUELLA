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

package com.tle.plugins.ump

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class PageRangeTest extends AnyFunSpec with Matchers {

  describe("PageRange") {
    it("throws IllegalArgumentException for negative limit") {
      an[IllegalArgumentException] should be thrownBy PageRange(limit = -1, offset = 0)
    }

    it("throws IllegalArgumentException for negative offset") {
      an[IllegalArgumentException] should be thrownBy PageRange(limit = 1, offset = -1)
    }

    it("accepts zero limit") {
      PageRange(limit = 0, offset = 0).limit shouldBe 0
    }

    it("accepts zero offset") {
      PageRange(limit = 1, offset = 0).offset shouldBe 0
    }

    it("accepts normal value") {
      val pageRange = PageRange(limit = 5, offset = 6)
      pageRange.limit shouldBe 5
      pageRange.offset shouldBe 6
    }
  }
}
