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
import type { ReactNode } from "react";
import * as React from "react";
import { searchFavouriteSearches } from "../modules/FavouriteModule";
import type { SearchOptions } from "../modules/SearchModule";
import SearchResult from "../search/components/SearchResult";
import {
  defaultSearchPageOptions,
  SearchPageHeaderConfig,
} from "../search/SearchPageHelper";
import type { SearchPageSearchResult } from "../search/SearchPageReducer";
import FavouritesSearch from "./components/FavouritesSearch";

export const defaultFavouritesSearchesOptions = {
  ...defaultSearchPageOptions,
};

/**
 * Build the bookmark_owner musts clause for the supplied user.
 * Returns undefined when no user is provided.
 */
export const buildBookmarkOwnerMusts = (
  user?: OEQ.LegacyContent.CurrentUserDetails,
): OEQ.Search.Must[] | undefined =>
  pipe(
    O.fromNullable(user),
    O.map((user) => [["bookmark_owner", [user.id]]] as OEQ.Search.Must[]),
    O.toUndefined,
  );

export const getDefaultFavouritesItemsOptions = (
  user?: OEQ.LegacyContent.CurrentUserDetails,
) => ({
  ...defaultSearchPageOptions,
  musts: buildBookmarkOwnerMusts(user),
});

export const favouritesPageHeaderConfig: SearchPageHeaderConfig = {
  enableCSVExportButton: false,
  enableShareSearchButton: false,
  enableFavouriteSearchButton: false,
};

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
        />
      );
    }),
  );

/**
 * Render the favourite searches search results.
 *
 * @param searchResults - The search results to render.
 * @param highlight - The list of highlight terms.
 */
export const favouritesSearchesResult = (
  searchResults: OEQ.Favourite.FavouriteSearch[],
  highlight: string[],
): ReactNode =>
  pipe(
    searchResults,
    A.map((search) => (
      <FavouritesSearch
        key={search.id}
        favouriteSearch={search}
        highlights={highlight}
      />
    )),
  );

/**
 * Returns true when the supplied favourites type represents favourite resources
 * (as opposed to favourite searches).
 *
 * @param favouritesType A string indicating the current favourites type.
 */
export const isFavouritesTypeResources = (favouritesType: string) =>
  favouritesType === "resources";
