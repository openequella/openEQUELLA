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
import type { SelectedCategories } from "../../../tsrc/modules/SearchFacetsModule";
import * as SearchModule from "../../../tsrc/modules/SearchModule";

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

  it("should return undefined if no categories are selected", () => {
    expect(SearchModule.generateCategoryWhereQuery(undefined)).toBeUndefined();
    expect(SearchModule.generateCategoryWhereQuery([])).toBeUndefined();
  });
});
