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
import { API_BASE_URL } from "../config";
import { SortOrder } from "../settings/Search/SearchSettingsModule";

/**
 * A function that takes search options and converts search options to search params,
 * and then does a search and returns a list of Items.
 * @param searchOptions  Search options selected on Search page.
 */
export const searchItems = (
  searchOptions: SearchOptions
): Promise<OEQ.Common.PagedResult<OEQ.Search.SearchResultItem>> => {
  const { rowsPerPage, currentPage, sortOrder } = searchOptions;
  const searchParams: OEQ.Search.SearchParams = {
    start: currentPage * rowsPerPage,
    length: rowsPerPage,
    status: [
      "LIVE" as OEQ.Common.ItemStatus,
      "REVIEW" as OEQ.Common.ItemStatus,
    ],
    order: sortOrder,
  };
  return OEQ.Search.search(API_BASE_URL, searchParams);
};

export const defaultPagedSearchResult: OEQ.Common.PagedResult<OEQ.Search.SearchResultItem> = {
  start: 0,
  length: 10,
  available: 10,
  results: [],
};

/**
 * Type of all search options on Search page
 */
export interface SearchOptions {
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
  sortOrder: SortOrder | undefined;
}

export const defaultSearchOptions: SearchOptions = {
  rowsPerPage: 10,
  currentPage: 0,
  sortOrder: undefined,
};
