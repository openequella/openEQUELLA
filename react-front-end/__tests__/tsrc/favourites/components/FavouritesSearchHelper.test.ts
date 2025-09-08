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

import "@testing-library/jest-dom";
import { pipe } from "fp-ts/function";
import { getAdvancedSearchDefinition } from "../../../../__mocks__/AdvancedSearchModule.mock";
import {
  getHierarchyDetails,
  topicWithShortAndLongDesc,
} from "../../../../__mocks__/Hierarchy.mock";
import { allSearchPageOptions } from "../../../../__mocks__/searchOptions.mock";
import * as A from "fp-ts/Array";
import {
  FavouriteSearchOptionsSummary,
  stringifySearchOptions,
} from "../../../../tsrc/favourites/components/FavouritesSearchHelper";
import * as AdvancedSearchModule from "../../../../tsrc/modules/AdvancedSearchModule";
import * as HierarchyModule from "../../../../tsrc/modules/HierarchyModule";
import { languageStrings } from "../../../../tsrc/util/langstrings";

const { end: endLabel, start: startLabel } =
  languageStrings.favourites.favouritesSearch.searchCriteriaLabels;

jest
  .spyOn(HierarchyModule, "getHierarchyDetails")
  .mockImplementation(getHierarchyDetails);

jest
  .spyOn(AdvancedSearchModule, "getAdvancedSearchByUuid")
  .mockResolvedValue(getAdvancedSearchDefinition);

describe("stringifySearchOptions", () => {
  const { compoundUuid: hierarchyUuid, name: hierarchyName } =
    topicWithShortAndLongDesc;

  const { query, collections, owner, mimeTypes } = allSearchPageOptions;

  const expectedResult: FavouriteSearchOptionsSummary = {
    query,
    collections: pipe(
      collections!,
      A.map((c) => c.name),
    ),
    hierarchy: hierarchyName,
    advancedSearch: getAdvancedSearchDefinition.name,
    lastModifiedDateRange: [
      `${startLabel}: Tue May 26 2020`,
      `${endLabel}: Wed May 27 2020`,
    ],
    owner: owner!.username,
    mimeTypes,
    classifications: ["/item/place: Hobart", "/item/other: Some cool things"],
  };

  it("generates human readable strings for search options", async () => {
    const result = await stringifySearchOptions(
      allSearchPageOptions,
      hierarchyUuid,
      "369c92fa-ae59-4845-957d-8fcaa22c15e3",
    )();
    expect(result).toStrictEqual(expectedResult);
  });
});
