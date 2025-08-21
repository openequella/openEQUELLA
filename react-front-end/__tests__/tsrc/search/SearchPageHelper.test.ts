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
import { createBrowserHistory } from "history";
import { getCollectionMap } from "../../../__mocks__/getCollectionsResp";
import { getMimeTypeFilters } from "../../../__mocks__/MimeTypeFilter.mock";
import {
  allSearchPageOptions,
  basicSearchPageOptions,
} from "../../../__mocks__/searchOptions.mock";
import * as UserModuleMock from "../../../__mocks__/UserModule.mock";
import * as CollectionsModule from "../../../tsrc/modules/CollectionsModule";
import * as SearchFilterSettingsModule from "../../../tsrc/modules/SearchFilterSettingsModule";
import type { SearchOptions } from "../../../tsrc/modules/SearchModule";
import * as UserModule from "../../../tsrc/modules/UserModule";
import {
  buildOpenSummaryPageHandler,
  defaultSearchPageOptions,
  generateQueryStringFromSearchPageOptions,
  generateSearchPageOptionsFromLocation,
  legacyQueryStringToSearchPageOptions,
  newSearchQueryToSearchPageOptions,
  processLegacyAdvSearchCriteria,
  SearchPageOptions,
} from "../../../tsrc/search/SearchPageHelper";
import type { DateRange } from "../../../tsrc/util/Date";
import { updateMockGetBaseUrl } from "../BaseUrlHelper";
import { basicRenderData, updateMockGetRenderData } from "../RenderDataHelper";

