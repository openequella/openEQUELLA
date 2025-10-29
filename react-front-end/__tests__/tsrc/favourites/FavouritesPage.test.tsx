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
import { render, type RenderResult } from "@testing-library/react";
import { createMemoryHistory, MemoryHistory } from "history";
import * as React from "react";
import { Router } from "react-router-dom";
import {
  getEmptyGallerySearchResp,
  getFavouriteResourcesResp,
} from "../../../__mocks__/Favourites.mock";
import { getCollectionMap } from "../../../__mocks__/getCollectionsResp";
import { createMatchMedia } from "../../../__mocks__/MockUseMediaQuery";
import { itemWithBookmark } from "../../../__mocks__/SearchResult.mock";
import { basicSearchObj } from "../../../__mocks__/searchresult_mock_data";
import { getCurrentUserMock } from "../../../__mocks__/UserModule.mock";
import FavouritesPage from "../../../tsrc/favourites/FavouritesPage";
import { FAVOURITES_TYPE_PARAM } from "../../../tsrc/favourites/FavouritesPageHelper";
import { AppContext } from "../../../tsrc/mainui/App";
import * as FavouriteModule from "../../../tsrc/modules/FavouriteModule";
import { FavouritesType } from "../../../tsrc/modules/FavouriteModule";
import * as SearchSettingsModule from "../../../tsrc/modules/SearchSettingsModule";
import { defaultSearchPageOptions } from "../../../tsrc/search/SearchPageHelper";
import { languageStrings } from "../../../tsrc/util/langstrings";
import {
  clickButton,
  clickSelect,
  countPresentSelectOptions,
  isToggleButtonChecked,
} from "../MuiTestHelpers";
import {
  getRefineSearchComponent,
  mockCollaborators,
  queryClassificationPanel,
  queryListItems,
  queryRefineSearchComponent,
  queryWildcardSearchSwitch,
  SORTORDER_SELECT_ID,
  waitForSearchCompleted,
} from "../search/SearchPageTestHelper";
import { mockApisForFavouriteSearches } from "./components/FavouritesSearchTestHelper";

const { resources: resourcesLabel, searches: searchesLabel } =
  languageStrings.favourites.favouritesSelector;
const { tags: tagsLabel } = languageStrings.favourites.favouritesItem;
const {
  displayModeSelector: { modeItemList, modeGalleryImage, modeGalleryVideo },
  sortOptions,
} = languageStrings.searchpage;
const { dateFavourited } = languageStrings.favourites.sortOptions;

const {
  mockCollections,
  mockCurrentUser,
  mockSearchSettings,
  mockSearch,
  mockImageGallerySearch,
  mockVideoGallerySearch,
  mockListClassification,
} = mockCollaborators();
mockCollections.mockResolvedValue(getCollectionMap);
mockCurrentUser.mockResolvedValue(getCurrentUserMock);
mockSearchSettings.mockResolvedValue({
  ...SearchSettingsModule.defaultSearchSettings,
  searchingShowNonLiveCheckbox: true,
});

const mockFavResourcesSearch = jest
  .spyOn(FavouriteModule, "searchFavouriteItems")
  .mockResolvedValue(getFavouriteResourcesResp);
const { mockFavSearchesSearch } = mockApisForFavouriteSearches();

const renderFavouritesPage = async (
  history?: MemoryHistory,
): Promise<RenderResult> => {
  window.matchMedia = createMatchMedia(1280);

  const memoryHistory = history ?? createMemoryHistory();

  const page = render(
    <ThemeProvider theme={createTheme()}>
      <Router history={memoryHistory}>
        <AppContext.Provider
          value={{
            appErrorHandler: jest.fn(),
            refreshUser: jest.fn(),
            currentUser: getCurrentUserMock,
          }}
        >
          <FavouritesPage updateTemplate={jest.fn()} />
        </AppContext.Provider>
      </Router>
    </ThemeProvider>,
  );
  // Wait for the first completion of initial search
  await waitForSearchCompleted();

  return page;
};

