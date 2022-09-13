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
import * as A from "fp-ts/Array";
import * as E from "fp-ts/Either";
import { flow, identity, pipe } from "fp-ts/function";
import * as M from "fp-ts/Map";
import * as O from "fp-ts/Option";
import * as S from "fp-ts/string";
import * as T from "fp-ts/Task";
import * as TO from "fp-ts/TaskOption";
import { History, Location } from "history";
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
  Tuple,
  Union,
  Unknown,
} from "runtypes";
import type {
  FieldValueMap,
  PathValueMap,
} from "../components/wizard/WizardHelper";
import {
  RuntypesControlTarget,
  RuntypesControlValue,
} from "../components/wizard/WizardHelper";
import { routes } from "../mainui/routes";
import {
  clearDataFromLocalStorage,
  readDataFromLocalStorage,
  saveDataToLocalStorage,
} from "../modules/BrowserStorageModule";
import {
  Collection,
  findCollectionsByUuid,
} from "../modules/CollectionsModule";
import {
  buildSelectionSessionItemSummaryLink,
  isSelectionSessionOpen,
} from "../modules/LegacySelectionSessionModule";
import { getMimeTypeFiltersById } from "../modules/SearchFilterSettingsModule";
import {
  defaultSearchOptions,
  DisplayMode,
  DisplayModeRuntypes,
  SearchOptions,
  SearchOptionsFields,
} from "../modules/SearchModule";
import { findUserById } from "../modules/UserModule";
import { LegacyMyResourcesRuntypes } from "../myresources/MyResourcesPageHelper";
import { DateRange, isDate } from "../util/Date";
import { languageStrings } from "../util/langstrings";
import { simpleMatch } from "../util/match";
import { pfTernary } from "../util/pointfree";
import type { RefinePanelControl } from "./components/RefineSearchPanel";
import type { SortOrderOptions } from "./components/SearchOrderSelect";
import type { StatusSelectorProps } from "./components/StatusSelector";

/**
 * This helper is intended to assist with processing related to the Presentation Layer -
 * as opposed to the Business Layer which is handled by the Modules.
 */

/**
 * Type of search options that are specific to Search page presentation layer.
 */
export interface SearchPageOptions extends SearchOptions {
  /**
   * Whether to enable Quick mode (true) or to use custom date pickers (false).
   */
  dateRangeQuickModeEnabled: boolean;
  /**
   * How to display the search results - also determines the type of results.
   */
  displayMode: DisplayMode;
  /**
   * Currently configured Advanced search criteria.
   */
  advFieldValue?: FieldValueMap;
  /**
   * Advanced search criteria configured in the Old UI. This prop is intended to support searches shared
   * or favourited from Old UI.
   */
  legacyAdvSearchCriteria?: PathValueMap;
  /**
   * Open/closed state of refine expansion panel
   */
  filterExpansion?: boolean;
}

export const defaultSearchPageOptions: SearchPageOptions = {
  ...defaultSearchOptions,
  displayMode: "list",
  dateRangeQuickModeEnabled: true,
};

/**
 * Type definition for the configuration of SearchPageHeader.
 */
export interface SearchPageHeaderConfig {
  /**
   * `true` to enable the CSV Export button.
   */
  enableCSVExportButton?: boolean;
  /**
   * `true` to enable the Share Search button.
   */
  enableShareSearchButton?: boolean;
  /**
   * Additional components displayed in the CardHeader.
   */
  additionalHeaders?: JSX.Element[];
  /**
   * Customised options for sorting the search result.
   */
  customSortingOptions?: SortOrderOptions;
  /**
   * Custom configuration to be used with a 'new search' - e.g. when the 'New Search' button is clicked,
   * or when other actions which trigger the search state to be cleared.
   */
  newSearchConfig?: {
    /**
     * A path which is recognised by the React Router which points to a page
     * the user will be navigated to execute the new search.
     */
    path: string;
    /**
     * Search criteria that should be included in a new search.
     */
    criteria?: SearchPageOptions;
  };
}

export const defaultSearchPageHeaderConfig: SearchPageHeaderConfig = {
  enableCSVExportButton: true,
  enableShareSearchButton: true,
};