describe("newSearchQueryToSearchOptions", () => {
  const mockedResolvedUser = jest.spyOn(UserModule, "resolveUsers");
  const mockedCollectionListSummary = jest.spyOn(
    CollectionsModule,
    "collectionListSummary",
  );
  const mockGetMimeTypeFiltersFromServer = jest.spyOn(
    SearchFilterSettingsModule,
    "getMimeTypeFiltersFromServer",
  );

  beforeEach(() => {
    mockedResolvedUser.mockResolvedValue([UserModuleMock.users[0]]);
    mockedCollectionListSummary.mockResolvedValueOnce(getCollectionMap);
    mockGetMimeTypeFiltersFromServer.mockResolvedValueOnce(getMimeTypeFilters);
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  it("should convert query string to searchOptions", async () => {
    const longSearch =
      '{"rowsPerPage":10,"currentPage":0,"sortOrder":"name","query":"test machine","rawMode":true,"status":["LIVE","REVIEW"],"searchAttachments":true,"selectedCategories":[{"id":766943,"categories":["Hobart"]},{"id":766944,"categories":["Some cool things"]}],"collections":[{"uuid":"8e3caf16-f3cb-b3dd-d403-e5eb8d545fff"},{"uuid":"8e3caf16-f3cb-b3dd-d403-e5eb8d545ffe"},{"uuid":"8e3caf16-f3cb-b3dd-d403-e5eb8d545ffg"},{"uuid":"8e3caf16-f3cb-b3dd-d403-e5eb8d545ffa"},{"uuid":"8e3caf16-f3cb-b3dd-d403-e5eb8d545ffb"}],"lastModifiedDateRange":{"start":"2020-05-26T03:24:00.889Z","end":"2020-05-27T03:24:00.889Z"},"owner":{"id":"680f5eb7-22e2-4ab6-bcea-25205165e36e"}, "mimeTypeFilters": [{"id":"fe79c485-a6dd-4743-81e8-52de66494632"},{"id":"fe79c485-a6dd-4743-81e8-52de66494631"}],"advFieldValue": [[{"schemaNode": ["/controls/editbox"], "type": "editbox", "isValueTokenised": true}, ["hello world"]]] }';
    const convertedParamsPromise =
      await newSearchQueryToSearchPageOptions(longSearch);
    expect(convertedParamsPromise).toEqual(allSearchPageOptions);
  });

  const emptyEndDateQueryString =
    '{"lastModifiedDateRange":{"start":"2020-05-26T03:24:00.889Z"}}';
  const expectedEmptyEndDateSearchOptions: SearchOptions = {
    ...basicSearchPageOptions,
    lastModifiedDateRange: {
      start: new Date("2020-05-26T13:24:00.889+10:00"),
      end: undefined,
    },
  };

  const emptyStartDateQueryString =
    '{"lastModifiedDateRange":{"end":"2020-05-27T03:24:00.889Z"}}';
  const expectedEmptyStartDateSearchOptions: SearchOptions = {
    ...basicSearchPageOptions,
    lastModifiedDateRange: {
      start: undefined,
      end: new Date("2020-05-27T13:24:00.889+10:00"),
    },
  };

  const fullDateQueryString =
    '{"lastModifiedDateRange":{"start":"2020-05-26T03:24:00.889Z","end":"2020-05-27T03:24:00.889Z"}}';
  const expectedFullDateSearchOptions: SearchOptions = {
    ...basicSearchPageOptions,
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
    ["no end date", emptyEndDateQueryString, expectedEmptyEndDateSearchOptions],
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
      expectedSearchOptions: SearchOptions,
    ) => {
      expect(await newSearchQueryToSearchPageOptions(queryString)).toEqual(
        expectedSearchOptions,
      );
    },
  );

  it("should be able to convert SearchOptions to a query string, and back to SearchOptions again", async () => {
    const queryStringFromSearchOptions =
      await generateQueryStringFromSearchPageOptions(allSearchPageOptions);
    expect(
      await newSearchQueryToSearchPageOptions(
        new URLSearchParams(queryStringFromSearchOptions).get(
          "searchOptions",
        ) ?? "",
      ),
    ).toEqual(allSearchPageOptions);
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
    const convertedParamsPromise =
      await generateSearchPageOptionsFromLocation(mockLocation);
    expect(convertedParamsPromise).toBeUndefined();
  });
});

describe("legacyQueryStringToSearchOptions", () => {
  const mockedResolvedUser = jest.spyOn(UserModule, "resolveUsers");
  const mockedCollectionListSummary = jest.spyOn(
    CollectionsModule,
    "collectionListSummary",
  );
  jest
    .spyOn(SearchFilterSettingsModule, "getMimeTypeFiltersFromServer")
    .mockResolvedValue(getMimeTypeFilters);
  const defaultConvertedSearchOptions = {
    ...defaultSearchPageOptions,
    rawMode: true,
  };

  it("should return default search for any parameters that aren't supported", async () => {
    const unsupportedQueryString = "?test=nothing&cool=beans";
    expect(
      await legacyQueryStringToSearchPageOptions(
        new URLSearchParams(unsupportedQueryString),
      ),
    ).toEqual(defaultConvertedSearchOptions);
  });

  it("should convert legacy search parameters to searchOptions", async () => {
    mockedResolvedUser.mockResolvedValue([UserModuleMock.users[0]]);
    mockedCollectionListSummary.mockResolvedValue(getCollectionMap);

    //Query string was obtained from legacy UI searching.do->Share URL
    const fullQueryString =
      "?in=C8e3caf16-f3cb-b3dd-d403-e5eb8d545fff&q=test&sort=datecreated&owner=680f5eb7-22e2-4ab6-bcea-25205165e36e&dp=1601510400000&dr=AFTER&doc=<xml><editbox>box</editbox></xml>";

    const convertedParamsPromise = await legacyQueryStringToSearchPageOptions(
      new URLSearchParams(fullQueryString),
    );

    const expectedSearchOptions: SearchPageOptions = {
      ...defaultConvertedSearchOptions,
      sortOrder: "datecreated",
      searchAttachments: true,
      collections: [
        {
          uuid: "8e3caf16-f3cb-b3dd-d403-e5eb8d545fff",
          name: "DRM Test Collection",
        },
      ],
      query: "test",
      owner: {
        id: "f9ec8b09-cf64-44ff-8a0a-08a8f2f9272a",
        username: "user200",
        firstName: "Racheal",
        lastName: "Carlyle",
      },
      lastModifiedDateRange: { start: new Date("2020-10-01T00:00:00.000Z") },
      legacyAdvSearchCriteria: new Map([["/editbox", ["box"]]]),
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
      const convertedSearchOptions = await legacyQueryStringToSearchPageOptions(
        new URLSearchParams(queryString),
      );
      expect(convertedSearchOptions).toEqual({
        ...defaultConvertedSearchOptions,
        lastModifiedDateRange: expectedRange,
      });
    },
  );

  it("should return default search options when collectionId and userId do not exist", async () => {
    mockedResolvedUser.mockResolvedValue([]);
    mockedCollectionListSummary.mockResolvedValue(getCollectionMap);
    const collectionQueryString = "?in=Cunknowncollection&owner=unknown";
    const convertedSearchOptions = await legacyQueryStringToSearchPageOptions(
      new URLSearchParams(collectionQueryString),
    );
    expect(convertedSearchOptions).toEqual(defaultConvertedSearchOptions);
  });

  it("throws an error when unknown display mode is provided", async () => {
    await expect(
      legacyQueryStringToSearchPageOptions(new URLSearchParams("?type=test")),
    ).rejects.toThrow();
  });
});

describe("generateQueryStringFromSearchPageOptions", () => {
  it("converts all searchOptions to a url encoded json string", () => {
    expect(generateQueryStringFromSearchPageOptions(allSearchPageOptions)).toBe(
      "searchOptions=%7B%22rowsPerPage%22%3A10%2C%22currentPage%22%3A0%2C%22sortOrder%22%3A%22name%22%2C%22rawMode%22%3Atrue%2C%22status%22%3A%5B%22LIVE%22%2C%22REVIEW%22%5D%2C%22searchAttachments%22%3Atrue%2C%22query%22%3A%22test+machine%22%2C%22collections%22%3A%5B%7B%22uuid%22%3A%228e3caf16-f3cb-b3dd-d403-e5eb8d545ffg%22%7D%2C%7B%22uuid%22%3A%228e3caf16-f3cb-b3dd-d403-e5eb8d545ffa%22%7D%2C%7B%22uuid%22%3A%228e3caf16-f3cb-b3dd-d403-e5eb8d545fff%22%7D%2C%7B%22uuid%22%3A%228e3caf16-f3cb-b3dd-d403-e5eb8d545ffe%22%7D%2C%7B%22uuid%22%3A%228e3caf16-f3cb-b3dd-d403-e5eb8d545ffb%22%7D%5D%2C%22selectedCategories%22%3A%5B%7B%22id%22%3A766943%2C%22categories%22%3A%5B%22Hobart%22%5D%7D%2C%7B%22id%22%3A766944%2C%22categories%22%3A%5B%22Some+cool+things%22%5D%7D%5D%2C%22lastModifiedDateRange%22%3A%7B%22start%22%3A%222020-05-26T03%3A24%3A00.889Z%22%2C%22end%22%3A%222020-05-27T03%3A24%3A00.889Z%22%7D%2C%22owner%22%3A%7B%22id%22%3A%22f9ec8b09-cf64-44ff-8a0a-08a8f2f9272a%22%7D%2C%22mimeTypeFilters%22%3A%5B%7B%22id%22%3A%22fe79c485-a6dd-4743-81e8-52de66494632%22%7D%2C%7B%22id%22%3A%22fe79c485-a6dd-4743-81e8-52de66494631%22%7D%5D%2C%22displayMode%22%3A%22list%22%2C%22dateRangeQuickModeEnabled%22%3Atrue%2C%22advFieldValue%22%3A%5B%5B%7B%22schemaNode%22%3A%5B%22%2Fcontrols%2Feditbox%22%5D%2C%22type%22%3A%22editbox%22%2C%22isValueTokenised%22%3Atrue%7D%2C%5B%22hello+world%22%5D%5D%5D%7D",
    );
  });

  it.each([["advancedSearchCriteria"], ["mimeTypes"]])(
    "always skips field %s",
    (field: string) => {
      expect(
        generateQueryStringFromSearchPageOptions(defaultSearchPageOptions),
      ).not.toContain(field);
    },
  );

  it("excludes any undefined properties", () => {
    //defaultSearchOptions contains an undefined sortOrder property
    expect(
      generateQueryStringFromSearchPageOptions(defaultSearchPageOptions),
    ).not.toContain("sortOrder");
  });
});

describe("builderOpenSummaryPageHandler", () => {
  const UUID = "369c92fa-ae59-4845-957d-8fcaa22c15e3";
  const VERSION = 1;
  const history = createBrowserHistory();

  it("build URLS and onClick handlers for normal pages", () => {
    const { url } = buildOpenSummaryPageHandler(UUID, VERSION, history);
    expect(url).toBe("/items/369c92fa-ae59-4845-957d-8fcaa22c15e3/1/");
  });

  it("build URLS and onClick handlers for Selection Session", () => {
    updateMockGetBaseUrl();
    updateMockGetRenderData(basicRenderData);
    const { url } = buildOpenSummaryPageHandler(UUID, VERSION, history);
    expect(url).toBe(
      "http://localhost:8080/vanilla/items/369c92fa-ae59-4845-957d-8fcaa22c15e3/1/?_sl.stateId=1&a=coursesearch",
    );
  });
});

describe("processLegacyAdvSearchCriteria", () => {
  it("converts a XML string into a PathValueMap", () => {
    const pathValueMap = processLegacyAdvSearchCriteria(
      "<xml><name>hello</name></xml>",
    );
    expect(pathValueMap).toStrictEqual(new Map([["/name", ["hello"]]]));
  });

  it("supports one path which has multiple values", () => {
    const pathValueMap = processLegacyAdvSearchCriteria(
      "<xml><name>hello</name><name>world</name></xml>",
    );
    expect(pathValueMap).toStrictEqual(
      new Map([["/name", ["hello", "world"]]]),
    );
  });

  it("drops path which does not have values", () => {
    const pathValueMap = processLegacyAdvSearchCriteria(
      "<xml><country>aus</country><city></city><town/></xml>",
    );
    expect(pathValueMap).toStrictEqual(new Map([["/country", ["aus"]]]));
  });

  it("supports a Schema node that targets to an attribute", () => {
    const pathValueMap = processLegacyAdvSearchCriteria(
      "<xml><country population='100'><city size='small'>Hobart</city><city size='small'>Darwin</city><city size='medium'>Canberra</city></country></xml>",
    );
    expect(pathValueMap).toStrictEqual(
      new Map([
        ["/country/city", ["Hobart", "Darwin", "Canberra"]],
        ["/country/city/@size", ["small", "medium"]],
        ["/country/@population", ["100"]],
      ]),
    );
  });
});
