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
import { pipe } from "fp-ts/function";
import { Location } from "history";
import { pick } from "lodash";
import {
  Array as RuntypeArray,
  Boolean,
  Guard,
  Literal,
  match,
  Number,
  Partial,
  Record,
  Static,
  String,
  Undefined,
  Union,
  Unknown,
} from "runtypes";
import {
  Collection,
  findCollectionsByUuid,
} from "../modules/CollectionsModule";
import { SelectedCategories } from "../modules/SearchFacetsModule";
import {
  getMimeTypeFiltersById,
  MimeTypeFilter,
} from "../modules/SearchFilterSettingsModule";
import {
  DateRange,
  DisplayMode,
  isDate,
  SearchOptions,
  SearchOptionsFields,
} from "../modules/SearchModule";
import { findUserById } from "../modules/UserModule";
import { getISODateString } from "../util/Date";
import type { SearchPageOptions } from "./SearchPage";

/**
 * This helper is intended to assist with processing related to the Presentation Layer -
 * as opposed to the Business Layer which is handled by the Modules.
 */

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

export const defaultSearchPageOptions: SearchPageOptions = {
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
  dateRangeQuickModeEnabled: true,
  displayMode: "list",
};

export const defaultPagedSearchResult: OEQ.Search.SearchResult<OEQ.Search.SearchResultItem> = {
  start: 0,
  length: 10,
  available: 0,
  results: [],
  highlight: [],
};

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
  Literal("in"),
  Literal("mt"),
  Literal("_int.mimeTypes"),
  Literal("type")
);

type LegacyParams = Static<typeof LegacySearchParams>;
/**
 * Represents the shape of data returned from generateQueryStringFromSearchOptions
 */
const DehydratedSearchPageOptionsRunTypes = Partial({
  query: String,
  rowsPerPage: Number,
  currentPage: Number,
  sortOrder: OEQ.SearchSettings.SortOrderRunTypes,
  collections: RuntypeArray(Record({ uuid: String })),
  rawMode: Boolean,
  lastModifiedDateRange: Partial({ start: Guard(isDate), end: Guard(isDate) }),
  owner: Record({ id: String }),
  // Runtypes guard function would not work when defining the type as Array(OEQ.Common.ItemStatuses) or Guard(OEQ.Common.ItemStatuses.guard),
  // So the Union of Literals has been copied from the OEQ.Common module.
  status: RuntypeArray(OEQ.Common.ItemStatuses),
  selectedCategories: RuntypeArray(
    Record({
      id: Number,
      categories: RuntypeArray(String),
    })
  ),
  searchAttachments: Boolean,
  mimeTypeFilters: RuntypeArray(Record({ id: String })),
  //displayMode: DisplayModeRuntypes,
  dateRangeQuickModeEnabled: Boolean,
});

type DehydratedSearchPageOptions = Static<
  typeof DehydratedSearchPageOptionsRunTypes
>;

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
 * @param searchOptions Search options to be converted to search params.
 */
