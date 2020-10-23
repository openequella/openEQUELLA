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
import { getCollectionMap } from "../../../__mocks__/getCollectionsResp";
import { getSearchResult } from "../../../__mocks__/SearchResult.mock";
import { users } from "../../../__mocks__/UserSearch.mock";
import * as CollectionModule from "../../../tsrc/modules/CollectionsModule";
import type { SelectedCategories } from "../../../tsrc/modules/SearchFacetsModule";
import * as SearchModule from "../../../tsrc/modules/SearchModule";
import {
  convertParamsToSearchOptions,
  DateRange,
  defaultSearchOptions,
  SearchOptions,
} from "../../../tsrc/modules/SearchModule";
import { SortOrder } from "../../../tsrc/modules/SearchSettingsModule";
import * as UserModule from "../../../tsrc/modules/UserModule";

jest.mock("@openequella/rest-api-client");
const mockedSearch = (OEQ.Search.search as jest.Mock<
  Promise<OEQ.Search.SearchResult<OEQ.Search.SearchResultItem>>
>).mockResolvedValue(getSearchResult);

describe("SearchModule", () => {
  describe("searchItems", () => {
    it("should provide a list of items", async () => {
      const searchResult = await SearchModule.searchItems(
        SearchModule.defaultSearchOptions
      );
      expect(searchResult.available).toBe(12);
      expect(searchResult.results).toHaveLength(12);
    });

    const validateSearchQuery = (expectedQuery: string) => {
      const calls = mockedSearch.mock.calls;
      const params = calls[0][1]; // Second parameter of the call is the 'params'
      expect(params.query).toEqual(expectedQuery);
    };

    it("should not append a wildcard for a search which is empty when trimmed", async () => {
      mockedSearch.mockReset();
      await SearchModule.searchItems({
        ...SearchModule.defaultSearchOptions,
        query: "   ",
      });
      validateSearchQuery("");
    });

    it("should append a wildcard for a search non-rawMode, non-empty query", async () => {
      mockedSearch.mockReset();
      const queryTerm = "non RAW";
      await SearchModule.searchItems({
        ...SearchModule.defaultSearchOptions,
        query: queryTerm,
      });
      validateSearchQuery(`${queryTerm}*`);
    });

    it("should NOT append a wildcard for a rawMode search with a non-empty query", async () => {
      mockedSearch.mockReset();
      const queryTerm = "RAW mode!";
      await SearchModule.searchItems({
        ...SearchModule.defaultSearchOptions,
        query: queryTerm,
        rawMode: true,
      });
      validateSearchQuery(`${queryTerm}`);
    });
  });

  describe("generateCategoryWhereQuery", () => {
    const selectedCategories: SelectedCategories[] = [
      {
        id: 766942,
        schemaNode: "/item/language",
        categories: ["Java", "Scala"],
      },
      {
        id: 766943,
        schemaNode: "/item/city",
        categories: ["Hobart"],
      },
    ];

    it("should generate a where clause for one category", () => {
      const singleCategory = [selectedCategories[1]];
      expect(SearchModule.generateCategoryWhereQuery(singleCategory)).toBe(
        "(/xml/item/city='Hobart')"
      );
    });

    it("should generate a where clause for multiple groups of categories", () => {
      expect(SearchModule.generateCategoryWhereQuery(selectedCategories)).toBe(
        "(/xml/item/language='Java' OR /xml/item/language='Scala') AND (/xml/item/city='Hobart')"
      );
    });
  });

  it("should return undefined if no categories are selected", () => {
    expect(SearchModule.generateCategoryWhereQuery(undefined)).toBeUndefined();
    expect(SearchModule.generateCategoryWhereQuery([])).toBeUndefined();
  });

  describe("convertParamsToSearchOptions", () => {
    const mockedResolvedUser = jest.spyOn(UserModule, "resolveUsers");
    const mockedCollectionListSummary = jest.spyOn(
      CollectionModule,
      "collectionListSummary"
    );

    afterEach(() => {
      jest.clearAllMocks();
    });

    it("should return undefined if no query string parameters are defined", async () => {
      const convertedParamsPromise = await convertParamsToSearchOptions("");
      expect(convertedParamsPromise).toBeUndefined();
    });

    it("should convert legacy search parameters to searchOptions", async () => {
      mockedResolvedUser.mockResolvedValue(users);
      mockedCollectionListSummary.mockResolvedValue(getCollectionMap);

      //Query string was obtained from legacy UI searching.do->Share URL
      const fullQueryString =
        "?in=C8e3caf16-f3cb-b3dd-d403-e5eb8d545fff&q=test&sort=datecreated&owner=680f5eb7-22e2-4ab6-bcea-25205165e36e&dp=1601510400000&dr=AFTER";

      const convertedParamsPromise = await convertParamsToSearchOptions(
        fullQueryString
      );

      const expectedSearchOptions: SearchOptions = {
        ...defaultSearchOptions,
        sortOrder: SortOrder.DATECREATED,
        searchAttachments: true,
        collections: [
          {
            uuid: "8e3caf16-f3cb-b3dd-d403-e5eb8d545fff",
            name: "DRM Test Collection",
          },
        ],
        query: "test",
        owner: {
          id: "680f5eb7-22e2-4ab6-bcea-25205165e36e",
          username: "user200",
          firstName: "Fabienne",
          lastName: "Hobson",
        },
        lastModifiedDateRange: { start: new Date("2020-10-01T00:00:00.000Z") },
      };

      expect(convertedParamsPromise).toEqual(expectedSearchOptions);
    });

    // All combinations of Date Modified parameters that legacy UI uses. Epoch Unix Time Stamp formatted
    // dp: DatePrimary ds: DateSecondary dr: DateRange

    // dp=15 October 2020 00:00:00
    const beforeDateQuery = "?dp=1602720000000&dr=BEFORE";
    // dp=15 October 2020 00:00:00
    const afterDateQuery = "?dp=1602720000000&dr=AFTER";
    // dp=13 October 2020 00:00:00 ds=15 October 2020 00:00:00
    const betweenDateQuery = "?dp=1602547200000&ds=1602720000000&dr=BETWEEN";
    // dp=15 October 2020 00:00:00 ds=16 October 2020 00:00:00
    const onDateQuery = "?dp=1602720000000&dr=ON";

    const expectedBeforeRange = {
      end: new Date("2020-10-15T00:00:00.000+00:00"),
    };
    const expectedAfterRange = {
      start: new Date("2020-10-15T00:00:00.000+00:00"),
    };
    const expectedBetweenRange = {
      start: new Date("2020-10-13T00:00:00.000+00:00"),
      end: new Date("2020-10-15T00:00:00.000+00:00"),
    };
    const expectedOnRange = {
      start: new Date("2020-10-15T00:00:00.000+00:00"),
      end: new Date("2020-10-15T00:00:00.000+00:00"),
    };

    it.each<[string, DateRange]>([
      [beforeDateQuery, expectedBeforeRange],
      [afterDateQuery, expectedAfterRange],
      [betweenDateQuery, expectedBetweenRange],
      [onDateQuery, expectedOnRange],
    ])(
      "converts legacy date range query params: %s to search options containing lastModifiedDateRange of %s",
      async (queryString, expectedRange) => {
        const convertedSearchOptions = await convertParamsToSearchOptions(
          queryString
        );
        expect(convertedSearchOptions).toEqual({
          ...defaultSearchOptions,
          lastModifiedDateRange: expectedRange,
        });
      }
    );
  });
});
