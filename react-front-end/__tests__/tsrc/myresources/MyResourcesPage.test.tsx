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
import * as OEQ from "@openequella/rest-api-client";
import { CurrentUserDetails } from "@openequella/rest-api-client/dist/LegacyContent";
import "@testing-library/jest-dom/extend-expect";
import {
  queryByLabelText,
  queryByText,
  render,
  screen,
} from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import * as A from "fp-ts/Array";
import { pipe } from "fp-ts/function";
import { concatAll } from "fp-ts/Monoid";
import * as N from "fp-ts/number";
import { createMemoryHistory } from "history";
import * as React from "react";
import { Router } from "react-router-dom";
import { getSearchResult } from "../../../__mocks__/SearchResult.mock";
import { getCurrentUserMock } from "../../../__mocks__/UserModule.mock";
import { AppContext } from "../../../tsrc/mainui/App";
import { nonDeletedStatuses } from "../../../tsrc/modules/SearchModule";
import { defaultSearchSettings } from "../../../tsrc/modules/SearchSettingsModule";
import { guestUser } from "../../../tsrc/modules/UserModule";
import MyResourcesPage from "../../../tsrc/myresources/MyResourcesPage";
import type { MyResourcesType } from "../../../tsrc/myresources/MyResourcesPageHelper";
import { defaultSortOrder } from "../../../tsrc/myresources/MyResourcesPageHelper";
import { defaultSearchPageOptions } from "../../../tsrc/search/SearchPageHelper";
import { languageStrings } from "../../../tsrc/util/langstrings";
import { clickSelect, querySelectOption } from "../MuiTestHelpers";
import {
  initialiseEssentialMocks,
  mockCollaborators,
  queryRefineSearchComponent,
  SORTORDER_SELECT_ID,
  waitForSearch,
} from "../search/SearchPageTestHelper";

const history = createMemoryHistory();
const defaultTheme = createTheme({
  props: { MuiWithWidth: { initialWidth: "md" } },
});
const buildMyResourcesSearchPageOptions = (
  status: OEQ.Common.ItemStatus[],
  resourcesType: MyResourcesType
) => ({
  ...defaultSearchPageOptions,
  status,
  owner: getCurrentUserMock,
  sortOrder: defaultSortOrder(resourcesType),
});

const {
  mockCollections,
  mockCurrentUser,
  mockListClassification,
  mockSearch,
  mockSearchSettings,
} = mockCollaborators();
initialiseEssentialMocks({
  mockCollections,
  mockCurrentUser,
  mockListClassification,
  mockSearchSettings,
});
mockSearchSettings.mockResolvedValue({
  ...defaultSearchSettings,
  searchingShowNonLiveCheckbox: true,
});

const searchPromise = mockSearch.mockResolvedValue(getSearchResult);