export const buildSearchParams = ({
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
 * A function that takes and parses a saved search query string from a shared legacy searching.do or /page/search URL, and converts it into a SearchPageOptions object.
 * @param location representing a Location which includes search query params - such as from the <SearchPage>
 * @return SearchPageOptions containing the options encoded in the query string params, or undefined if there were none.
 */
export const generateSearchPageOptionsFromQueryString = async (
  location: Location
): Promise<SearchPageOptions | undefined> => {
  if (!location.search) {
    return undefined;
  }
  const params = new URLSearchParams(location.search);
  const searchPageOptions = params.get("searchOptions");

  // If the query params contain `searchOptions` convert to `SearchOptions` with `newSearchQueryToSearchPageOptions`.
  // Else if the query params contain params from legacy `searching.do` (i.e. `LegacySearchParams`) then convert to
  // `searchOptions` with `legacyQueryStringToSearchPageOptions`.
  // For all else, return `undefined`.
  if (searchPageOptions) {
    return await newSearchQueryToSearchPageOptions(searchPageOptions);
  } else if (
    Array.from(params.keys()).some((key) => LegacySearchParams.guard(key))
  ) {
    return await legacyQueryStringToSearchPageOptions(params);
  }
  return undefined;
};

/**
 * A function that takes search options and converts it to to a JSON representation.
 * Collections and owner properties are both reduced down to their uuid and id properties respectively.
 * Undefined properties are excluded.
 * Intended to be used in conjunction with SearchModule.newSearchQueryToSearchOptions
 * @param searchPageOptions Search options selected on Search page.
 * @return url encoded key/value pair of JSON searchOptions
 */
export const generateQueryStringFromSearchPageOptions = (
  searchPageOptions: SearchPageOptions
): string => {
  const params = new URLSearchParams();
  params.set(
    "searchOptions",
    JSON.stringify(
      searchPageOptions,
      (key: string, value: object[] | undefined) => {
        return match(
          [
            Literal("collections"),
            () => value?.map((collection) => pick(collection, ["uuid"])),
          ],
          [Literal("owner"), () => (value ? pick(value, ["id"]) : undefined)],
          [
            Literal("mimeTypeFilters"),
            () => value?.map((filter) => pick(filter, ["id"])),
          ],
          // As we can get MIME types from filters, we can skip key "mimeTypes".
          [Literal("mimeTypes"), () => undefined],
          [Unknown, () => value ?? undefined]
        )(key);
      }
    )
  );
  console.log(params.toString());
  return params.toString();
};

// Use a list of Collection IDs extracted from a dehydrated SearchOptions to find Collections.
const rehydrateCollections = async (
  options: DehydratedSearchPageOptions
): Promise<Collection[] | undefined> =>
  options.collections
    ? await findCollectionsByUuid(options.collections.map((c) => c.uuid))
    : undefined;

// Use a list of MIME type filter IDs extracted from a dehydrated SearchOptions to find MIME type filters.
const rehydrateMIMETypeFilter = async (
  options: DehydratedSearchPageOptions
): Promise<MimeTypeFilter[] | undefined> =>
  options.mimeTypeFilters
    ? await getMimeTypeFiltersById(options.mimeTypeFilters.map((f) => f.id))
    : undefined;

// Use a user ID extracted from a dehydrated SearchOptions to find a user's details.
const rehydrateOwner = async (
  options: DehydratedSearchPageOptions
): Promise<OEQ.UserQuery.UserDetails | undefined> =>
  options.owner ? await findUserById(options.owner.id) : undefined;

/**
 * A function that takes a JSON representation of a SearchPageOptions object, and converts it into an actual SearchPageOptions object.
 * Note: currently, due to lack of support from runtypes extraneous properties will end up in the resultant object (which then could be stored in state).
 * @see {@link https://github.com/pelotom/runtypes/issues/169 }
 * @param searchOptionsJSON a JSON representation of a SearchPageOptions object.
 * @return searchPageOptions A deserialized representation of that provided by `searchOptionsJSON`
 */
const newSearchQueryToSearchPageOptions = async (
  searchOptionsJSON: string
): Promise<SearchPageOptions> => {
  console.log(searchOptionsJSON);

  const parsedOptions: unknown = JSON.parse(searchOptionsJSON, (key, value) => {
    if (key === "lastModifiedDateRange") {
      let { start, end } = value;
      start = start ? new Date(start) : undefined;
      end = end ? new Date(end) : undefined;
      return { start, end };
    }
    return value;
  });
  console.log(parsedOptions);
  console.log(DehydratedSearchPageOptionsRunTypes);
  if (!DehydratedSearchPageOptionsRunTypes.guard(parsedOptions)) {
    console.warn("Invalid search query params received. Using defaults.");
    return defaultSearchPageOptions;
  }
  const mimeTypeFilters = await rehydrateMIMETypeFilter(parsedOptions);
  const mimeTypes = mimeTypeFilters?.flatMap((f) => f.mimeTypes);

  return {
    ...defaultSearchPageOptions,
    ...parsedOptions,
    collections: await rehydrateCollections(parsedOptions),
    owner: await rehydrateOwner(parsedOptions),
    mimeTypeFilters,
    mimeTypes,
  };
};

/**
 * A function that takes query string params from a shared searching.do URL and converts all applicable params to SearchPageOptions.
 * @param params URLSearchParams from a shared `searching.do` URL
 * @return SearchPageOptions object.
 */
const legacyQueryStringToSearchPageOptions = async (
  params: URLSearchParams
): Promise<SearchPageOptions> => {
  const getQueryParam = (paramName: LegacyParams) => {
    return params.get(paramName) ?? undefined;
  };

  const collectionId = getQueryParam("in")?.substring(1);
  const ownerId = getQueryParam("owner");
  const dateRange = getQueryParam("dr");
  const datePrimary = getQueryParam("dp");
  const dateSecondary = getQueryParam("ds");

  const RangeTypeLiterals = Union(
    Literal("between"),
    Literal("after"),
    Literal("before"),
    Literal("on")
  );

  type RangeType = Static<typeof RangeTypeLiterals>;

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

  const displayMode: DisplayMode = pipe(
    getQueryParam("type"),
    match(
      // When type is 'standard' or undefined, default to 'list'.
      [Union(Literal("standard"), Undefined), (): DisplayMode => "list"],
      [Literal("gallery"), (): DisplayMode => "gallery-image"],
      [Literal("video"), (): DisplayMode => "gallery-video"],
      [
        Unknown,
        () => {
          throw new Error("Unknown Legacy display mode");
        },
      ]
    )
  );

  const parseCollectionUuid = async (
    collectionUuid: string | undefined
  ): Promise<Collection[] | undefined> => {
    if (!collectionUuid) return defaultSearchPageOptions.collections;
    const collectionDetails:
      | Collection[]
      | undefined = await findCollectionsByUuid([collectionUuid]);

    return typeof collectionDetails !== "undefined" &&
      collectionDetails.length > 0
      ? collectionDetails
      : defaultSearchPageOptions.collections;
  };

  const sortOrderParam = getQueryParam("sort")?.toUpperCase();
  const mimeTypeFilters = await getMimeTypeFiltersById(params.getAll("mt"));
  const mimeTypes = mimeTypeFilters?.flatMap(({ mimeTypes }) => mimeTypes);
  const getExternalMIMETypes = () => {
    const integrationMIMETypes = params.getAll("_int.mimeTypes");
    return integrationMIMETypes.length > 0 ? integrationMIMETypes : undefined;
  };

  return {
    ...defaultSearchPageOptions,
    collections: await parseCollectionUuid(collectionId),
    query: getQueryParam("q") ?? defaultSearchPageOptions.query,
    owner: ownerId
      ? await findUserById(ownerId)
      : defaultSearchPageOptions.owner,
    lastModifiedDateRange:
      getLastModifiedDateRange(
        dateRange as RangeType,
        datePrimary ? new Date(parseInt(datePrimary)) : undefined,
        dateSecondary ? new Date(parseInt(dateSecondary)) : undefined
      ) ?? defaultSearchPageOptions.lastModifiedDateRange,
    sortOrder: OEQ.SearchSettings.SortOrderRunTypes.guard(sortOrderParam)
      ? sortOrderParam
      : defaultSearchPageOptions.sortOrder,
    mimeTypes,
    mimeTypeFilters,
    externalMimeTypes: getExternalMIMETypes(),
    rawMode: true,
    displayMode,
  };
};

/**
 * Call this function to get partial SearchOptions.
 * @param options An object of SearchOptions
 * @param fields What fields of SearchOptions to get
 */
export const getPartialSearchOptions = (
  options: SearchOptions,
  fields: SearchOptionsFields[]
) => pick(options, fields);
