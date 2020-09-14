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
import { CircularProgress } from "@material-ui/core";
import * as OEQ from "@openequella/rest-api-client";
import "@testing-library/jest-dom/extend-expect";
import {
  fireEvent,
  getByText,
  render,
  RenderResult,
  screen,
  waitFor,
} from "@testing-library/react";
import { mount, ReactWrapper } from "enzyme";
import * as React from "react";
import { act } from "react-dom/test-utils";
import { BrowserRouter } from "react-router-dom";
import { getCollectionMap } from "../../../__mocks__/getCollectionsResp";
import {
  getEmptySearchResult,
  getSearchResult,
  getSearchResultsCustom,
} from "../../../__mocks__/getSearchResult";
import * as UserSearchMock from "../../../__mocks__/UserSearch.mock";
import { DateRangeSelector } from "../../../tsrc/components/DateRangeSelector";
import * as CollectionsModule from "../../../tsrc/modules/CollectionsModule";
import {
  liveStatuses,
  nonLiveStatuses,
} from "../../../tsrc/modules/SearchModule";
import * as SearchModule from "../../../tsrc/modules/SearchModule";
import * as SearchSettingsModule from "../../../tsrc/modules/SearchSettingsModule";
import * as UserModule from "../../../tsrc/modules/UserModule";
import { CollectionSelector } from "../../../tsrc/search/components/CollectionSelector";
import SearchPage, { SearchPageOptions } from "../../../tsrc/search/SearchPage";
import { languageStrings } from "../../../tsrc/util/langstrings";
import { paginatorControls } from "../components/SearchPaginationTestHelper";
import {
  clearSelection,
  selectUser,
} from "./components/OwnerSelectTestHelpers";
import {
  queryCollectionSelector,
  queryDateRangeSelector,
  queryOwnerSelector,
  getRefineSearchComponent,
  querySearchAttachmentsSelector,
  queryStatusSelector,
} from "./SearchPageHelper";

const SEARCHBAR_ID = "input[id='searchBar']";
const RAW_SEARCH_TOGGLE_ID = "input[id='rawSearch']";

const mockCollections = jest.spyOn(CollectionsModule, "collectionListSummary");
const mockListUsers = jest.spyOn(UserModule, "listUsers");
const mockSearch = jest.spyOn(SearchModule, "searchItems");
const mockSearchSettings = jest.spyOn(
  SearchSettingsModule,
  "getSearchSettingsFromServer"
);
window.scrollTo = jest.fn();
const searchSettingPromise = mockSearchSettings.mockResolvedValue(
  SearchSettingsModule.defaultSearchSettings
);
const searchPromise = mockSearch.mockResolvedValue(getSearchResult);
mockCollections.mockResolvedValue(getCollectionMap);
mockListUsers.mockResolvedValue(UserSearchMock.users);

const defaultSearchPageOptions: SearchPageOptions = {
  ...SearchModule.defaultSearchOptions,
  sortOrder: SearchSettingsModule.SortOrder.RANK,
  dateRangeQuickModeEnabled: true,
};
const defaultCollectionPrivileges = ["SEARCH_COLLECTION"];

/**
 * Simple helper to wrap the process of waiting for the execution of a search based on checking the
 * `searchPromise`. Being that it is abstracted out, in the future could change as needed to be
 * something other than the `searchPromise`.
 */
const waitForSearch = async () =>
  await act(async () => {
    await searchPromise;
  });

/**
 * Helper function for the initial render of the `<SearchPage>` for tests below. Also includes
 * the wait for the initial search call.
 *
 * @returns The RenderResult from the `render` of the `<SearchPage>`
 */
const renderSearchPage = async (): Promise<RenderResult> => {
  window.history.replaceState({}, "Clean history state");
  const page = render(
    <BrowserRouter>
      <SearchPage updateTemplate={jest.fn()} />{" "}
    </BrowserRouter>
  );
  // Wait for the first completion of initial search
  await waitForSearch();

  return page;
};

/**
 * Helper function to unmount current Search page and re-render Search page.
 * @param page Current Search page.
 */
