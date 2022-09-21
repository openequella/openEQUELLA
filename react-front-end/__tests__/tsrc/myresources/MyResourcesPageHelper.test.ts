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
import * as OEQ from "@openequella/rest-api-client";
import { createMemoryHistory } from "history";
import { saveSearchPageOptions } from "../../../tsrc/modules/ScrapbookModule";
import {
  getMyResourcesTypeFromQueryParam,
  getSearchPageOptionsFromStorage,
  getSortOrderFromQueryParam,
  getSubStatusFromQueryParam,
  MyResourcesType,
} from "../../../tsrc/myresources/MyResourcesPageHelper";
import { defaultSearchPageOptions } from "../../../tsrc/search/SearchPageHelper";

describe("MyResourcesPageHelper", () => {
  const history = createMemoryHistory();

  describe("support for Legacy query params", () => {
    it("gets My resources type from Legacy query param", () => {
      history.push(
        "http://localhost:8080/fiveo/access/myresources.do?type=modqueue"
      );

      expect(
        getMyResourcesTypeFromQueryParam(history.location)
      ).toBe<MyResourcesType>("Moderation queue");
    });

    it("gets Item status from Legacy query param", () => {
      history.push(
        "http://localhost:8080/fiveo/access/myresources.do?type=modqueue&mstatus=moderating&status=live"
      );

      expect(getSubStatusFromQueryParam(history.location)).toStrictEqual<
        OEQ.Common.ItemStatus[]
      >(["MODERATING"]);
    });

    it("gets sort order from Legacy query param", () => {
      history.push(
        "http://localhost:8080/fiveo/access/myresources.do?type=modqueue&modsort=lastmod&sort=title&sbsort=datecreated"
      );

      expect(
        getSortOrderFromQueryParam(history.location)
      ).toBe<OEQ.Search.SortOrder>("task_lastaction");
    });
  });

  describe("support for New UI query params", () => {
    const baseUrl = "https://oeq/testing/page/myresources";

    it("determines My Resource type via myResourcesType", () => {
      history.push(baseUrl + "?myResourcesType=All resources");

      expect(
        getMyResourcesTypeFromQueryParam(history.location)
      ).toBe<MyResourcesType>("All resources");
    });

    it("ignores unrecognised values for myResourcesType", () => {
      history.push(baseUrl + "?myResourcesType=rubbishIn");

      expect(
        getMyResourcesTypeFromQueryParam(history.location)
      ).toBeUndefined();
    });

    it("use the item statuses from searchOptions", () => {
      history.push(
        baseUrl +
          '?myResourcesType=Moderation+queue&searchOptions={"rowsPerPage"%3A10%2C"currentPage"%3A0%2C"sortOrder"%3A"task_submitted"%2C"rawMode"%3Afalse%2C"status"%3A["MODERATING"%2C"REVIEW"]%2C"searchAttachments"%3Atrue%2C"query"%3A""%2C"collections"%3A[]%2C"lastModifiedDateRange"%3A{}%2C"owner"%3A{"id"%3A"TLE_ADMINISTRATOR"}%2C"mimeTypeFilters"%3A[]%2C"displayMode"%3A"list"%2C"dateRangeQuickModeEnabled"%3Atrue}'
      );

      expect(getSubStatusFromQueryParam(history.location)).toStrictEqual<
        OEQ.Common.ItemStatus[]
      >(["MODERATING", "REVIEW"]);
    });

    it("uses the sort order from searchOptions", () => {
      history.push(
        baseUrl +
          '?myResourcesType=Scrapbook&searchOptions={"rowsPerPage"%3A10%2C"currentPage"%3A0%2C"sortOrder"%3A"datecreated"%2C"rawMode"%3Afalse%2C"status"%3A["PERSONAL"]%2C"searchAttachments"%3Atrue%2C"query"%3A""%2C"collections"%3A[]%2C"lastModifiedDateRange"%3A{}%2C"owner"%3A{"id"%3A"TLE_ADMINISTRATOR"}%2C"mimeTypeFilters"%3A[]%2C"displayMode"%3A"list"%2C"dateRangeQuickModeEnabled"%3Atrue}'
      );

      expect(
        getSortOrderFromQueryParam(history.location)
      ).toBe<OEQ.Search.SortOrder>("datecreated");
    });
  });

  describe("interact with session storage", () => {
    it("gets SearchPageOptions from session storage", () => {
      const uuid = saveSearchPageOptions(defaultSearchPageOptions);

      history.push(
        `http://localhost:8080/page/myresources?type=scrapbook&searchPageOptionsID=${uuid}`
      );
      const searchPageOptions = getSearchPageOptionsFromStorage(
        history.location
      );

      // Do not compare the parsed object with `defaultSearchPageOptions` because they do have some acceptable differences.
      // Here we only need to check the parsed result is an object, and it has the mandatory field `dateRangeQuickModeEnabled`.
      expect(searchPageOptions).toBeInstanceOf(Object);
      expect(searchPageOptions?.["dateRangeQuickModeEnabled"]).toBeDefined();
    });
  });
});
