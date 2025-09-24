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
import {
  act,
  queryAllByLabelText,
  render,
  RenderResult,
  screen,
  waitFor,
  waitForElementToBeRemoved,
} from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { createMemoryHistory } from "history";
import * as React from "react";
import { Router } from "react-router-dom";
import { getAdvancedSearchesFromServerResult } from "../../../__mocks__/AdvancedSearchModule.mock";
import * as CategorySelectorMock from "../../../__mocks__/CategorySelector.mock";
import { DRM_VIOLATION } from "../../../__mocks__/Drm.mock";
import { getCollectionMap } from "../../../__mocks__/getCollectionsResp";
import { getMimeTypeFilters } from "../../../__mocks__/MimeTypeFilter.mock";
import { createMatchMedia } from "../../../__mocks__/MockUseMediaQuery";
import { getRemoteSearchesFromServerResult } from "../../../__mocks__/RemoteSearchModule.mock";
import { getCurrentUserMock } from "../../../__mocks__/UserModule.mock";
import { AppContext } from "../../../tsrc/mainui/App";
import * as AdvancedSearchModule from "../../../tsrc/modules/AdvancedSearchModule";
import * as BrowserStorageModule from "../../../tsrc/modules/BrowserStorageModule";
import * as CollectionsModule from "../../../tsrc/modules/CollectionsModule";
import * as DrmModule from "../../../tsrc/modules/DrmModule";
import * as FavouriteModule from "../../../tsrc/modules/FavouriteModule";
import * as GallerySearchModule from "../../../tsrc/modules/GallerySearchModule";
import * as MimeTypesModule from "../../../tsrc/modules/MimeTypesModule";
import * as RemoteSearchModule from "../../../tsrc/modules/RemoteSearchModule";
import * as SearchFacetsModule from "../../../tsrc/modules/SearchFacetsModule";
import * as SearchFilterSettingsModule from "../../../tsrc/modules/SearchFilterSettingsModule";
import * as SearchModule from "../../../tsrc/modules/SearchModule";
import * as SearchSettingsModule from "../../../tsrc/modules/SearchSettingsModule";
import * as UserModule from "../../../tsrc/modules/UserModule";
import SearchPage from "../../../tsrc/search/SearchPage";
import * as SearchPageHelper from "../../../tsrc/search/SearchPageHelper";
import { languageStrings } from "../../../tsrc/util/langstrings";
import { getMuiButtonByText, getMuiTextField } from "../MuiQueries";

const {
  searchResult: { ariaLabel: listItemAriaLabel },
  gallerySearchResult: { ariaLabel: galleryItemAriaLabel },
} = languageStrings.searchpage;

export const SORTORDER_SELECT_ID = "#sort-order-select";

/**
 * Provides a centralised place to mock all the Collaborators used by SearchPage, providing an object
 * with those which are often customised or interacted with. Also mocks some key system level items
 * like the clipboard and the `window.scrollTo` functionality.
 */
