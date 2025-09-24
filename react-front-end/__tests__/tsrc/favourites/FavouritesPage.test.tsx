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
import { createMemoryHistory } from "history";
import * as React from "react";
import { Router } from "react-router-dom";
import {
  getEmptyTransformedGallerySearchResp,
  getFavouriteResourcesResp,
} from "../../../__mocks__/Favourites.mock";
import { getCollectionMap } from "../../../__mocks__/getCollectionsResp";
import { createMatchMedia } from "../../../__mocks__/MockUseMediaQuery";
import { getCurrentUserMock } from "../../../__mocks__/UserModule.mock";
import FavouritesPage from "../../../tsrc/favourites/FavouritesPage";
import { AppContext } from "../../../tsrc/mainui/App";
import * as SearchSettingsModule from "../../../tsrc/modules/SearchSettingsModule";
import { languageStrings } from "../../../tsrc/util/langstrings";
import {
  clickSelect,
  countPresentSelectOptions,
  isToggleButtonChecked,
  selectToggleButton,
} from "../MuiTestHelpers";
import {
  mockCollaborators,
  queryListItems,
  SORTORDER_SELECT_ID,
  waitForSearchCompleted,
} from "../search/SearchPageTestHelper";
import * as OEQ from "@openequella/rest-api-client";
import * as FavouriteModule from "../../../tsrc/modules/FavouriteModule";
import { mockApisForFavouriteSearches } from "./components/FavouritesSearchTestHelper";

const { resources: resourcesLabel, searches: searchesLabel } =
  languageStrings.favourites.favouritesSelector;
const {
  wildcardSearch,
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
} = mockCollaborators();

mockCollections.mockResolvedValue(getCollectionMap);
mockCurrentUser.mockResolvedValue(getCurrentUserMock);
mockSearchSettings.mockResolvedValue(
  SearchSettingsModule.defaultSearchSettings,
);

const mockFavResourcesSearch = jest
  .spyOn(FavouriteModule, "searchFavouriteItems")
  .mockResolvedValue(getFavouriteResourcesResp);
const { mockFavSearchesSearch } = mockApisForFavouriteSearches();

describe("<FavouritesPage/>", () => {
  const renderFavouritesPage = async (
    screenWidth: number = 1280,
    currentUser: OEQ.LegacyContent.CurrentUserDetails = getCurrentUserMock,
  ): Promise<RenderResult> => {
    window.matchMedia = createMatchMedia(screenWidth);

    const history = createMemoryHistory();

    const page = render(
      <ThemeProvider theme={createTheme()}>
        <Router history={history}>
          <AppContext.Provider
            value={{
              appErrorHandler: jest.fn(),
              refreshUser: jest.fn(),
              currentUser,
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

  let page: RenderResult;
  beforeEach(async () => {
    page = await renderFavouritesPage();
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  it("shows Favourites resources in list mode when we first land on the page", async () => {
    const listSearchResults = queryListItems(page.container);

    expect(mockFavResourcesSearch).toHaveBeenCalled();
    expect(mockSearch).not.toHaveBeenCalled();
    expect(isToggleButtonChecked(page.container, resourcesLabel)).toBeTruthy();
    expect(isToggleButtonChecked(page.container, modeItemList)).toBeTruthy();
    expect(listSearchResults).toHaveLength(2);
  });

  it.each([
    [
      modeGalleryImage,
      mockImageGallerySearch,
      getEmptyTransformedGallerySearchResp,
    ],
    [
      modeGalleryVideo,
      mockVideoGallerySearch,
      getEmptyTransformedGallerySearchResp,
    ],
  ])(
    "shows favourite resources's %s",
    async (mode, gallerySearch, mockResponse) => {
      gallerySearch.mockResolvedValue(mockResponse);
      await selectToggleButton(page.container, mode);

      expect(isToggleButtonChecked(page.container, mode)).toBeTruthy();
      expect(gallerySearch).toHaveBeenCalledTimes(1);
      expect(mockFavResourcesSearch).toHaveBeenCalled();
      expect(mockSearch).not.toHaveBeenCalled();
    },
  );

  it("supports changing Favourites Type to 'Searches' using Favourites Selector", async () => {
    await selectToggleButton(page.container, searchesLabel);

    expect(isToggleButtonChecked(page.container, searchesLabel)).toBeTruthy();
    expect(mockFavSearchesSearch).toHaveBeenCalled();
  });

  it.each([
    [
      "shows",
      resourcesLabel,
      (wildcardToggle: HTMLElement | null) =>
        expect(wildcardToggle).toBeInTheDocument(),
    ],
    [
      "hides",
      searchesLabel,
      (wildcardToggle: HTMLElement | null) =>
        expect(wildcardToggle).not.toBeInTheDocument(),
    ],
  ])(
    "%s the wildcard search toggle for Favourite Type: %s",
    async (_, favouriteType, assertion) => {
      const isFavouriteSearches = favouriteType === searchesLabel;
      if (isFavouriteSearches) {
        await selectToggleButton(page.container, searchesLabel);
      }
      const wildcardToggle = page.queryByRole("switch", {
        name: wildcardSearch,
      });
      assertion(wildcardToggle);
    },
  );

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
  ])(
    "shows custom sort order for Favourite Type: %s",
    async (favouriteType, options) => {
      if (favouriteType === searchesLabel) {
        await selectToggleButton(page.container, searchesLabel);
      }
      // Click the menu
      await clickSelect(page.container, SORTORDER_SELECT_ID);
      // Check how many of the expected options are now on screen
      const foundOptions = countPresentSelectOptions(options);

      expect(foundOptions).toBe(options.length);
    },
  );
});