describe("<MyResourcesPage/>", () => {
  const renderMyResourcesPage = async (
    resourceType: MyResourcesType = "Published",
    currentUser: OEQ.LegacyContent.CurrentUserDetails = getCurrentUserMock
  ) => {
    history.push("/page/myresources", {
      customData: {
        myResourcesType: resourceType,
      },
    });
    const page = render(
      <MuiThemeProvider theme={defaultTheme}>
        <Router history={history}>
          <AppContext.Provider
            value={{
              refreshUser: jest.fn(),
              appErrorHandler: jest.fn(),
              currentUser,
            }}
          >
            <MyResourcesPage updateTemplate={jest.fn()} />
          </AppContext.Provider>
        </Router>
      </MuiThemeProvider>
    );

    await waitForSearch(searchPromise);
    return page;
  };

  describe("supports views of different My resources types", () => {
    it.each<[string, MyResourcesType, OEQ.Common.ItemStatus[]]>([
      ["live", "Published", ["LIVE", "REVIEW"]],
      ["draft", "Drafts", ["DRAFT"]],
      ["personal", "Scrapbook", ["PERSONAL"]],
      ["moderating", "Moderation queue", ["MODERATING", "REJECTED", "REVIEW"]],
      ["archived", "Archive", ["ARCHIVED"]],
      ["all", "All resources", nonDeletedStatuses],
    ])(
      "shows a list of %s Items",
      async (
        _: string,
        resourcesType: MyResourcesType,
        statuses: OEQ.Common.ItemStatus[]
      ) => {
        await renderMyResourcesPage(resourcesType);
        expect(mockSearch).toHaveBeenLastCalledWith({
          ...buildMyResourcesSearchPageOptions(statuses, resourcesType),
          includeAttachments: false,
        });
      }
    );
  });

  describe("Refine search panel", () => {
    it("always displays MyResourcesSelector", async () => {
      const { container } = await renderMyResourcesPage();
      expect(
        queryRefineSearchComponent(container, "MyResourcesSelector")
      ).toBeInTheDocument();
    });

    it("always hides Advanced search selector, Remote search selector and Owner selector", async () => {
      const { container } = await renderMyResourcesPage();
      [
        "AdvancedSearchSelector",
        "RemoteSearchSelector",
        "OwnerSelector",
      ].forEach((componentSuffix) =>
        expect(
          queryRefineSearchComponent(container, componentSuffix)
        ).not.toBeInTheDocument()
      );

      expect.assertions(3);
    });

    it("hides Collection selector for Scrapbook", async () => {
      const { container } = await renderMyResourcesPage(
        "Scrapbook",
        getCurrentUserMock
      );
      expect(
        queryRefineSearchComponent(container, "CollectionSelector")
      ).not.toBeInTheDocument();
    });

    it.each([["Moderation queue"], ["All resources"]])(
      "uses Item status selector in advanced mode for %s even though the selector is not enabled in Search settings",
      async (resourceType: string) => {
        mockSearchSettings.mockResolvedValueOnce({
          ...defaultSearchSettings,
          searchingShowNonLiveCheckbox: false,
        });

        const { container } = await renderMyResourcesPage(
          resourceType as MyResourcesType
        );

        const statusSelector = queryRefineSearchComponent(
          container,
          "StatusSelector"
        );
        expect(statusSelector).toBeInTheDocument();

        // The selector is used in advanced mode so there should be a text input within it.
        expect(
          statusSelector?.querySelector("input[type='text']")
        ).toBeInTheDocument();
      }
    );

    it.each([
      ["Resource type selector", "MIMETypeSelector"],
      ["Date range selector", "DateRangeSelector"],
    ])(
      "displays %s based on Search settings",
      async (_: string, componentSuffix: string) => {
        const { container } = await renderMyResourcesPage();
        expect(
          queryRefineSearchComponent(container, componentSuffix)
        ).toBeInTheDocument();
      }
    );
  });

  describe("Browser history data", () => {
    it("saves currently selected My resources type in the browser history", async () => {
      const resourceType: MyResourcesType = "Archive";
      await renderMyResourcesPage(resourceType);
      expect(history.location.state).toEqual({
        searchPageOptions: buildMyResourcesSearchPageOptions(
          ["ARCHIVED"],
          resourceType
        ),
        customData: {
          myResourcesType: "Archive",
        },
      });
    });
  });

  describe("Access to Scrapbook", () => {
    it.each([
      ["shows", "enabled", getCurrentUserMock, true],
      ["hides", "disabled", guestUser, false],
    ])(
      "%s the option of Scrapbook if access to Scrapbook is %s",
      async (
        _: string,
        status: string,
        user: CurrentUserDetails | undefined,
        expecting: boolean
      ) => {
        const { getByText } = await renderMyResourcesPage("Published", user);

        userEvent.click(
          getByText(languageStrings.myResources.resourceType.published)
        );
        const scrapbookOptionFound = !!screen.queryByText("Scrapbook", {
          selector: "li",
        });

        expect(scrapbookOptionFound).toBe(expecting);
      }
    );
  });

  describe("custom UI for SearchResult", () => {
    const {
      moderating: { since },
      scrapbook: { addScrapbook, createFile, createPage },
    } = languageStrings.myResources;

    it("displays custom UI for Scrapbook and Moderating items in All resources", async () => {
      const { getByText } = await renderMyResourcesPage("All resources");

      // There is one Scrapbook in the search result and an Edit Icon button should be displayed for the Scrapbook.
      const scrapbook = getByText("personal").closest("li");
      if (!scrapbook) {
        throw new Error("Failed to render SearchResult for Scrapbook");
      }
      expect(
        queryByLabelText(scrapbook, languageStrings.common.action.edit)
      ).toBeInTheDocument();

      // There is one moderating Item in the search result so the text of 'Moderating since'
      // should be displayed for this Item.
      const moderatingItem = getByText("moderating").closest("li");
      if (!moderatingItem) {
        throw new Error(
          "Failed to render SearchResult for Items in moderation"
        );
      }
      expect(queryByText(moderatingItem, since)).toBeInTheDocument();
    });

    it("displays custom UI in Scrapbook page", async () => {
      const { getByLabelText } = await renderMyResourcesPage("Scrapbook");

      const addToScrapbookButton = getByLabelText(addScrapbook);
      expect(addToScrapbookButton).toBeInTheDocument();

      userEvent.click(addToScrapbookButton);

      expect(screen.getByText(createPage)).toBeInTheDocument();
      expect(screen.getByText(createFile)).toBeInTheDocument();
    });
  });

  describe("Custom sort orders", () => {
    const sortOptions = languageStrings.myResources.sortOptions;

    const countOptions = (options: string[]) =>
      pipe(
        options,
        A.map(querySelectOption),
        A.filter((r) => r !== null),
        A.size
      );

    it.each<[MyResourcesType, string[]]>([
      [
        "Scrapbook",
        [sortOptions.lastModified, sortOptions.dateCreated, sortOptions.title],
      ],
      [
        "Moderation queue",
        [
          sortOptions.submitted,
          sortOptions.lastAction,
          sortOptions.title,
          sortOptions.lastModified,
          sortOptions.dateCreated,
        ],
      ],
    ])("has custom sort order for %s", async (type, options) => {
      const { container } = await renderMyResourcesPage(type);

      // Click the menu
      clickSelect(container, SORTORDER_SELECT_ID);

      // Check how many of the expected options are now on screen
      const foundOptions = countOptions(options);

      expect(foundOptions).toBe(options.length);
    });

    it("does not display the moderation sort options in non-moderation views", async () => {
      const nonModerationTypes: MyResourcesType[] = [
        "Scrapbook",
        "All resources",
        "Published",
        "Drafts",
        "Archive",
      ];

      const countModOptions = async (
        type: MyResourcesType
      ): Promise<number> => {
        const moderationSortOptions = [
          sortOptions.submitted,
          sortOptions.lastAction,
        ];

        const { container } = await renderMyResourcesPage(type);
        clickSelect(container, SORTORDER_SELECT_ID);
        return countOptions(moderationSortOptions);
      };

      const occurrences = pipe(
        await Promise.all<number>(nonModerationTypes.map(countModOptions)),
        concatAll(N.MonoidSum)
      );

      expect(occurrences).toBe(0);
    });
  });
});
