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
import { ThemeProvider } from "@mui/material";
import { createTheme } from "@mui/material/styles";
import "@testing-library/jest-dom";
import { act, render, type RenderResult, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { createMemoryHistory } from "history";
import * as React from "react";
import { Router } from "react-router-dom";
import { classifications } from "../../../__mocks__/CategorySelector.mock";
import { getCollectionMap } from "../../../__mocks__/getCollectionsResp";
import { createMatchMedia } from "../../../__mocks__/MockUseMediaQuery";
import { customRefinePanelControl } from "../../../__mocks__/RefinePanelControl.mock";
import { getRemoteSearchesFromServerResult } from "../../../__mocks__/RemoteSearchModule.mock";
import {
  getEmptySearchResult,
  getSearchResult,
} from "../../../__mocks__/SearchResult.mock";
import * as CollectionsModule from "../../../tsrc/modules/CollectionsModule";
import * as RemoteSearchModule from "../../../tsrc/modules/RemoteSearchModule";
import { defaultSearchSettings } from "../../../tsrc/modules/SearchSettingsModule";
import {
  SearchPageBody,
  SearchPageBodyProps,
} from "../../../tsrc/search/SearchPageBody";
import {
  defaultSearchPageHeaderConfig,
  defaultSearchPageOptions,
  defaultSearchPageRefinePanelConfig,
  SearchContext,
  SearchPageOptions,
} from "../../../tsrc/search/SearchPageHelper";
import {
  SearchPageSearchResult,
  State,
} from "../../../tsrc/search/SearchPageReducer";
import { languageStrings } from "../../../tsrc/util/langstrings";
import {
  queryCollectionSelector,
  queryRefineSearchComponent,
  SORTORDER_SELECT_ID,
} from "./SearchPageTestHelper";

const remoteSearchesPromise = jest
  .spyOn(RemoteSearchModule, "getRemoteSearchesFromServer")
  .mockResolvedValue(getRemoteSearchesFromServerResult);
const collectionPromise = jest
  .spyOn(CollectionsModule, "collectionListSummary")
  .mockResolvedValue(getCollectionMap);

const defaultSearchPageBodyProps: SearchPageBodyProps = {
  pathname: "/page/search",
};

const mockSearch = jest.fn();
const mockHistory = createMemoryHistory();

const {
  showAdvancedSearchFilter: advancedSearchButtonLabel,
  wildcardSearch: wildcardSearchText,
} = languageStrings.searchpage;

const advancedSearchFilter = {
  onClick: jest.fn(),
  accent: false,
};

describe("<SearchPageBody />", () => {
  const advancedSearchButtonLocator =
    (roleSelector: RenderResult["queryByRole"] | RenderResult["getByRole"]) =>
    () =>
      roleSelector("button", { name: advancedSearchButtonLabel });

  const renderSearchPageBodyWithContext = async (
    props: SearchPageBodyProps,
    state: State = {
      status: "initialising",
      options: defaultSearchPageOptions,
    },
  ) => {
    window.matchMedia = createMatchMedia(1280);

    const page = render(
      <Router history={mockHistory}>
        <SearchContext.Provider
          value={{
            search: mockSearch,
            searchState: state,
            searchSettings: {
              core: defaultSearchSettings,
              mimeTypeFilters: [],
              advancedSearches: [],
            },
            searchPageErrorHandler: jest.fn(),
          }}
        >
          <ThemeProvider theme={createTheme()}>
            <SearchPageBody {...props} />
          </ThemeProvider>
        </SearchContext.Provider>
      </Router>,
    );

    await act(async () => {
      await remoteSearchesPromise;
      await collectionPromise;
    });

    return page;
  };

  const renderSearchPageBody = async (
    props: SearchPageBodyProps = defaultSearchPageBodyProps,
  ) => {
    return await renderSearchPageBodyWithContext(props);
  };

  it("supports additional panels", async () => {
    const label = "additional Panel";
    const { queryByLabelText } = await renderSearchPageBody({
      ...defaultSearchPageBodyProps,
      additionalPanels: [<div aria-label={label} />],
    });

    expect(queryByLabelText(label)).toBeInTheDocument();
  });

  it("supports additional headers", async () => {
    const text = "additional button";
    const { queryByText } = await renderSearchPageBody({
      ...defaultSearchPageBodyProps,
      additionalPanels: [<button>{text}</button>],
    });

    expect(queryByText(text, { selector: "button" })).toBeInTheDocument();
  });

  it("controls the visibility of Refine search filters", async () => {
    // Because each filter is controlled in the same way, we use CollectionSelector as the testing target.
    const { container } = await renderSearchPageBody({
      ...defaultSearchPageBodyProps,
      refinePanelConfig: {
        ...defaultSearchPageRefinePanelConfig,
        // Do not display CollectionSelector.
        enableCollectionSelector: false,
      },
    });

    expect(queryCollectionSelector(container)).not.toBeInTheDocument();
  });

  it("supports custom sorting options", async () => {
    const option = "custom option";
    const { container } = await renderSearchPageBody({
      ...defaultSearchPageBodyProps,
      headerConfig: {
        customSortingOptions: new Map([["rank", option]]),
      },
    });

    const sortingDropdown = container.querySelector(SORTORDER_SELECT_ID);
    if (!sortingDropdown) {
      throw new Error("Failed to find the Sorting selector");
    }
    await userEvent.click(sortingDropdown);
    expect(screen.queryByText(option)).toBeInTheDocument();
  });

  it("supports displaying custom Refine panel controls", async () => {
    const { container } = await renderSearchPageBody({
      ...defaultSearchPageBodyProps,
      refinePanelConfig: {
        ...defaultSearchPageRefinePanelConfig,
        customRefinePanelControl: [customRefinePanelControl],
      },
    });

    expect(
      queryRefineSearchComponent(container, customRefinePanelControl.idSuffix),
    ).toBeInTheDocument();
  });

  it("supports custom new search configuration", async () => {
    const path = "/test";
    const criteria: SearchPageOptions = {
      ...defaultSearchPageOptions,
      query: "test",
      sortOrder: "rank",
      externalMimeTypes: undefined,
    };
    const callback = jest.fn();

    const searchPageBodyProps: SearchPageBodyProps = {
      ...defaultSearchPageBodyProps,
      headerConfig: {
        ...defaultSearchPageHeaderConfig,
        newSearchConfig: {
          navigationTo: {
            path,
            selectionSessionPathBuilder: () => "",
          },
          criteria,
          callback,
        },
      },
    };

    const { getByText } =
      await renderSearchPageBodyWithContext(searchPageBodyProps);

    const newSearchButton = getByText(languageStrings.searchpage.newSearch);
    await userEvent.click(newSearchButton);

    // The first parameter should be the custom new search criteria and the new path
    // should have been pushed the history.
    expect(mockSearch.mock.calls[0][0]).toStrictEqual(criteria);
    expect(mockHistory.location.pathname).toBe(path);
    expect(callback).toHaveBeenCalledTimes(1);
  });

  it("supports custom control over whether to show the spinner", async () => {
    const state: State = {
      status: "success",
      options: defaultSearchPageOptions,
      result: {
        from: "item-search",
        content: {
          start: 0,
          length: 1,
          available: 0,
          results: [],
          highlight: [],
        },
      },
      classifications: [],
    };

    const { queryByRole } = await renderSearchPageBodyWithContext(
      {
        ...defaultSearchPageBodyProps,
        customShowSpinner: true,
      },
      state,
    );

    // The spinner should still be displayed although the state status is "success".
    expect(queryByRole("progressbar")).toBeInTheDocument();
  });

  it("displays custom UI for search results", async () => {
    const text = "This is a custom search result";
    const searchPageBodyProps: SearchPageBodyProps = {
      ...defaultSearchPageBodyProps,
      customRenderSearchResults: (searchResult: SearchPageSearchResult) =>
        searchResult.content.results.map((_, index) => (
          <p key={index}>{text}</p>
        )),
    };

    const { getAllByText } = await renderSearchPageBodyWithContext(
      searchPageBodyProps,
      {
        status: "success",
        options: defaultSearchPageOptions,
        result: {
          from: "item-search",
          content: getSearchResult,
        },
        classifications,
      },
    );

    // The search result list has 12 Items, so we should see that text 12 times.
    expect(getAllByText(text, { selector: "p" })).toHaveLength(12);
  });

  it("displays 'No results found.' when there are no search results", async () => {
    const { container } = await renderSearchPageBodyWithContext(
      defaultSearchPageBodyProps,
      {
        status: "success",
        options: defaultSearchPageOptions,
        result: {
          from: "item-search",
          content: getEmptySearchResult,
        },
        classifications,
      },
    );

    expect(container).toHaveTextContent(
      languageStrings.searchpage.noResultsFound,
    );
  });

  it("shows wildcard search toggle and hides advanced search filter button by default", async () => {
    const { getByText, queryByRole } = await renderSearchPageBody({
      ...defaultSearchPageBodyProps,
    });
    const queryAdvancedSearchButton = advancedSearchButtonLocator(queryByRole);

    expect(queryAdvancedSearchButton()).not.toBeInTheDocument();
    expect(getByText(wildcardSearchText)).toBeInTheDocument();
  });

  it("shows advanced search filter button when configured", async () => {
    const { getByRole } = await renderSearchPageBody({
      ...defaultSearchPageBodyProps,
      searchBarConfig: {
        advancedSearchFilter: { ...advancedSearchFilter },
      },
    });
    const getAdvancedSearchButton = advancedSearchButtonLocator(getByRole);

    expect(getAdvancedSearchButton()).toBeInTheDocument();
  });

  it("can hide the wildcard search toggle when specified", async () => {
    const { queryByText } = await renderSearchPageBody({
      ...defaultSearchPageBodyProps,
      searchBarConfig: {
        enableWildcardToggle: false,
      },
    });

    expect(queryByText(wildcardSearchText)).not.toBeInTheDocument();
  });

  it("can hide the wildcard search toggle while still showing the advanced search filter button", async () => {
    const { queryByText, getByRole } = await renderSearchPageBody({
      ...defaultSearchPageBodyProps,
      searchBarConfig: {
        advancedSearchFilter: { ...advancedSearchFilter },
        enableWildcardToggle: false,
      },
    });
    const getAdvancedSearchButton = advancedSearchButtonLocator(getByRole);

    expect(getAdvancedSearchButton()).toBeInTheDocument();
    expect(queryByText(wildcardSearchText)).not.toBeInTheDocument();
  });
});
