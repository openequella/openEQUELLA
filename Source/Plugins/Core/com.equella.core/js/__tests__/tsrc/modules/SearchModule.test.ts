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
import { getMimeTypeFilters } from "../../../__mocks__/MimeTypeFilter.mock";
import {
  allSearchOptions,
  basicSearchOptions,
} from "../../../__mocks__/searchOptions.mock";
import { getSearchResult } from "../../../__mocks__/SearchResult.mock";
import { users } from "../../../__mocks__/UserSearch.mock";
import * as CollectionsModule from "../../../tsrc/modules/CollectionsModule";
import type { SelectedCategories } from "../../../tsrc/modules/SearchFacetsModule";
import * as SearchModule from "../../../tsrc/modules/SearchModule";
import * as SearchFilterSettingsModule from "../../../tsrc/modules/SearchFilterSettingsModule";
import {
  DateRange,
  defaultSearchOptions,
  generateQueryStringFromSearchOptions,
  legacyQueryStringToSearchOptions,
  newSearchQueryToSearchOptions,
  queryStringParamsToSearchOptions,
  SearchOptions,
} from "../../../tsrc/modules/SearchModule";
import * as UserModule from "../../../tsrc/modules/UserModule";

jest.mock("@openequella/rest-api-client", () => {
  // We only want to mock module 'Search' because mocking the whole module
  // will break Runtypes.
  const restModule: typeof OEQ = jest.requireActual(
    "@openequella/rest-api-client"
  );
  return {
    ...restModule,
    Search: {
      search: jest.fn(),
    },
  };
});

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

    const expectSearchQueryToBeValid = (expectedQuery: string) => {
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
      expectSearchQueryToBeValid("");
    });

    it("should append a wildcard for a search non-rawMode, non-empty query", async () => {
      mockedSearch.mockReset();
      const queryTerm = "non RAW";
      await SearchModule.searchItems({
        ...SearchModule.defaultSearchOptions,
        query: queryTerm,
      });
      expectSearchQueryToBeValid(`${queryTerm}*`);
    });

    it("should NOT append a wildcard for a rawMode search with a non-empty query", async () => {
      mockedSearch.mockReset();
      const queryTerm = "RAW mode!";
      await SearchModule.searchItems({
        ...SearchModule.defaultSearchOptions,
        query: queryTerm,
        rawMode: true,
      });
      expectSearchQueryToBeValid(`${queryTerm}`);
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

  describe("newSearchQueryToSearchOptions", () => {
    const mockedResolvedUser = jest.spyOn(UserModule, "resolveUsers");
    const mockedCollectionListSummary = jest.spyOn(
      CollectionsModule,
      "collectionListSummary"
    );
    const mockGetMimeTypeFiltersFromServer = jest.spyOn(
      SearchFilterSettingsModule,
      "getMimeTypeFiltersFromServer"
    );

    beforeEach(() => {
      mockedResolvedUser.mockResolvedValue([users[0]]);
      mockedCollectionListSummary.mockResolvedValueOnce(getCollectionMap);
      mockGetMimeTypeFiltersFromServer.mockResolvedValueOnce(
        getMimeTypeFilters
      );
    });

    afterEach(() => {
      jest.clearAllMocks();
    });

    it("should convert query string to searchOptions", async () => {
      const longSearch =
        '{"rowsPerPage":10,"currentPage":0,"sortOrder":"NAME","query":"test machine","rawMode":true,"status":["LIVE","REVIEW"],"searchAttachments":true,"selectedCategories":[{"id":766943,"categories":["Hobart"]},{"id":766944,"categories":["Some cool things"]}],"collections":[{"uuid":"8e3caf16-f3cb-b3dd-d403-e5eb8d545fff"},{"uuid":"8e3caf16-f3cb-b3dd-d403-e5eb8d545ffe"},{"uuid":"8e3caf16-f3cb-b3dd-d403-e5eb8d545ffg"},{"uuid":"8e3caf16-f3cb-b3dd-d403-e5eb8d545ffa"},{"uuid":"8e3caf16-f3cb-b3dd-d403-e5eb8d545ffb"}],"lastModifiedDateRange":{"start":"2020-05-26T03:24:00.889Z","end":"2020-05-27T03:24:00.889Z"},"owner":{"id":"680f5eb7-22e2-4ab6-bcea-25205165e36e"}, "mimeTypeFilters": [{"id":"fe79c485-a6dd-4743-81e8-52de66494632"},{"id":"fe79c485-a6dd-4743-81e8-52de66494631"}]}';
      const convertedParamsPromise = await newSearchQueryToSearchOptions(
        longSearch
      );
      expect(convertedParamsPromise).toEqual(allSearchOptions);
    });

    const emptyEndDateQueryString =
      '{"lastModifiedDateRange":{"start":"2020-05-26T03:24:00.889Z"}}';
    const expectedEmptyEndDateSearchOptions: SearchOptions = {
      ...basicSearchOptions,
      lastModifiedDateRange: {
        start: new Date("2020-05-26T13:24:00.889+10:00"),
        end: undefined,
      },
    };

    const emptyStartDateQueryString =
      '{"lastModifiedDateRange":{"end":"2020-05-27T03:24:00.889Z"}}';
    const expectedEmptyStartDateSearchOptions: SearchOptions = {
      ...basicSearchOptions,
      lastModifiedDateRange: {
        start: undefined,
        end: new Date("2020-05-27T13:24:00.889+10:00"),
      },
    };

    const fullDateQueryString =
      '{"lastModifiedDateRange":{"start":"2020-05-26T03:24:00.889Z","end":"2020-05-27T03:24:00.889Z"}}';
    const expectedFullDateSearchOptions: SearchOptions = {
      ...basicSearchOptions,
      lastModifiedDateRange: {
        start: new Date("2020-05-26T13:24:00.889+10:00"),
        end: new Date("2020-05-27T13:24:00.889+10:00"),
      },
    };

    it.each([
      [
        "no start date",
        emptyStartDateQueryString,
        expectedEmptyStartDateSearchOptions,
      ],
      [
        "no end date",
        emptyEndDateQueryString,
        expectedEmptyEndDateSearchOptions,
      ],
      [
        "both start and end dates",
        fullDateQueryString,
        expectedFullDateSearchOptions,
      ],
    ])(
      "supports date ranges that have %s",
      async (
        dateRangeType: string,
        queryString: string,
        expectedSearchOptions: SearchOptions
      ) => {
        expect(await newSearchQueryToSearchOptions(queryString)).toEqual(
          expectedSearchOptions
        );
      }
    );

    it("should be able to convert SearchOptions to a query string, and back to SearchOptions again", async () => {
      const queryStringFromSearchOptions = await generateQueryStringFromSearchOptions(
        allSearchOptions
      );
      expect(
        await newSearchQueryToSearchOptions(
          new URLSearchParams(queryStringFromSearchOptions).get(
            "searchOptions"
          ) ?? ""
        )
      ).toEqual(allSearchOptions);
    });
  });

  describe("convertParamsToSearchOptions", () => {
    const mockLocation = {
      pathname: "/search",
      hash: "",
      search: "",
      state: "",
    };
    it("should return undefined if no query string parameters are defined", async () => {
      const convertedParamsPromise = await queryStringParamsToSearchOptions(
        mockLocation
      );
      expect(convertedParamsPromise).toBeUndefined();
    });
  });

  describe("legacyQueryStringToSearchOptions", () => {
    const mockedResolvedUser = jest.spyOn(UserModule, "resolveUsers");
    const mockedCollectionListSummary = jest.spyOn(
      CollectionsModule,
      "collectionListSummary"
    );
    jest
      .spyOn(SearchFilterSettingsModule, "getMimeTypeFiltersFromServer")
      .mockResolvedValue(getMimeTypeFilters);

    it("should return default search for any parameters that aren't supported", async () => {
      const unsupportedQueryString = "?test=nothing&cool=beans";
      expect(
        await legacyQueryStringToSearchOptions(
          new URLSearchParams(unsupportedQueryString)
        )
      ).toEqual(defaultSearchOptions);
    });

    it("should convert legacy search parameters to searchOptions", async () => {
      mockedResolvedUser.mockResolvedValue([users[0]]);
      mockedCollectionListSummary.mockResolvedValue(getCollectionMap);

      //Query string was obtained from legacy UI searching.do->Share URL
      const fullQueryString =
        "?in=C8e3caf16-f3cb-b3dd-d403-e5eb8d545fff&q=test&sort=datecreated&owner=680f5eb7-22e2-4ab6-bcea-25205165e36e&dp=1601510400000&dr=AFTER";

      const convertedParamsPromise = await legacyQueryStringToSearchOptions(
        new URLSearchParams(fullQueryString)
      );

      const expectedSearchOptions: SearchOptions = {
        ...defaultSearchOptions,
        sortOrder: "DATECREATED",
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
        const convertedSearchOptions = await legacyQueryStringToSearchOptions(
          new URLSearchParams(queryString)
        );
        expect(convertedSearchOptions).toEqual({
          ...defaultSearchOptions,
          lastModifiedDateRange: expectedRange,
        });
      }
    );

    it("should return default search options when collectionId and userId do not exist", async () => {
      mockedResolvedUser.mockResolvedValue([]);
      mockedCollectionListSummary.mockResolvedValue(getCollectionMap);
      const collectionQueryString = "?in=Cunknowncollection&owner=unknown";
      const convertedSearchOptions = await legacyQueryStringToSearchOptions(
        new URLSearchParams(collectionQueryString)
      );
      expect(convertedSearchOptions).toEqual(defaultSearchOptions);
    });
  });

  describe("generateQueryStringFromSearchOptions", () => {
    it("converts all searchOptions to a url encoded json string", () => {
      expect(generateQueryStringFromSearchOptions(allSearchOptions)).toEqual(
        "searchOptions=%7B%22rowsPerPage%22%3A10%2C%22currentPage%22%3A0%2C%22sortOrder%22%3A%22NAME%22%2C%22rawMode%22%3Atrue%2C%22status%22%3A%5B%22LIVE%22%2C%22REVIEW%22%5D%2C%22searchAttachments%22%3Atrue%2C%22query%22%3A%22test+machine%22%2C%22collections%22%3A%5B%7B%22uuid%22%3A%228e3caf16-f3cb-b3dd-d403-e5eb8d545fff%22%7D%2C%7B%22uuid%22%3A%228e3caf16-f3cb-b3dd-d403-e5eb8d545ffe%22%7D%2C%7B%22uuid%22%3A%228e3caf16-f3cb-b3dd-d403-e5eb8d545ffg%22%7D%2C%7B%22uuid%22%3A%228e3caf16-f3cb-b3dd-d403-e5eb8d545ffa%22%7D%2C%7B%22uuid%22%3A%228e3caf16-f3cb-b3dd-d403-e5eb8d545ffb%22%7D%5D%2C%22selectedCategories%22%3A%5B%7B%22id%22%3A766943%2C%22categories%22%3A%5B%22Hobart%22%5D%7D%2C%7B%22id%22%3A766944%2C%22categories%22%3A%5B%22Some+cool+things%22%5D%7D%5D%2C%22lastModifiedDateRange%22%3A%7B%22start%22%3A%222020-05-26T03%3A24%3A00.889Z%22%2C%22end%22%3A%222020-05-27T03%3A24%3A00.889Z%22%7D%2C%22owner%22%3A%7B%22id%22%3A%22680f5eb7-22e2-4ab6-bcea-25205165e36e%22%7D%2C%22mimeTypeFilters%22%3A%5B%7B%22id%22%3A%22fe79c485-a6dd-4743-81e8-52de66494632%22%7D%2C%7B%22id%22%3A%22fe79c485-a6dd-4743-81e8-52de66494631%22%7D%5D%7D"
      );
    });

    it("excludes any undefined properties", () => {
      //defaultSearchOptions contains an undefined sortOrder property
      expect(
        generateQueryStringFromSearchOptions(defaultSearchOptions)
      ).not.toContain("sortOrder");
    });
  });
});
