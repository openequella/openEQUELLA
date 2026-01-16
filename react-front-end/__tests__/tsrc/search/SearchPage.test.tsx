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
import "@testing-library/jest-dom";
import {
  getByLabelText,
  getByText,
  queryByLabelText,
  RenderResult,
  screen,
  waitFor,
} from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { omit } from "lodash";
import * as CategorySelectorMock from "../../../__mocks__/CategorySelector.mock";
import {
  transformedBasicImageSearchResponse,
  transformedBasicVideoSearchResponse,
} from "../../../__mocks__/GallerySearchModule.mock";
import { getCollectionMap } from "../../../__mocks__/getCollectionsResp";
import { getMimeTypeFilters } from "../../../__mocks__/MimeTypeFilter.mock";
import { getSearchResult } from "../../../__mocks__/SearchResult.mock";
import * as UserModuleMock from "../../../__mocks__/UserModule.mock";
import { getCurrentUserMock } from "../../../__mocks__/UserModule.mock";
import type { Collection } from "../../../tsrc/modules/CollectionsModule";
import { getGlobalCourseList } from "../../../tsrc/modules/LegacySelectionSessionModule";
import type { SelectedCategories } from "../../../tsrc/modules/SearchFacetsModule";
import * as SearchModule from "../../../tsrc/modules/SearchModule";
import type { SearchOptions } from "../../../tsrc/modules/SearchModule";
import * as SearchSettingsModule from "../../../tsrc/modules/SearchSettingsModule";
import { SearchPageOptions } from "../../../tsrc/search/SearchPageHelper";
import { languageStrings } from "../../../tsrc/util/langstrings";
import { updateMockGetBaseUrl } from "../BaseUrlHelper";
import { queryPaginatorControls } from "../components/SearchPaginationTestHelper";
import { updateMockGlobalCourseList } from "../CourseListHelper";
import {
  clickSelect,
  getSelectOption,
  isToggleButtonChecked,
  selectOption,
  clickButton,
} from "../MuiTestHelpers";
import { basicRenderData, updateMockGetRenderData } from "../RenderDataHelper";
import {
  clearSelection,
  selectUser,
} from "./components/OwnerSelectTestHelpers";
import {
  addSearchToFavourites,
  getRefineSearchComponent,
  getRefineSearchPanel,
  initialiseEssentialMocks,
  mockCollaborators,
  queryCollectionSelector,
  queryDateRangeSelector,
  queryMimeTypesSelector,
  queryOwnerSelector,
  querySearchAttachmentsSelector,
  queryStatusSelector,
  renderSearchPage,
  SORTORDER_SELECT_ID,
  changeQuery,
  queryListItems,
  queryGalleryItems,
  queryWildcardSearchSwitch,
} from "./SearchPageTestHelper";

// This has some big tests for rendering the Search Page, so we need a longer timeout.
// First we used 10s, but then found we started getting increased failures on GitLab CI so
// bumped it to 15s.
jest.setTimeout(15000);

const {
  mockConvertParamsToSearchOptions,
  mockCollections,
  mockCurrentUser,
  mockImageGallerySearch,
  mockListClassification,
  mockListImageGalleryClassifications,
  mockListUsers,
  mockListVideoGalleryClassifications,
  mockMimeTypeFilters,
  mockReadDataFromLocalStorage,
  mockSaveDataToLocalStorage,
  mockSearch,
  mockSearchSettings,
  mockVideoGallerySearch,
} = mockCollaborators();
initialiseEssentialMocks({
  mockCollections,
  mockCurrentUser,
  mockListClassification,
  mockSearchSettings,
});
mockSearch.mockResolvedValue(getSearchResult);
mockListUsers.mockResolvedValue(UserModuleMock.users);

const defaultSearchPageOptions: SearchPageOptions = {
  ...SearchModule.defaultSearchOptions,
  sortOrder: "rank",
  dateRangeQuickModeEnabled: true,
  mimeTypeFilters: [],
  displayMode: "list",
  includeAttachments: false, // in 'list' displayMode we exclude attachments
  selectedCategories: undefined,
};
const defaultCollectionPrivileges = [OEQ.Acl.ACL_SEARCH_COLLECTION];

