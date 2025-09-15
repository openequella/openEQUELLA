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
import { getSearchResult } from "../../../__mocks__/SearchResult.mock";
import {
  defaultFavSearchesSearchOptions,
  expandQueryWithBookmarkTags,
  searchFavouriteItems,
  searchFavouriteSearches,
} from "../../../tsrc/modules/FavouriteModule";
import {
  defaultSearchOptions,
  SearchOptions,
} from "../../../tsrc/modules/SearchModule";
import { getFavouriteSearchesResp } from "../../../__mocks__/getFavouriteSearchesResp";

jest.mock("@openequella/rest-api-client", () => {
  const restModule: typeof OEQ = jest.requireActual(
    "@openequella/rest-api-client",
  );
  return {
    ...restModule,
    Search: {
      search: jest.fn(),
    },
    Favourite: {
      searchFavouriteSearches: jest.fn(),
    },
  };
});

const mockedItemsSearch = (
  OEQ.Search.search as jest.Mock<
    Promise<OEQ.Search.SearchResult<OEQ.Search.SearchResultItem>>
  >
).mockResolvedValue(getSearchResult);

const mockedFavSearchesSearch = (
  OEQ.Favourite.searchFavouriteSearches as jest.Mock<
    Promise<OEQ.Search.SearchResult<OEQ.Favourite.FavouriteSearch>>
  >
).mockResolvedValue(getFavouriteSearchesResp);

describe("searchFavouriteItems", () => {
  beforeEach(() => {
    mockedItemsSearch.mockClear();
  });

  const getMockedItemsSearchParams = (): SearchOptions => {
    expect(mockedItemsSearch).toHaveBeenCalledTimes(1);
    return mockedItemsSearch.mock.calls[0][1]; // Second parameter of the call is the 'params'
  };

  const expectSearchQueryToBe = (expected?: string) => {
    expect(getMockedItemsSearchParams().query).toEqual(expected);
  };

  it("does not contain a query when the search term is whitespace only", async () => {
    await searchFavouriteItems({
      ...defaultSearchOptions,
      query: "   ",
      rawMode: true,
    });

    expectSearchQueryToBe(undefined);
  });

  it("appends a bookmark_tags query for a non-empty, raw mode search term", async () => {
    const queryTerm = "favItem";
    await searchFavouriteItems({
      ...defaultSearchOptions,
      query: queryTerm,
      rawMode: true,
    });

    const query = expandQueryWithBookmarkTags(queryTerm);
    expectSearchQueryToBe(query);
  });

  it("appends a wildcard and a bookmark_tags query for a non-empty, non-raw mode search term", async () => {
    const queryTerm = "non RAW";
    await searchFavouriteItems({
      ...defaultSearchOptions,
      query: queryTerm,
    });
    const query = expandQueryWithBookmarkTags(`${queryTerm}*`);
    expectSearchQueryToBe(query);
  });
});

describe("searchFavouriteSearches", () => {
  beforeEach(() => {
    mockedFavSearchesSearch.mockClear();
  });

  const expectSearchQueryToBe = (expected?: string) => {
    expect(mockedFavSearchesSearch).toHaveBeenCalledTimes(1);
    const params = mockedFavSearchesSearch.mock.calls[0][1];
    expect(params.query).toEqual(expected);
  };

  it("provides a list of favourite searches", async () => {
    const searchResult = await searchFavouriteSearches(
      defaultFavSearchesSearchOptions,
    );
    expect(searchResult.available).toBe(10);
    expect(searchResult.results).toHaveLength(10);
  });

  it("does not contain a query when the search term is whitespace only", async () => {
    await searchFavouriteSearches({
      ...defaultFavSearchesSearchOptions,
      query: "   ",
    });
    expectSearchQueryToBe(undefined);
  });

  it("removes leading and trailing whitespace from query string", async () => {
    await searchFavouriteSearches({
      ...defaultFavSearchesSearchOptions,
      query: " fav search   ",
    });
    expectSearchQueryToBe("fav search");
  });
});
