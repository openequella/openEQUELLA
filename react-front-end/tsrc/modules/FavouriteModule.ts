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
import { flow, pipe } from "fp-ts/function";
import * as O from "fp-ts/Option";
import { API_BASE_URL } from "../AppConfig";
import { getISODateString } from "../util/Date";
import { buildSearchParams, formatQuery, SearchOptions } from "./SearchModule";

/**
 * URLs stored for favourites are only partial URLs in that they _only_ contain the path (no host
 * details) and search parameters.
 */
export interface FavouriteURL {
  readonly path: string;
  readonly params: URLSearchParams;
}

/**
 * Represents the search options specific to searching for favourite searches.
 */
type FavSearchesSearchOptions = Pick<
  SearchOptions,
  | "query"
  | "rowsPerPage"
  | "currentPage"
  | "sortOrder"
  | "lastModifiedDateRange"
>;

/**
 * Expands a query string to include searching within `bookmark_tags`.
 *
 * @param query The base query string.
 * @returns A new query string that also searches in `bookmark_tags`.
 */
export const expandQueryWithBookmarkTags = (query: string): string =>
  `${query} OR bookmark_tags:(${query})`;

/**
 *  Appends bookmark tag search to the user's query if provided.
 *  e.g. "apple" → "apple OR bookmark_tags:(apple)"
 *
 * @param params The search parameters to be sent to the API.
 * @returns Search parameters where the `query` has been updated to include `bookmark_tags`.
 */
const buildFavouriteItemsQuery = (
  params: OEQ.Search.SearchParams,
): OEQ.Search.SearchParams => ({
  ...params,
  query: pipe(
    params.query,
    O.fromNullable,
    O.map(expandQueryWithBookmarkTags),
    O.toUndefined,
  ),
});

export const defaultFavSearchesSearchOptions: FavSearchesSearchOptions = {
  rowsPerPage: 10,
  currentPage: 0,
  sortOrder: "added",
  query: "",
  lastModifiedDateRange: { start: undefined, end: undefined },
};

/**
 * A function that converts search options to FavouriteSearchParams.
 *
 * @param searchOptions Search options to be converted to search params.
 */
const buildFavouriteSearchParams = ({
  query,
  rowsPerPage,
  currentPage,
  sortOrder,
  lastModifiedDateRange,
}: FavSearchesSearchOptions): OEQ.Favourite.FavouriteSearchParams => ({
  query: formatQuery(query),
  start: currentPage * rowsPerPage,
  length: rowsPerPage,
  order: sortOrder,
  addedBefore: getISODateString(lastModifiedDateRange?.end),
  addedAfter: getISODateString(lastModifiedDateRange?.start),
});

export type FavouritesType = "resources" | "searches";

/**
 * Add an Item to user's favourites.
 * @param itemID Item's unique ID
 * @param keywords Tags of a favourite Item
 * @param isAlwaysLatest `true` to always use Item's latest version
 * @return Details of the newly created favourite from the server.
 */
export const addFavouriteItem = (
  itemID: string,
  keywords: string[],
  isAlwaysLatest: boolean,
): Promise<OEQ.Favourite.FavouriteItem> =>
  OEQ.Favourite.addFavouriteItem(API_BASE_URL, {
    itemID,
    keywords,
    isAlwaysLatest,
  }).then((newFavouriteItem: OEQ.Favourite.FavouriteItem) => newFavouriteItem);

/**
 * Delete one Item from user's favourites.
 * @param bookmarkID ID of a bookmark
 */
export const deleteFavouriteItem = (bookmarkID: number): Promise<void> =>
  OEQ.Favourite.deleteFavouriteItem(API_BASE_URL, bookmarkID);

/**
 * Searches favourite items for the current user.
 * Extends the query to include bookmark tags
 *
 * @param searchOptions Base search options
 */
export const searchFavouriteItems = async (
  searchOptions: SearchOptions,
): Promise<OEQ.Search.SearchResult<OEQ.Search.SearchResultItem>> => {
  const buildFavItemsSearchParams = flow(
    buildSearchParams,
    buildFavouriteItemsQuery,
  );

  return OEQ.Search.search(
    API_BASE_URL,
    buildFavItemsSearchParams(searchOptions),
  );
};

/**
 * Add a search definition to user's favourites
 * @param name Name of a search definition
 * @param url Path to the new Search UI, including all query strings
 */
export const addFavouriteSearch = (
  name: string,
  { path, params }: FavouriteURL,
): Promise<OEQ.Favourite.FavouriteSearch> =>
  OEQ.Favourite.addFavouriteSearch(API_BASE_URL, {
    name,
    url: `${path}?${params.toString()}`,
  });

/**
 * Delete a favourite search by favourite search ID
 * @param searchID Favourite search ID
 */
export const deleteFavouriteSearch = (searchID: number): Promise<void> =>
  OEQ.Favourite.deleteFavouriteSearch(API_BASE_URL, searchID);

/**
 * A function that executes a search with provided search options to get the favourite searches for the current user.
 * @param searchOptions options to search for favourite searches
 */
export const searchFavouriteSearches = (
  searchOptions: FavSearchesSearchOptions,
): Promise<OEQ.Search.SearchResult<OEQ.Favourite.FavouriteSearch>> =>
  pipe(searchOptions, buildFavouriteSearchParams, (params) =>
    OEQ.Favourite.searchFavouriteSearches(API_BASE_URL, params),
  );