/**
 * Type definition for the configuration of SearchPageRefinePanel.
 */
export interface SearchPageRefinePanelConfig {
  /**
   * A list of custom Refine panel control.
   */
  customRefinePanelControl?: RefinePanelControl[];
  /**
   * `true` to enable the Display Mode selector.
   */
  enableDisplayModeSelector?: boolean;
  /**
   * `true` to enable the Collection selector.
   */
  enableCollectionSelector?: boolean;
  /**
   * `true` to enable the Advanced Search selector.
   */
  enableAdvancedSearchSelector?: boolean;
  /**
   * `true` to enable the Remote Search selector.
   */
  enableRemoteSearchSelector?: boolean;
  /**
   * `true` to enable the Date Range selector.
   */
  enableDateRangeSelector?: boolean;
  /**
   * `true` to enable the MIME Type selector.
   */
  enableMimeTypeSelector?: boolean;
  /**
   * `true` to enable the Owner selector.
   */
  enableOwnerSelector?: boolean;
  /**
   * `true` to enable the Search Attachment selector.
   */
  enableSearchAttachmentsSelector?: boolean;
  /**
   * `true` to enable the Item Status selector. However, whether the selector is displayed
   * also depends on the Search settings.
   */
  enableItemStatusSelector?: boolean;
  /**
   * Custom configuration for Status selector.
   */
  statusSelectorCustomConfig?: {
    /**
     * `true` to always show the selector regardless of 'enableItemStatusSelector' and the Search settings.
     */
    alwaysEnabled: boolean;
    /**
     * Props passed to the selector for customisation.
     */
    selectorProps?: StatusSelectorProps;
  };
}

export const defaultSearchPageRefinePanelConfig: SearchPageRefinePanelConfig = {
  enableDisplayModeSelector: true,
  enableCollectionSelector: true,
  enableAdvancedSearchSelector: true,
  enableRemoteSearchSelector: true,
  enableDateRangeSelector: true,
  enableMimeTypeSelector: true,
  enableOwnerSelector: true,
  enableItemStatusSelector: true,
  enableSearchAttachmentsSelector: true,
};

/**
 * Type definition for the configuration of SearchPageSearchBar.
 */
export interface SearchPageSearchBarConfig {
  /**
   * Configuration for the Advanced Search filter.
   */
  advancedSearchFilter: {
    /** Called when the filter button is clicked */
    onClick: () => void;
    /** If true the button wil be highlighted by the Secondary colour. */
    accent: boolean;
  };
}

export const defaultPagedSearchResult: OEQ.Search.SearchResult<OEQ.Search.SearchResultItem> =
  {
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
  Literal("type"),
  Literal("doc")
);

type LegacyParams = Static<typeof LegacySearchParams>;

/**
 * Represents the shape of data returned from generateQueryStringFromSearchOptions
 */
const DehydratedSearchPageOptionsRunTypes = Partial({
  query: String,
  rowsPerPage: Number,
  currentPage: Number,
  sortOrder: OEQ.Search.SortOrderRunTypes,
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
  advFieldValue: RuntypeArray(
    Tuple(RuntypesControlTarget, RuntypesControlValue)
  ),
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
          // Skip advancedSearchCriteria as we can build it from `advFieldValue`.
          [Literal("advancedSearchCriteria"), () => undefined],
          [
            Literal("advFieldValue"),
            () => pipe(value, O.fromNullable, O.map(Array.from), O.toUndefined),
          ],
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
): Promise<OEQ.SearchFilterSettings.MimeTypeFilter[] | undefined> =>
  options.mimeTypeFilters
    ? await getMimeTypeFiltersById(options.mimeTypeFilters.map((f) => f.id))
    : undefined;

// Use a user ID extracted from a dehydrated SearchOptions to find a user's details.
const rehydrateOwner = async (
  options: DehydratedSearchPageOptions
): Promise<OEQ.UserQuery.UserDetails | undefined> =>
  options.owner ? await findUserById(options.owner.id) : undefined;

// Use the array of `advFieldValue` extracted from a dehydrated SearchOptions to build FieldValueMap.
const rehydrateAdvFieldValue = ({
  advFieldValue,
}: DehydratedSearchPageOptions): FieldValueMap | undefined =>
  advFieldValue ? new Map(advFieldValue) : undefined;

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
    advFieldValue: rehydrateAdvFieldValue(parsedOptions),
  };
};

