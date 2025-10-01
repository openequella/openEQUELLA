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
import InfoIcon from "@mui/icons-material/Info";
import { IconButton } from "@mui/material";
import type { Meta, StoryFn } from "@storybook/react";
import * as React from "react";
import * as mockData from "../../__mocks__/searchresult_mock_data";
import SearchResult, {
  defaultActionButtonProps,
  SearchResultProps,
} from "../../tsrc/search/components/SearchResult";
import { itemWithBookmark } from "../../__mocks__/SearchResult.mock";

export default {
  title: "Search/SearchResult",
  component: SearchResult,
} as Meta<SearchResultProps>;

export const BasicSearchResult: StoryFn<SearchResultProps> = (args) => (
  <SearchResult {...args} />
);
BasicSearchResult.args = {
  item: mockData.basicSearchObj,
  highlights: [],
  getViewerDetails: async () => ({ viewerId: "fancy" }),
};

export const AttachmentSearchResult: StoryFn<SearchResultProps> = (args) => (
  <SearchResult {...args} />
);
AttachmentSearchResult.args = {
  ...BasicSearchResult.args,
  item: mockData.attachSearchObj,
  getItemAttachments: async () => mockData.attachSearchObj.attachments ?? [],
};

export const KeywordFoundInAttachmentSearchResult: StoryFn<
  SearchResultProps
> = (args) => <SearchResult {...args} />;
KeywordFoundInAttachmentSearchResult.args = {
  ...BasicSearchResult.args,
  item: { ...mockData.attachSearchObj, keywordFoundInAttachment: true },
  getItemAttachments: async () => mockData.attachSearchObj.attachments ?? [],
};

export const CustomMetadataSearchResult: StoryFn<SearchResultProps> = (
  args,
) => <SearchResult {...args} />;
CustomMetadataSearchResult.args = {
  ...BasicSearchResult.args,
  item: {
    ...mockData.customMetaSearchObj,
    keywordFoundInAttachment: false,
  },
  getItemAttachments: async () =>
    mockData.customMetaSearchObj.attachments ?? [],
};

export const HighlightedSearchResult: StoryFn<SearchResultProps> = (args) => (
  <SearchResult {...args} />
);
HighlightedSearchResult.args = {
  item: {
    ...mockData.basicSearchObj,
    name: "The life of cats and dogs in the big city",
    description:
      "This is the story of a dog and a cat in the city, exploring what cats and dogs do when there are no people. (Using highlights [cat, dog*].)",
  },
  highlights: ["cat", "dog*"],
};

export const CustomActionButtonSearchResult: StoryFn<SearchResultProps> = (
  args,
) => <SearchResult {...args} />;

CustomActionButtonSearchResult.args = {
  ...BasicSearchResult.args,
  customActionButtons: [
    <IconButton size="small">
      <InfoIcon />
    </IconButton>,
  ],
};

export const CustomTitleHandlerSearchResult: StoryFn<SearchResultProps> = (
  args,
) => <SearchResult {...args} />;

CustomTitleHandlerSearchResult.args = {
  ...BasicSearchResult.args,
  customOnClickTitleHandler: () => console.log("The is a custom handler"),
};

export const HideFavSearchResult: StoryFn<SearchResultProps> = (args) => (
  <SearchResult {...args} />
);

HideFavSearchResult.args = {
  ...BasicSearchResult.args,
  actionButtonConfig: {
    ...defaultActionButtonProps,
    showAddToFavourite: false,
  },
};

export const HideHierarchySearchResult: StoryFn<SearchResultProps> = (args) => (
  <SearchResult {...args} />
);

HideHierarchySearchResult.args = {
  ...BasicSearchResult.args,
  actionButtonConfig: {
    ...defaultActionButtonProps,
    showAddToHierarchy: false,
  },
};

export const ShowBookmarkTagsSearchResult: StoryFn<SearchResultProps> = (
  args,
) => <SearchResult {...args} />;

ShowBookmarkTagsSearchResult.args = {
  ...BasicSearchResult.args,
  item: itemWithBookmark,
  showBookmarkTags: true,
};
