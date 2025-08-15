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
import * as EQ from "fp-ts/Eq";
import { constFalse, flow, pipe } from "fp-ts/function";
import * as NEA from "fp-ts/NonEmptyArray";
import * as O from "fp-ts/Option";
import * as S from "fp-ts/string";
import * as TE from "fp-ts/TaskEither";
import * as t from "io-ts";
import { API_BASE_URL } from "../AppConfig";
import { DateRange, getISODateString } from "../util/Date";
import { isNonEmptyString } from "../util/validation";
import type { Collection } from "./CollectionsModule";
import type { SelectedCategories } from "./SearchFacetsModule";

export type BasicSearchResultItem = Pick<
  OEQ.Search.SearchResultItem,
  "uuid" | "version" | "status"
>;

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
export const nonLiveStatuses: OEQ.Common.ItemStatus[] =
  OEQ.Codec.Common.ItemStatusCodec.types
    .map(({ value }) => value)
    .filter(nonLiveStatus);

/**
 * All statuses except "DELETED".
 */
export const nonDeletedStatuses: OEQ.Common.ItemStatus[] =
  OEQ.Codec.Common.ItemStatusCodec.types
    .map(({ value }) => value)
    .filter((status) => status !== "DELETED");

/**
 * Function to check if the supplied SearchResultItem refers to a live Item.
 * Item status returned from 'search2' is a lowercase string so convert it to uppercase.
 */
export const isLiveItem = (item: OEQ.Search.SearchResultItem): boolean =>
  pipe(
    OEQ.Codec.Common.ItemStatusCodec.decode(item.status.toUpperCase()),
    E.fold(constFalse, (s) => liveStatuses.includes(s)),
  );

export const DisplayModeCodec = t.union([
  t.literal("list"),
  t.literal("gallery-image"),
  t.literal("gallery-video"),
]);

/**
 * Available modes for displaying search results.
 * @see { @link DisplayModeRuntypes } for original definition.
 */
export type DisplayMode = t.TypeOf<typeof DisplayModeCodec>;

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
  sortOrder: OEQ.Search.SortOrder | undefined;
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
   * Whether to include full attachment details in results. Including attachments incurs extra
   * processing and can slow down response times. In a typical 'list' search we don't include
   * them so that they're only requested _if_ a user expands out the attachments. This allows
   * the search page to render quicker.
   */
  includeAttachments?: boolean;
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
  mimeTypeFilters?: OEQ.SearchFilterSettings.MimeTypeFilter[];
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
   * Advanced search criteria defined by Wizard controls' schema nodes and values.
   */
  advancedSearchCriteria?: OEQ.Search.WizardControlFieldValue[];
  /**
   * The UUID of a hierarchy topic.
   */
  hierarchy?: string;
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
 * Helper function, to support formatting of search query in raw mode. When _not_ raw mode
 * we append a wildcard to support the idea of a simple (typeahead) search.
 *
 * The function returns `undefined` if the search query is empty (after trimming) or undefined.
 *
 * @param addWildCard  flag for wild-card formatting.
 * @param query the intended search query to be sent to the API.
 */
export const formatQuery = (
  addWildCard: boolean,
  query?: string,
): string | undefined =>
  pipe(
    query,
    O.fromNullable,
    O.map(S.trim),
    O.filter(isNonEmptyString),
    O.map((q) => (addWildCard ? `${q}*` : q)),
    O.toUndefined,
  );

/**
 * Generates a Where clause through Classifications.
 * Each Classification that has categories selected is joined by AND.
 * Each selected category of one Classification is joined by OR.
 *
 * @param selectedCategories A list of selected Categories grouped by Classification ID.
 */
export const generateCategoryWhereQuery = (
  selectedCategories?: SelectedCategories[],
): string | undefined => {
  if (!selectedCategories || selectedCategories.length === 0) {
    return undefined;
  }

  const and = " AND ";
  const or = " OR ";
  const processNodeTerms = (
    categories: string[],
    schemaNode?: string,
  ): string => categories.map((c) => `/xml${schemaNode}='${c}'`).join(or);

  return selectedCategories
    .filter((c) => c.categories.length > 0)
    .map(
      ({ schemaNode, categories }: SelectedCategories) =>
        `(${processNodeTerms(categories, schemaNode)})`,
    )
    .join(and);
};

/**
 * List of Collection UUIDs that are externally configured to filter search results.
 * If provided, the Collections used in a search should be either this list or the intersection
 * between this list and the list of Collections selected by the user.
 *
 * It is usually provided through OEQ Legacy server side rendering together with Legacy content
 * API. For more details, please check the use of {@link PageContent#script} and {@link LegacyContent}
 */
declare const configuredCollections: string[] | undefined;

