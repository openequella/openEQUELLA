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
import * as OEQ from "@openequella/rest-api-client";
import { CurrentUserDetails } from "@openequella/rest-api-client/dist/LegacyContent";
import "@testing-library/jest-dom";
import {
  getByLabelText,
  getByText,
  queryByLabelText,
  queryByText,
  render,
  screen,
} from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import * as A from "fp-ts/Array";
import { pipe } from "fp-ts/function";
import * as O from "fp-ts/Option";
import * as TE from "fp-ts/TaskEither";
import { createMemoryHistory } from "history";
import * as React from "react";
import { Router } from "react-router-dom";
import { createMatchMedia } from "../../../__mocks__/MockUseMediaQuery";
import {
  getModerationItemsSearchResult,
  getScrapbookItemSearchResult,
  getSearchResult,
  IMAGE_SCRAPBOOK,
  WEBPAGE_SCRAPBOOK,
  webpageScrapbook,
  ZIP_SCRAPBOOK,
} from "../../../__mocks__/SearchResult.mock";
import * as UserModuleMock from "../../../__mocks__/UserModule.mock";
import { getCurrentUserMock } from "../../../__mocks__/UserModule.mock";
import { AppContext } from "../../../tsrc/mainui/App";
import type { FavouriteURL } from "../../../tsrc/modules/FavouriteModule";
import * as ScrapbookModule from "../../../tsrc/modules/ScrapbookModule";
import {
  nonDeletedStatuses,
  SearchOptions,
} from "../../../tsrc/modules/SearchModule";
import { defaultSearchSettings } from "../../../tsrc/modules/SearchSettingsModule";
import * as UserModule from "../../../tsrc/modules/UserModule";
import { guestUser } from "../../../tsrc/modules/UserModule";
import type { ViewerConfig } from "../../../tsrc/modules/ViewerModule";
import MyResourcesPage from "../../../tsrc/myresources/MyResourcesPage";
import type { MyResourcesType } from "../../../tsrc/myresources/MyResourcesPageHelper";
import * as MyResourcesPageHelper from "../../../tsrc/myresources/MyResourcesPageHelper";
import {
  defaultSortOrder,
  PARAM_MYRESOURCES_TYPE,
} from "../../../tsrc/myresources/MyResourcesPageHelper";
import { defaultSearchPageOptions } from "../../../tsrc/search/SearchPageHelper";
import { languageStrings } from "../../../tsrc/util/langstrings";
import { clickSelect, querySelectOption } from "../MuiTestHelpers";
import {
  addSearchToFavourites,
  initialiseEssentialMocks,
  mockCollaborators,
  queryRefineSearchComponent,
  SORTORDER_SELECT_ID,
  waitForSearchCompleted,
} from "../search/SearchPageTestHelper";

const { title: addKeyResourceText } = languageStrings.searchpage.addToHierarchy;

const history = createMemoryHistory();
const buildMyResourcesSearchPageOptions = (
  status: OEQ.Common.ItemStatus[],
  resourcesType: MyResourcesType,
) => ({
  ...defaultSearchPageOptions,
  status,
  owner: getCurrentUserMock,
  sortOrder: defaultSortOrder(resourcesType),
});

