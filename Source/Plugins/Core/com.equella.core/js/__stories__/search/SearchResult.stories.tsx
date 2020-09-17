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
import type { Story } from "@storybook/react";
import * as React from "react";
import * as mockData from "../../__mocks__/searchresult_mock_data";
import SearchResult from "../../tsrc/search/components/SearchResult";

export default {
  title: "Search/SearchResult",
  component: SearchResult,
};

export const BasicSearchResult: Story<OEQ.Search.SearchResultItem> = (args) => (
  <SearchResult {...args} />
);
BasicSearchResult.args = {
  ...mockData.basicSearchObj,
};

export const AttachmentSearchResult: Story<OEQ.Search.SearchResultItem> = (
  args
) => <SearchResult {...args} />;
AttachmentSearchResult.args = {
  ...mockData.attachSearchObj,
};

export const KeywordFoundInAttachmentSearchResult: Story<OEQ.Search.SearchResultItem> = (
  args
) => <SearchResult {...args} />;
KeywordFoundInAttachmentSearchResult.args = {
  ...mockData.attachSearchObj,
  keywordFoundInAttachment: true,
};

export const CustomMetadataSearchResult: Story<OEQ.Search.SearchResultItem> = (
  args
) => <SearchResult {...args} />;
CustomMetadataSearchResult.args = {
  ...mockData.customMetaSearchObj,
  keywordFoundInAttachment: false,
};
