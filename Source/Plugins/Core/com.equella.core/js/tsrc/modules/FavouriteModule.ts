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

/**
 * A type consisting of Item's UUID, version, bookmark ID and the flag of
 * whether the Item is on the latest version.
 */
export type FavouriteItemInfo = Pick<
  OEQ.Search.SearchResultItem,
  "bookmarkId" | "uuid" | "version" | "isLatestVersion"
>;

export const defaultFavouriteItem: FavouriteItemInfo = {
  uuid: "",
  version: 0,
  bookmarkId: 0,
  isLatestVersion: false,
};

export const addFavouriteItem = (
  itemID: string,
  keywords: string[],
  isAlwaysLatest: boolean
) =>
  OEQ.Favourite.addFavouriteItem(API_BASE_URL, {
    itemID,
    keywords,
    isAlwaysLatest,
  }).then((newFavouriteItem: OEQ.Favourite.FavouriteItem) => newFavouriteItem);

export const deleteFavouriteItem = (bookmarkID: number) =>
  OEQ.Favourite.deleteFavouriteItem(API_BASE_URL, bookmarkID);
