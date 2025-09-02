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
import { sequenceT } from "fp-ts/Apply";
import * as E from "fp-ts/Either";
import { identity, pipe } from "fp-ts/function";
import { getBaseUrl } from "../../AppConfig";
import { DateRange } from "../../util/Date";
import { languageStrings } from "../../util/langstrings";
import * as TE from "../../util/TaskEither.extended";
import {
  getAdvancedSearchByUuid,
  getAdvancedSearchIdFromUrl,
} from "../../modules/AdvancedSearchModule";
import { getHierarchyDetails } from "../../modules/HierarchyModule";
import {
  buildFavouritesSearchSelectionSessionLink,
  isSelectionSessionOpen,
} from "../../modules/LegacySelectionSessionModule";
import * as O from "fp-ts/Option";
import type { SelectedCategories } from "../../modules/SearchFacetsModule";
import { SearchOptions } from "../../modules/SearchModule";
import { generateSearchPageOptionsFromUrl } from "../../search/SearchPageHelper";
import * as A from "fp-ts/Array";
import * as T from "fp-ts/Task";

const { end: endLabel, start: startLabel } =
  languageStrings.favourites.favouritesSearch.searchCriteriaLabels;

/**
 * Search options shown in the favourites search.
 */
export interface FavouriteSearchOptionsSummary {
  /**
   * The search query string.
   */
  query?: string;
  /**
   * A list of collection names.
   */
  collections?: string[];
  /**
   * The hierarchy name.
   */
  hierarchy?: string;
  /**
   * The advanced search name.
   */
  advancedSearch?: string;
  /**
   * The last modified date range, with each date prefixed by "Start: " or "End: ".
   */
  lastModifiedDateRange?: [string, string];
  /**
   * The owner's username.
   */
  owner?: string;
  /**
   * A list of MIME types.
   */
  mimeTypes?: string[];
  /**
   * A list of classifications in the format "schemaNode: category".
   */
  classifications?: string[];
}

/**
 * Builds the path to be used for the links of Favourite Searches on the Favourite Searches Page.
 * Takes into consideration if the link is being used in Selection Sessions, and whether it is for a normal search or a search within a Hierarchy topic.
 *
 * @param originalPath The original path of the favourite search.
 */
export const buildFavouritesSearchUrl = (originalPath: string): string =>
  pipe(
    originalPath,
    E.fromPredicate<string, string>(
      () => !isSelectionSessionOpen(),
      () => buildFavouritesSearchSelectionSessionLink(originalPath),
    ),
    E.getOrElse(identity),
  );

// Try to get the hierarchy name, if it fails, return the compound UUID.
const getHierarchyName = (compoundUuid: string) =>
  pipe(
    TE.tryCatch(
      () => getHierarchyDetails(compoundUuid),
      (e) => `Failed to get hierarchy: ${e}`,
    ),
    TE.match(
      (e) => {
        console.warn(e);
        return compoundUuid;
      },
      (hierarchy) => hierarchy.summary.name,
    ),
  );

// Try to get the advanced search name, if it fails, return the UUID.
const getAdvancedSearchName = (advancedSearchUuid: string) =>
  pipe(
    TE.tryCatch(
      () => getAdvancedSearchByUuid(advancedSearchUuid),
      (e) => `Failed to get advanced search: ${e}`,
    ),
    TE.match(
      (e) => {
        console.warn(e);
        return advancedSearchUuid;
      },
      (search) => search.name,
    ),
  );

/**
 * Builds a function which converts an array of values to an array of strings, or returns undefined if the array is undefined.
 *
 * @param mapFn A function to convert each element of the array to a string.
 */
const nullableArrayToStringArray =
  <T>(mapFn: (t: T) => string) =>
  (arr?: T[]): string[] | undefined =>
    pipe(
      arr,
      O.fromNullable,
      O.map((a) => a.map(mapFn)),
      O.filter(A.isNonEmpty),
      O.toUndefined,
    );

