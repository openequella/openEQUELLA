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
import userEvent from "@testing-library/user-event";
import { createMemoryHistory } from "history";
import * as React from "react";
import { Router } from "react-router-dom";
import { getAdvancedSearchDefinition } from "../../../../__mocks__/AdvancedSearchModule.mock";
import { fullOptionsFavouriteSearch } from "../../../../__mocks__/Favourites.mock";
import FavouritesSearch, {
  FavouritesSearchProps,
} from "../../../../tsrc/favourites/components/FavouritesSearch";
import { languageStrings } from "../../../../tsrc/util/langstrings";
import {
  findByLabelText,
  findByText,
  render,
  RenderResult,
} from "@testing-library/react";
import { getCollectionMap } from "../../../../__mocks__/getCollectionsResp";
import { getHierarchyDetails } from "../../../../__mocks__/Hierarchy.mock";
import { getMimeTypeFilters } from "../../../../__mocks__/MimeTypeFilter.mock";
import * as UserModuleMock from "../../../../__mocks__/UserModule.mock";
import * as AdvancedSearchModule from "../../../../tsrc/modules/AdvancedSearchModule";
import * as CollectionsModule from "../../../../tsrc/modules/CollectionsModule";
import * as HierarchyModule from "../../../../tsrc/modules/HierarchyModule";
import * as SearchFilterSettingsModule from "../../../../tsrc/modules/SearchFilterSettingsModule";
import * as UserModule from "../../../../tsrc/modules/UserModule";
import * as FavouriteModule from "../../../../tsrc/modules/FavouriteModule";
import { updateMockGetBaseUrl } from "../../BaseUrlHelper";

const {
  searchCriteria: searchCriteriaLabels,
  showMoreSearchCriteria: showMoreSearchCriteriaLabel,
  remove: removeLabel,
} = languageStrings.favourites.favouritesSearch;
const { ok: okButtonLabel, cancel: cancelButtonLabel } =
  languageStrings.common.action;

/**
 * Mocks all API modules used by the `FavouritesSearch` component.
 *
 * @returns an object containing spy for `deleteFavouriteSearch` for use in tests.
 */
export const mockApis = (): { mockDeleteFavouriteSearch: jest.SpyInstance } => {
  jest
    .spyOn(HierarchyModule, "getHierarchyDetails")
    .mockImplementation(getHierarchyDetails);
  jest
    .spyOn(AdvancedSearchModule, "getAdvancedSearchByUuid")
    .mockResolvedValue(getAdvancedSearchDefinition);
  jest
    .spyOn(UserModule, "resolveUsers")
    .mockResolvedValue([UserModuleMock.users[0]]);
  jest
    .spyOn(CollectionsModule, "collectionListSummary")
    .mockResolvedValue(getCollectionMap);
  jest
    .spyOn(SearchFilterSettingsModule, "getMimeTypeFiltersFromServer")
    .mockResolvedValue(getMimeTypeFilters);
  updateMockGetBaseUrl();

  const mockDeleteFavouriteSearch = jest
    .spyOn(FavouriteModule, "deleteFavouriteSearch")
    .mockResolvedValue(undefined);

  return { mockDeleteFavouriteSearch };
};

/**
 * Renders the `FavouritesSearch` component with a `Router`, using default props
 * if none are supplied.
 *
 * @param props Optional props to pass to the component.
 * @returns The `RenderResult` from React Testing Library.
 */
export const renderFavouriteSearch = (
  props: FavouritesSearchProps = defaultProps,
): RenderResult => {
  const history = createMemoryHistory();

  return render(
    <Router history={history}>
      <FavouritesSearch {...props} />
    </Router>,
  );
};

export const defaultProps: FavouritesSearchProps = {
  favouriteSearch: fullOptionsFavouriteSearch,
  highlights: [],
  onFavouriteRemoved: jest.fn(),
};

/**
 * Wait for the search options to appear in the container.
 *
 * @param container The container that may contain the search options.
 */
export const waitForSearchOptions = (
  container: HTMLElement,
): Promise<HTMLElement> => findByText(container, searchCriteriaLabels);

/**
 * Helper function to click the "More search criteria" icon.
 *
 * @param container The container that contains the "More search criteria" icon.
 */
export const showAllSearchCriteria = async (
  container: HTMLElement,
): Promise<void> => {
  const moreOptions = await findByLabelText(
    container,
    showMoreSearchCriteriaLabel,
  );
  return userEvent.click(moreOptions);
};

/**
 * Helper to format a date string.
 */
export const dateString = (date: string): string =>
  new Date(date).toDateString();

/**
 * Open the `Remove from favourites` confirmation dialog and return the dialog element.
 *
 * @param page The RenderResult returned from rendering the component.
 */
export const openRemoveDialog = async (page: RenderResult) => {
  const removeButton = page.getByRole("button", { name: removeLabel });
  await userEvent.click(removeButton);
  return page.findByRole("dialog");
};

/**
 * Clicks the 'OK' button in the `Remove from favourites` confirmation dialog.
 *
 * @param page The RenderResult returned from rendering the component.
 */
export const clickOkInRemoveDialog = async (page: RenderResult) => {
  const confirmButton = page.getByRole("button", {
    name: okButtonLabel,
  });
  await userEvent.click(confirmButton);
};

/**
 * Clicks the 'Cancel' button in the `Remove from favourites` confirmation dialog.
 *
 * @param page The RenderResult returned from rendering the component.
 */
export const clickCancelInRemoveDialog = async (page: RenderResult) => {
  const cancelButton = page.getByRole("button", {
    name: cancelButtonLabel,
  });
  await userEvent.click(cancelButton);
};
