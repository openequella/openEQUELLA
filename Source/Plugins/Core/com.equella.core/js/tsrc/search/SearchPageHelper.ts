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
import * as O from "fp-ts/Option";
import * as A from "fp-ts/Array";
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
import {
  getMimeTypeFiltersById,
  MimeTypeFilter,
} from "../modules/SearchFilterSettingsModule";
import {
  defaultSearchOptions,
  DisplayMode,
  DisplayModeRuntypes,
  SearchOptions,
  SearchOptionsFields,
} from "../modules/SearchModule";
import { findUserById } from "../modules/UserModule";
import { DateRange, isDate } from "../util/Date";
import type { SearchPageOptions } from "./SearchPage";

/**
 * This helper is intended to assist with processing related to the Presentation Layer -
 * as opposed to the Business Layer which is handled by the Modules.
 */

export const defaultSearchPageOptions: SearchPageOptions = {
  ...defaultSearchOptions,
  displayMode: "list",
  dateRangeQuickModeEnabled: true,
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
  status: RuntypeArray(OEQ.Common.ItemStatuses),
  selectedCategories: RuntypeArray(
    Record({
      id: Number,
      categories: RuntypeArray(String),
    })
  ),
  searchAttachments: Boolean,
  mimeTypeFilters: RuntypeArray(Record({ id: String })),
  displayMode: DisplayModeRuntypes,
  dateRangeQuickModeEnabled: Boolean,
});

type DehydratedSearchPageOptions = Static<
  typeof DehydratedSearchPageOptionsRunTypes
>;

/**
 * A function that takes and parses a saved search query string from a shared legacy searching.do or /page/search URL, and converts it into a SearchPageOptions object.
 *
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
 *
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
 *
 * @see {@link https://github.com/pelotom/runtypes/issues/169 }
 * @param searchOptionsJSON a JSON representation of a SearchPageOptions object.
 * @return searchPageOptions A deserialized representation of that provided by `searchOptionsJSON`
 */
export const newSearchQueryToSearchPageOptions = async (
  searchOptionsJSON: string
): Promise<SearchPageOptions> => {
  const parsedOptions: unknown = JSON.parse(searchOptionsJSON, (key, value) => {
    if (key === "lastModifiedDateRange") {
      let { start, end } = value;
      start = start ? new Date(start) : undefined;
      end = end ? new Date(end) : undefined;
      return { start, end };
    }
    return value;
  });

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

type RangeType = "between" | "after" | "before" | "on";

const getLastModifiedDateRangeFromLegacyParams = (
  rangeType: RangeType,
  primaryDate?: Date,
  secondaryDate?: Date
): DateRange | undefined =>
  !primaryDate && !secondaryDate
    ? undefined
    : pipe(
        rangeType.toLowerCase() as RangeType,
        match(
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
        )
      );

const getDisplayModeFromLegacyParams = (
  legacyDisplayMode: string | undefined
): DisplayMode =>
  pipe(
    legacyDisplayMode,
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

const getCollectionFromLegacyParams = async (
  collectionUuid: string | undefined
): Promise<Collection[] | undefined> => {
  if (!collectionUuid) return defaultSearchOptions.collections;
  const collectionDetails:
    | Collection[]
    | undefined = await findCollectionsByUuid([collectionUuid]);

  return typeof collectionDetails !== "undefined" &&
    collectionDetails.length > 0
    ? collectionDetails
    : defaultSearchOptions.collections;
};

const getOwnerFromLegacyParams = async (ownerId: string | undefined) =>
  ownerId ? await findUserById(ownerId) : defaultSearchOptions.owner;

/**
 * A function that takes query string params from a shared searching.do URL and converts all applicable params to SearchPageOptions.
 *
 * @param params URLSearchParams from a shared `searching.do` URL
 * @return SearchPageOptions object.
 */
export const legacyQueryStringToSearchPageOptions = async (
  params: URLSearchParams
): Promise<SearchPageOptions> => {
  const getQueryParam = (paramName: LegacyParams) => {
    return params.get(paramName) ?? undefined;
  };
  const query = getQueryParam("q") ?? defaultSearchOptions.query;
  const collections = await getCollectionFromLegacyParams(
    getQueryParam("in")?.substring(1)
  );
  const owner = await getOwnerFromLegacyParams(getQueryParam("owner"));
  const sortOrder = pipe(
    getQueryParam("sort")?.toUpperCase(),
    O.fromPredicate(OEQ.SearchSettings.SortOrderRunTypes.guard),
    O.getOrElse(() => defaultSearchOptions.sortOrder)
  );

  const datePrimary = getQueryParam("dp");
  const dateSecondary = getQueryParam("ds");
  const lastModifiedDateRange =
    getLastModifiedDateRangeFromLegacyParams(
      getQueryParam("dr") as RangeType,
      datePrimary ? new Date(parseInt(datePrimary)) : undefined,
      dateSecondary ? new Date(parseInt(dateSecondary)) : undefined
    ) ?? defaultSearchOptions.lastModifiedDateRange;

  const mimeTypeFilters = await getMimeTypeFiltersById(params.getAll("mt"));
  const mimeTypes = mimeTypeFilters?.flatMap(({ mimeTypes }) => mimeTypes);
  const externalMimeTypes = pipe(
    params.getAll("_int.mimeTypes"),
    O.fromPredicate(A.isNonEmpty),
    O.toUndefined
  );

  const displayMode = getDisplayModeFromLegacyParams(getQueryParam("type"));
  return {
    ...defaultSearchPageOptions,
    collections,
    query,
    owner,
    lastModifiedDateRange,
    sortOrder,
    mimeTypes,
    mimeTypeFilters,
    externalMimeTypes,
    rawMode: true,
    displayMode,
  };
};

/**
 * Call this function to get partial SearchOptions.
 *
 * @param options An object of SearchOptions
 * @param fields What fields of SearchOptions to get
 */
export const getPartialSearchOptions = (
  options: SearchOptions,
  fields: SearchOptionsFields[]
) => pick(options, fields);