describe("<SearchPage/>", () => {
  afterEach(() => {
    jest.clearAllMocks();
  });

  describe("Add and remove favourite Item,", () => {
    const { add, remove } = languageStrings.searchpage.favouriteItem;
    let page: RenderResult;

    beforeEach(async () => {
      page = await renderSearchPage();
    });

    it.each([
      // The mocked search result has two Items named "a" and "b" so let's use them.
      [add, remove, "a"],
      [remove, add, "b"],
    ])(
      "shows FavouriteItemDialog to %s",
      async (defaultIcon, updatedIcon, itemName: string) => {
        const searchResultItem = page
          .getByText(itemName, { selector: "span" })
          .closest("li");
        if (!searchResultItem) {
          throw new Error("Failed to find the mocked search result Item.");
        }
        const heartIcon = getByLabelText(searchResultItem, defaultIcon, {
          selector: "button",
        });
        await userEvent.click(heartIcon);

        const dialog = page.getByRole("dialog");
        const confirmBtn = dialog.querySelector(
          "#confirm-dialog-confirm-button",
        );
        if (!confirmBtn) {
          throw new Error("Failed to find confirm button.");
        }

        await userEvent.click(confirmBtn);
        // Now a different Heart Icon should be used.
        const updatedHeartIcon = queryByLabelText(
          searchResultItem,
          updatedIcon,
          {
            selector: "button",
          },
        );
        expect(updatedHeartIcon).toBeInTheDocument();
      },
    );
  });

  describe("Add favourite search", () => {
    it("supports creating a favourite of the current search", async () => {
      const successfulSave = await addSearchToFavourites(
        await renderSearchPage(),
        "new favourite",
      );
      expect(successfulSave).toBe(true);
    });
  });

  describe("Changing display mode", () => {
    const { modeGalleryImage, modeGalleryVideo, modeItemList } =
      languageStrings.searchpage.displayModeSelector;

    it("has a default of item list mode", async () => {
      const { container } = await renderSearchPage();

      // Check that the button is visually correct
      expect(isToggleButtonChecked(container, modeItemList)).toBeTruthy();

      // Check that it's all wired up correctly - i.e. no mime types were passed to the search
      expect(mockSearch).toHaveBeenLastCalledWith(defaultSearchPageOptions);

      // And lastly check that it was a item list display - not a gallery
      expect(queryListItems(container).length).toBeGreaterThan(0);
    });

    it.each([
      [
        modeGalleryImage,
        mockImageGallerySearch,
        mockListImageGalleryClassifications,
        transformedBasicImageSearchResponse,
      ],
      [
        modeGalleryVideo,
        mockVideoGallerySearch,
        mockListVideoGalleryClassifications,
        transformedBasicVideoSearchResponse,
      ],
    ])(
      "supports changing mode - [%s]",
      async (
        mode: string,
        gallerySearch,
        listClassifications,
        mockResponse,
      ) => {
        const { container } = await renderSearchPage();

        expect(queryListItems(container).length).toBeGreaterThan(0);
        expect(queryGalleryItems(container)).toHaveLength(0);

        // Monitor the search and classifications functions, and change the mode
        await Promise.all([
          gallerySearch.mockResolvedValue(mockResponse),
          listClassifications.mockResolvedValue(
            CategorySelectorMock.classifications,
          ),
          clickButton(container, mode),
        ]);

        // Make sure the search has been triggered
        expect(gallerySearch).toHaveBeenCalledTimes(1);
        expect(listClassifications).toHaveBeenCalledTimes(1);

        // And now check the visual change
        expect(queryGalleryItems(container).length).toBeGreaterThan(0);
        expect(queryListItems(container)).toHaveLength(0);
        expect(queryMimeTypesSelector(container)).not.toBeInTheDocument();
      },
    );
  });

  describe("Classifications", () => {
    const JAVA_CATEGORY = "java";
    const selectedCategories: SelectedCategories[] = [
      { id: 766942, schemaNode: "/item/language", categories: [JAVA_CATEGORY] },
    ];
    const clickCategory = async (container: HTMLElement, category: string) => {
      await userEvent.click(getByText(container, category));
    };

    it("should filter search result by categories", async () => {
      const { container } = await renderSearchPage();
      await clickCategory(container, JAVA_CATEGORY);
      expect(mockSearch).toHaveBeenLastCalledWith({
        ...defaultSearchPageOptions,
        selectedCategories: selectedCategories,
      });
    });

    it("should also filter Classification list by categories", async () => {
      const { container } = await renderSearchPage();
      await clickCategory(container, JAVA_CATEGORY);

      // Drop 'includeAttachments' as it is not passed in for listClassification calls
      const expected: SearchOptions = omit(
        {
          ...defaultSearchPageOptions,
          selectedCategories: selectedCategories,
        },
        "includeAttachments",
      );

      expect(mockListClassification).toHaveBeenLastCalledWith(expected);
    });
  });

  describe("Collapsible refine filter section", () => {
    let page: RenderResult;
    beforeEach(async () => {
      page = await renderSearchPage();
    });

    it("Should contain the correct controls", async () => {
      const refineSearchPanel = getRefineSearchPanel(page.container);
      const collapsibleSection = refineSearchPanel.querySelector(
        ".collapsibleRefinePanel",
      );
      if (!collapsibleSection) {
        throw new Error(
          "Unable to find collapsible filter section inside refine search panel",
        );
      }

      expect(collapsibleSection).toContainElement(
        queryOwnerSelector(refineSearchPanel),
      );
      expect(collapsibleSection).toContainElement(
        queryDateRangeSelector(refineSearchPanel),
      );
      expect(collapsibleSection).toContainElement(
        querySearchAttachmentsSelector(refineSearchPanel),
      );
      expect(collapsibleSection).not.toContainElement(
        queryCollectionSelector(refineSearchPanel),
      );
    });

    it("Should change button text when clicked", async () => {
      const expansionButton = page.container.querySelector(
        "#collapsibleRefinePanelButton",
      );
      if (!expansionButton) {
        throw new Error("Unable to find collapsible refine panel button");
      }
      await userEvent.click(expansionButton);
      expect(expansionButton).toHaveTextContent(
        languageStrings.common.action.showLess,
      );
    });
  });

  describe("Copy and share search", () => {
    it("should copy a search link to clipboard, and show a snackbar", async () => {
      const mockClipboard = jest
        .spyOn(navigator.clipboard, "writeText")
        .mockResolvedValueOnce();

      const { getByLabelText } = await renderSearchPage();
      const copySearchButton = getByLabelText(
        languageStrings.searchpage.shareSearchHelperText,
      );

      await userEvent.click(copySearchButton);

      expect(mockConvertParamsToSearchOptions).toHaveBeenCalledTimes(1);
      expect(mockClipboard).toHaveBeenCalledWith(
        "/page/search?searchOptions=%7B%22rowsPerPage%22%3A10%2C%22currentPage%22%3A0%2C%22sortOrder%22%3A%22rank%22%2C%22rawMode%22%3Afalse%2C%22status%22%3A%5B%22LIVE%22%2C%22REVIEW%22%5D%2C%22searchAttachments%22%3Atrue%2C%22query%22%3A%22%22%2C%22collections%22%3A%5B%5D%2C%22lastModifiedDateRange%22%3A%7B%7D%2C%22mimeTypeFilters%22%3A%5B%5D%2C%22displayMode%22%3A%22list%22%2C%22dateRangeQuickModeEnabled%22%3Atrue%7D",
      );
      expect(
        screen.getByText(
          languageStrings.searchpage.shareSearchConfirmationText,
        ),
      ).toBeInTheDocument();
    });
  });

  describe("Export search result", () => {
    const selectCollections = async (
      page: RenderResult,
      ...collectionNames: string[]
    ) => {
      await userEvent.click(
        page.getByLabelText(
          languageStrings.searchpage.collectionSelector.title,
        ),
      );
      await Promise.all(
        collectionNames.map(
          async (name) => await userEvent.click(screen.getByText(name)),
        ),
      );
    };

    it("shows a Download Icon button to allow exporting", async () => {
      const page = await renderSearchPage();
      await selectCollections(page, getCollectionMap[0].name);
      expect(
        page.queryByLabelText(languageStrings.searchpage.export.title),
      ).toBeInTheDocument();
    });

    it.each([
      ["zero", []],
      ["more than one", getCollectionMap.slice(0, 2).map((c) => c.name)],
    ])(
      "disables export if %s collection is selected",
      async (collectionNumber: string, collections: string[]) => {
        const page = await renderSearchPage();
        await selectCollections(page, ...collections);
        await userEvent.click(
          page.getByLabelText(languageStrings.searchpage.export.title),
        );
        expect(
          screen.getByText(languageStrings.searchpage.export.collectionLimit),
        ).toBeInTheDocument();
      },
    );

    it("doesn't show the Download button if user have no permission", async () => {
      // Remove previously rendered result so that we can mock the current user details.
      const { queryByLabelText } = await renderSearchPage(
        undefined,
        undefined,
        {
          ...getCurrentUserMock,
          canDownloadSearchResult: false,
        },
      );

      expect(
        queryByLabelText(languageStrings.searchpage.export.title),
      ).not.toBeInTheDocument();
    });
  });

  describe("Hide Gallery", () => {
    const {
      modeItemList: itemListLabel,
      modeGalleryImage: imageLabel,
      modeGalleryVideo: videoLabel,
    } = languageStrings.searchpage.displayModeSelector;
    it.each([
      [
        "image",
        {
          ...SearchSettingsModule.defaultSearchSettings,
          searchingDisableGallery: true,
        },
        [itemListLabel, videoLabel],
        [imageLabel],
      ],
      [
        "video",
        {
          ...SearchSettingsModule.defaultSearchSettings,
          searchingDisableVideos: true,
        },
        [itemListLabel, imageLabel],
        [videoLabel],
      ],
      [
        "both image and video",
        {
          ...SearchSettingsModule.defaultSearchSettings,
          searchingDisableVideos: true,
          searchingDisableGallery: true,
        },
        [itemListLabel],
        [videoLabel, imageLabel],
      ],
    ])(
      "hides %s gallery",
      async (
        mode: string,
        settings: OEQ.SearchSettings.Settings,
        enabledModes: string[],
        disabledModes: string[],
      ) => {
        mockSearchSettings.mockResolvedValueOnce(settings);
        const { queryByLabelText } = await renderSearchPage();
        disabledModes.forEach((hiddenLabel) =>
          expect(queryByLabelText(hiddenLabel)).not.toBeInTheDocument(),
        );
        enabledModes.forEach((visibleLabel) =>
          expect(queryByLabelText(visibleLabel)).toBeInTheDocument(),
        );
      },
    );
  });

  describe("Hide Refine Search controls", () => {
    it.each([
      // Reuse default Search settings as disableStatusSelector, enableOwnerSelector and enableDateSelector.
      [
        "Owner Selector",
        (container: HTMLElement) => queryOwnerSelector(container),
        {
          ...SearchSettingsModule.defaultSearchSettings,
          searchingDisableOwnerFilter: true,
        },
      ],
      [
        "Date Selector",
        (container: HTMLElement) => queryDateRangeSelector(container),
        {
          ...SearchSettingsModule.defaultSearchSettings,
          searchingDisableDateModifiedFilter: true,
        },
      ],
      [
        "Status Selector",
        (container: HTMLElement) => queryStatusSelector(container),
        SearchSettingsModule.defaultSearchSettings,
      ],
    ])(
      "should be possible to disable %s",
      async (
        testName: string,
        getSelector: (container: HTMLElement) => HTMLElement | null,
        disableSelector: OEQ.SearchSettings.Settings,
      ) => {
        mockSearchSettings.mockResolvedValueOnce(disableSelector);
        const page = await renderSearchPage();
        expect(getSelector(page.container)).toBeNull();
      },
    );

    it("If not MIME type filters are available, the selector should be hidden", async () => {
      mockMimeTypeFilters.mockResolvedValueOnce([]);
      const { container } = await renderSearchPage();
      expect(queryMimeTypesSelector(container)).not.toBeInTheDocument();
    });
  });

  describe("In Selection Session", () => {
    beforeEach(() => {
      updateMockGlobalCourseList();
      updateMockGetBaseUrl();
      updateMockGetRenderData(basicRenderData);
    });

    it("should make each Search result Item which represents a live item draggable", async () => {
      await renderSearchPage();

      const searchResults = getSearchResult.results;
      // Make sure the search result definitely has Items.
      expect(searchResults.length).toBeGreaterThan(0);

      searchResults.filter(SearchModule.isLiveItem).forEach(({ uuid }) => {
        expect(
          getGlobalCourseList().prepareDraggableAndBind,
        ).toHaveBeenCalledWith(`#${uuid}`, true);
      });
    });

    it("should not show the share search button", async () => {
      const { queryByTitle } = await renderSearchPage();

      const copySearchButton = queryByTitle(
        languageStrings.searchpage.shareSearchHelperText,
      );
      expect(copySearchButton).not.toBeInTheDocument();
    });
  });

  describe("New search", () => {
    it("should clear search options and perform a new search", async () => {
      await renderSearchPage();
      const newSearchButton = screen.getByText(
        languageStrings.searchpage.newSearch,
      );

      await userEvent.click(newSearchButton);
      expect(mockSearch).toHaveBeenLastCalledWith(defaultSearchPageOptions);
    });
  });

  describe("Pagination", () => {
    it("should support changing the number of items displayed per page", async () => {
      const page = await renderSearchPage();
      // Initial items per page is 10
      const { getPageCount, getItemsPerPageOption, getItemsPerPageSelect } =
        queryPaginatorControls(page.container);
      expect(getPageCount()).toHaveTextContent("1–10 of 12");

      await userEvent.click(getItemsPerPageSelect());
      const itemsPerPageDesired = 25;
      await userEvent.click(getItemsPerPageOption(itemsPerPageDesired));

      expect(SearchModule.searchItems).toHaveBeenCalledWith({
        ...defaultSearchPageOptions,
        rowsPerPage: itemsPerPageDesired,
      });
      expect(getPageCount()).toHaveTextContent("1–12 of 12");
    });

    it("navigates to the previous and next page when requested", async () => {
      const page = await renderSearchPage();

      const { getNextPageButton, getPageCount, getPreviousPageButton } =
        queryPaginatorControls(page.container);

      await userEvent.click(getNextPageButton());
      expect(getPageCount()).toHaveTextContent("11–12 of 12");

      await userEvent.click(getPreviousPageButton());
      expect(getPageCount()).toHaveTextContent("1–10 of 12");
    });

    it("moves to the first and last page when requested", async () => {
      const page = await renderSearchPage();

      const { getPageCount } = queryPaginatorControls(page.container);

      // Test going to the last page
      const lastPageButton = screen.getByLabelText(
        languageStrings.searchpage.pagination.lastPageButton,
      );

      await userEvent.click(lastPageButton);
      expect(getPageCount()).toHaveTextContent("11–12 of 12");

      const firstPageButton = screen.getByLabelText(
        languageStrings.searchpage.pagination.firstPageButton,
      );
      // ... and now back to the first
      await userEvent.click(firstPageButton);

      expect(getPageCount()).toHaveTextContent("1–10 of 12");
    });
  });

  describe("Refine search by Collections", () => {
    it("should retrieve collections when the page is opened", async () => {
      jest.clearAllMocks();
      await renderSearchPage();
      expect(mockCollections).toHaveBeenCalledTimes(1);
      expect(mockCollections).toHaveBeenCalledWith(defaultCollectionPrivileges);
    });

    it("filters by selected collection", async () => {
      const targetCollection: Collection = getCollectionMap[0];
      await renderSearchPage();

      await userEvent.click(
        screen.getByLabelText(
          languageStrings.searchpage.collectionSelector.title,
        ),
      );
      await userEvent.click(screen.getByText(targetCollection.name));

      expect(mockSearch).toHaveBeenLastCalledWith({
        ...defaultSearchPageOptions,
        collections: [targetCollection],
      });
    });
  });

  describe("Refine search by searching attachments", () => {
    let page: RenderResult;

    beforeEach(async () => {
      page = await renderSearchPage();
    });

    const getSearchAttachmentsSelector = (container: Element): HTMLElement =>
      getRefineSearchComponent(container, "SearchAttachmentsSelector");
    const changeOption = async (selector: HTMLElement, option: string) =>
      await userEvent.click(getByText(selector, option));
    const { yes: yesLabel, no: noLabel } = languageStrings.common.action;

    it("Should default to searching attachments", async () => {
      expect(mockSearch).toHaveBeenLastCalledWith(defaultSearchPageOptions);
    });

    it("Should not search attachments if No is selected", async () => {
      await changeOption(getSearchAttachmentsSelector(page.container), noLabel);
      expect(mockSearch).toHaveBeenLastCalledWith({
        ...defaultSearchPageOptions,
        searchAttachments: false,
      });
    });

    it("Should search attachments if Yes is selected", async () => {
      await changeOption(
        getSearchAttachmentsSelector(page.container),
        yesLabel,
      );
      expect(mockSearch).toHaveBeenLastCalledWith(defaultSearchPageOptions);
    });
  });

  describe("Refine search by status", () => {
    const { live: liveButtonLabel, all: allButtonLabel } =
      languageStrings.searchpage.statusSelector;

    const expectSearchItemsCalledWithStatus = (
      status: OEQ.Common.ItemStatus[],
    ) =>
      expect(mockSearch).toHaveBeenLastCalledWith({
        ...defaultSearchPageOptions,
        status: status,
      });

    const getStatusSelector = (container: Element): HTMLElement =>
      getRefineSearchComponent(container, "StatusSelector");

    const selectStatus = async (container: Element, status: string) =>
      await userEvent.click(getByText(getStatusSelector(container), status));

    const { liveStatuses, nonLiveStatuses } = SearchModule;
    beforeEach(() => {
      // Status selector is disabled by default so enable it before test.
      mockSearchSettings.mockResolvedValueOnce({
        ...SearchSettingsModule.defaultSearchSettings,
        searchingShowNonLiveCheckbox: true,
      });
    });

    it("Should default to LIVE statuses", async () => {
      await renderSearchPage();
      expectSearchItemsCalledWithStatus(liveStatuses);
    });

    it("Should search for items of all statuses if ALL is clicked", async () => {
      const page = await renderSearchPage();
      await selectStatus(page.container, allButtonLabel);
      expectSearchItemsCalledWithStatus(liveStatuses.concat(nonLiveStatuses));
    });

    it("Should search for items of 'live' statuses if LIVE is clicked", async () => {
      const page = await renderSearchPage();
      await selectStatus(page.container, liveButtonLabel);
      expectSearchItemsCalledWithStatus(liveStatuses);
    });
  });

  describe("Refine search by Owner", () => {
    const testUser = UserModuleMock.users[0];
    let page: RenderResult;

    beforeEach(async () => {
      page = await renderSearchPage();
    });

    const getOwnerSelector = (container: Element): HTMLElement =>
      getRefineSearchComponent(container, "OwnerSelector");

    const confirmSelectedUser = (username: string) =>
      screen.getByText(username);

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

      // Now clear the selection and wait until the search is completed.
      await clearSelection();
      await waitFor(() => confirmSelectedUserCleared(testUser.username));

      expect(SearchModule.searchItems).toHaveBeenCalledWith(
        defaultSearchPageOptions,
      );
    });
  });

  describe("Refine search by MIME type filters", () => {
    it("supports multiple filters", async () => {
      const filters = getMimeTypeFilters;
      const page = await renderSearchPage();
      await userEvent.click(
        page.getByLabelText(
          languageStrings.searchpage.mimeTypeFilterSelector.helperText,
        ),
      );
      await Promise.all(
        filters.map(
          async ({ name }) => await userEvent.click(screen.getByText(name)),
        ),
      );

      expect(mockSearch).toHaveBeenLastCalledWith({
        ...defaultSearchPageOptions,
        mimeTypes: filters.flatMap((f) => f.mimeTypes),
        mimeTypeFilters: filters,
      });
    });
  });

  describe("Refine search by date range", () => {
    it("filters by date range derived from 'Quick Options'", async () => {
      const { container } = await renderSearchPage();
      // Click the <Select>
      await clickSelect(container, "#date-range-selector-quick-options");
      // then click the option in the list
      await userEvent.click(getSelectOption("Today"));

      const start = mockSearch.mock.lastCall?.[0]?.lastModifiedDateRange?.start;
      expect(start?.toLocaleDateString()).toBe(new Date().toLocaleDateString()); // i.e. Today as per the quick option
    });
  });

  describe("Responsiveness", () => {
    // We can query the Refine Search Panel as it always exists in the Side Panel.
    const querySidePanel = () =>
      screen.queryByText(languageStrings.searchpage.refineSearchPanel.title, {
        selector: "h5",
      });

    it("should hide the side panel in small screens", async () => {
      await renderSearchPage(undefined, 600);
      expect(querySidePanel()).not.toBeInTheDocument();
    });

    it("should display the button controlling the side panel visibility", async () => {
      const page = await renderSearchPage(undefined, 600);
      const refineSearchButton = page.queryByLabelText(
        languageStrings.searchpage.refineSearchPanel.title,
      );
      expect(refineSearchButton).toBeInTheDocument();
      await userEvent.click(refineSearchButton!); // It's safe to add a '!' now.
      expect(querySidePanel()).toBeInTheDocument();
    });
  });

  describe("Search query", () => {
    beforeEach(() => {
      jest.useFakeTimers();
    });

    afterEach(() => {
      jest.runOnlyPendingTimers();
      jest.useRealTimers();
    });

    it("should support debounce query search and display search results", async () => {
      const { container } = await renderSearchPage();
      await changeQuery(container, "new query", true);

      // After 1s the second search should be triggered. (The first being the initial component mount.)
      expect(mockSearch).toHaveBeenCalledTimes(2);
      expect(mockSearch).toHaveBeenCalledWith({
        ...defaultSearchPageOptions,
        query: "new query",
      });

      expect(container).toHaveTextContent(
        "266bb0ff-a730-4658-aec0-c68bbefc227c",
      );
      expect(container).not.toHaveTextContent("No results found."); // Should be the lang string
    });

    it("sends the query as-is when in non-wildcard search mode", async () => {
      const { container } = await renderSearchPage();
      // When a raw mode search is done
      await changeQuery(container, "non-wildcard search test", false);

      // assert that the query was passed in as-is
      expect(mockSearch).toHaveBeenLastCalledWith({
        ...defaultSearchPageOptions,
        rawMode: true,
        query: "non-wildcard search test",
      });
      // There should be three calls:
      // 1. The initial call on component mount
      // 2. Switching to raw mode
      // 3. Typing a query, and hitting enter
      expect(mockSearch).toHaveBeenCalledTimes(3);
    });
  });

  describe("Sort search result", () => {
    it("sort search results based on selection", async () => {
      const { container } = await renderSearchPage();
      await selectOption(
        container,
        SORTORDER_SELECT_ID,
        languageStrings.settings.searching.searchPageSettings.lastModified,
      );

      // Because sorting is done on the server-side and we are using mock data, we can only check if the selected
      // sort order is included in the search params
      expect(mockSearch).toHaveBeenCalledWith({
        ...defaultSearchPageOptions,
        sortOrder: "datemodified",
      });
    });
  });

  describe("Wildcard mode persistence", () => {
    it("saves wildcard mode value in browser local storage", async () => {
      await renderSearchPage();

      const wildcardModeSwitch = screen.getByText(
        languageStrings.searchpage.wildcardSearch,
      );
      await userEvent.click(wildcardModeSwitch);

      expect(mockSaveDataToLocalStorage).toHaveBeenCalled();
    });

    it("retrieves wildcard mode value from local storage", async () => {
      // By default wildcard mode is turned on, so we save 'false' in local storage.
      mockReadDataFromLocalStorage.mockReturnValueOnce(true);
      const { container } = await renderSearchPage();
      expect(mockReadDataFromLocalStorage).toHaveBeenCalled();
      const wildcardModeSwitch = queryWildcardSearchSwitch(container);

      // Since rawMode is true, the checkbox should not be checked.
      expect(wildcardModeSwitch).not.toBeChecked();
    });
  });
});
