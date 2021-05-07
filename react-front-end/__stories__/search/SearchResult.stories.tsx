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
import type { Meta, Story } from "@storybook/react";
import * as React from "react";
import * as mockData from "../../__mocks__/searchresult_mock_data";
import SearchResult, {
  SearchResultProps,
} from "../../tsrc/search/components/SearchResult";

export default {
  title: "Search/SearchResult",
  component: SearchResult,
} as Meta<SearchResultProps>;

export const BasicSearchResult: Story<SearchResultProps> = (args) => (
  <SearchResult {...args} />
);
BasicSearchResult.args = {
  item: mockData.basicSearchObj,
  highlights: [],
  getViewerDetails: async () => ({ viewerId: "fancy" }),
};

export const AttachmentSearchResult: Story<SearchResultProps> = (args) => (
  <SearchResult {...args} />
);
AttachmentSearchResult.args = {
  ...BasicSearchResult.args,
  item: mockData.attachSearchObj,
};

export const KeywordFoundInAttachmentSearchResult: Story<SearchResultProps> = (
  args
) => <SearchResult {...args} />;
KeywordFoundInAttachmentSearchResult.args = {
  ...BasicSearchResult.args,
  item: { ...mockData.attachSearchObj, keywordFoundInAttachment: true },
};

export const CustomMetadataSearchResult: Story<SearchResultProps> = (args) => (
  <SearchResult {...args} />
);
CustomMetadataSearchResult.args = {
  ...BasicSearchResult.args,
  item: {
    ...mockData.customMetaSearchObj,
    keywordFoundInAttachment: false,
  },
};

export const HighlightedSearchResult: Story<SearchResultProps> = (args) => (
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
