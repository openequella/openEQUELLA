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

package com.tle.core.institution.migration.v20262

import com.tle.core.institution.migration.v20262.MigrateHierarchyFavouriteSearchUrlHelper.toUrlSafeFormat
import com.tle.web.api.browsehierarchy.HierarchyCompoundUuid
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.prop.TableDrivenPropertyChecks._

class MigrateHierarchyFavouriteSearchUrlHelperTest extends AnyFunSpec with Matchers {
  private val topicUuid  = "886aa61d-f8df-4e82-8984-c487849f80ff"
  private val parentUuid = "46249813-019d-4d14-b772-2a8ca0120c99"

  // Base64 encoding of "Music? Theory > Jazz"
  // Standard: TXVzaWM/IFRoZW9yeSA+IEpheno=
  // URL-safe: TXVzaWM_IFRoZW9yeSA-IEpheno
  private val standardName = "TXVzaWM/IFRoZW9yeSA+IEpheno="
  private val urlSafeName  = "TXVzaWM_IFRoZW9yeSA-IEpheno"

  private val hierarchyPage = "/page/hierarchy/"

  private val searchOptions = "?searchOptions=%7B%22query%22%3A%22a%2Bb%2Fc%3D%22%7D"

  describe("toUrlSafeFormat") {
    it("rewrites the compound UUID of a hierarchy favourite search") {
      val urls = Table(
        ("description", "url", "expected"),
        (
          "a virtual hierarchy",
          s"$hierarchyPage$topicUuid:$standardName$searchOptions",
          s"$hierarchyPage$topicUuid:$urlSafeName$searchOptions"
        ),
        (
          "a virtual hierarchy with a virtual ancestor",
          s"$hierarchyPage$topicUuid:$standardName,$parentUuid:TWF0aHM/IFllcw==$searchOptions",
          s"$hierarchyPage$topicUuid:$urlSafeName,$parentUuid:TWF0aHM_IFllcw$searchOptions"
        ),
        (
          "a virtual hierarchy without search options",
          s"$hierarchyPage$topicUuid:$standardName",
          s"$hierarchyPage$topicUuid:$urlSafeName"
        )
      )

      forAll(urls) { (_, url, expected) =>
        toUrlSafeFormat(url) shouldBe expected
      }
    }

    it("leaves every other favourite search untouched") {
      val urls = Table(
        ("description", "url"),
        (
          "a hierarchy already in the unpadded URL-safe format",
          s"$hierarchyPage$topicUuid:$urlSafeName$searchOptions"
        ),
        ("a non virtual hierarchy", s"$hierarchyPage$topicUuid"),
        // A percent encoded compound UUID cannot be translated without decoding it first, which
        // would be guessing at how it was encoded.
        (
          "a percent encoded compound UUID",
          s"$hierarchyPage$topicUuid%3ATXVzaWM%2FIFRoZW9yeSA%2BIEpheno%3D"
        ),
        // The page path only counts when it is in the path, not when the search options mention it.
        (
          "a search whose options mention the hierarchy page",
          s"/page/search?searchOptions=$hierarchyPage$topicUuid"
        ),
        ("a normal search", "/page/search?searchOptions=%7B%22query%22%3A%22a%2Bb%22%7D"),
        ("an advanced search", s"/page/advancedsearch/$topicUuid$searchOptions"),
        ("my resources", s"/page/myresources$searchOptions"),
        ("favourites", s"/page/favourites$searchOptions"),
        (
          "a legacy UI hierarchy search",
          s"/hierarchy.do?topic=$topicUuid%3AD%252C%2BDavid&sort=rank&dr=AFTER"
        ),
        ("a legacy UI hierarchy browse", "/hierarchy.do?topic=ALL&sort=rank&dr=AFTER"),
        ("a legacy UI search", "/searching.do?in=all&q=Search+B&type=standard"),
        ("an empty URL", ""),
        ("a null URL", null)
      )

      forAll(urls) { (_, url) =>
        toUrlSafeFormat(url) shouldBe url
      }
    }
  }
}
