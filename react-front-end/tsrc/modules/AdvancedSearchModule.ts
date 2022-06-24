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
import { Location } from "history";
import { memoize } from "lodash";
import { API_BASE_URL } from "../AppConfig";

/**
 * Retrieve the list of advanced searches from the server, utilising cached (memoized) results
 * if available.
 */
export const getAdvancedSearchesFromServer: () => Promise<
  OEQ.Common.BaseEntitySummary[]
> = memoize(
  async (): Promise<OEQ.Common.BaseEntitySummary[]> =>
    await OEQ.AdvancedSearch.listAdvancedSearches(API_BASE_URL)
);

/**
 * Retrieve definition of the Advanced search by UUID.
 *
 * @param uuid UUID of the Advanced search.
 */
export const getAdvancedSearchByUuid = (
  uuid: string
): Promise<OEQ.AdvancedSearch.AdvancedSearchDefinition> =>
  OEQ.AdvancedSearch.getAdvancedSearchByUuid(API_BASE_URL, uuid);

/**
 * If the URL is the new Advanced Search path, get ID from the path.
 * If it's the old one, get ID from query param.
 *
 * @param location Location of current window.
 */
export const getAdvancedSearchIdFromLocation = (
  location: Location
): string | undefined => {
  // Regex of the new Advanced Search page path. The last group is expected to be a UUID. We can use
  // a more strict regex to ensure it's UUID, but...
  // For example: /page/advancedsearch/c9fd1ae8-0dc1-ab6f-e923-1f195a22d537
  const advancedSearchPagePath = /(\/page\/advancedsearch\/)(.+)/;
  const matches: string[] | null = location.pathname.match(
    advancedSearchPagePath
  );
  if (matches) {
    return matches.pop() ?? undefined;
  }

  const currentParams = new URLSearchParams(location.search);
  const legacyAdvancedSearchId = currentParams.get("in");
  return legacyAdvancedSearchId?.startsWith("P")
    ? legacyAdvancedSearchId.substring(1)
    : undefined;
};