const {
  mockAddFavouriteSearch,
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
const user = userEvent.setup();
describe("<MyResourcesPage/>", () => {
  // Provides increased control over the state of the rendered page compared to
  // `renderMyResourcesPage`. A key use if is you want to control the Location to
  // influence how the page is rendered.
  const renderMyResourcesPageWithUser = async (
    currentUser: OEQ.LegacyContent.CurrentUserDetails,
  ) => {
    window.matchMedia = createMatchMedia(1280);

    const page = render(
      <ThemeProvider theme={createTheme()}>
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
      </ThemeProvider>,
    );

    await waitForSearchCompleted();

    return page;
  };

  const renderMyResourcesPage = async (
    resourceType: MyResourcesType = "Published",
    currentUser: OEQ.LegacyContent.CurrentUserDetails = getCurrentUserMock,
  ) => {
    history.push("/page/myresources", {
      customData: {
        myResourcesType: resourceType,
      },
    });

    return await renderMyResourcesPageWithUser(currentUser);
  };

  // Should only be used when 'getSearchResult' is used to mock the search result because
  // there is only one Scrapbook in this mocked result.
  const getScrapbook = (container: HTMLElement) => {
    const scrapbook = getByText(container, "personal").closest("li");
    if (!scrapbook) {
      throw new Error("Failed to render SearchResult for Scrapbook");
    }

    return scrapbook;
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
        statuses: OEQ.Common.ItemStatus[],
      ) => {
        await renderMyResourcesPage(resourcesType);
        expect(mockSearch).toHaveBeenLastCalledWith({
          ...buildMyResourcesSearchPageOptions(statuses, resourcesType),
          includeAttachments: false,
        });
      },
    );
  });

  describe("Refine search panel", () => {
    it("always displays MyResourcesSelector", async () => {
      const { container } = await renderMyResourcesPage();
      expect(
        queryRefineSearchComponent(container, "MyResourcesSelector"),
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
          queryRefineSearchComponent(container, componentSuffix),
        ).not.toBeInTheDocument(),
      );

      expect.assertions(3);
    });

    it("hides Collection selector for Scrapbook", async () => {
      const { container } = await renderMyResourcesPage(
        "Scrapbook",
        getCurrentUserMock,
      );
      expect(
        queryRefineSearchComponent(container, "CollectionSelector"),
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
          resourceType as MyResourcesType,
        );

        const statusSelector = queryRefineSearchComponent(
          container,
          "StatusSelector",
        );
        expect(statusSelector).toBeInTheDocument();

        // The selector is used in advanced mode so there should be a text input within it.
        expect(
          statusSelector?.querySelector("input[type='text']"),
        ).toBeInTheDocument();
      },
    );

    it.each([
      ["Resource type selector", "MIMETypeSelector"],
      ["Date range selector", "DateRangeSelector"],
    ])(
      "displays %s based on Search settings",
      async (_: string, componentSuffix: string) => {
        const { container } = await renderMyResourcesPage();
        expect(
          queryRefineSearchComponent(container, componentSuffix),
        ).toBeInTheDocument();
      },
    );
  });

  describe("Browser history data", () => {
    it("saves currently selected My resources type in the browser history", async () => {
      const resourceType: MyResourcesType = "Archive";
      await renderMyResourcesPage(resourceType);
      expect(history.location.state).toEqual({
        searchPageOptions: buildMyResourcesSearchPageOptions(
          ["ARCHIVED"],
          resourceType,
        ),
        customData: {
          myResourcesType: "Archive",
        },
      });
    });
  });

  describe("Support for Scrapbook", () => {
    const renderWithViewerConfiguration = async (
      viewerConfig: ViewerConfig,
    ) => {
      jest
        .spyOn(MyResourcesPageHelper, "getScrapbookViewerConfig")
        .mockReturnValueOnce(
          TE.right<string, O.Option<ViewerConfig>>(O.of(viewerConfig)),
        );
      searchPromise.mockResolvedValueOnce(getScrapbookItemSearchResult());
      return await renderMyResourcesPage("Scrapbook");
    };

    it.each([
      ["shows", "enabled", getCurrentUserMock, true],
      ["hides", "disabled", guestUser, false],
    ])(
      "%s the option of Scrapbook if access to Scrapbook is %s",
      async (
        _: string,
        status: string,
        userDetails: CurrentUserDetails | undefined,
        expecting: boolean,
      ) => {
        const { getByText } = await renderMyResourcesPage(
          "Published",
          userDetails,
        );

        await user.click(
          getByText(languageStrings.myResources.resourceType.published),
        );
        const scrapbookOptionFound = !!screen.queryByText("Scrapbook", {
          selector: "li",
        });

        expect(scrapbookOptionFound).toBe(expecting);
      },
    );

    it("supports editing a Scrapbook", async () => {
      const openLegacyFileEditingPage = jest
        .spyOn(ScrapbookModule, "openLegacyFileEditingPage")
        .mockImplementationOnce(() => Promise.resolve());

      const { container } = await renderMyResourcesPage("Scrapbook");
      const editIcon = getByLabelText(
        getScrapbook(container),
        languageStrings.common.action.edit,
      );

      await user.click(editIcon);
      expect(openLegacyFileEditingPage).toHaveBeenCalledTimes(1);
    });

    it("supports deleting a Scrapbook", async () => {
      const deleteScrapbook = jest
        .spyOn(ScrapbookModule, "deleteScrapbook")
        .mockImplementationOnce(() => Promise.resolve());

      const { container } = await renderMyResourcesPage("Scrapbook");
      const binButton = getByLabelText(
        getScrapbook(container),
        languageStrings.common.action.delete,
      );

      // Click the bin icon should show a dialog which has an 'OK' button.
      await user.click(binButton);
      const dialog = screen.getByRole("dialog");
      const okButton = getByText(dialog, languageStrings.common.action.ok);

      await user.click(okButton);

      expect(deleteScrapbook).toHaveBeenCalledTimes(1);
    });

    it("supports viewing an image Scrapbook in the Lightbox", async () => {
      const { getByText, queryByLabelText } =
        await renderWithViewerConfiguration({
          viewerType: "lightbox",
          config: {
            src: "", // src doesn't matter in the test.
            mimeType: "image/jpeg",
          },
        });

      await user.click(getByText(IMAGE_SCRAPBOOK, { selector: "a" }));

      // Confirm that the lightbox has now been displayed - with the unique element being
      // the lightbox's 'embed code' button.
      expect(
        queryByLabelText(languageStrings.embedCode.copy),
      ).toBeInTheDocument();

      // Access to summary page is disabled.
      expect(
        queryByLabelText(languageStrings.lightboxComponent.openSummaryPage),
      ).not.toBeInTheDocument();
    });

    it.each([
      ["non-image file", "http://download", ZIP_SCRAPBOOK],
      [
        "webpage",
        `items/${webpageScrapbook.uuid}/${webpageScrapbook.version}/viewpages.jsp`,
        WEBPAGE_SCRAPBOOK,
      ],
    ])(
      "opens a new tab to view a %s Scrapbook",
      async (_: string, url: string, scrapbookTitle: string) => {
        const mockWindowOpen = jest
          .spyOn(window, "open")
          .mockReturnValueOnce(global.window);

        const { getByText } = await renderWithViewerConfiguration({
          viewerType: "link",
          url,
        });

        await user.click(getByText(scrapbookTitle, { selector: "a" }));

        expect(mockWindowOpen).toHaveBeenLastCalledWith(url, "_blank");
      },
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
        queryByLabelText(scrapbook, languageStrings.common.action.edit),
      ).toBeInTheDocument();

      // There is one moderating Item in the search result so the text of 'Moderating since'
      // should be displayed for this Item.
      const moderatingItem = getByText("moderating").closest("li");
      if (!moderatingItem) {
        throw new Error(
          "Failed to render SearchResult for Items in moderation",
        );
      }
      expect(queryByText(moderatingItem, since)).toBeInTheDocument();
    });

    it("displays custom UI in Scrapbook page", async () => {
      const { getByLabelText } = await renderMyResourcesPage("Scrapbook");

      const addToScrapbookButton = getByLabelText(addScrapbook);
      expect(addToScrapbookButton).toBeInTheDocument();

      await user.click(addToScrapbookButton);

      expect(screen.getByText(createPage)).toBeInTheDocument();
      expect(screen.getByText(createFile)).toBeInTheDocument();
    });

    it("disables add to hierarchy button for Scrapbook page", async () => {
      const { queryAllByLabelText } = await renderMyResourcesPage("Scrapbook");

      expect(queryAllByLabelText(addKeyResourceText)).toHaveLength(0);
    });

    it("disables the button for sharing a search", async () => {
      const { queryByLabelText } = await renderMyResourcesPage();

      expect(
        queryByLabelText(languageStrings.searchpage.shareSearchHelperText),
      ).not.toBeInTheDocument();
    });
  });

  describe("Custom sort orders", () => {
    const sortOptions = languageStrings.myResources.sortOptions;

    const countOptions = (options: string[]) =>
      pipe(
        options,
        A.map(querySelectOption),
        A.filter((r) => r !== null),
        A.size,
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
      await clickSelect(container, SORTORDER_SELECT_ID);

      // Check how many of the expected options are now on screen
      const foundOptions = countOptions(options);

      expect(foundOptions).toBe(options.length);
    });

    it.each<MyResourcesType>([
      "Scrapbook",
      "All resources",
      "Published",
      "Drafts",
      "Archive",
    ])(
      "does not display the moderation sort options in non-moderation views (%s)",
      async (nonModerationType: MyResourcesType) => {
        const countModOptions = async (
          type: MyResourcesType,
        ): Promise<number> => {
          const moderationSortOptions = [
            sortOptions.submitted,
            sortOptions.lastAction,
          ];

          const { container } = await renderMyResourcesPage(type);
          await clickSelect(container, SORTORDER_SELECT_ID);
          return countOptions(moderationSortOptions);
        };

        expect(await countModOptions(nonModerationType)).toBe(0);
      },
    );
  });

  describe("Moderation queue", () => {
    const moderationQueueStrings =
      languageStrings.myResources.moderationItemTable;

    const renderModerationQueue = async () =>
      await renderMyResourcesPage("Moderation queue");

    it("uses a custom UI for Moderation queue", async () => {
      const { queryByLabelText } = await renderModerationQueue();

      expect(
        queryByLabelText(moderationQueueStrings.ariaLabel),
      ).toBeInTheDocument();
    });

    it("has a dialog to display rejection comments for rejected items", async () => {
      mockSearch.mockImplementationOnce(() =>
        Promise.resolve(getModerationItemsSearchResult()),
      );
      const { getByText } = await renderModerationQueue();

      const displayRejectionButton =
        getByText("REJECTED").querySelector("button");
      expect(displayRejectionButton).toBeInTheDocument();

      await user.click(displayRejectionButton!);
      expect(
        await screen.findByText(
          moderationQueueStrings.rejectionCommentDialogTitle,
        ),
      ).toBeInTheDocument();
    });
  });

  describe("State definition from query params", () => {
    const baseUrl = "/page/myresources";

    it("selects the resource type based on myResourcesType", async () => {
      history.push(baseUrl + "?myResourcesType=Moderation queue");
      mockSearch.mockResolvedValueOnce(getModerationItemsSearchResult());
      const { queryByLabelText } =
        await renderMyResourcesPageWithUser(getCurrentUserMock);

      expect(
        queryByLabelText(
          languageStrings.myResources.moderationItemTable.ariaLabel,
        ),
      ).toBeInTheDocument();
    });

    it("uses the sort order and statuses from searchOptions", async () => {
      jest
        .spyOn(UserModule, "resolveUsers")
        // UserModuleMock.users[0] matches the owner.id in the `searchOptions` below.
        .mockResolvedValue([UserModuleMock.users[0]]);

      history.push(
        baseUrl +
          '?myResourcesType=All+resources&searchOptions={"rowsPerPage"%3A10%2C"currentPage"%3A0%2C"sortOrder"%3A"name"%2C"rawMode"%3Afalse%2C"status"%3A["PERSONAL"%2C"LIVE"]%2C"searchAttachments"%3Atrue%2C"query"%3A""%2C"collections"%3A[]%2C"lastModifiedDateRange"%3A{}%2C"owner"%3A{"id"%3A"680f5eb7-22e2-4ab6-bcea-25205165e36e"}%2C"mimeTypeFilters"%3A[]%2C"displayMode"%3A"list"%2C"dateRangeQuickModeEnabled"%3Atrue}',
      );
      await renderMyResourcesPageWithUser(getCurrentUserMock);

      const searchCriteria: SearchOptions | undefined =
        searchPromise.mock.lastCall?.[0];
      expect(searchCriteria?.status).toEqual<OEQ.Common.ItemStatus[]>([
        "PERSONAL",
        "LIVE",
      ]);
      expect(searchCriteria?.sortOrder).toBe<OEQ.Search.SortOrder>("name");
    });
  });

  describe("Saving Favourites", () => {
    it("supports creating a favourite of the current my resources state - type and options", async () => {
      const resourceType: MyResourcesType = "All resources";
      const successfulSave = await addSearchToFavourites(
        await renderMyResourcesPage(resourceType),
        "new favourite",
      );

      // Pull out the params from the mocked call to save
      const params = pipe(
        mockAddFavouriteSearch.mock.lastCall,
        O.fromNullable,
        // get the second argument to the mocked call
        O.chain(A.lookup(1)),
        // check type - perhaps excessively
        O.chain(
          O.fromPredicate(
            (a): a is FavouriteURL => typeof a === "object" && "params" in a,
          ),
        ),
        O.map((url) => url.params),
        O.toUndefined,
      );

      // Now see if it all worked
      expect(successfulSave).toBe(true);
      expect(params).toBeDefined();
      expect(params!.get(PARAM_MYRESOURCES_TYPE)).toBe<MyResourcesType>(
        resourceType,
      );
    });
  });
});