type RangeType = "between" | "after" | "before" | "on";

const getLastModifiedDateRangeFromLegacyParams = (
  rangeType: RangeType,
  primaryDate?: Date,
  secondaryDate?: Date
): DateRange | undefined =>
  pipe(
    !primaryDate && !secondaryDate
      ? O.none
      : O.some<RangeType>(rangeType.toLowerCase() as RangeType),
    O.map(
      simpleMatch<DateRange>({
        between: () => ({ start: primaryDate, end: secondaryDate }),
        after: () => ({ start: primaryDate }),
        before: () => ({ end: primaryDate }),
        on: () => ({
          start: primaryDate,
          end: primaryDate,
        }),
        _: (range) => {
          throw new TypeError(`Unknown date range mode [${range}]`);
        },
      })
    ),
    O.toUndefined
  );

const getDisplayModeFromLegacyParams = (
  legacyDisplayMode: string | undefined
): DisplayMode =>
  pipe(
    legacyDisplayMode,
    O.fromNullable,
    O.fold(
      () => "list", // default to list mode if none defined
      simpleMatch<DisplayMode>({
        standard: () => "list",
        gallery: () => "gallery-image",
        video: () => "gallery-video",
        _: (mode) => {
          // Because Old UI also uses query string `type` for My resources type and Legacy
          // My resources page does not have galleries, we always return "list".
          if (LegacyMyResourcesRuntypes.guard(mode)) {
            return "list";
          }
          throw new TypeError(`Unknown Legacy display mode [${mode}]`);
        },
      })
    )
  );

const getCollectionFromLegacyParams = async (
  collectionUuid: string | undefined
): Promise<Collection[] | undefined> =>
  pipe(
    collectionUuid,
    O.fromNullable,
    O.map(
      pfTernary(
        (withPrefix) => withPrefix.startsWith("C"),
        (withPrefix) => withPrefix.substring(1),
        identity
      )
    ),
    TO.fromOption,
    TO.chain((uuid) => TO.tryCatch(() => findCollectionsByUuid([uuid]))),
    TO.filter((collections) => !!collections && A.isNonEmpty(collections)),
    TO.getOrElse(() => T.of(defaultSearchOptions.collections))
  )();

const getOwnerFromLegacyParams = async (ownerId: string | undefined) =>
  ownerId ? await findUserById(ownerId) : defaultSearchOptions.owner;

// Function to build a PathValueMap by walking through a XML tree.
const buildPathValueMap = (node: Element, parentPath = ""): PathValueMap => {
  const { nodeType, nodeName, textContent, children, TEXT_NODE, attributes } =
    node;
  // Skip the path if the node is a TEXT NODE or the node is "xml".
  const path =
    nodeType === TEXT_NODE || nodeName === "xml" ? S.empty : `/${nodeName}`;
  const fullPath = parentPath + path;
  const buildWithFullPath = (n: Element) => buildPathValueMap(n, fullPath);

  // Function to merge the values of two maps into one array.
  // For example, given two maps like "/item/options/ => ['a']", "/item/options/ => ['b']",
  // the result is "/item/options/ => ['a', 'b']"
  const mergeMaps = (
    firstMap: Map<string, string[]>,
    secondMap: Map<string, string[]>
  ) => pipe(firstMap, M.union(S.Eq, A.getUnionSemigroup(S.Eq))(secondMap));

  const buildMapForLastNode = () =>
    textContent ? new Map([[fullPath, [textContent]]]) : new Map();

  const valueMap: Map<string, string[]> = pipe(
    Array.from(children),
    pfTernary(
      A.isNonEmpty,
      flow(
        A.map(buildWithFullPath),
        A.filter((m): m is Map<string, string[]> => typeof m !== "undefined"),
        A.reduce(new Map(), mergeMaps)
      ),
      buildMapForLastNode
    )
  );

  const attributeMap: Map<string, string[]> = pipe(
    Array.from(attributes),
    A.map<Attr, [string, [string]]>(({ name, value }) => [
      `${fullPath}/@${name}`,
      [value],
    ]),
    M.fromFoldable(S.Eq, A.getSemigroup<string>(), A.Foldable)
  );

  return mergeMaps(valueMap, attributeMap);
};

