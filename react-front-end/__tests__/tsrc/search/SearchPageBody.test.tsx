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
import { MuiThemeProvider } from "@material-ui/core";
import { createTheme } from "@material-ui/core/styles";
import userEvent from "@testing-library/user-event";
import { createMemoryHistory } from "history";
import * as React from "react";
import { render, screen } from "@testing-library/react";
import { Router } from "react-router-dom";
import { customRefinePanelControl } from "../../../__mocks__/RefinePanelControl.mock";
import { defaultSearchSettings } from "../../../tsrc/modules/SearchSettingsModule";
import { SearchContext } from "../../../tsrc/search/Search";
import {
  SearchPageBody,
  SearchPageBodyProps,
} from "../../../tsrc/search/SearchPageBody";
import "@testing-library/jest-dom/extend-expect";
import {
  defaultSearchPageHeaderConfig,
  defaultSearchPageOptions,
  defaultSearchPageRefinePanelConfig,
  SearchPageOptions,
} from "../../../tsrc/search/SearchPageHelper";
import { languageStrings } from "../../../tsrc/util/langstrings";
import {
  queryCollectionSelector,
  queryRefineSearchComponent,
} from "./SearchPageTestHelper";

const defaultSearchPageBodyProps: SearchPageBodyProps = {
  pathname: "/page/search",
};

// Refine panel is hidden in small screens, so we mock the screen size to make it bigger.
const defaultTheme = createTheme({
  props: { MuiWithWidth: { initialWidth: "md" } },
});

describe("<SearchPageBody />", () => {
  const history = createMemoryHistory();
  const search = jest.fn();

  const renderSearchPageBody = (
    props: SearchPageBodyProps = defaultSearchPageBodyProps
  ) => {
    return render(
      <MuiThemeProvider theme={defaultTheme}>
        <Router history={history}>
          <SearchContext.Provider
            value={{
              search,
              searchState: {
                status: "initialising",
                options: defaultSearchPageOptions,
              },
              searchSettings: {
                core: defaultSearchSettings,
                mimeTypeFilters: [],
                advancedSearches: [],
              },
              searchPageErrorHandler: jest.fn(),
            }}
          >
            <SearchPageBody {...props} />
          </SearchContext.Provider>
        </Router>
      </MuiThemeProvider>
    );
  };

  it("supports additional panels", () => {
    const label = "additional Panel";
    const { queryByLabelText } = renderSearchPageBody({
      ...defaultSearchPageBodyProps,
      additionalPanels: [<div aria-label={label} />],
    });

    expect(queryByLabelText(label)).toBeInTheDocument();
  });

  it("supports additional headers", () => {
    const text = "additional button";
    const { queryByText } = renderSearchPageBody({
      ...defaultSearchPageBodyProps,
      additionalPanels: [<button>{text}</button>],
    });

    expect(queryByText(text, { selector: "button" })).toBeInTheDocument();
  });

  it("controls the visibility of Refine search filters", () => {
    // Because each filter is controlled in the same way, we use CollectionSelector as the testing target.
    const { container } = renderSearchPageBody({
      ...defaultSearchPageBodyProps,
      refinePanelConfig: {
        ...defaultSearchPageRefinePanelConfig,
        // Do not display CollectionSelector.
        enableCollectionSelector: false,
      },
    });

    expect(queryCollectionSelector(container)).not.toBeInTheDocument();
  });

  it("supports custom sorting options", () => {
    const option = "custom option";
    const { container } = renderSearchPageBody({
      ...defaultSearchPageBodyProps,
      headerConfig: {
        customSortingOptions: new Map([["RANK", option]]),
      },
    });

    const sortingDropdown = container.querySelector("#sort-order-select");
    if (!sortingDropdown) {
      throw new Error("Failed to find the Sorting selector");
    }
    userEvent.click(sortingDropdown);
    expect(screen.queryByText(option)).toBeInTheDocument();
  });

  it("supports displaying custom Refine panel controls", () => {
    const { container } = renderSearchPageBody({
      ...defaultSearchPageBodyProps,
      refinePanelConfig: {
        ...defaultSearchPageRefinePanelConfig,
        customRefinePanelControl: [customRefinePanelControl],
      },
    });

    expect(
      queryRefineSearchComponent(container, customRefinePanelControl.idSuffix)
    ).toBeInTheDocument();
  });

  it("supports custom new search criteria", () => {
    const path = "/test";
    const criteria: SearchPageOptions = {
      ...defaultSearchPageOptions,
      query: "test",
      sortOrder: "RANK",
      externalMimeTypes: undefined,
    };

    const { getByText } = renderSearchPageBody({
      ...defaultSearchPageBodyProps,
      headerConfig: {
        ...defaultSearchPageHeaderConfig,
        newSearchConfig: {
          path,
          criteria,
        },
      },
    });

    const newSearchButton = getByText(languageStrings.searchpage.newSearch);
    userEvent.click(newSearchButton);

    // The first parameter should be the custom new search criteria and the new path
    // should have been pushed the history.
    expect(search.mock.calls[0][0]).toStrictEqual(criteria);
    expect(history.location.pathname).toBe(path);
  });
});
