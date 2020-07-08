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
import { getSearchResult } from "../../../__mocks__/getSearchResult";
import { createMount } from "@material-ui/core/test-utils";
import * as React from "react";
import SearchPage from "../../../tsrc/search/SearchPage";
import { ReactWrapper } from "enzyme";
import { act } from "react-dom/test-utils";
import * as SearchModule from "../../../tsrc/search/SearchModule";
import * as SearchSettingsModule from "../../../tsrc/settings/Search/SearchSettingsModule";
import { defaultSearchSettings } from "../../../tsrc/settings/Search/SearchSettingsModule";
import { BrowserRouter } from "react-router-dom";
import { CircularProgress } from "@material-ui/core";

jest.useFakeTimers();
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

describe("<SearchPage/>", () => {
  let mount: ReturnType<typeof createMount>;
  let component: ReactWrapper<any, Readonly<{}>, React.Component<{}, {}, any>>;

  beforeEach(async () => {
    mount = createMount();
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
    mount.cleanUp();
  });

  const changeQuery = (query: string) => {
    act(() => {
      const input = component.find("input.MuiInputBase-input");
      input.simulate("change", { target: { value: query } });
    });
  };

  it("should retrieve search settings and do a search when the page is opened", async () => {
    expect(
      SearchSettingsModule.getSearchSettingsFromServer
    ).toHaveBeenCalledTimes(1);
    expect(SearchModule.searchItems).toHaveBeenCalledTimes(1);
  });

  it("should display 'No results found.' when there are no search results", () => {
    changeQuery("old title");
    //use a timed callback to wait for the debounce before asserting results have populated the page
    setTimeout(() => {
      expect(component.html()).toContain("No results found.");
    }, 1000);
  });

  it("should contain the test data after a search bar text change and render", () => {
    changeQuery("new title");
    setTimeout(() => {
      expect(component.html()).not.toContain("No results found.");
      expect(component.html()).toContain(
        "266bb0ff-a730-4658-aec0-c68bbefc227c"
      );
    }, 1000);
  });

  it("should display a spinner when search is in progress", async () => {
    // Trigger a search by changing search query.
    changeQuery("new query");
    setTimeout(async () => {
      expect(component.find(CircularProgress)).toHaveLength(1);
    }, 1000);
    await act(async () => {
      await searchPromise;
    });
    expect(component.find(CircularProgress)).toHaveLength(0);
  });
});