describe("<FavouritesPage/>", () => {
  afterEach(() => {
    jest.clearAllMocks();
  });

  it("shows Favourites resources in list mode when we first land on the page", async () => {
    const { container } = await renderFavouritesPage();
    const listSearchResults = queryListItems(container);

    expect(mockFavResourcesSearch).toHaveBeenCalled();
    expect(mockSearch).not.toHaveBeenCalled();
    expect(isToggleButtonChecked(container, resourcesLabel)).toBe(true);
    expect(isToggleButtonChecked(container, modeItemList)).toBe(true);
    expect(listSearchResults).toHaveLength(2);
  });

  it.each([
    [modeGalleryImage, mockImageGallerySearch],
    [modeGalleryVideo, mockVideoGallerySearch],
  ])("shows Favourite Resources's %s", async (mode, gallerySearch) => {
    gallerySearch.mockResolvedValueOnce(getEmptyGallerySearchResp);
    const { container } = await renderFavouritesPage();
    await clickButton(container, mode);

    expect(isToggleButtonChecked(container, mode)).toBe(true);
    expect(gallerySearch).toHaveBeenCalledTimes(1);
    expect(mockFavResourcesSearch).toHaveBeenCalled();
    expect(mockSearch).not.toHaveBeenCalled();
  });

  it("supports changing Favourites Type to 'Searches' using Favourites Selector", async () => {
    const { container } = await renderFavouritesPage();
    await clickButton(container, searchesLabel);

    expect(isToggleButtonChecked(container, searchesLabel)).toBe(true);
    expect(mockFavSearchesSearch).toHaveBeenCalled();
  });

  it("shows wildcard toggle for Favourite Resources and hides it for Favourite Searches", async () => {
    const { container } = await renderFavouritesPage();

    // Initially on Favourite Resources
    expect(queryWildcardSearchSwitch(container)).toBeInTheDocument();

    // Switch to Favourite Searches
    await clickButton(container, searchesLabel);
    expect(queryWildcardSearchSwitch(container)).not.toBeInTheDocument();
  });

  it.each([
    [
      resourcesLabel,
      [
        sortOptions.lastModified,
        sortOptions.dateCreated,
        sortOptions.title,
        sortOptions.relevance,
        sortOptions.userRating,
        dateFavourited,
      ],
    ],
    [searchesLabel, [sortOptions.title, dateFavourited]],
  ])("shows custom sort order for Favourite %s", async (typeLabel, options) => {
    const { container } = await renderFavouritesPage();
    await clickButton(container, typeLabel);
    await clickSelect(container, SORTORDER_SELECT_ID);
    const foundOptions = countPresentSelectOptions(options);

    expect(foundOptions).toBe(options.length);
  });

  describe("Side panel", () => {
    it("shows all expected refine panel controls for Favourite Resources", async () => {
      const { container } = await renderFavouritesPage();
      const options: string[] = [
        "FavouritesSelector",
        "DisplayModeSelector",
        "CollectionSelector",
        "DateRangeSelector",
        "MIMETypeSelector",
        "OwnerSelector",
        "StatusSelector",
        "SearchAttachmentsSelector",
      ];

      options.forEach((componentSuffix: string) =>
        expect(
          getRefineSearchComponent(container, componentSuffix),
        ).toBeInTheDocument(),
      );
      expect.assertions(options.length);
    });

    it("hides all expected refine panel control for Favourite Resources", async () => {
      const { container } = await renderFavouritesPage();
      const options: string[] = [
        "AdvancedSearchSelector",
        "RemoteSearchSelector",
      ];

      options.forEach((componentSuffix: string) =>
        expect(
          queryRefineSearchComponent(container, componentSuffix),
        ).not.toBeInTheDocument(),
      );
      expect.assertions(options.length);
    });

    it("does not list Classifications and show Classification panel for Favourite Resources", async () => {
      const { container } = await renderFavouritesPage();

      expect(mockListClassification).not.toHaveBeenCalled();
      expect(queryClassificationPanel(container)).not.toBeInTheDocument();
    });

    it("shows only Favourites Selector and Date Range Selector refine panel controls for Favourite Searches", async () => {
      const { container } = await renderFavouritesPage();
      await clickButton(container, searchesLabel);
      const options: string[] = ["FavouritesSelector", "DateRangeSelector"];

      options.forEach((componentSuffix) =>
        expect(
          getRefineSearchComponent(container, componentSuffix),
        ).toBeInTheDocument(),
      );
      expect.assertions(options.length);
    });
  });

  it("should display bookmark tags if the tags are available for a Favourite Resource", async () => {
    const { getByText } = await renderFavouritesPage();
    const tags = itemWithBookmark.bookmark!.tags;

    tags.forEach((tag) => {
      expect(getByText(tag)).toBeInTheDocument();
    });
    expect.assertions(tags.length);
  });

  it("should not show bookmark tags if the tags are not available for a Favourite Resource", async () => {
    mockFavResourcesSearch.mockResolvedValueOnce({
      ...getFavouriteResourcesResp,
      results: [basicSearchObj],
      length: 1,
      available: 1,
    });

    const { queryByText } = await renderFavouritesPage();

    expect(queryByText(tagsLabel)).not.toBeInTheDocument();
  });

  it("should save favourites type in the History", async () => {
    const history = createMemoryHistory();
    const { container } = await renderFavouritesPage(history);

    await clickButton(container, searchesLabel);
    expect(history.location.state).toEqual({
      searchPageOptions: {
        ...defaultSearchPageOptions,
        sortOrder: "added_at",
        filterExpansion: false,
      },
      customData: {
        favouritesType: "searches",
      },
    });
  });

  it.each<[FavouritesType, string]>([
    ["resources", resourcesLabel],
    ["searches", searchesLabel],
  ])(
    "should read favourites type from the params",
    async (favouritesType: FavouritesType, selectedButtonLabel: string) => {
      const history = createMemoryHistory();
      history.push({
        pathname: "/favourites",
        search: `?${FAVOURITES_TYPE_PARAM}=${favouritesType}`,
      });

      const { container } = await renderFavouritesPage(history);
      expect(isToggleButtonChecked(container, selectedButtonLabel)).toBe(true);
    },
  );
});