/**
 * A function that converts search options to search params.
 *
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
  includeAttachments,
  searchAttachments,
  selectedCategories,
  mimeTypes,
  mimeTypeFilters,
  externalMimeTypes,
  musts,
  hierarchy,
}: SearchOptions): OEQ.Search.SearchParams => {
  // We use selected filters to generate MIME types. However, in Image Gallery,
  // image MIME types are applied before any filter gets selected.
  // So the logic is, we use MIME type filters if any are selected, or specific MIME types
  // already provided by the Image Gallery.
  const _mimeTypes =
    mimeTypeFilters && mimeTypeFilters.length > 0
      ? mimeTypeFilters.flatMap((f) => f.mimeTypes)
      : mimeTypes;

  const selectedCollections = collections?.map(({ uuid }) => uuid) ?? [];
  const restrictByConfiguredCollections = (restrictions: string[]) =>
    A.isNonEmpty(selectedCollections)
      ? pipe(restrictions, A.intersection(S.Eq)(selectedCollections))
      : restrictions;

  return {
    query: formatQuery(!rawMode, query),
    start: currentPage * rowsPerPage,
    length: rowsPerPage,
    status,
    order: sortOrder,
    collections:
      typeof configuredCollections !== "undefined"
        ? restrictByConfiguredCollections(configuredCollections)
        : selectedCollections,
    modifiedAfter: getISODateString(lastModifiedDateRange?.start),
    modifiedBefore: getISODateString(lastModifiedDateRange?.end),
    owner: owner?.id,
    includeAttachments: includeAttachments,
    searchAttachments: searchAttachments,
    whereClause: generateCategoryWhereQuery(selectedCategories),
    mimeTypes: externalMimeTypes ?? _mimeTypes,
    musts,
    hierarchy,
  };
};

/**
 * A function that converts search options to search advanced search params.
 *
 * @param searchOptions Search options to be converted to search advanced params.
 */
const buildAdvancedSearchParams = ({
  advancedSearchCriteria,
}: SearchOptions): OEQ.Search.AdvancedSearchParams => ({
  advancedSearchCriteria,
});

/**
 * A function that executes a search with provided search options. If Advanced search criteria exists
 * in the search options, do the search with normal params and additional params through a POST request.
 * Otherwise, do the search through a GET request with normal params.
 *
 * @param searchOptions Search options selected on Search page.
 */
export const searchItems = (
  searchOptions: SearchOptions,
): Promise<OEQ.Search.SearchResult<OEQ.Search.SearchResultItem>> => {
  const normalParams = buildSearchParams(searchOptions);
  return pipe(
    searchOptions,
    O.fromPredicate(
      ({ advancedSearchCriteria }) =>
        !!advancedSearchCriteria && A.isNonEmpty(advancedSearchCriteria),
    ),
    O.match(
      () => OEQ.Search.search(API_BASE_URL, normalParams),
      (options) =>
        OEQ.Search.searchWithAdvancedParams(
          API_BASE_URL,
          buildAdvancedSearchParams(options),
          normalParams,
        ),
    ),
  );
};

/**
 * Retrieve the search results for a single item - including attachment details. Useful if a
 * search has been done excluding attachment results to delay retrieval of attachments to a later
 * time.
 *
 * @param uuid The UUID of the target item
 * @param version The specific version of the target item
 */
export const searchItemAttachments = async (
  uuid: string,
  version: number,
): Promise<OEQ.Search.Attachment[]> => {
  const extractAndValidateItem: (
    searchResult: OEQ.Search.SearchResult<OEQ.Search.SearchResultItem>,
  ) => TE.TaskEither<string, OEQ.Search.SearchResultItem> = flow(
    ({ results }) => results,
    TE.fromPredicate(
      A.isNonEmpty,
      () => `Search for item ${uuid}/${version} returned no results`,
    ),
    TE.map(NEA.head),
  );

  const extractAttachments: (
    item: OEQ.Search.SearchResultItem,
  ) => OEQ.Search.Attachment[] = flow(
    ({ attachments }) => attachments,
    O.fromNullable,
    O.getOrElse(() => [] as OEQ.Search.Attachment[]),
  );

  const attachmentsMaybe = await pipe(
    TE.tryCatch(
      () =>
        searchItems({
          ...defaultSearchOptions,
          musts: [
            ["uuid", [uuid]],
            ["version", [`${version}`]],
          ],
          searchAttachments: false,
          status: [], // As we are searching for a specific Item we should discard the default Item status.
        }),
      (reason) =>
        `Failed to retrieve details of item ${uuid}/${version}: ${reason}`,
    ),
    TE.chain(extractAndValidateItem),
    TE.map(extractAttachments),
    TE.mapLeft(E.toError),
  )();

  return pipe(
    attachmentsMaybe,
    E.getOrElseW((err) => {
      throw err;
    }),
  );
};

/**
 * A function that builds a URL for exporting a search result
 *
 * @param searchOptions Search options selected on Search page.
 */
export const buildExportUrl = (searchOptions: SearchOptions): string =>
  OEQ.Search.buildExportUrl(
    API_BASE_URL,
    buildSearchParams({ ...searchOptions, currentPage: 0 }),
  );

/**
 * Send a request to confirm if an export is valid.
 *
 * @param searchOptions Search options selected on Search page.
 */
export const confirmExport = (searchOptions: SearchOptions): Promise<boolean> =>
  OEQ.Search.confirmExportRequest(
    API_BASE_URL,
    buildSearchParams(searchOptions),
  );

/**
 * Eq for `OEQ.Search.SearchResultItem` with equality based on the UUID and version.
 */
export const itemEq: EQ.Eq<OEQ.Search.SearchResultItem> = EQ.contramap(
  (item: OEQ.Search.SearchResultItem) => item.uuid + item.version,
)(S.Eq);
