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
import { action } from "@storybook/addon-actions";
import type { Meta, StoryFn } from "@storybook/react";
import { createRef } from "react";
import * as React from "react";
import {
  getEmptySearchResult as emptySearch,
  getSearchResult as singlePageSearch,
} from "../../__mocks__/SearchResult.mock";
import {
  mapSearchResultItems,
  SearchResultList,
  SearchResultListProps,
} from "../../tsrc/search/components/SearchResultList";
import { defaultSearchPageOptions } from "../../tsrc/search/SearchPageHelper";

export default {
  title: "Search/SearchResultList",
  component: SearchResultList,
  argTypes: {
    onClearSearchOptions: { action: "Clear search criteria" },
    onCopySearchLink: { action: "Copy a search link" },
    onSaveSearch: { action: "Save a search" },
  },
} as Meta<SearchResultListProps>;

const sharedPaginationArgs = {
  currentPage: defaultSearchPageOptions.currentPage,
  rowsPerPage: defaultSearchPageOptions.rowsPerPage,
  /* I wasn't able to get nested actions to work inside argTypes, so falling back to the old addon-actions for these. See https://github.com/storybookjs/storybook/issues/11525 and https://github.com/storybookjs/storybook/issues/10979#issuecomment-657640744*/
  onPageChange: action("onRowsPerPageChange"),
  onRowsPerPageChange: action("onRowsPerPageChange"),
};

export const EmptyResultListComponent: StoryFn<SearchResultListProps> = (
  args,
) => <SearchResultList {...args} />;

EmptyResultListComponent.args = {
  showSpinner: false,
  paginationProps: {
    ...sharedPaginationArgs,
    count: emptySearch.available,
  },
  orderSelectProps: {
    onChange: action("orderSelect onChange called"),
    value: defaultSearchPageOptions.sortOrder,
  },
  refineSearchProps: {
    isCriteriaSet: true,
    showRefinePanel: action("show Refine Panel"),
  },
  exportProps: {
    isExportPermitted: true,
    exportLinkProps: {
      url: "http://localhost:8080/export",
      onExport: action("Export search result"),
      alreadyExported: false,
      linkRef: createRef<HTMLAnchorElement>(),
    },
  },
};

export const BasicSearchResultListComponent: StoryFn<SearchResultListProps> = (
  args,
) => (
  <SearchResultList {...args}>
    {mapSearchResultItems(singlePageSearch.results, [], async () => ({
      viewerId: "fancy",
    }))}
  </SearchResultList>
);

BasicSearchResultListComponent.args = {
  ...EmptyResultListComponent.args,
  paginationProps: {
    ...sharedPaginationArgs,
    count: singlePageSearch.available,
  },
};

export const LoadingSearchResultListComponent: StoryFn<
  SearchResultListProps
> = (args) => <SearchResultList {...args} />;

LoadingSearchResultListComponent.args = {
  ...BasicSearchResultListComponent.args,
  showSpinner: true,
};
