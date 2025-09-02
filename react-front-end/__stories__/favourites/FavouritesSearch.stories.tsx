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
import { Meta, StoryFn } from "@storybook/react";
import * as React from "react";
import FavouritesSearch, {
  FavouritesSearchProps,
} from "../../tsrc/favourites/components/FavouritesSearch";
import * as OEQ from "@openequella/rest-api-client";
import { FavouriteSearchOptionsSummary } from "../../tsrc/favourites/components/FavouritesSearchHelper";

export default {
  title: "Favourites/FavouritesSearch",
  component: FavouritesSearch,
  argTypes: {
    onChange: { action: "onChange" },
  },
} as Meta<FavouritesSearchProps>;

const defaultSearch: OEQ.Favourite.FavouriteSearch = {
  id: 1,
  name: "Title of Favourite Search",
  url: "/page/search?searchOptions=%7B%22rowsPerPage%22%3A10%2C%22currentPage%22%3A0%2C%22sortOrder%22%3A%22rank%22%2C%22rawMode%22%3Afalse%2C%22status%22%3A%5B%22LIVE%22%2C%22REVIEW%22%5D%2C%22searchAttachments%22%3Atrue%2C%22query%22%3A%22%22%2C%22collections%22%3A%5B%7B%22uuid%22%3A%22f5768fd7-5f2d-44c7-a7de-a2a483723881%22%7D%2C%7B%22uuid%22%3A%2229c6294b-58df-485b-b194-92740d565fef%22%7D%5D%2C%22lastModifiedDateRange%22%3A%7B%22start%22%3A%222025-07-12T07%3A03%3A17.251Z%22%7D%2C%22owner%22%3A%7B%22id%22%3A%22e50ce7b8-61b3-43c8-9d50-5f311da93707%22%7D%2C%22mimeTypeFilters%22%3A%5B%7B%22id%22%3A%229f36d922-0baf-4fa4-9065-f215ac2b2d7e%22%7D%2C%7B%22id%22%3A%22eed0667b-ef27-4372-9d27-6280c7722ce7%22%7D%5D%2C%22displayMode%22%3A%22list%22%2C%22dateRangeQuickModeEnabled%22%3Atrue%7D",
  addedAt: new Date("2023-10-01T12:00:00Z"),
};

const defaultArgs: FavouritesSearchProps = {
  favouriteSearch: defaultSearch,
  highlights: ["Title"],
};

const defaultSearchOptionLabels: FavouriteSearchOptionsSummary = {
  query: "Apple",
  collections: ["Collection 1", "Collection 2"],
  lastModifiedDateRange: ["Start: Mon Aug 17 2025", "End: Mon Aug 18 2025"],
  owner: "username",
  mimeTypes: ["Image/png", "Image/jpeg", "Application/pdf"],
  classifications: [
    "/item/itembody/ResourceType: Optional+reading",
    "/item/itembody/Author/Name: Fox,+Quick+Brown",
  ],
};

export const Standard: StoryFn<FavouritesSearchProps> = (args) => (
  <FavouritesSearch {...args} />
);
Standard.args = {
  ...defaultArgs,
  favouriteSearchOptionsSummaryProvider: async () => defaultSearchOptionLabels,
};

export const AdvancedSearch: StoryFn<FavouritesSearchProps> = (args) => (
  <FavouritesSearch {...args} />
);
AdvancedSearch.args = {
  ...defaultArgs,
  favouriteSearchOptionsSummaryProvider: async () => ({
    ...defaultSearchOptionLabels,
    advancedSearch: "Name of the Advanced Search",
  }),
};

export const HierarchySearch: StoryFn<FavouritesSearchProps> = (args) => (
  <FavouritesSearch {...args} />
);
HierarchySearch.args = {
  ...defaultArgs,
  favouriteSearchOptionsSummaryProvider: async () => ({
    ...defaultSearchOptionLabels,
    hierarchy: "Name of the hierarchy",
  }),
};

export const Loading: StoryFn<FavouritesSearchProps> = (args) => (
  <FavouritesSearch {...args} />
);
Loading.args = {
  ...defaultArgs,
  favouriteSearchOptionsSummaryProvider: () => new Promise(() => {}),
};

export const WithoutSearchOptions: StoryFn<FavouritesSearchProps> = (args) => (
  <FavouritesSearch {...args} />
);
WithoutSearchOptions.args = {
  ...defaultArgs,
  favouriteSearch: { ...defaultSearch, url: "/page/no-search-options" },
  favouriteSearchOptionsSummaryProvider: async () => undefined,
};

export const WithErrorMessage: StoryFn<FavouritesSearchProps> = (args) => (
  <FavouritesSearch {...args} />
);
WithErrorMessage.args = {
  ...defaultArgs,
  favouriteSearchOptionsSummaryProvider: () =>
    new Promise((_, reject) => reject("This is an error message")),
};
