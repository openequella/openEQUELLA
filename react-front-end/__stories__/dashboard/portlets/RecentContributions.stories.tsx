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
import { Meta, StoryFn } from "@storybook/react";
import * as React from "react";
import { BrowserRouter } from "react-router-dom";
import { publicRecentContributionsPortlet } from "../../../__mocks__/Dashboard.mock";
import {
  getEmptySearchResult,
  getSearchResult,
  itemWithAttachment,
  itemWithBookmark,
  itemWithLongDescription,
} from "../../../__mocks__/SearchResult.mock";
import {
  PortletRecentContributions,
  PortletRecentContributionsProps,
} from "../../../tsrc/dashboard/portlet/PortletRecentContributions";

export default {
  title: "Dashboard/portlets/PortletRecentContributions",
  component: PortletRecentContributions,
  decorators: [
    (Story) => (
      <BrowserRouter>
        <Story />
      </BrowserRouter>
    ),
  ],
} as Meta<PortletRecentContributionsProps>;

const Template: StoryFn<PortletRecentContributionsProps> = (args) => (
  <PortletRecentContributions {...args} />
);

// Mock search providers with different scenarios
const mockSearchProvider = async (): Promise<
  OEQ.Search.SearchResult<OEQ.Search.SearchResultItem>
> => getSearchResult;

const emptySearchProvider = async (): Promise<
  OEQ.Search.SearchResult<OEQ.Search.SearchResultItem>
> => getEmptySearchResult;

const longDescriptionSearchProvider = async (): Promise<
  OEQ.Search.SearchResult<OEQ.Search.SearchResultItem>
> => ({
  start: 0,
  length: 3,
  available: 3,
  results: [itemWithLongDescription, itemWithAttachment, itemWithBookmark],
  highlight: [],
});

const failingSearchProvider = async (): Promise<
  OEQ.Search.SearchResult<OEQ.Search.SearchResultItem>
> => {
  throw new Error("Search failed");
};

const slowSearchProvider = async (): Promise<
  OEQ.Search.SearchResult<OEQ.Search.SearchResultItem>
> => {
  await new Promise((resolve) => setTimeout(resolve, 3000));
  return getSearchResult;
};

// Base configuration
const basePortletConfig = {
  ...publicRecentContributionsPortlet,
  commonDetails: {
    ...publicRecentContributionsPortlet.commonDetails,
    name: "Recent Contributions",
  },
};

export const Default = Template.bind({});
Default.args = {
  cfg: basePortletConfig,
  searchProvider: mockSearchProvider,
  position: { order: 0, column: 0 },
};

export const TitleOnly = Template.bind({});
TitleOnly.args = {
  ...Default.args,
  cfg: {
    ...basePortletConfig,
    isShowTitleOnly: true,
  },
};

export const WithLongDescriptions = Template.bind({});
WithLongDescriptions.args = {
  ...Default.args,
  searchProvider: longDescriptionSearchProvider,
};

export const NoResults = Template.bind({});
NoResults.args = {
  ...Default.args,
  searchProvider: emptySearchProvider,
};

export const WithSpecificQuery = Template.bind({});
WithSpecificQuery.args = {
  ...Default.args,
  cfg: {
    ...basePortletConfig,
    query: "hitchhiker guide galaxy",
    commonDetails: {
      ...basePortletConfig.commonDetails,
      name: "Hitchhiker's Guide Items",
    },
  },
  searchProvider: longDescriptionSearchProvider,
};

export const ErrorOnSearch = Template.bind({});
ErrorOnSearch.args = {
  ...Default.args,
  searchProvider: failingSearchProvider,
};

export const SlowLoading = Template.bind({});
SlowLoading.args = {
  ...Default.args,
  searchProvider: slowSearchProvider,
};
