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
import {
  getEmptySearchResult,
  getSearchResult,
  getSearchResultsCustom,
} from "../../../__mocks__/getSearchResult";
import { getCollectionMap } from "../../../__mocks__/getCollectionsResp";
import * as React from "react";
import SearchPage, { SearchPageOptions } from "../../../tsrc/search/SearchPage";
import { mount, ReactWrapper } from "enzyme";
import { act } from "react-dom/test-utils";
import * as SearchModule from "../../../tsrc/modules/SearchModule";
import * as CollectionsModule from "../../../tsrc/modules/CollectionsModule";
import * as SearchSettingsModule from "../../../tsrc/modules/SearchSettingsModule";
import { BrowserRouter } from "react-router-dom";
import { CircularProgress } from "@material-ui/core";
import { CollectionSelector } from "../../../tsrc/search/components/CollectionSelector";
import { paginatorControls } from "../components/SearchPaginationTestHelper";
import { DateRangeSelector } from "../../../tsrc/components/DateRangeSelector";
import { render, waitFor, screen, fireEvent } from "@testing-library/react";
import "@testing-library/jest-dom/extend-expect";
import { languageStrings } from "../../../tsrc/util/langstrings";

const SEARCHBAR_ID = "input[id='searchBar']";
const RAW_SEARCH_TOGGLE_ID = "input[id='rawSearch']";
const FIRST_PAGE_PAGINATION = "1-10 of 12";
const mockSearch = jest.spyOn(SearchModule, "searchItems");
const mockSearchSettings = jest.spyOn(
  SearchSettingsModule,
  "getSearchSettingsFromServer"
);
const mockCollections = jest.spyOn(CollectionsModule, "collectionListSummary");
const searchSettingPromise = mockSearchSettings.mockImplementation(() =>
  Promise.resolve(SearchSettingsModule.defaultSearchSettings)
);
const searchPromise = mockSearch.mockImplementation(() =>
  Promise.resolve(getSearchResult)
);
mockCollections.mockImplementation(() => Promise.resolve(getCollectionMap));
window.scrollTo = jest.fn();
const defaultSearchPageOptions: SearchPageOptions = {
  ...SearchModule.defaultSearchOptions,
  sortOrder: SearchSettingsModule.SortOrder.RANK,
  dateRangeQuickModeEnabled: true,
};
const defaultCollectionPrivileges = ["SEARCH_COLLECTION"];