const reRenderSearchPage = async (page: RenderResult) => {
  page.unmount();
  return await renderSearchPage();
};

describe("Refine search by searching attachments", () => {
  let page: RenderResult;

  beforeEach(async () => {
    page = await renderSearchPage();
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  const getSearchAttachmentsSelector = (container: Element): HTMLElement =>
    getRefineSearchComponent(container, "SearchAttachmentsSelector");
  const changeOption = (selector: HTMLElement, option: string) =>
    fireEvent.click(getByText(selector, option));
  const { yes: yesLabel, no: noLabel } = languageStrings.common.action;

  it("Should default to searching attachments", async () => {
    expect(mockSearch).toHaveBeenLastCalledWith(defaultSearchPageOptions);
  });

  it("Should not search attachments if No is selected", async () => {
    changeOption(getSearchAttachmentsSelector(page.container), noLabel);
    await waitForSearch();
    expect(mockSearch).toHaveBeenLastCalledWith({
      ...defaultSearchPageOptions,
      searchAttachments: false,
    });
  });

  it("Should search attachments if Yes is selected", async () => {
    changeOption(getSearchAttachmentsSelector(page.container), yesLabel);
    await waitForSearch();
    expect(mockSearch).toHaveBeenLastCalledWith(defaultSearchPageOptions);
  });
});

describe("Refine search by status", () => {
  const {
    live: liveButtonLabel,
    all: allButtonLabel,
  } = languageStrings.searchpage.statusSelector;

  const expectSearchItemsCalledWithStatus = (status: OEQ.Common.ItemStatus[]) =>
    expect(mockSearch).toHaveBeenLastCalledWith({
      ...defaultSearchPageOptions,
      status: status,
    });

  const getStatusSelector = (container: Element): HTMLElement =>
    getRefineSearchComponent(container, "StatusSelector");

  const selectStatus = (container: Element, status: string) =>
    fireEvent.click(getByText(getStatusSelector(container), status));

  beforeEach(() => {
    // Status selector is disabled by default so enable it before test.
    searchSettingPromise.mockResolvedValueOnce({
      ...SearchSettingsModule.defaultSearchSettings,
      searchingShowNonLiveCheckbox: true,
    });
  });

  afterEach(() => {
    // Needed to keep Enzyme tests below happy
    jest.clearAllMocks();
  });

  it("Should default to LIVE statuses", async () => {
    await renderSearchPage();
    expectSearchItemsCalledWithStatus(liveStatuses);
  });

  it("Should search for items of all statuses if ALL is clicked", async () => {
    const page = await renderSearchPage();
    selectStatus(page.container, allButtonLabel);
    await waitForSearch();
    expectSearchItemsCalledWithStatus(liveStatuses.concat(nonLiveStatuses));
  });

  it("Should search for items of 'live' statuses if LIVE is clicked", async () => {
    const page = await renderSearchPage();
    selectStatus(page.container, liveButtonLabel);
    await waitForSearch();
    expectSearchItemsCalledWithStatus(liveStatuses);
  });
});

describe("Refine search by Owner", () => {
  const testUser = UserSearchMock.users[0];
  let page: RenderResult;

  beforeEach(async () => {
    page = await renderSearchPage();
  });

  afterEach(() => {
    // Needed to keep Enzyme tests below happy
    jest.clearAllMocks();
  });

  const getOwnerSelector = (container: Element): HTMLElement =>
    getRefineSearchComponent(container, "OwnerSelector");

  const confirmSelectedUser = (username: string) => screen.getByText(username);

  const confirmSelectedUserCleared = (username: string) => {
    let stillPresent = true;
    try {
      confirmSelectedUser(username);
    } catch {
      stillPresent = false;
    }
    if (stillPresent) {
      throw new Error("Can still see the username: " + username);
    }
  };

  const _selectUser = async (container: HTMLElement, username: string) => {
    await selectUser(getOwnerSelector(container), username);
    // The selected user will now be displayed
    await waitFor(() => confirmSelectedUser(username));
  };

  it("should be possible to set the owner", async () => {
    await _selectUser(page.container, testUser.username);

    expect(SearchModule.searchItems).toHaveBeenCalledWith({
      ...defaultSearchPageOptions,
      owner: testUser,
    });
  });

  it("should be possible to clear the owner filter", async () => {
    await _selectUser(page.container, testUser.username);

    // Now clear the selection
    clearSelection();
    await waitFor(() => confirmSelectedUserCleared(testUser.username));

    expect(SearchModule.searchItems).toHaveBeenCalledWith(
      defaultSearchPageOptions
    );
  });
});

describe("Collapsible refine filter section", () => {
  let page: RenderResult;
  beforeEach(async () => {
    page = await renderSearchPage();
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  it("Should contain the correct controls", async () => {
    const pageContainer = page.container;
    const collapsibleSections = page.container.getElementsByClassName(
      "collapsibleRefinePanel"
    );
    expect(collapsibleSections).toHaveLength(1);

    const collapsibleSection = collapsibleSections.item(0);

    expect(collapsibleSection).toContainElement(
      queryOwnerSelector(pageContainer)
    );
    expect(collapsibleSection).toContainElement(
      queryDateRangeSelector(pageContainer)
    );
    expect(collapsibleSection).toContainElement(
      querySearchAttachmentsSelector(pageContainer)
    );
    expect(collapsibleSection).not.toContainElement(
      queryCollectionSelector(pageContainer)
    );
  });

  it("Should change button text when clicked", async () => {
    const expansionButton = page.getByText(
      languageStrings.common.action.showMore
    );
    fireEvent.click(expansionButton);
    expect(expansionButton).toHaveTextContent(
      languageStrings.common.action.showLess
    );
  });
});

describe("Hide Refine Search controls", () => {
  let page: RenderResult;
  beforeEach(async () => {
    page = await renderSearchPage();
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  const _queryOwnerSelector = () => queryOwnerSelector(page.container);
  const _queryDateSelector = () => queryDateRangeSelector(page.container);
  const _queryStatusSelector = () => queryStatusSelector(page.container);
  const disableDateSelector = {
    ...SearchSettingsModule.defaultSearchSettings,
    searchingDisableDateModifiedFilter: true,
  };
  const disableOwnerSelector = {
    ...SearchSettingsModule.defaultSearchSettings,
    searchingDisableOwnerFilter: true,
  };
  const enableStatusSelector = {
    ...SearchSettingsModule.defaultSearchSettings,
    searchingShowNonLiveCheckbox: true,
  };
  it.each([
    // Reuse default Search settings as disableStatusSelector, enableOwnerSelector and enableDateSelector.
    [
      "Owner Selector",
      _queryOwnerSelector,
      disableOwnerSelector,
      SearchSettingsModule.defaultSearchSettings,
    ],
    [
      "Date Selector",
      _queryDateSelector,
      disableDateSelector,
      SearchSettingsModule.defaultSearchSettings,
    ],
    [
      "Status Selector",
      _queryStatusSelector,
      SearchSettingsModule.defaultSearchSettings,
      enableStatusSelector,
    ],
  ])(
    "should be possible to disable %s",
    async (
      testName: string,
      getSelector: () => HTMLElement | null,
      disableSelector: SearchSettingsModule.SearchSettings,
      enableSelector: SearchSettingsModule.SearchSettings
    ) => {
      // Explicitly enable selectors.
      searchSettingPromise.mockResolvedValueOnce(enableSelector);
      page = await reRenderSearchPage(page);
      expect(getSelector()).toBeInTheDocument();
      // Now disable them and re-render the page.
      searchSettingPromise.mockResolvedValueOnce(disableSelector);
      page = await reRenderSearchPage(page);
      // They should disappear.
      expect(getSelector()).toBeNull();
    }
  );
});

describe("<SearchPage/> with react-testing-library", () => {
  let page: RenderResult;
  beforeEach(async () => {
    page = await renderSearchPage();
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  it("should clear search options and perform a new search", async () => {
    const query = "clear query";
    const queryBar = page.container.querySelector("#searchBar");
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
    component.update();
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
