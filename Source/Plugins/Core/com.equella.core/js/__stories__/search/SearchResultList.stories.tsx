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
import {
  getEmptySearchResult as emptySearch,
  getSearchResult as singlePageSearch,
} from "../../__mocks__/getSearchResult";
import { defaultSearchOptions } from "../../tsrc/modules/SearchModule";
import {
  SearchResultList,
  SearchResultListProps,
} from "../../tsrc/search/components/SearchResultList";
import { action } from "@storybook/addon-actions";

export default {
  title: "Search/SearchResultList",
  component: SearchResultList,
  argTypes: {
    onClearSearchOptions: { action: "onClearSearchOptions called" },
  },
} as Meta<SearchResultListProps>;

const sharedPaginationArgs = {
  currentPage: defaultSearchOptions.currentPage,
  rowsPerPage: defaultSearchOptions.rowsPerPage,
  /* I wasn't able to get nested actions to work inside argTypes, so falling back to the old addon-actions for these. See https://github.com/storybookjs/storybook/issues/11525 and https://github.com/storybookjs/storybook/issues/10979#issuecomment-657640744*/
  onPageChange: action("onRowsPerPageChange"),
  onRowsPerPageChange: action("onRowsPerPageChange"),
};

const sharedOrderSelectProps = {
  value: defaultSearchOptions.sortOrder,
  /* I wasn't able to get nested actions to work inside argTypes, so falling back to the old addon-actions for these. See https://github.com/storybookjs/storybook/issues/11525 and https://github.com/storybookjs/storybook/issues/10979#issuecomment-657640744*/
  onChange: action("onChange called"),
};

export const EmptyResultListComponent: Story<SearchResultListProps> = (
  args
) => <SearchResultList {...args}></SearchResultList>;

EmptyResultListComponent.args = {
  searchResultItems: emptySearch.results,
  showSpinner: false,
  paginationProps: {
    ...sharedPaginationArgs,
    count: emptySearch.available,
  },
  orderSelectProps: {
    ...sharedOrderSelectProps,
  },
};

export const BasicSearchResultListComponent: Story<SearchResultListProps> = (
  args
) => <SearchResultList {...args}></SearchResultList>;

BasicSearchResultListComponent.args = {
  searchResultItems: singlePageSearch.results,
  showSpinner: false,
  paginationProps: {
    ...sharedPaginationArgs,
    count: singlePageSearch.available,
  },
  orderSelectProps: {
    ...sharedOrderSelectProps,
  },
};

export const LoadingSearchResultListComponent: Story<SearchResultListProps> = (
  args
) => <SearchResultList {...args}></SearchResultList>;

LoadingSearchResultListComponent.args = {
  searchResultItems: singlePageSearch.results,
  showSpinner: true,
  paginationProps: {
    ...sharedPaginationArgs,
    count: singlePageSearch.available,
  },
  orderSelectProps: {
    ...sharedOrderSelectProps,
  },
};
