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
import { waitFor } from "@testing-library/react";
import { pipe } from "fp-ts/function";
import { DateTime } from "luxon";
import { getAdvancedSearchDefinition } from "../../../../__mocks__/AdvancedSearchModule.mock";
import {
  advancedSearchFavouriteSearch,
  emptyOptionsFavouriteSearch,
  hierarchyFavouriteSearch,
  invalidFavouriteSearch,
} from "../../../../__mocks__/Favourites.mock";
import { BOOKS, VIDEOS } from "../../../../__mocks__/getCollectionsResp";
import { topicWithShortAndLongDesc } from "../../../../__mocks__/Hierarchy.mock";
import { getMimeTypeFilters } from "../../../../__mocks__/MimeTypeFilter.mock";
import * as UserModuleMock from "../../../../__mocks__/UserModule.mock";
import { languageStrings } from "../../../../tsrc/util/langstrings";
import * as A from "fp-ts/Array";
import {
  showAllSearchCriteria,
  dateString,
  defaultProps,
  mockApis,
  renderFavouriteSearch,
  waitForSearchOptions,
} from "./FavouritesSearchTestHelper";

const {
  searchCriteria: searchCriteriaLabel,
  searchCriteriaLabels: {
    query: queryLabel,
    collection: collectionLabel,
    hierarchy: hierarchyLabel,
    advancedSearch: advancedSearchLabel,
    start: startLabel,
    end: endLabel,
  },
  remove: removeLabel,
} = languageStrings.favourites.favouritesSearch;

mockApis();

describe("<FavouriteSearch />", () => {
  it("should display title", () => {
    const { queryByText } = renderFavouriteSearch();

    expect(queryByText(defaultProps.favouriteSearch.name)).toBeInTheDocument();
  });

  it("should display added at time", () => {
    const { queryByText } = renderFavouriteSearch();

    const dateString = DateTime.fromJSDate(
      defaultProps.favouriteSearch.addedAt,
    ).toRelative();

    expect(queryByText(dateString!)).toBeInTheDocument();
  });

  it("should display remove icon", () => {
    const { queryByLabelText } = renderFavouriteSearch();

    expect(queryByLabelText(removeLabel)).toBeInTheDocument();
  });

  it("should display default search options", async () => {
    const expectedLabels = [
      `${queryLabel}: apple`,
      `${collectionLabel}: ${BOOKS.name}`,
      `${collectionLabel}: ${VIDEOS.name}`,
    ];

    const { container, queryByText } = renderFavouriteSearch();
    await waitForSearchOptions(container);

    pipe(
      expectedLabels,
      A.map((expectedLabel) => {
        const label = queryByText(expectedLabel);
        expect(label).toBeInTheDocument();
      }),
    );
    expect.assertions(expectedLabels.length);
  });

  it.each([
    [
      "hierarchy",
      hierarchyFavouriteSearch,
      `${hierarchyLabel}: ${topicWithShortAndLongDesc.name}`,
    ],
    [
      "advanced search",
      advancedSearchFavouriteSearch,
      `${advancedSearchLabel}: ${getAdvancedSearchDefinition.name}`,
    ],
  ])(
    "should display %s name in default search options",
    async (_, favouriteSearch, expectedLabel) => {
      const { container, queryByText } = renderFavouriteSearch({
        ...defaultProps,
        favouriteSearch,
      });
      await waitForSearchOptions(container);

      expect(queryByText(expectedLabel)).toBeInTheDocument();
    },
  );

  it("should display more search options", async () => {
    const { container, queryByText } = renderFavouriteSearch();
    await waitForSearchOptions(container);
    await showAllSearchCriteria(container);

    const expectedLabels = [
      // Query.
      "apple",
      // Collections.
      BOOKS.name,
      VIDEOS.name,
      // Last modified date range.
      `${startLabel}: ${dateString("2025-07-31T14:00:00.000Z")}`,
      `${endLabel}: ${dateString("2025-08-22T14:00:00.000Z")}`,
      // Owner.
      UserModuleMock.users[0].username,
      // Mime types.
      ...getMimeTypeFilters[0].mimeTypes,
      ...getMimeTypeFilters[1].mimeTypes,
    ];

    pipe(
      expectedLabels,
      A.map((expectedLabel) => {
        const label = queryByText(expectedLabel);
        expect(label).toBeInTheDocument();
      }),
    );
    expect.assertions(expectedLabels.length);
  });

  it.each([
    ["hierarchy", hierarchyFavouriteSearch, topicWithShortAndLongDesc.name],
    [
      "advanced search",
      advancedSearchFavouriteSearch,
      getAdvancedSearchDefinition.name,
    ],
  ])(
    "should display %s name in more search options",
    async (_, favouriteSearch, expectedLabel) => {
      const { container, queryByText } = renderFavouriteSearch({
        ...defaultProps,
        favouriteSearch,
      });
      await waitForSearchOptions(container);
      await showAllSearchCriteria(container);

      expect(queryByText(expectedLabel!)).toBeInTheDocument();
    },
  );

  it("should not display search options if search options are empty", async () => {
    const { queryByText, container } = renderFavouriteSearch({
      ...defaultProps,
      favouriteSearch: emptyOptionsFavouriteSearch,
    });

    // Skeleton should disappear first.
    await waitFor(() =>
      expect(
        container.querySelector("span.MuiSkeleton-root"),
      ).not.toBeInTheDocument(),
    );
    expect(queryByText(searchCriteriaLabel)).not.toBeInTheDocument();
  });

  it("should display an error message if it failed to generate search options", async () => {
    const error =
      "Error: Failed to generate search options: SyntaxError: Unexpected token 'i', \"invalid\" is not valid JSON";
    const { findByText } = renderFavouriteSearch({
      ...defaultProps,
      favouriteSearch: invalidFavouriteSearch,
    });

    const errorMessage = await findByText(error);
    expect(errorMessage).toBeInTheDocument();
  });
});
