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
import { privateSearchPortlet } from "../../../__mocks__/Dashboard.mock";
import {
  getEmptySearchResult,
  getSearchResult,
} from "../../../__mocks__/SearchResult.mock";
import {
  PortletQuickSearch,
  PortletQuickSearchProps,
} from "../../../tsrc/dashboard/portlet/PortletQuickSearch";
import { defaultSearchSettings } from "../../../tsrc/modules/SearchSettingsModule";

const mockSearchSettingsProvider = async () => defaultSearchSettings;
const errorSearchSettingsProvider = async () => {
  throw new Error("Failed to load settings");
};

export default {
  title: "Dashboard/portlets/PortletQuickSearch",
  component: PortletQuickSearch,
} as Meta<PortletQuickSearchProps>;

const Template: StoryFn<PortletQuickSearchProps> = (args) => (
  <PortletQuickSearch {...args} />
);

const mockSearchProvider = async () => getSearchResult;
const emptySearchProvider = async () => getEmptySearchResult;
const failingSearchProvider = async () => {
  throw new Error("Search failed");
};
const slowSearchProvider = async () => {
  await new Promise((resolve) => setTimeout(resolve, 2000));
  return getSearchResult;
};

export const Simple = Template.bind({});
Simple.args = {
  cfg: privateSearchPortlet,
  searchProvider: mockSearchProvider,
  searchSettingsProvider: mockSearchSettingsProvider,
};

export const NoResults = Template.bind({});
NoResults.args = {
  ...Simple.args,
  searchProvider: emptySearchProvider,
};

export const ErrorOnSearch = Template.bind({});
ErrorOnSearch.args = {
  ...Simple.args,
  searchProvider: failingSearchProvider,
};

export const SettingsError = Template.bind({});
SettingsError.args = {
  ...Simple.args,
  searchSettingsProvider: errorSearchSettingsProvider,
};

export const SlowSearch = Template.bind({});
SlowSearch.args = {
  ...Simple.args,
  searchProvider: slowSearchProvider,
};