export const mockCollaborators = () => {
  // Charlie (mrblippy) tried mocking this using window.navigator.clipboard.writeText = jest.fn(),
  // but the navigator object is undefined
  Object.assign(navigator, {
    clipboard: {
      writeText: jest.fn(),
    },
  });

  window.scrollTo = jest.fn();

  jest
    .spyOn(DrmModule, "listDrmViolations")
    .mockResolvedValue({ violation: DRM_VIOLATION });

  // Mock out a collaborator of SearchResult
  jest
    .spyOn(MimeTypesModule, "getMimeTypeDefaultViewerDetails")
    .mockResolvedValue({
      viewerId: "fancy",
    } as OEQ.MimeType.MimeTypeViewerDetail);

  // Mock out collaborator which populates the Remote Search selector
  jest
    .spyOn(RemoteSearchModule, "getRemoteSearchesFromServer")
    .mockResolvedValue(getRemoteSearchesFromServerResult);

  jest.spyOn(FavouriteModule, "addFavouriteItem").mockResolvedValue({
    itemID: "abc",
    keywords: ["a", "b"],
    isAlwaysLatest: true,
    bookmarkID: 456,
  });

  jest.spyOn(FavouriteModule, "deleteFavouriteItem").mockResolvedValue();

  return {
    mockGetAdvancedSearchesFromServer: jest
      .spyOn(AdvancedSearchModule, "getAdvancedSearchesFromServer")
      .mockResolvedValue(getAdvancedSearchesFromServerResult),
    mockCollections: jest.spyOn(CollectionsModule, "collectionListSummary"),
    mockListUsers: jest.spyOn(UserModule, "listUsers"),
    mockCurrentUser: jest.spyOn(UserModule, "getCurrentUserDetails"),
    mockListClassification: jest.spyOn(
      SearchFacetsModule,
      "listClassifications",
    ),
    mockSearch: jest.spyOn(SearchModule, "searchItems"),
    mockImageGallerySearch: jest.spyOn(
      GallerySearchModule,
      "imageGallerySearch",
    ),
    mockVideoGallerySearch: jest.spyOn(
      GallerySearchModule,
      "videoGallerySearch",
    ),
    mockListImageGalleryClassifications: jest.spyOn(
      GallerySearchModule,
      "listImageGalleryClassifications",
    ),
    mockListVideoGalleryClassifications: jest.spyOn(
      GallerySearchModule,
      "listVideoGalleryClassifications",
    ),
    mockSearchSettings: jest.spyOn(
      SearchSettingsModule,
      "getSearchSettingsFromServer",
    ),
    mockConvertParamsToSearchOptions: jest.spyOn(
      SearchPageHelper,
      "generateSearchPageOptionsFromLocation",
    ),
    mockMimeTypeFilters: jest
      .spyOn(SearchFilterSettingsModule, "getMimeTypeFiltersFromServer")
      .mockResolvedValue(getMimeTypeFilters),
    mockSaveDataToLocalStorage: jest
      .spyOn(BrowserStorageModule, "saveDataToStorage")
      .mockImplementation(jest.fn),
    mockReadDataFromLocalStorage: jest.spyOn(
      BrowserStorageModule,
      "readDataFromStorage",
    ),
    mockGetAdvancedSearchByUuid: jest.spyOn(
      AdvancedSearchModule,
      "getAdvancedSearchByUuid",
    ),
    mockAddFavouriteSearch: jest
      .spyOn(FavouriteModule, "addFavouriteSearch")
      .mockResolvedValue({
        id: 123,
        name: "test",
        url: "/page/search?searchOptions=%7B%22rowsPerPage%22%3A10%2C%22currentPage%22%3A0%2C%22sortOrder%22%3A%22RATING%22%2C%22rawMode%22%3Afalse%2C%22status%22%3A%5B%22LIVE%22%2C%22REVIEW%22%5D%2C%22searchAttachments%22%3Atrue%2C%22query%22%3A%22crab%22%2C%22collections%22%3A%5B%5D%2C%22lastModifiedDateRange%22%3A%7B%7D%2C%22mimeTypeFilters%22%3A%5B%5D%2C%22dateRangeQuickModeEnabled%22%3Atrue%7D",
        addedAt: new Date("2025-08-01T01:42:02.960-05:00"),
      }),
  };
};

/**
 * There are a number of 'essential' mocks that need to be initialised for the SearchPage to function
 * correctly. This function can do that to those returned from `mockCollaborators()`.
 */
export const initialiseEssentialMocks = ({
  mockCollections,
  mockCurrentUser,
  mockListClassification,
  mockSearchSettings,
}: {
  mockCollections: jest.SpyInstance;
  mockCurrentUser: jest.SpyInstance;
  mockListClassification: jest.SpyInstance;
  mockSearchSettings: jest.SpyInstance;
}) => {
  mockCollections.mockResolvedValue(getCollectionMap);
  mockCurrentUser.mockResolvedValue(getCurrentUserMock);
  mockListClassification.mockResolvedValue(
    CategorySelectorMock.classifications,
  );
  mockSearchSettings.mockResolvedValue(
    SearchSettingsModule.defaultSearchSettings,
  );
};

/**
 * A mock specifically for the Promise for when searches are executed to be able to watch for when
 * a search etc. has occurred.
 */
export type MockedSearchPromise = jest.SpyInstance<
  Promise<OEQ.Search.SearchResult<OEQ.Search.SearchResultItem>>
>;

/**
 * Helper function to wrap the process of waiting for the completion of a search which is indicated
 * by the Spinner being removed from the page.
 */
export const waitForSearchCompleted = async () =>
  await waitForElementToBeRemoved(() =>
    screen.getByLabelText(languageStrings.searchpage.loading),
  );

