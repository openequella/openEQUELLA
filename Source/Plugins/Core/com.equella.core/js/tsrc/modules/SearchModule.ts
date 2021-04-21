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
import { Literal, Static, Union } from "runtypes";
import { API_BASE_URL } from "../AppConfig";
import { buildSearchParams } from "../search/SearchPageHelper";
import { Collection } from "./CollectionsModule";
import { SelectedCategories } from "./SearchFacetsModule";
import { MimeTypeFilter } from "./SearchFilterSettingsModule";

/**
 * Type of all search options on Search page
 */
export interface SearchOptions {
  /**
   * The query string of the current search. Can be left blank for a default search.
   */
  query?: string;
  /**
   * The number of items displayed in one page.
   */
  rowsPerPage: number;
  /**
   * Selected page.
   */
  currentPage: number;
  /**
   * Selected search result sorting order.
   */
  sortOrder: OEQ.SearchSettings.SortOrder | undefined;
  /**
   * A list of collections.
   */
  collections?: Collection[];
  /**
   * Whether to send the `query` as is (true) or to apply some processing (such as appending
   * a wildcard operator).
   */
  rawMode: boolean;
  /**
   * A date range for searching items by last modified date.
   */
  lastModifiedDateRange?: DateRange;
  /**
   * A user for which to filter the search by based on ownership of items.
   */
  owner?: OEQ.UserQuery.UserDetails;
  /**
   * Filter search results to only include items with the specified statuses.
   */
  status?: OEQ.Common.ItemStatus[];
  /**
   * A list of categories selected in the Category Selector and grouped by Classification ID.
   */
  selectedCategories?: SelectedCategories[];
  /**
   * Whether to search attachments or not.
   */
  searchAttachments?: boolean;
  /**
   * A list of MIME types generated from filters or provided by Image/Video Gallery.
   */
  mimeTypes?: string[];
  /**
   * A list of selected MIME type filters.
   */
  mimeTypeFilters?: MimeTypeFilter[];
  /**
   * A list of MIME types provided by an Integration (e.g. with Moodle), which has a higher priority than `mimeTypes`.
   */
  externalMimeTypes?: string[];
  /**
   * List of search index key/value pairs to filter by. e.g. videothumb:true or realthumb:true.
   *
   * @see OEQ.Search.SearchParams for examples
   */
  musts?: OEQ.Search.Must[];
  /**
   * How to display the search results - also determines the type of results.
   */
  displayMode?: DisplayMode;
}

/**
 * The type representing fields of SearchOptions.
 */
export type SearchOptionsFields = keyof SearchOptions;

export const DisplayModeRuntypes = Union(
  Literal("list"),
  Literal("gallery-image"),
  Literal("gallery-video")
);

export type DisplayMode = Static<typeof DisplayModeRuntypes>;

/**
 * Represent a date range which has an optional start and end.
 */
export interface DateRange {
  /**
   * The start date of a date range.
   */
  start?: Date;
  /**
   * The end date of a date range.
   */
  end?: Date;
}

export const isDate = (value: unknown): value is Date => value instanceof Date;

/**
 * A function that executes a search with provided search options.
 * @param searchOptions Search options selected on Search page.
 */
export const searchItems = (
  searchOptions: SearchOptions
): Promise<OEQ.Search.SearchResult<OEQ.Search.SearchResultItem>> =>
  OEQ.Search.search(API_BASE_URL, buildSearchParams(searchOptions));

/**
 * A function that builds a URL for exporting a search result
 * @param searchOptions Search options selected on Search page.
 */
export const buildExportUrl = (searchOptions: SearchOptions): string =>
  OEQ.Search.buildExportUrl(
    API_BASE_URL,
    buildSearchParams({ ...searchOptions, currentPage: 0 })
  );
