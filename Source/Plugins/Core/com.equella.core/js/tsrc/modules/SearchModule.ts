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
import { DateRange, getISODateString } from "../util/Date";
import type { Collection } from "./CollectionsModule";
import type { SelectedCategories } from "./SearchFacetsModule";
import type { MimeTypeFilter } from "./SearchFilterSettingsModule";

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

/**
 * A Runtypes object which represents three display modes: list, gallery-image and gallery-video.
 */
export const DisplayModeRuntypes = Union(
  Literal("list"),
  Literal("gallery-image"),
  Literal("gallery-video")
);

/**
 * Available modes for displaying search results.
 * @see { @link DisplayModeRuntypes } for original definition.
 */
export type DisplayMode = Static<typeof DisplayModeRuntypes>;

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
}

/**
 * The type representing fields of SearchOptions.
 */
export type SearchOptionsFields = keyof SearchOptions;

export const defaultSearchOptions: SearchOptions = {
  rowsPerPage: 10,
  currentPage: 0,
  sortOrder: undefined,
  rawMode: false,
  status: liveStatuses,
  searchAttachments: true,
  query: "",
  collections: [],
  lastModifiedDateRange: { start: undefined, end: undefined },
  owner: undefined,
  mimeTypes: [],
  mimeTypeFilters: [],
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
 * Generates a Where clause through Classifications.
 * Each Classification that has categories selected is joined by AND.
 * Each selected category of one Classification is joined by OR.
 *
 * @param selectedCategories A list of selected Categories grouped by Classification ID.
 */
export const generateCategoryWhereQuery = (
  selectedCategories?: SelectedCategories[]
): string | undefined => {
  if (!selectedCategories || selectedCategories.length === 0) {
    return undefined;
  }

  const and = " AND ";
  const or = " OR ";
  const processNodeTerms = (
    categories: string[],
    schemaNode?: string
  ): string => categories.map((c) => `/xml${schemaNode}='${c}'`).join(or);

  return selectedCategories
    .filter((c) => c.categories.length > 0)
    .map(
      ({ schemaNode, categories }: SelectedCategories) =>
        `(${processNodeTerms(categories, schemaNode)})`
    )
    .join(and);
};

/**
 * A function that converts search options to search params.
 *
 * @param searchOptions Search options to be converted to search params.
 */
const buildSearchParams = ({
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
  mimeTypes,
  mimeTypeFilters,
  externalMimeTypes,
  musts,
}: SearchOptions): OEQ.Search.SearchParams => {
  const processedQuery = query ? formatQuery(query, !rawMode) : undefined;
  // We use selected filters to generate MIME types. However, in Image Gallery,
  // image MIME types are applied before any filter gets selected.
  // So the logic is, we use MIME type filters if any are selected, or specific MIME types
  // already provided by the Image Gallery.
  const _mimeTypes =
    mimeTypeFilters && mimeTypeFilters.length > 0
      ? mimeTypeFilters.flatMap((f) => f.mimeTypes)
      : mimeTypes;
  return {
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
    mimeTypes: externalMimeTypes ?? _mimeTypes,
    musts: musts,
  };
};

/**
 * A function that executes a search with provided search options.
 *
 * @param searchOptions Search options selected on Search page.
 */
export const searchItems = (
  searchOptions: SearchOptions
): Promise<OEQ.Search.SearchResult<OEQ.Search.SearchResultItem>> =>
  OEQ.Search.search(API_BASE_URL, buildSearchParams(searchOptions));

/**
 * A function that builds a URL for exporting a search result
 *
 * @param searchOptions Search options selected on Search page.
 */
export const buildExportUrl = (searchOptions: SearchOptions): string =>
  OEQ.Search.buildExportUrl(
    API_BASE_URL,
    buildSearchParams({ ...searchOptions, currentPage: 0 })
  );

/**
 * Send a request to confirm if an export is valid.
 *
 * @param searchOptions Search options selected on Search page.
 */
export const confirmExport = (searchOptions: SearchOptions): Promise<boolean> =>
  OEQ.Search.confirmExportRequest(
    API_BASE_URL,
    buildSearchParams(searchOptions)
  );
