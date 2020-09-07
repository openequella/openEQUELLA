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
import { SelectedCategories } from "./SearchFacetsModule";
import { SortOrder } from "./SearchSettingsModule";
import { Collection } from "./CollectionsModule";
import { DateRange } from "../components/DateRangeSelector";
import { getISODateString } from "../util/Date";

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
  sortOrder: SortOrder | undefined;
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
}

/**
 * List of status which are considered 'live'.
 */
export const liveStatuses: OEQ.Common.ItemStatus[] = ["LIVE", "REVIEW"];

/**
 * Predicate for checking if a provided status is not one of `liveStatuses`.
 * @param status a status to check for liveliness
 */
export const nonLiveStatus = (status: OEQ.Common.ItemStatus): boolean =>
  !liveStatuses.find((liveStatus) => status === liveStatus);

/**
 * List of statuses which are considered non-live.
 */
export const nonLiveStatuses: OEQ.Common.ItemStatus[] = OEQ.Common.ItemStatuses.alternatives
  .map((status) => status.value)
  .filter(nonLiveStatus);

export const defaultSearchOptions: SearchOptions = {
  rowsPerPage: 10,
  currentPage: 0,
  sortOrder: undefined,
  rawMode: false,
  status: liveStatuses,
  searchAttachments: true,
};

export const defaultPagedSearchResult: OEQ.Common.PagedResult<OEQ.Search.SearchResultItem> = {
  start: 0,
  length: 10,
  available: 10,
  results: [],
};

/**
 * Helper function, to support formatting of query in raw mode. When _not_ raw mode
 * we append a wildcard to support the idea of a simple (typeahead) search.
 *
 * @param query the intended search query to be sent to the API
 * @param addWildcard whether a wildcard should be appended
 */
export const formatQuery = (query: string, addWildcard: boolean): string => {
  const trimmedQuery = query ? query.trim() : "";
  const appendWildcard = addWildcard && trimmedQuery.length > 0;
  return trimmedQuery + (appendWildcard ? "*" : "");
};

/**
 * Generates a Where clause for search. Each condition is linked by a AND.
 *
 * @param selectedCategories A list of selected Categories grouped by Classification ID.
 */
export const generateCategoryWhereQuery = (
  selectedCategories?: SelectedCategories[]
): string | undefined => {
  if (!selectedCategories || selectedCategories.length === 0) {
    return undefined;
  }
  // Convert a list of categories into a list of where clause search conditions.
  // The format is "node='category'".
  const processNodeTerms = (
    categories: string[],
    schemaNode?: string
  ): string[] => categories.map((c) => `/xml${schemaNode}='${c}'`);

  const and = " AND ";
  // Concatenate all search conditions with AND.
  return selectedCategories
    .flatMap(({ schemaNode, categories }: SelectedCategories) =>
      processNodeTerms(categories, schemaNode)
    )
    .join(and);
};

/**
 * A function that takes search options and converts search options to search params,
 * and then does a search and returns a list of Items.
 * @param searchOptions  Search options selected on Search page.
 */
export const searchItems = ({
  query,
  rowsPerPage,
  currentPage,
  sortOrder,
  collections,
  rawMode,
  lastModifiedDateRange,
  owner,
  status = liveStatuses,
  searchAttachments,
  selectedCategories,
}: SearchOptions): Promise<
  OEQ.Common.PagedResult<OEQ.Search.SearchResultItem>
> => {
  const processedQuery = query ? formatQuery(query, !rawMode) : undefined;
  const searchParams: OEQ.Search.SearchParams = {
    query: processedQuery,
    start: currentPage * rowsPerPage,
    length: rowsPerPage,
    status: status,
    order: sortOrder,
    collections: collections?.map((collection) => collection.uuid),
    modifiedAfter: getISODateString(lastModifiedDateRange?.start),
    modifiedBefore: getISODateString(lastModifiedDateRange?.end),
    owner: owner?.id,
    searchAttachments: searchAttachments,
    whereClause: generateCategoryWhereQuery(selectedCategories),
  };
  return OEQ.Search.search(API_BASE_URL, searchParams);
};
