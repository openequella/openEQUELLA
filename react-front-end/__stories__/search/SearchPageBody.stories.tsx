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
import {
  Button,
  Card,
  CardContent,
  CardHeader,
  List,
  ListItem,
} from "@mui/material";
import type { Meta, StoryFn, Decorator } from "@storybook/react";
import * as React from "react";
import { classifications } from "../../__mocks__/CategorySelector.mock";
import { customRefinePanelControl } from "../../__mocks__/RefinePanelControl.mock";
import { getRemoteSearchesFromServer } from "../../__mocks__/RemoteSearchModule.mock";
import { getSearchResult } from "../../__mocks__/SearchResult.mock";
import { defaultSearchSettings } from "../../tsrc/modules/SearchSettingsModule";
import {
  SearchPageBody,
  SearchPageBodyProps,
} from "../../tsrc/search/SearchPageBody";
import {
  defaultSearchPageHeaderConfig,
  defaultSearchPageOptions,
  defaultSearchPageRefinePanelConfig,
  SearchContext,
  SearchContextProps,
} from "../../tsrc/search/SearchPageHelper";
import { SearchPageSearchResult } from "../../tsrc/search/SearchPageReducer";

const nop = () => {};

const defaultSearchContextProps: SearchContextProps = {
  search: nop,
  searchState: { status: "initialising", options: defaultSearchPageOptions },
  searchSettings: {
    core: defaultSearchSettings,
    mimeTypeFilters: [],
    advancedSearches: [],
  },
  searchPageErrorHandler: nop,
};

const searchPageBodyProps: SearchPageBodyProps = {
  pathname: "/page/search",
  getRemoteSearchesProvider: getRemoteSearchesFromServer,
};

const buildSearchContextDecorator =
  (
    searchContextProps: SearchContextProps = defaultSearchContextProps,
  ): Decorator =>
  (Story) => (
    <SearchContext.Provider value={searchContextProps}>
      {Story()}
    </SearchContext.Provider>
  );

export default {
  title: "Search/SearchPageBody",
  component: SearchPageBody,
  decorators: [buildSearchContextDecorator()],
} as Meta<SearchPageBodyProps>;

export const Initialising: StoryFn<SearchPageBodyProps> = (args) => (
  <SearchPageBody {...args} />
);
Initialising.args = {
  ...searchPageBodyProps,
};

export const WithSearchResult: StoryFn<SearchPageBodyProps> = (args) => (
  <SearchPageBody {...args} />
);
WithSearchResult.decorators = [
  buildSearchContextDecorator({
    ...defaultSearchContextProps,
    searchState: {
      status: "success",
      options: defaultSearchPageOptions,
      result: {
        from: "item-search",
        content: getSearchResult,
      },
      classifications,
    },
  }),
];
WithSearchResult.args = {
  ...searchPageBodyProps,
};

export const AdditionalPanel: StoryFn<SearchPageBodyProps> = (args) => (
  <SearchPageBody {...args} />
);
AdditionalPanel.args = {
  ...searchPageBodyProps,
  additionalPanels: [
    <Card>
      <CardHeader title="Additional Panel" />
      <CardContent>This is an addition panel</CardContent>
    </Card>,
  ],
};

export const AdditionalHeader: StoryFn<SearchPageBodyProps> = (args) => (
  <SearchPageBody {...args} />
);
AdditionalHeader.args = {
  ...searchPageBodyProps,
  headerConfig: {
    ...defaultSearchPageHeaderConfig,
    additionalHeaders: [<Button color="secondary">Test</Button>],
  },
};

export const DisableCollectionFilter: StoryFn<SearchPageBodyProps> = (args) => (
  <SearchPageBody {...args} />
);
DisableCollectionFilter.args = {
  ...searchPageBodyProps,
  refinePanelConfig: {
    ...defaultSearchPageRefinePanelConfig,
    enableCollectionSelector: false,
  },
};

export const CustomSortingOptions: StoryFn<SearchPageBodyProps> = (args) => (
  <SearchPageBody {...args} />
);
CustomSortingOptions.args = {
  ...searchPageBodyProps,
  headerConfig: {
    ...defaultSearchPageHeaderConfig,
    customSortingOptions: new Map([
      ["rank", "custom option 1"],
      ["datemodified", "custom option 2"],
    ]),
  },
};

export const ShowAdvancedSearchFilter: StoryFn<SearchPageBodyProps> = (
  args,
) => <SearchPageBody {...args} />;
ShowAdvancedSearchFilter.args = {
  ...searchPageBodyProps,
  searchBarConfig: {
    advancedSearchFilter: {
      onClick: () => {},
      accent: true,
    },
  },
};

export const CustomRefinePanelControls: StoryFn<SearchPageBodyProps> = (
  args,
) => <SearchPageBody {...args} />;
CustomRefinePanelControls.args = {
  ...searchPageBodyProps,
  refinePanelConfig: {
    ...defaultSearchPageRefinePanelConfig,
    customRefinePanelControl: [customRefinePanelControl],
  },
};

export const CustomSearchResult: StoryFn<SearchPageBodyProps> = (args) => (
  <SearchPageBody {...args} />
);
CustomSearchResult.decorators = WithSearchResult.decorators;
CustomSearchResult.args = {
  ...searchPageBodyProps,
  customRenderSearchResults: (searchResult: SearchPageSearchResult) => (
    <List>
      {searchResult.content.results.map(({ name, uuid, version }) => (
        <ListItem key={uuid}>
          name: {name} | uuid: {uuid} | version: {version}
        </ListItem>
      ))}
    </List>
  ),
};
