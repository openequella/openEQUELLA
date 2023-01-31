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
import { act, render } from "@testing-library/react";
import { createMemoryHistory } from "history";
import * as React from "react";
import { Router } from "react-router-dom";
import { getSearchResult } from "../../../__mocks__/SearchResult.mock";
import { Search } from "../../../tsrc/search/Search";
import {
  defaultSearchPageOptions,
  SearchPageOptions,
} from "../../../tsrc/search/SearchPageHelper";
import {
  initialiseEssentialMocks,
  mockCollaborators,
} from "./SearchPageTestHelper";

const {
  mockGetAdvancedSearchesFromServer,
  mockCollections,
  mockConvertParamsToSearchOptions,
  mockCurrentUser,
  mockListClassification,
  mockMimeTypeFilters,
  mockSearchSettings,
  mockSearch,
} = mockCollaborators();
initialiseEssentialMocks({
  mockCollections,
  mockCurrentUser,
  mockListClassification,
  mockSearchSettings,
});
const searchPromise = mockSearch.mockResolvedValue(getSearchResult);

const renderSearch = async (queryString?: string) => {
  const history = createMemoryHistory();
  if (queryString) history.push(queryString);

  const page = render(
    <Router history={history}>
      <Search updateTemplate={jest.fn()}>
        <div />
      </Search>
    </Router>
  );

  await act(async () => {
    await searchPromise;
  });

  return page;
};

describe("performing general tasks", () => {
  beforeEach(async () => {
    await renderSearch();
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  it("retrieves Search settings", () => {
    expect(mockSearchSettings).toHaveBeenCalledTimes(1);
  });

  it("retrieves MIME type filters", () => {
    expect(mockMimeTypeFilters).toHaveBeenCalledTimes(1);
  });

  it("retrieves advanced searches", () => {
    expect(mockGetAdvancedSearchesFromServer).toHaveBeenCalledTimes(1);
  });

  it("performs the initial search", async () => {
    expect(mockSearch).toHaveBeenCalledTimes(1);
  });
});

describe("conversion of parameters to SearchPageOptions", () => {
  const searchPageOptions: SearchPageOptions = {
    ...defaultSearchPageOptions,
    dateRangeQuickModeEnabled: false,
  };

  it("converts query strings to SearchPageOptions", async () => {
    mockConvertParamsToSearchOptions.mockResolvedValueOnce(searchPageOptions);

    await renderSearch("?q=test");
    expect(mockConvertParamsToSearchOptions).toHaveBeenCalledTimes(1);
  });
});
