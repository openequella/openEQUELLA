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
import { DateRange } from "@material-ui/icons";
import * as OEQ from "@openequella/rest-api-client";
import { API_BASE_URL } from "../AppConfig";
import { Location } from "history";
import { map, pick } from "lodash";
import { Literal, match, Static, Union, Unknown } from "runtypes";
import { getISODateString } from "../util/Date";
import { Collection, collectionListSummary } from "./CollectionsModule";
import { SelectedCategories } from "./SearchFacetsModule";
import { SortOrder } from "./SearchSettingsModule";
import { resolveUsers } from "./UserModule";

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

/**
 * Legacy searching.do parameters currently supported by SearchPage component.
 */
const LegacySearchParams = Union(
  Literal("dp"),
  Literal("ds"),
  Literal("dr"),
  Literal("q"),
  Literal("sort"),
  Literal("owner"),
  Literal("in")
);

type LegacyParams = Static<typeof LegacySearchParams>;

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

export const defaultPagedSearchResult: OEQ.Search.SearchResult<OEQ.Search.SearchResultItem> = {
  start: 0,
  length: 10,
  available: 10,
  results: [],
  highlight: [],
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
 * A function that takes a parses a saved search query string from a shared legacy searching.do or /page/search URL, and converts it into a SearchOptions object
 * @param location object, typically obtained from the SearchPage component.
 * @return SearchOptions object, or undefined if there were no query string parameters.
 */
export const queryStringParamsToSearchOptions = async (
  location: Location
): Promise<SearchOptions | undefined> => {
  if (!location.search) return undefined;
  const params = new URLSearchParams(location.search);

  if (location.pathname.endsWith("searching.do")) {
    return await legacyQueryStringToSearchOptions(params);
  }
  return await newSearchQueryToSearchOptions(params.get("searchOptions") ?? "");
};

/**
 * A function that takes search options and converts it to to a json representation.
 * Collections and owner properties are both reduced down to their uuid and id properties respectively.
 * Undefined properties are excluded.
 * Intended to be used in conjunction with SearchModule.newSearchQueryToSearchOptions
 * @param searchOptions Search options selected on Search page.
 * @return url encoded key/value pair of JSON searchOptions
 */
export const generateQueryStringFromSearchOptions = (
  searchOptions: SearchOptions
): string => {
  const params = new URLSearchParams();
  params.set(
    "searchOptions",
    JSON.stringify(
      searchOptions,
      (key: string, value: (object | Collection)[]) => {
        return match(
          [
            Literal("collections"),
            () => value.map((collection) => pick(collection, ["uuid"])),
          ],
          [Literal("owner"), () => pick(value, ["id"])],
          [Unknown, () => value ?? undefined]
        )(key);
      }
    )
  );
  return params.toString();
};

/**
 * A function that takes a JSON representation of a SearchOptions object, and converts it into an actual SearchOptions object.
 * @param searchOptionsJSON a JSON representation of a SearchOptions object.
 * @return searchOptions Search options selected on Search page.
 */
export const newSearchQueryToSearchOptions = async (
  searchOptionsJson: string
): Promise<SearchOptions> => {
  const parsedOptions: SearchOptions = JSON.parse(
    searchOptionsJson,
    (key, value) => {
      if (key === "lastModifiedDateRange") {
        let { start, end } = value;
        start = start ? new Date(start) : undefined;
        end = end ? new Date(end) : undefined;
        return { start, end };
      }
      return value;
    }
  );
  const collectionUuids: string[] | undefined = map(
    parsedOptions?.collections,
    "uuid"
  );
  const ownerId: string | undefined = parsedOptions?.owner?.id;
  if (typeof parsedOptions.collections !== "undefined") {
    parsedOptions.collections = collectionUuids
      ? await getCollectionDetails(collectionUuids)
      : defaultSearchOptions.collections;
  }
  if (typeof parsedOptions.owner !== "undefined") {
    parsedOptions.owner = ownerId
      ? await getUserDetails(ownerId)
      : defaultSearchOptions.owner;
  }
  return { ...defaultSearchOptions, ...parsedOptions };
};

/**
 * A function that takes search options and converts search options to search params,
 * and then does a search and returns a list of Items.
 * @param searchOptions Search options selected on Search page.
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
  OEQ.Search.SearchResult<OEQ.Search.SearchResultItem>
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

const getCollectionDetails = async (
  collectionUuids: string[]
): Promise<Collection[] | undefined> => {
  const collectionList = await collectionListSummary([
    OEQ.Acl.ACL_SEARCH_COLLECTION,
  ]);
  const filteredCollectionList = collectionList.filter((c) => {
    return collectionUuids.includes(c.uuid);
  });
  return filteredCollectionList.length > 0
    ? filteredCollectionList
    : defaultSearchOptions.collections;
};

const getUserDetails = async (
  userId: string
): Promise<OEQ.UserQuery.UserDetails | undefined> => {
  const userDetails = await resolveUsers([userId]);
  return userDetails.length > 0 ? userDetails[0] : defaultSearchOptions.owner;
};

/**
 * A function that takes query string params from a shared searching.do URL and converts all applicable params to Search options
 * @param location query string params from a shared `searching.do` URL
 * @return SearchOptions object.
 */
export const legacyQueryStringToSearchOptions = async (
  params: URLSearchParams
): Promise<SearchOptions> => {
  const getQueryParam = (paramName: LegacyParams) => {
    return params.get(paramName) ?? undefined;
  };

  const collectionId = getQueryParam("in")?.substring(1);
  const ownerId = getQueryParam("owner");
  const dateRange = getQueryParam("dr");
  const datePrimary = getQueryParam("dp");
  const dateSecondary = getQueryParam("ds");

  const RangeType = Union(
    Literal("between"),
    Literal("after"),
    Literal("before"),
    Literal("on")
  );

  type RangeType = Static<typeof RangeType>;

  const getLastModifiedDateRange = (
    rangeType: RangeType,
    primaryDate?: Date,
    secondaryDate?: Date
  ): DateRange | undefined => {
    if (!primaryDate && !secondaryDate) {
      return undefined;
    }
    return match(
      [
        Literal("between"),
        (): DateRange => ({ start: primaryDate, end: secondaryDate }),
      ],
      [Literal("after"), (): DateRange => ({ start: primaryDate })],
      [Literal("before"), (): DateRange => ({ end: primaryDate })],
      [
        Literal("on"),
        (): DateRange => ({
          start: primaryDate,
          end: primaryDate,
        }),
      ]
    )(rangeType.toLowerCase() as RangeType);
  };

  const searchOptions: SearchOptions = {
    ...defaultSearchOptions,
    collections: collectionId
      ? await getCollectionDetails([collectionId])
      : defaultSearchOptions.collections,
    query: getQueryParam("q") ?? defaultSearchOptions.query,
    owner: ownerId ? await getUserDetails(ownerId) : defaultSearchOptions.owner,
    lastModifiedDateRange:
      getLastModifiedDateRange(
        dateRange as RangeType,
        datePrimary ? new Date(parseInt(datePrimary)) : undefined,
        dateSecondary ? new Date(parseInt(dateSecondary)) : undefined
      ) ?? defaultSearchOptions.lastModifiedDateRange,
    sortOrder:
      (getQueryParam("sort")?.toUpperCase() as SortOrder) ??
      defaultSearchOptions.sortOrder,
  };
  return searchOptions;
};