/**
 * Convert a XML string into a Map where key is a string representing a unique schema node
 * and value is an array of strings.
 *
 * @param s A XML string representing Legacy Advanced search criteria
 */
export const processLegacyAdvSearchCriteria = (
  s: string
): PathValueMap | undefined => {
  const parser = new DOMParser();

  // Validate the root element. The element name should be `xml`.
  const validateFirstNode: (node: Element) => E.Either<string, Element> = flow(
    E.fromPredicate(
      ({ nodeName }) => nodeName === "xml",
      () => "Failed to find root node `xml`"
    )
  );

  return pipe(
    E.tryCatch(
      () => parser.parseFromString(s, "text/xml"),
      (reason) =>
        reason instanceof Error
          ? reason.message
          : "Failed to parse the provided XML string"
    ),
    E.map(({ documentElement }) => documentElement),
    E.chain(validateFirstNode),
    E.mapLeft(console.error),
    O.fromEither,
    O.map(buildPathValueMap),
    O.toUndefined
  );
};

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
  const collections = await getCollectionFromLegacyParams(getQueryParam("in"));
  const owner = await getOwnerFromLegacyParams(getQueryParam("owner"));
  const sortOrder = pipe(
    getQueryParam("sort")?.toLowerCase(),
    O.fromPredicate(OEQ.Search.SortOrderRunTypes.guard),
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
    legacyAdvSearchCriteria: pipe(
      getQueryParam("doc"),
      O.fromNullable,
      O.map(processLegacyAdvSearchCriteria),
      O.toUndefined
    ),
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

export const RAW_MODE_STORAGE_KEY = "raw_mode";

/**
 * Read the value of wildcard mode from LocalStorage.
 */
export const getRawModeFromStorage = (): boolean =>
  readDataFromLocalStorage(RAW_MODE_STORAGE_KEY, Boolean.guard) ??
  defaultSearchOptions.rawMode;

export const writeRawModeToStorage = (value: boolean): void =>
  saveDataToLocalStorage(RAW_MODE_STORAGE_KEY, value);

export const deleteRawModeFromStorage = (): void =>
  clearDataFromLocalStorage(RAW_MODE_STORAGE_KEY);

/**
 * This function returns an object which consists of a URL of Item Summary page and a onClick handler
 * which is used to open the Summary page.
 *
 * @param uuid Item's UUID.
 * @param version Item's version.
 * @param history The History object used in the context, which is typically provided by calling 'useHistory' in components.
 */
export const buildOpenSummaryPageHandler = (
  uuid: string,
  version: number,
  history: History
): {
  url: string;
  onClick: () => void;
} =>
  pipe(
    routes.ViewItem.to(uuid, version),
    E.fromPredicate<string, string>(
      () => !isSelectionSessionOpen(),
      () => buildSelectionSessionItemSummaryLink(uuid, version)
    ),
    E.fold(
      // Selection session values
      (url) => ({
        url,
        onClick: () => window.open(url, "_self"),
      }),
      // Normal page values
      (url) => ({
        url,
        onClick: () => history.push(url),
      })
    )
  );

/**
 * Given an `ApiError`, return an error message depending on the status code.
 *
 * @param error API error captured when exporting a search result.
 */
export const generateExportErrorMessage = (
  error: OEQ.Errors.ApiError
): string => {
  const { badRequest, unauthorised, notFound } =
    languageStrings.searchpage.export.errorMessages;

  return pipe(
    error.message,
    simpleMatch<string>({
      400: () => badRequest,
      403: () => unauthorised,
      404: () => notFound,
      _: () => error.message,
    })
  );
};
