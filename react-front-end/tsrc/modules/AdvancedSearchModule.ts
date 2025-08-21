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
import { Location as HistoryLocation } from "history";
import { memoize } from "lodash";
import { API_BASE_URL } from "../AppConfig";
import * as OEQ from "@openequella/rest-api-client";
import { constFalse, pipe } from "fp-ts/function";
import * as O from "fp-ts/Option";
import * as S from "fp-ts/string";

const LEGACY_ADVANCED_SEARCH_PARAM = "in";
const LEGACY_ADVANCED_SEARCH_PREFIX = "P";

/**
 * Retrieve the list of advanced searches from the server, utilising cached (memoized) results
 * if available.
 */
export const getAdvancedSearchesFromServer: () => Promise<
  OEQ.Common.BaseEntitySummary[]
> = memoize(
  async (): Promise<OEQ.Common.BaseEntitySummary[]> =>
    await OEQ.AdvancedSearch.listAdvancedSearches(API_BASE_URL),
);

/**
 * Retrieve definition of the Advanced search by UUID.
 *
 * @param uuid UUID of the Advanced search.
 */
export const getAdvancedSearchByUuid = (
  uuid: string,
): Promise<OEQ.AdvancedSearch.AdvancedSearchDefinition> =>
  OEQ.AdvancedSearch.getAdvancedSearchByUuid(API_BASE_URL, uuid);

// Check if the current URL params contains the legacy Advanced search parameters.
const isLegacyAdvancedSearch = (params: URLSearchParams) =>
  pipe(
    params.get(LEGACY_ADVANCED_SEARCH_PARAM),
    O.fromNullable,
    O.map(S.startsWith(LEGACY_ADVANCED_SEARCH_PREFIX)),
    O.getOrElse(constFalse),
  );

/**
 * Check if the current URL represents the legacy Advanced search path.
 * @param location Location of current window.
 */
export const isLegacyAdvancedSearchLocation = (
  location: Location | HistoryLocation,
): boolean => {
  const params = new URLSearchParams(location.search);
  return isLegacyAdvancedSearch(params);
};

// Check if the current URL represents the legacy Advanced search path.
const isLegacyAdvancedSearchUrl = (url: URL) => {
  const params = new URLSearchParams(url.search);
  return isLegacyAdvancedSearch(params);
};

/**
 * Extracts the Advanced Search ID from the new UI pathname.
 *
 * @param pathname The pathname of the URL.
 *                 Expected to be in the format of `/page/advancedsearch/{uuid}`.
 */
const getAdvancedSearchIdFromNewUI = (pathname: string): O.Option<string> => {
  // Regex of the new Advanced Search page path. The last group is expected to be a UUID. We can use
  // a more strict regex to ensure it's UUID, but...
  // For example: /page/advancedsearch/c9fd1ae8-0dc1-ab6f-e923-1f195a22d537
  const advancedSearchPagePath = /(\/page\/advancedsearch\/)(.+)/;

  return pipe(
    pathname.match(advancedSearchPagePath),
    O.fromNullable,
    O.chainNullableK((matches) => matches.pop()),
  );
};

// Get the Advanced Search ID from the old UI search parameters.
const getAdvancedSearchIdFromOldUI = (
  params: URLSearchParams,
): O.Option<string> =>
  pipe(
    params.get(LEGACY_ADVANCED_SEARCH_PARAM),
    O.fromNullable,
    O.filter(S.startsWith(LEGACY_ADVANCED_SEARCH_PREFIX)),
    O.map((param) => param.substring(LEGACY_ADVANCED_SEARCH_PREFIX.length)),
  );

/**
 * If the URL is the new Advanced Search path, get ID from the path.
 * If it's the old one, get ID from query param.
 *
 * @param url Provided URL to check.
 */
export const getAdvancedSearchIdFromUrl = (url: string): string | undefined =>
  pipe(
    O.tryCatch(() => new URL(url)),
    O.chain((u) =>
      isLegacyAdvancedSearchUrl(u)
        ? getAdvancedSearchIdFromOldUI(u.searchParams)
        : getAdvancedSearchIdFromNewUI(u.pathname),
    ),
    O.toUndefined,
  );

/**
 * If the URL is the new Advanced Search path, get ID from the path.
 * If it's the old one, get ID from query param.
 *
 * @param location Location of current window.
 */
export const getAdvancedSearchIdFromLocation = (
  location: HistoryLocation,
): string | undefined => {
  const params = new URLSearchParams(location.search);

  const uuid = isLegacyAdvancedSearchLocation(location)
    ? getAdvancedSearchIdFromOldUI(params)
    : getAdvancedSearchIdFromNewUI(location.pathname);

  return O.toUndefined(uuid);
};