/**
 * Helper function for the initial render of the `<SearchPage>` for tests below. Also includes
 * the wait for the initial search call.
 *
 * @param queryString a string to set the query bar to for initial search
 * @param screenWidth The emulated screen width, default to 1280px.
 * @param currentUser Details of currently signed-in user.
 *
 * @returns The RenderResult from the `render` of the `<SearchPage>`
 */
export const renderSearchPage = async (
  queryString?: string,
  screenWidth: number = 1280,
  currentUser: OEQ.LegacyContent.CurrentUserDetails = getCurrentUserMock,
): Promise<RenderResult> => {
  window.matchMedia = createMatchMedia(screenWidth);

  const history = createMemoryHistory();
  if (queryString) history.push(queryString);
  history.push({});

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
          <SearchPage updateTemplate={jest.fn()} />
        </AppContext.Provider>
      </Router>
    </ThemeProvider>,
  );
  // Wait for the first completion of initial search
  await waitForSearchCompleted();

  return page;
};

/**
 * Helper function to assist in finding the Refine Search panel.
 * @param container The root container where <RefineSearchPanel/> exists
 */
export const queryRefineSearchPanel = (
  container: Element,
): HTMLDivElement | null =>
  container.querySelector<HTMLDivElement>("#refine-panel");

/**
 * Similar to queryRefineSearchPanel but throws an error if RefineSearchPanel is not found.
 * @param container The root container where <RefineSearchPanel/> exists
 */
export const getRefineSearchPanel = (container: Element): HTMLDivElement => {
  const refineSearchPanel = queryRefineSearchPanel(container);
  if (!refineSearchPanel) {
    throw new Error("Unable to find refine search panel");
  }

  return refineSearchPanel;
};

/**
 * Similar to queryRefineSearchComponent but throws an error if the component is not found.
 *
 * @see queryRefineSearchComponent
 * @param container The root container to start the search from
 * @param componentSuffix Typically the `idSuffix` provided in `SearchPage.tsx`
 */
export const getRefineSearchComponent = (
  container: Element,
  componentSuffix: string,
) => {
  const e = queryRefineSearchComponent(container, componentSuffix);
  if (!e) {
    throw new Error(`Failed to find ${componentSuffix}`);
  }

  return e as HTMLElement;
};

/**
 * Helper function to find individual Refine Search components based on the their `idSuffix`,
 * or return null if the component is not found.
 *
 * @param container The root container to start the search from
 * @param componentSuffix Typically the `idSuffix` provided in `SearchPage.tsx`
 */
export const queryRefineSearchComponent = (
  container: Element,
  componentSuffix: string,
): HTMLElement | null => {
  const id = `#RefineSearchPanel-${componentSuffix}`;
  return container.querySelector(id);
};

/**
 * Helper function to assist in finding the Owner selector
 *
 * @param container a root container within which <OwnerSelector/> exists
 */
export const queryOwnerSelector = (container: HTMLElement) =>
  queryRefineSearchComponent(container, "OwnerSelector");

/**
 * Helper function to assist in finding the Date Range selector
 *
 * @param container a root container within which <DateRangeSelector/> exists
 */
export const queryDateRangeSelector = (container: HTMLElement) =>
  queryRefineSearchComponent(container, "DateRangeSelector");

/**
 * Helper function to assist in finding the Search Attachments selector
 *
 * @param container a root container within which <SearchAttachmentsSelector/> exists
 */
export const querySearchAttachmentsSelector = (container: HTMLElement) =>
  queryRefineSearchComponent(container, "SearchAttachmentsSelector");

/**
 * Helper function to assist in finding the Collection selector
 *
 * @param container a root container within which <CollectionSelector/> exists
 */
export const queryCollectionSelector = (container: HTMLElement) =>
  queryRefineSearchComponent(container, "CollectionSelector");

/**
 * Helper function to assist in finding the Remote Search selector
 *
 * @param container a root container within which `Remote Search selector` exists
 */
export const queryRemoteSearchSelector = (container: HTMLElement) =>
  queryRefineSearchComponent(container, "RemoteSearchSelector");

/**
 * Helper function to assist in finding the Status selector
 *
 * @param container a root container within which <StatusSelector/> exists
 */
export const queryStatusSelector = (container: HTMLElement) =>
  queryRefineSearchComponent(container, "StatusSelector");

