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
import { API_BASE_URL } from "../AppConfig";
import * as t from "io-ts";

/**
 * Type for the two version options.
 */
export type FavouriteItemVersionOption = "latest" | "this";

export const FavouritesTypeCodec = t.union([
  t.literal("resources"),
  t.literal("searches"),
]);

export type FavouritesType = t.TypeOf<typeof FavouritesTypeCodec>;

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
 * URLs stored for favourites are only partial URLs in that they _only_ contain the path (no host
 * details) and search parameters.
 */
export interface FavouriteURL {
  readonly path: string;
  readonly params: URLSearchParams;
}

/**
 * Add a search definition to user's favourites
 * @param name Name of a search definition
 * @param url Path to the new Search UI, including all query strings
 */
export const addFavouriteSearch = (
  name: string,
  { path, params }: FavouriteURL,
): Promise<OEQ.Favourite.FavouriteSearchModel> =>
  OEQ.Favourite.addFavouriteSearch(API_BASE_URL, {
    name,
    url: `${path}?${params.toString()}`,
  });
