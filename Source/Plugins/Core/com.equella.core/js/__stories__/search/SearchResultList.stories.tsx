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
import { boolean, object } from "@storybook/addon-knobs";
import * as React from "react";
import {
  defaultPagedSearchResult,
  defaultSearchOptions,
} from "../../tsrc/modules/SearchModule";
import { SearchResultList } from "../../tsrc/search/components/SearchResultList";
import {
  getSearchResult as singlePageSearch,
  getEmptySearchResult as emptySearch,
} from "../../__mocks__/getSearchResult";

export default {
  title: "Search/SearchResultList",
  component: SearchResultList,
};

const paginationProps = {
  currentPage: defaultSearchOptions.currentPage,
  rowsPerPage: defaultSearchOptions.rowsPerPage,
  onPageChange: action("onPageChange called"),
  onRowsPerPageChange: action("onRowsPerPageChange called"),
};

const orderSelectProps = {
  value: defaultSearchOptions.sortOrder,
  onChange: action("onChange called"),
};

const clearSearchAction = action("onClearSearchOptions called");

export const EmptyResultListComponent = () => (
  <SearchResultList
    searchResultItems={object("results", emptySearch.results)}
    showSpinner={boolean("showSpinner", false)}
    orderSelectProps={object("order", {
      ...orderSelectProps,
    })}
    paginationProps={object("pagination", {
      count: emptySearch.available,
      ...paginationProps,
    })}
    onClearSearchOptions={clearSearchAction}
  ></SearchResultList>
);

export const BasicSearchResultListComponent = () => (
  <SearchResultList
    searchResultItems={object("results", singlePageSearch.results)}
    showSpinner={boolean("showSpinner", false)}
    orderSelectProps={object("order", {
      ...orderSelectProps,
    })}
    paginationProps={object("pagination", {
      count: singlePageSearch.available,
      ...paginationProps,
    })}
    onClearSearchOptions={clearSearchAction}
  ></SearchResultList>
);

export const LoadingSearchResultListComponent = () => (
  <SearchResultList
    searchResultItems={object("results", defaultPagedSearchResult.results)}
    showSpinner={boolean("showSpinner", true)}
    orderSelectProps={object("order", {
      ...orderSelectProps,
    })}
    paginationProps={object("pagination", {
      count: defaultPagedSearchResult.available,
      ...paginationProps,
    })}
    onClearSearchOptions={clearSearchAction}
  ></SearchResultList>
);
