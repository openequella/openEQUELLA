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
import { is } from 'typescript-is';
import { DELETE, POST } from './AxiosInstance';

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
 * Type matching server-side model FavouriteSearch
 */
export interface FavouriteSearch {
  /**
   * Unique ID of a favourite search
   */
  id: number;
  /**
   * Name of a favourite search
   */
  name: string;
  /**
   * Relative path to the new Search UI including query strings
   * If the search is added from new Search UI, the URL will be `/page/search?searchOptions=xxxxx`.
   * If it's added from old UI, the URL will be `searching.do?xxx=yyy`.
   */
  url: string;
  /**
   * Owner of a favourite search
   */
  owner: string;
  /**
   * Last modified date
   */
  dateModified: string;
  /**
   * Name of selected Collection
   */
  within?: string;
  /**
   * Search query
   */
  query?: string;
  /**
   * Advanced search criteria
   */
  criteria?: string;
}

/**
 * Data structure for adding a search definition to user's favourite search
 */
export interface FavouriteSearchModel {
  /**
   * ID of a favourite search. The value is null when the search doesn't persist to DB.
   */
  id?: number;
  /**
   * Name of a search definition
   */
  name: string;
  /**
   * Relative path to the new Search UI, including all query params.
   */
  url: string;
}

const FAVOURITE_ITEM_PATH = '/favourite/item';
const FAVOURITE_SEARCH_PATH = '/favourite/search';

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
    (data): data is FavouriteItem => is<FavouriteItem>(data),
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
  DELETE<void>(`${apiBasePath}${FAVOURITE_ITEM_PATH}/${bookmarkID}`);

/**
 * Add a search to user's favourites.
 * @param apiBasePath Base URI to the oEQ institution and API
 * @param searchInfo required information for adding a favourite search
 */
export const addFavouriteSearch = (
  apiBasePath: string,
  searchInfo: FavouriteSearchModel
): Promise<FavouriteSearchModel> =>
  POST(
    apiBasePath + FAVOURITE_SEARCH_PATH,
    (data): data is FavouriteSearchModel => is<FavouriteSearchModel>(data),
    searchInfo
  );
