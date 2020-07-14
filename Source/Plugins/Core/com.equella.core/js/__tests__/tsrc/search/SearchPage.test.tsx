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
} from "../../../__mocks__/getSearchResult";
import * as React from "react";
import SearchPage from "../../../tsrc/search/SearchPage";
import { mount, ReactWrapper } from "enzyme";
import { act } from "react-dom/test-utils";
import * as SearchModule from "../../../tsrc/search/SearchModule";
import * as SearchSettingsModule from "../../../tsrc/settings/Search/SearchSettingsModule";
import {
  defaultSearchSettings,
  SortOrder,
} from "../../../tsrc/settings/Search/SearchSettingsModule";
import { BrowserRouter } from "react-router-dom";
import { CircularProgress } from "@material-ui/core";

const ENTER_KEYCODE = 13;
const SEARCHBAR_ID = "input[id='searchBar']";
const RAW_SEARCH_TOGGLE_ID = "input[id='rawSearch']";
const mockSearch = jest.spyOn(SearchModule, "searchItems");
const mockSearchSettings = jest.spyOn(
  SearchSettingsModule,
  "getSearchSettingsFromServer"
);
const searchSettingPromise = mockSearchSettings.mockImplementation(() =>
  Promise.resolve(defaultSearchSettings)
);
const searchPromise = mockSearch.mockImplementation(() =>
  Promise.resolve(getSearchResult)
);
const defaultSearchOptions = {
  rowsPerPage: 10,
  currentPage: 0,
  sortOrder: SortOrder.RANK,
};

describe("<SearchPage/>", () => {
  let component: ReactWrapper<any, Readonly<{}>, React.Component<{}, {}, any>>;

  beforeEach(async () => {
    component = mount(
      <BrowserRouter>
        <SearchPage updateTemplate={jest.fn()} />{" "}
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
    });
    //Hit Enter and wait for debounce
    await awaitAct(() => {
      input.simulate("keyDown", {
        keyCode: ENTER_KEYCODE,
      });
      jest.advanceTimersByTime(1000);
    });
  };

  it("should retrieve search settings and do a search when the page is opened", () => {
    expect(
      SearchSettingsModule.getSearchSettingsFromServer
    ).toHaveBeenCalledTimes(1);
    expect(SearchModule.searchItems).toHaveBeenCalledTimes(1);
    expect(SearchModule.searchItems).toHaveBeenCalledWith(defaultSearchOptions);
  });

  it("should support debounce query search and display search results", async () => {
    await querySearch("new query");
    // After 1s the second search should be triggered
    expect(SearchModule.searchItems).toHaveBeenCalledTimes(2);
    expect(SearchModule.searchItems).toHaveBeenCalledWith({
      ...defaultSearchOptions,
      query: "new query*",
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
    expect(component.html()).toContain("1-10 of 12");
    const itemsPerPageSelect = component.find(
      ".MuiTablePagination-input input"
    );
    await awaitAct(() =>
      itemsPerPageSelect.simulate("change", { target: { value: 25 } })
    );
    expect(SearchModule.searchItems).toHaveBeenCalledWith({
      ...defaultSearchOptions,
      rowsPerPage: 25,
    });
    expect(component.html()).toContain("1-12 of 12");
  });

  it("should support navigating to previous/next page", async () => {
    const prevPageButton = component
      .find(".MuiTablePagination-actions button")
      .at(0);
    const nextPageButton = component
      .find(".MuiTablePagination-actions button")
      .at(1);
    await awaitAct(() => nextPageButton.simulate("click"));
    expect(component.html()).toContain("11-12 of 12");
    await awaitAct(() => prevPageButton.simulate("click"));
    expect(component.html()).toContain("1-10 of 12");
  });

  it("should support sorting search results", async () => {
    const sortingControl = component.find(".MuiCardHeader-action input");
    await awaitAct(() =>
      sortingControl.simulate("change", {
        target: { value: SortOrder.DATEMODIFIED },
      })
    );
    // Because sorting is done on the server-side and we are using mock data, we can only check if the selected
    // sort order is included in the search params
    expect(SearchModule.searchItems).toHaveBeenCalledWith({
      ...defaultSearchOptions,
      sortOrder: SortOrder.DATEMODIFIED,
    });
  });

  it("should display a spinner when search is in progress", async () => {
    // Trigger a search by changing sorting order
    const sortingControl = component.find(".MuiCardHeader-action input");
    sortingControl.simulate("change", {
      target: { value: SortOrder.DATEMODIFIED },
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
    //assert that the query was passed in as-is
    expect(SearchModule.searchItems).toHaveBeenLastCalledWith({
      ...defaultSearchOptions,
      query: "raw search test",
    });
    /*assert that there were only two calls - initial search and Enter key triggered.
    if the debounce was still on, this would be three calls -
    the initial search, the search triggered by the debounce,
    and the search triggered by the Enter key.*/
    expect(SearchModule.searchItems).toHaveBeenCalledTimes(2);
  });
});