/**
 * Helper function to assist in finding the Classification panel.
 * @param container The root container where <ClassificationsPanel/> exists
 */
export const queryClassificationPanel = (
  container: Element,
): HTMLDivElement | null =>
  container.querySelector<HTMLDivElement>("#classification-panel");

/**
 * Similar to queryClassificationPanel but throws an error if ClassificationsPanel is not found.
 * @param container The root container where <ClassificationsPanel/> exists
 */
export const getClassificationPanel = (container: Element): HTMLDivElement => {
  const classificationPanel = queryClassificationPanel(container);
  if (!classificationPanel) {
    throw new Error("Unable to find Classification panel");
  }

  return classificationPanel;
};

/**
 * Interacts with search pages to click the 'favourite search' button and create a new favourite
 * search with the specified `favouriteName`. If the UI indicates all was successful, will return
 * `true`.
 */
export const addSearchToFavourites = async (
  { getByLabelText, getByRole }: RenderResult,
  favouriteName: string,
): Promise<boolean> => {
  const {
    saveSearchConfirmationText: successMsg,
    saveSearchFailedText: failMsg,
  } = languageStrings.searchpage.favouriteSearch;

  const heartIcon = getByLabelText(
    languageStrings.searchpage.favouriteSearch.title,
    {
      selector: "button",
    },
  );
  await userEvent.click(heartIcon);

  const dialog = getByRole("dialog");
  const favouriteNameInput = getMuiTextField(
    dialog,
    languageStrings.searchpage.favouriteSearch.text,
  );
  await userEvent.type(favouriteNameInput, favouriteName);
  const confirmButton = getMuiButtonByText(
    dialog,
    languageStrings.common.action.ok,
  );

  await userEvent.click(confirmButton);

  // Now wait until either a success or fail message is displayed
  await waitFor(() =>
    screen.getByText(new RegExp(`(${successMsg})|(${failMsg})`)),
  );

  return !!screen.queryByText(successMsg);
};

/**
 * Helper function to assist in finding the MIME type selector.
 *
 * @param container a root container within which `Remote Search selector` exists
 */
export const queryMimeTypesSelector = (container: HTMLElement) =>
  queryRefineSearchComponent(container, "MIMETypeSelector");

/**
 * Helper function to assist in finding the Search bar.
 *
 * @param container a root container within which `Remote Search selector` exists
 */
export const getQueryBar = (container: Element): HTMLElement => {
  const queryBar = container.querySelector<HTMLElement>("#searchBar");
  if (!queryBar) {
    throw new Error("Failed to locate the search bar, unable to continue.");
  }

  return queryBar;
};

/**
 * Interact with the Search Bar to type the provided query. The wildcard mode is enabled by default.
 * In order to test the debouncing properly, this function will set up a testing environment to use the fake timer.
 * It's recommended to call 'useFakeTimer()' in `beforeEach` and call `runOnlyPendingTimers` and `useRealTimer`
 * in `afterEach`.
 *
 * @param container Root container within which the Search Bar exists
 * @param query Query to be typed in the textfield.
 * @param wildcardMode `true` to enable wildcard mode.
 */
export const changeQuery = async (
  container: Element,
  query: string,
  wildcardMode: boolean = true,
) => {
  const fakeTimerUser = userEvent.setup({
    advanceTimers: jest.advanceTimersByTime,
  });

  if (!wildcardMode) {
    const wildcardModeSwitch = container.querySelector("#wildcardSearch");
    if (!wildcardModeSwitch) {
      throw new Error("Failed to find the raw mode switch!");
    }
    await fakeTimerUser.click(wildcardModeSwitch);
  }

  const queryBar = getQueryBar(container);

  await fakeTimerUser.type(queryBar, query);
  await act(async () => {
    jest.advanceTimersByTime(1000);
  });

  await waitFor(() => {
    expect(queryBar).toHaveDisplayValue(query);
  });
};

/**
 * Helper function to find all list items in a search result.
 *
 * @param container The root container to search within.
 */
export const queryListItems = (container: HTMLElement) =>
  queryAllByLabelText(container, listItemAriaLabel);

/**
 * Helper function to find all gallery items in a search result.
 *
 * @param container The root container to search within.
 */
export const queryGalleryItems = (container: HTMLElement) =>
  queryAllByLabelText(container, galleryItemAriaLabel);
