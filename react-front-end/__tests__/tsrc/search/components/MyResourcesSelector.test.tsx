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
import { CurrentUserDetails } from "@openequella/rest-api-client/dist/LegacyContent";
import userEvent from "@testing-library/user-event";
import * as React from "react";
import { render, screen } from "@testing-library/react";
import { getCurrentUserMock } from "../../../../__mocks__/UserModule.mock";
import { AppContext } from "../../../../tsrc/mainui/App";
import { guestUser } from "../../../../tsrc/modules/UserModule";
import { MyResourcesSelector } from "../../../../tsrc/search/components/MyResourcesSelector";
import { MyResourcesPageContext } from "../../../../tsrc/search/MyResourcesPage";
import { MyResourcesType } from "../../../../tsrc/search/MyResourcesPageHelper";
import { SearchContext } from "../../../../tsrc/search/Search";
import { defaultSearchPageOptions } from "../../../../tsrc/search/SearchPageHelper";

describe("<MyResourcesSelector />", () => {
  const onChange = jest.fn();
  const search = jest.fn();
  const renderMyResourcesSelector = (
    currentUser: OEQ.LegacyContent.CurrentUserDetails = guestUser
  ) =>
    render(
      <AppContext.Provider
        value={{
          refreshUser: jest.fn(),
          appErrorHandler: jest.fn(),
          currentUser,
        }}
      >
        <MyResourcesPageContext.Provider value={{ onChange }}>
          <SearchContext.Provider
            value={{
              search,
              searchState: {
                status: "initialising",
                options: defaultSearchPageOptions,
              },
              searchSettings: {
                core: undefined,
                mimeTypeFilters: [],
                advancedSearches: [],
              },
              searchPageErrorHandler: jest.fn(),
            }}
          >
            <MyResourcesSelector value="Published" />
          </SearchContext.Provider>
        </MyResourcesPageContext.Provider>
        );
      </AppContext.Provider>
    );

  const selectResourceType = (resourceType: MyResourcesType) => {
    const { getByLabelText } = renderMyResourcesSelector();
    userEvent.click(getByLabelText("Open"));
    userEvent.click(screen.getByText(resourceType, { selector: "li" }));
  };

  afterEach(() => {
    jest.clearAllMocks();
  });

  it.each([
    ["shows", "enabled", getCurrentUserMock, true],
    ["hides", "disabled", undefined, false],
  ])(
    "%s the option of Scrapbook if access to Scrapbook is %s",
    (
      _: string,
      status: string,
      user: CurrentUserDetails | undefined,
      expecting: boolean
    ) => {
      const { getByLabelText } = renderMyResourcesSelector(user);

      // Click the caret to show the options.
      userEvent.click(getByLabelText("Open"));
      const scrapbookOptionFound = !!screen.queryByText("Scrapbook", {
        selector: "li",
      });

      expect(scrapbookOptionFound).toBe(expecting);
    }
  );

  it("updates the selected My resources type by calling function 'onChange' provided by MyResourcesPageContext", () => {
    selectResourceType("Drafts");
    expect(onChange).toHaveBeenCalledTimes(1);
  });

  it("performs a search with the Item statuses converted from the selected My resources type", () => {
    selectResourceType("Drafts");
    expect(search).toHaveBeenCalledTimes(1);
    expect(search).toHaveBeenLastCalledWith({
      ...defaultSearchPageOptions,
      status: ["DRAFT"],
    });
  });
});
