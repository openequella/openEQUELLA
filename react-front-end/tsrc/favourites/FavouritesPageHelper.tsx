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
import * as A from "fp-ts/Array";
import { pipe } from "fp-ts/function";
import * as O from "fp-ts/Option";
import * as H from "history";
import type { ReactNode } from "react";
import * as React from "react";
import {
  type FavouritesType,
  searchFavouriteSearches,
} from "../modules/FavouriteModule";
import type { SearchOptions } from "../modules/SearchModule";
import { SortOrderOptions } from "../search/components/SearchOrderSelect";
import SearchResult from "../search/components/SearchResult";
import { SearchPageHistoryState } from "../search/Search";
import {
  defaultSearchPageOptions,
  defaultSortingOptions,
  SearchPageHeaderConfig,
  SearchPageOptions,
} from "../search/SearchPageHelper";
import type { SearchPageSearchResult } from "../search/SearchPageReducer";
import { languageStrings } from "../util/langstrings";
import FavouritesSearch from "./components/FavouritesSearch";

const { title } = languageStrings.searchpage.sortOptions;
const { dateFavourited } = languageStrings.favourites.sortOptions;

export const SORT_ORDER_ADDED_AT = "added_at";

export const FAVOURITES_TYPE_PARAM = "favouritesType";

export const defaultFavouritesPageOptions: SearchPageOptions = {
  ...defaultSearchPageOptions,
  sortOrder: SORT_ORDER_ADDED_AT,
};

/**
 * Build the configuration for the SearchPageHeader.
 *
 * @param favouritesType The type of Favourites.
 */
export const favouritesPageHeaderConfig = (
  favouritesType: FavouritesType,
): SearchPageHeaderConfig => ({
  enableCSVExportButton: false,
  enableShareSearchButton: false,
  enableFavouriteSearchButton: false,
  newSearchConfig: {
    criteria: defaultFavouritesPageOptions,
  },
  customSortingOptions: sortOrderOptions(favouritesType),
});

export const favouritesSearchRefinePanelConfig = {
  enableDisplayModeSelector: false,
  enableCollectionSelector: false,
  enableAdvancedSearchSelector: false,
  enableRemoteSearchSelector: false,
  enableDateRangeSelector: false,
  enableMimeTypeSelector: false,
  enableOwnerSelector: false,
  enableItemStatusSelector: false,
  enableSearchAttachmentsSelector: false,
};

/**
 * Function to return a list of favourite searches.
 *
 * @param options - The search options.
 */
export const listFavouriteSearches = async (
  options: SearchOptions,
): Promise<SearchPageSearchResult> => ({
  from: "favourite-search",
  content: await searchFavouriteSearches(options),
});

/**
 * Render the favourite items search results.
 *
 * @param items - The search result items to render.
 * @param highlight - The list of highlight terms.
 * @param onRemoveFavouriteItem - Callback when a favourite item is removed.
 */
export const favouritesItemsResult = (
  items: OEQ.Search.SearchResultItem[],
  highlight: string[],
  onRemoveFavouriteItem: () => void,
) =>
  pipe(
    items,
    A.map((item) => {
      const { uuid, version } = item;
      return (
        <SearchResult
          key={`${uuid}/${version}`}
          item={item}
          highlights={highlight}
          onFavouriteRemoved={onRemoveFavouriteItem}
          showBookmarkTags
        />
      );
    }),
  );

/**
 * Render the favourite searches search results.
 *
 * @param searchResults - The search results to render.
 * @param highlight - The list of highlight terms.
 * @param onFavouriteRemoved - Callback invoked when a favourite search is removed.
 */
export const favouritesSearchesResult = (
  searchResults: OEQ.Favourite.FavouriteSearch[],
  highlight: string[],
  onFavouriteRemoved: () => void,
): ReactNode =>
  pipe(
    searchResults,
    A.map((search) => (
      <FavouritesSearch
        key={search.id}
        favouriteSearch={search}
        highlights={highlight}
        onFavouriteRemoved={onFavouriteRemoved}
      />
    )),
  );

/**
 * Returns true when the supplied favourites type represents favourite resources
 * (as opposed to favourite searches).
 *
 * @param favouritesType A string indicating the current favourites type.
 */
export const isFavouritesResources = (favouritesType: FavouritesType) =>
  favouritesType === "resources";

/**
 * Given a specific `FavouritesType` build the SortOrderOptions representing the options used in
 * UI for sorting in the related view.
 *
 * @param favouritesType the type of favourites to generate the options for
 */
export const sortOrderOptions = (
  favouritesType: FavouritesType,
): SortOrderOptions =>
  isFavouritesResources(favouritesType)
    ? new Map<OEQ.Search.SortOrder, string>([
        ...defaultSortingOptions,
        [SORT_ORDER_ADDED_AT, dateFavourited],
      ])
    : new Map<OEQ.Search.SortOrder, string>([
        ["name", title],
        [SORT_ORDER_ADDED_AT, dateFavourited],
      ]);

/**
 * Read the value of favourites type from History.
 */
export const getFavouritesTypeFromHistory = (
  history: H.History<SearchPageHistoryState<FavouritesType>>,
): O.Option<FavouritesType> =>
  O.fromNullable(history.location.state?.customData?.["favouritesType"]);

/**
 * Read the value of favourites type from URL search parameters.
 *
 * @param searchParams The URL search parameters.
 */
export const getFavouriteTypeFromSearchParams = (
  searchParams: URLSearchParams,
): O.Option<FavouritesType> =>
  pipe(
    searchParams.get(FAVOURITES_TYPE_PARAM),
    O.fromNullable,
    O.filter((favType) => favType === "resources" || favType === "searches"),
  );

/**
 * Get the initial favourites type from either History custom data or URL search parameters.
 * Defaults to "resources" if neither are present.
 *
 * @param history The history to read from.
 */
export const getInitialFavouritesType = (
  history: H.History<SearchPageHistoryState<FavouritesType>>,
): FavouritesType =>
  pipe(
    getFavouritesTypeFromHistory(history),
    O.alt(() =>
      getFavouriteTypeFromSearchParams(
        new URLSearchParams(history.location.search),
      ),
    ),
    O.getOrElse<FavouritesType>(() => "resources"),
  );

/**
 * Write the value of favourites type to History custom data.
 *
 * @param favouritesType The type of Favourites.
 * @param history The history to write to.
 */
export const writeFavouritesTypeToHistory = (
  favouritesType: FavouritesType,
  history: H.History<SearchPageHistoryState<FavouritesType>>,
) =>
  history.replace({
    ...history.location,
    state: {
      ...history.location.state,
      customData: {
        favouritesType,
      },
    },
  });
