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
import { pipe } from 'fp-ts/function';
import { DELETE, GET, POST } from './AxiosInstance';
import { FavouriteItemCodec, FavouriteSearchRawCodec } from './gen/Favourite';
import { SearchResultCodec } from './gen/Search';
import { SearchResult } from './Search';
import type { SortOrder } from './Search';
import { convertDateFields, validate } from './Utils';

/**
 * Type matching server-side Favourite Item model
 */
export interface FavouriteItem {
  /**
   * Item's ID consisting of Item's UUID and version
   */
  itemID: string;
  /**
   * Tags of this Favourite Item
   */
  keywords: string[];
  /**
   * Whether this Favourite Item uses latest Item version
   */
  isAlwaysLatest: boolean;
  /**
   * ID of the related Bookmark.
   */
  bookmarkID?: number;
}

/**
 * Shared properties of raw and transformed Favourite Searches.
 */
interface FavouriteSearchBase {
  /** Unique ID of a favourite search. */
  id: number;
  /** Name of the favourite search */
  name: string;
  /**
   * Relative path to the new Search UI including query strings
   * If the search is added from new Search UI, the URL will be `/page/search?searchOptions=xxxxx`.
   * If it's added from old UI, the URL will be `searching.do?xxx=yyy`.
   */
  url: string;
}

/**
 * Favourite Search as it is returned by API
 */
interface FavouriteSearchRaw extends FavouriteSearchBase {
  /** The date when favourite was created. */
  addedAt: string;
}

/**
 * Full details of a Favourite search.
 */
export interface FavouriteSearch extends FavouriteSearchBase {
  /** The date when favourite was created. */
  addedAt: Date;
}

/**
 * Query parameters accepted to fetch favourite Searches
 */
export interface FavouriteSearchParams {
  /**
   * Search query
   */
  query?: string;
  /**
   * The first record of the results to return.
   */
  start?: number;
  /**
   * The number of results to return.
   */
  length?: number;
  /**
   * Return only favourite searches **added on or before** this date
   * (`yyyy‑MM‑dd`).
   */
  addedBefore?: string;
  /**
   * Return only favourite searches **added on or after** this date
   * (`yyyy‑MM‑dd`).
   */
  addedAfter?: string;
  /**
   * Field used to sort the results.
   */
  order?: SortOrder;
}

const FAVOURITE_ITEM_PATH = '/favourite/item';
const FAVOURITE_SEARCH_PATH = '/favourite/search';

const favouriteSearchResultValidator = pipe(
  FavouriteSearchRawCodec,
  SearchResultCodec,
  validate
);

const processRawFavouriteSearch = <R, D>(data: R) =>
  convertDateFields<D>(data, ['addedAt']);

/**
 * Add an Item to user's favourites.
 * @param apiBasePath Base URI to the oEQ institution and API
 * @param favouriteItem The Item to be added
 */
export const addFavouriteItem = (
  apiBasePath: string,
  favouriteItem: FavouriteItem
): Promise<FavouriteItem> =>
  POST(
    apiBasePath + FAVOURITE_ITEM_PATH,
    validate(FavouriteItemCodec),
    favouriteItem
  );

/**
 * Delete one Item from user's favourites.
 * @param apiBasePath Base URI to the oEQ institution and API
 * @param bookmarkID ID of a bookmark
 */
export const deleteFavouriteItem = (
  apiBasePath: string,
  bookmarkID: number
): Promise<void> =>
  DELETE(`${apiBasePath}${FAVOURITE_ITEM_PATH}/${bookmarkID}`);

/**
 * Add a search to user's favourites.
 * @param apiBasePath Base URI to the oEQ institution and API
 * @param searchDetails details of the new favourite search
 */
export const addFavouriteSearch = (
  apiBasePath: string,
  searchDetails: Omit<FavouriteSearchBase, 'id'>
): Promise<FavouriteSearch> =>
  POST(
    apiBasePath + FAVOURITE_SEARCH_PATH,
    validate(FavouriteSearchRawCodec),
    searchDetails
  ).then(processRawFavouriteSearch<FavouriteSearchRaw, FavouriteSearch>);

/**
 * Delete a search from user's favourites.
 * @param apiBasePath Base URI to the oEQ institution and API
 * @param searchID ID of the favourite search
 */
export const deleteFavouriteSearch = (
  apiBasePath: string,
  searchID: number
): Promise<void> =>
  DELETE(`${apiBasePath}${FAVOURITE_SEARCH_PATH}/${searchID}`);

/**
 * Search for all favourite searches with specified search criteria.
 * @param apiBasePath Base URI to the oEQ institution and API
 * @param params Query parameters as search criteria.
 */
export const searchFavouriteSearches = (
  apiBasePath: string,
  params?: FavouriteSearchParams
): Promise<SearchResult<FavouriteSearch>> =>
  GET<SearchResult<FavouriteSearchRaw>>(
    apiBasePath + FAVOURITE_SEARCH_PATH,
    favouriteSearchResultValidator,
    params
  ).then(
    processRawFavouriteSearch<
      SearchResult<FavouriteSearchRaw>,
      SearchResult<FavouriteSearch>
    >
  );
