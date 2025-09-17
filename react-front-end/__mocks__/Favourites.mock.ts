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
import { getAdvancedSearchesFromServerResult } from "./AdvancedSearchModule.mock";
import { topicWithShortAndLongDesc } from "./Hierarchy.mock";

const searchOptions =
  "?searchOptions=%7B%22rowsPerPage%22%3A10%2C%22currentPage%22%3A0%2C%22sortOrder%22%3A%22rank%22%2C%22rawMode%22%3Atrue%2C%22status%22%3A%5B%22LIVE%22%2C%22REVIEW%22%5D%2C%22searchAttachments%22%3Atrue%2C%22query%22%3A%22apple%22%2C%22collections%22%3A%5B%7B%22uuid%22%3A%228e3caf16-f3cb-b3dd-d403-e5eb8d545ffa%22%7D%2C%7B%22uuid%22%3A%228e3caf16-f3cb-b3dd-d403-e5eb8d545ffb%22%7D%5D%2C%22lastModifiedDateRange%22%3A%7B%22start%22%3A%222025-07-31T14%3A00%3A00.000Z%22%2C%22end%22%3A%222025-08-22T14%3A00%3A00.000Z%22%7D%2C%22owner%22%3A%7B%22id%22%3A%22test%22%7D%2C%22mimeTypeFilters%22%3A%5B%7B%22id%22%3A%22fe79c485-a6dd-4743-81e8-52de66494632%22%7D%2C%7B%22id%22%3A%22fe79c485-a6dd-4743-81e8-52de66494631%22%7D%5D%2C%22displayMode%22%3A%22list%22%2C%22dateRangeQuickModeEnabled%22%3Atrue%7D";
const addedAt = new Date("2025-08-26T00:00:00Z");

export const fullOptionsFavouriteSearch: OEQ.Favourite.FavouriteSearch = {
  id: 1,
  name: "Favourite Search",
  url: "/page/search" + searchOptions,
  addedAt,
};

export const hierarchyFavouriteSearch: OEQ.Favourite.FavouriteSearch = {
  id: 2,
  name: "Favourite Search for hierarchy",
  url: `/page/hierarchy/${topicWithShortAndLongDesc.compoundUuid}${searchOptions}`,
  addedAt,
};

export const advancedSearchFavouriteSearch: OEQ.Favourite.FavouriteSearch = {
  id: 3,
  name: "Favourite Search for advanced search",
  url: `/page/advancedsearch/${getAdvancedSearchesFromServerResult[0].uuid}${searchOptions}`,
  addedAt,
};

export const invalidFavouriteSearch: OEQ.Favourite.FavouriteSearch = {
  id: 4,
  name: "Invalid Favourite Search",
  url: "/page/search?searchOptions=invalid",
  addedAt,
};

export const emptyOptionsFavouriteSearch: OEQ.Favourite.FavouriteSearch = {
  id: 5,
  name: "Favourite Search",
  url: "/page/search?searchOptions=%7B%22rowsPerPage%22%3A10%7D",
  addedAt,
};

export const basicOptionsFavouriteSearch: OEQ.Favourite.FavouriteSearch = {
  id: 6,
  name: "Basic Favourite Search",
  url: "/page/search?searchOptions=%7B%22query%22%3A%22apple%22%7D",
  addedAt,
};