describe("<SearchPage/> with react-testing-library", () => {
  let container: HTMLElement = document.createElement("div");
  const renderSearchPage = async () => {
    const { container } = render(
      <BrowserRouter>
        <SearchPage updateTemplate={jest.fn()} />
      </BrowserRouter>
    );
    // When Pagination shows the correct data, the render is completed.
    await waitFor(() =>
      screen.getByText(FIRST_PAGE_PAGINATION, { selector: "p" })
    );
    return container;
  };

  beforeEach(async () => {
    container = await renderSearchPage();
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  it("should clear search options and perform a new search", async () => {
    const query = "clear query";
    const queryBar = container.querySelector("#searchBar");
    if (!queryBar) {
      throw new Error("Failed to locate the search bar, unable to continue.");
    }
    const sortingDropdown = screen.getByDisplayValue(
      SearchSettingsModule.SortOrder.RANK
    );
    const newSearchButton = screen.getByText(
      languageStrings.searchpage.newSearch
    );

    // We will change the debounced query so use fake timer here.
    jest.useFakeTimers("modern");
    // Change search options now.
    fireEvent.change(queryBar, { target: { value: query } });
    await waitFor(() => {
      expect(queryBar).toHaveDisplayValue(query);
      jest.advanceTimersByTime(1000);
    });
    fireEvent.change(sortingDropdown, {
      target: { value: SearchSettingsModule.SortOrder.NAME },
    });

    // Perform a new search and check.
    fireEvent.click(newSearchButton);
    await waitFor(() => {
      expect(sortingDropdown).toHaveValue(SearchSettingsModule.SortOrder.RANK);
      expect(queryBar).toHaveValue("");
    });
    // Four searches have been performed: initial search, one for query change and
    // one for sorting change, and one for clearing.
    expect(SearchModule.searchItems).toHaveBeenCalledTimes(4);
    expect(SearchModule.searchItems).toHaveBeenLastCalledWith(
      defaultSearchPageOptions
    );
  });
});

describe("<SearchPage/>", () => {
  let component: ReactWrapper<any, Readonly<{}>, React.Component<{}, {}, any>>;

  beforeEach(async () => {
    window.history.replaceState({}, "Clean history state");
    component = mount(
      <BrowserRouter>
        <SearchPage updateTemplate={jest.fn()} />
      </BrowserRouter>
    );
    // Wait until Search settings are returned.
    await act(async () => {
      await searchSettingPromise;
    });
    // Wait until the first search is completed.
    await act(async () => {
      await searchPromise;
    });
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  /**
   * Wait for the completion of an asynchronous act.
   * @param update A function that simulates UI behaviours such as selecting a different value from a dropdown.
   */
  const awaitAct = async (update: () => void) =>
    await act(async () => await update());

  /**
   * Do a query search with fake timer.
   * @param searchTerm The specified search term.
   */
  const querySearch = async (searchTerm: string) => {
    jest.useFakeTimers("modern");
    const input = component.find(SEARCHBAR_ID);
    await awaitAct(() => {
      input.simulate("change", { target: { value: searchTerm } });
      jest.advanceTimersByTime(1000);
    });
  };

  /**
   * Do a raw query search with fake timer. Turns on raw search mode, enters a search and hits enter.
   * Waits for the debounce after the enter key.
   * @param searchTerm The specified search term.
   */
  const rawQuerySearch = async (searchTerm: string) => {
    jest.useFakeTimers("modern");
    const input = component.find(SEARCHBAR_ID);
    const rawModeSwitch = component.find(RAW_SEARCH_TOGGLE_ID);
    //turn raw search mode on
    await awaitAct(() =>
      rawModeSwitch.simulate("change", { target: { checked: true } })
    );
    //add the searchTerm
    await awaitAct(() => {
      input.simulate("change", { target: { value: searchTerm } });
      jest.advanceTimersByTime(1000);
    });
  };

  it("should retrieve search settings and collections, and do a search when the page is opened", () => {
    expect(
      SearchSettingsModule.getSearchSettingsFromServer
    ).toHaveBeenCalledTimes(1);
    expect(SearchModule.searchItems).toHaveBeenCalledTimes(1);
    expect(SearchModule.searchItems).toHaveBeenCalledWith(
      defaultSearchPageOptions
    );
    expect(CollectionsModule.collectionListSummary).toHaveBeenCalledTimes(1);
    expect(CollectionsModule.collectionListSummary).toHaveBeenCalledWith(
      defaultCollectionPrivileges
    );
  });

  it("should support debounce query search and display search results", async () => {
    await querySearch("new query");
    // After 1s the second search should be triggered. (The first being the initial component mount.)
    expect(SearchModule.searchItems).toHaveBeenCalledTimes(2);
    expect(SearchModule.searchItems).toHaveBeenCalledWith({
      ...defaultSearchPageOptions,
      query: "new query",
    });
    expect(component.html()).not.toContain("No results found.");
    expect(component.html()).toContain("266bb0ff-a730-4658-aec0-c68bbefc227c");
  });

  it("should display 'No results found.' when there are no search results", async () => {
    mockSearch.mockImplementationOnce(() =>
      Promise.resolve(getEmptySearchResult)
    );
    await querySearch("no items");
    expect(component.html()).toContain("No results found.");
  });

  it("should support changing the number of items displayed per page", async () => {
    // Initial items per page is 10
    const { pageCount } = paginatorControls(component);
    expect(pageCount.text()).toContain("1-10 of 12");
    const itemsPerPageSelect = component.find(
      ".MuiTablePagination-input input"
    );
    await awaitAct(() =>
      itemsPerPageSelect.simulate("change", { target: { value: 25 } })
    );
    expect(SearchModule.searchItems).toHaveBeenCalledWith({
      ...defaultSearchPageOptions,
      rowsPerPage: 25,
    });
    expect(pageCount.text()).toContain("1-12 of 12");
  });

  it("should support navigating to previous/next page", async () => {
    await querySearch("");
    component.update();
    const { nextPageButton, pageCount, previousPageButton } = paginatorControls(
      component
    );
    await awaitAct(() => nextPageButton.simulate("click"));
    expect(pageCount.text()).toContain("11-12 of 12");
    await querySearch("");
    component.update();
    await awaitAct(() => previousPageButton.simulate("click"));
    expect(pageCount.text()).toContain("1-10 of 12");
  });

  it("should support navigating to first/last page of results", async () => {
    mockSearch.mockImplementation(() =>
      Promise.resolve(getSearchResultsCustom(30))
    );
    await querySearch("");
    component.update();
    const { firstPageButton, lastPageButton, pageCount } = paginatorControls(
      component
    );
    expect(pageCount.text()).toContain("1-10 of 30");

    await awaitAct(() => lastPageButton.simulate("click"));
    component.update();
    expect(pageCount.text()).toContain("21-30 of 30");
    await querySearch("");
    component.update();
    await awaitAct(() => firstPageButton.simulate("click"));
    expect(component.html()).toContain("1-10 of 30");
  });

  it("should support sorting search results", async () => {
    const sortingControl = component.find(".MuiCardHeader-action input");
    await awaitAct(() =>
      sortingControl.simulate("change", {
        target: { value: SearchSettingsModule.SortOrder.DATEMODIFIED },
      })
    );
    // Because sorting is done on the server-side and we are using mock data, we can only check if the selected
    // sort order is included in the search params
    expect(SearchModule.searchItems).toHaveBeenCalledWith({
      ...defaultSearchPageOptions,
      sortOrder: SearchSettingsModule.SortOrder.DATEMODIFIED,
    });
  });

  it("should display a spinner when search is in progress", async () => {
    // Trigger a search by changing sorting order
    const sortingControl = component.find(".MuiCardHeader-action input");
    sortingControl.simulate("change", {
      target: { value: SearchSettingsModule.SortOrder.DATEMODIFIED },
    });
    expect(component.find(CircularProgress)).toHaveLength(1);
    await act(async () => {
      await searchPromise;
    });
    component.update();
    expect(component.find(CircularProgress)).toHaveLength(0);
  });

  it("should not debounce and send query as-is when in raw search mode", async () => {
    await rawQuerySearch("raw search test");
    // assert that the query was passed in as-is
    expect(SearchModule.searchItems).toHaveBeenLastCalledWith({
      ...defaultSearchPageOptions,
      rawMode: true,
      query: "raw search test",
    });
    // There should be three calls:
    // 1. The initial call on component mount
    // 2. Switching to raw mode
    // 3. Typing a query, and hitting enter
    expect(SearchModule.searchItems).toHaveBeenCalledTimes(3);
  });

  it("should filter search results by collections", async () => {
    const selectedCollections = [
      {
        uuid: "8e3caf16-f3cb-b3dd-d403-e5eb8d545fff",
        name: "DRM Test Collection",
      },
      {
        uuid: "8e3caf16-f3cb-b3dd-d403-e5eb8d545ffe",
        name: "Generic Testing Collection",
      },
    ];
    component.update();
    const collectionSelector = component.find(CollectionSelector);
    const handleCollectionChange: (
      collections: CollectionsModule.Collection[]
    ) => void = collectionSelector.prop("onSelectionChange");
    await awaitAct(() => handleCollectionChange(selectedCollections));
    expect(SearchModule.searchItems).toHaveBeenCalledTimes(2);
    expect(SearchModule.searchItems).toHaveBeenCalledWith({
      ...defaultSearchPageOptions,
      collections: selectedCollections,
    });
  });

  it("should support selecting a date range through Quick options", async () => {
    const quickOptions = component.find("#date_range_selector input");
    await awaitAct(() =>
      quickOptions.simulate("change", { target: { value: "Today" } })
    );
    component.update();
    const dateRangeSelector = component.find(DateRangeSelector);
    expect(SearchModule.searchItems).toHaveBeenCalledWith({
      ...defaultSearchPageOptions,
      lastModifiedDateRange: {
        start: dateRangeSelector.prop("dateRange")!.start,
        end: undefined,
      },
    });
  });
});