// Convert categories (classifications) to a string array with the format "schemaNode:category".
const processCategories = (
  categories?: SelectedCategories[],
): string[] | undefined => {
  const processCategory = ({
    schemaNode,
    categories,
  }: SelectedCategories): O.Option<string[]> =>
    pipe(
      schemaNode,
      O.fromNullable,
      O.map((node) => categories.map((c) => `${node}: ${c}`)),
    );

  return pipe(
    categories,
    O.fromNullable,
    O.map(A.map(processCategory)),
    O.map(A.compact),
    O.map(A.flatten),
    O.toUndefined,
  );
};

// Convert the date range to a tuple of strings, or return undefined if date is undefined.
const processLastModifiedDateRange = (
  lastModifiedDateRange?: DateRange,
): [string, string] | undefined => {
  // Convert a date to a string with a label, or return undefined if the date is undefined.
  const processDate = (label: string, date?: Date) =>
    pipe(
      O.fromNullable(date),
      O.map((d) => `${label}: ${d.toDateString()}`),
    );

  return pipe(
    lastModifiedDateRange,
    O.fromNullable,
    O.chain((range) =>
      sequenceT(O.Apply)(
        processDate(startLabel, range.start),
        processDate(endLabel, range.end),
      ),
    ),
    O.toUndefined,
  );
};

/**
 * Converts SearchOptions to a map of strings for display in the favourites search.
 *
 * @param searchOption The search options.
 * @param advancedSearch The UUID of the advanced search.
 */
export const stringifySearchOptions = (
  searchOption?: SearchOptions,
  advancedSearch?: string,
): T.Task<FavouriteSearchOptionsSummary> => {
  const hierarchyNameTask: T.Task<string | undefined> = searchOption?.hierarchy
    ? getHierarchyName(searchOption.hierarchy)
    : T.of(undefined);

  const advancedSearchNameTask: T.Task<string | undefined> = advancedSearch
    ? getAdvancedSearchName(advancedSearch)
    : T.of(undefined);

  const stringifyCommonOptions = (
    opts?: SearchOptions,
  ): FavouriteSearchOptionsSummary => {
    if (opts === undefined) {
      return {};
    }

    const {
      query,
      collections,
      lastModifiedDateRange,
      owner,
      mimeTypes,
      selectedCategories,
    } = opts;

    return {
      query,
      collections: pipe(
        collections,
        nullableArrayToStringArray((c) => c.name),
      ),
      lastModifiedDateRange: processLastModifiedDateRange(
        lastModifiedDateRange,
      ),
      owner: owner?.username,
      mimeTypes: pipe(mimeTypes, nullableArrayToStringArray(identity)),
      classifications: processCategories(selectedCategories),
    };
  };

  return pipe(
    [hierarchyNameTask, advancedSearchNameTask],
    T.sequenceArray,
    T.map(([hierarchyName, advancedSearchName]) => ({
      ...stringifyCommonOptions(searchOption),
      hierarchy: hierarchyName,
      advancedSearch: advancedSearchName,
    })),
  );
};

/**
 * Build search options summary from the relative URL of a favourite search.
 *
 * @param path The relative URL of the favourite search.
 */
export const buildSearchOptionsSummary = (
  path: string,
): Promise<FavouriteSearchOptionsSummary | undefined> => {
  const buildErrorMessage = (error: unknown) =>
    `Failed to generate search options: ${String(error)}`;

  const parseUrl = (path: string): E.Either<string, URL> =>
    E.tryCatch(() => new URL(path, getBaseUrl()), buildErrorMessage);

  const generateSearchPageOptionsTask = (url: URL) =>
    TE.tryCatch(
      () => generateSearchPageOptionsFromUrl(url), // Promise<SearchPageOptions>
      buildErrorMessage,
    );

  return pipe(
    parseUrl(path),
    TE.fromEither,
    TE.bindTo("url"),
    TE.bind("options", ({ url }) => generateSearchPageOptionsTask(url)),
    TE.flatMapTask(({ url, options }) => {
      const advSearchUuid = getAdvancedSearchIdFromUrl(url);
      return stringifySearchOptions(options, advSearchUuid);
    }),
    TE.getOrThrow,
  )();
};
