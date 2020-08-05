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
import * as React from "react";
import { useEffect, useRef, useState } from "react";
import {
  templateDefaults,
  templateError,
  TemplateUpdateProps,
} from "../mainui/Template";
import { languageStrings } from "../util/langstrings";
import { Grid } from "@material-ui/core";
import {
  defaultPagedSearchResult,
  defaultSearchOptions,
  searchItems,
  SearchOptions,
} from "../modules/SearchModule";
import SearchBar from "../search/components/SearchBar";
import * as OEQ from "@openequella/rest-api-client";
import { generateFromError } from "../api/errors";
import {
  getSearchSettingsFromServer,
  SearchSettings,
  SortOrder,
} from "../modules/SearchSettingsModule";
import {
  RefinePanelControl,
  RefineSearchPanel,
} from "./components/RefineSearchPanel";
import { SearchResultList } from "./components/SearchResultList";
import { CollectionSelector } from "./components/CollectionSelector";
import { Collection } from "../modules/CollectionsModule";
import { useHistory } from "react-router";
import { DateRange, DateRangeSelector } from "../components/DateRangeSelector";
import OwnerSelector from "./components/OwnerSelector";

/**
 * Type of search options that are specific to Search page presentation layer.
 */
export interface SearchPageOptions extends SearchOptions {
  /**
   * Whether to enable Quick mode (true) or to use custom date pickers (false).
   */
  dateRangeQuickModeEnabled: boolean;
}

const SearchPage = ({ updateTemplate }: TemplateUpdateProps) => {
  const searchStrings = languageStrings.searchpage;
  const {
    title: dateModifiedSelectorTitle,
    quickOptionDropdown,
  } = searchStrings.lastModifiedDateSelector;
  const { title: collectionSelectorTitle } = searchStrings.collectionSelector;

  const history = useHistory();

  const defaultSearchPageOptions: SearchPageOptions = {
    ...defaultSearchOptions,
    dateRangeQuickModeEnabled: true,
  };
  const [searchPageOptions, setSearchPageOptions] = useState<SearchPageOptions>(
    // If the user has gone 'back' to this page, then use their previous options. Otherwise
    // we start fresh - i.e. if a new navigation to Search Page.
    (history.location.state as SearchPageOptions) ?? defaultSearchPageOptions
  );
  const [pagedSearchResult, setPagedSearchResult] = useState<
    OEQ.Common.PagedResult<OEQ.Search.SearchResultItem>
  >(defaultPagedSearchResult);
  const [showSpinner, setShowSpinner] = useState<boolean>(false);

  /**
   * Update the page title and retrieve Search settings.
   */
  useEffect(() => {
    updateTemplate((tp) => ({
      ...templateDefaults(searchStrings.title)(tp),
    }));

    // Show spinner before calling API to retrieve Search settings.
    setShowSpinner(true);
    getSearchSettingsFromServer().then((settings: SearchSettings) => {
      handleSortOrderChanged(
        searchPageOptions.sortOrder ?? settings.defaultSearchSort
      );
    });
  }, []);

  /**
   * Trigger a search when state values change, but skip the initial values.
   */
  const isInitialSearch = useRef(true);
  useEffect(() => {
    if (isInitialSearch.current) {
      isInitialSearch.current = false;
    } else {
      search();
    }
  }, [searchPageOptions]);

  const handleError = (error: Error) => {
    updateTemplate(templateError(generateFromError(error)));
  };

  /**
   * Search items with specified search criteria and show a spinner when the search is in progress.
   */
  const search = (): void => {
    setShowSpinner(true);
    searchItems(searchPageOptions)
      .then((items: OEQ.Common.PagedResult<OEQ.Search.SearchResultItem>) => {
        setPagedSearchResult(items);
        history.replace({ ...history.location, state: searchPageOptions });
        // scroll back up to the top of the page
        window.scrollTo(0, 0);
      })
      .catch(handleError)
      .finally(() => setShowSpinner(false));
  };

  const handleSortOrderChanged = (order: SortOrder) =>
    setSearchPageOptions({ ...searchPageOptions, sortOrder: order });

  const handleQueryChanged = (query: string) =>
    setSearchPageOptions({
      ...searchPageOptions,
      query: query,
      currentPage: 0,
    });

  const handleCollectionSelectionChanged = (collections: Collection[]) => {
    setSearchPageOptions({
      ...searchPageOptions,
      collections: collections,
      currentPage: 0,
    });
  };

  const handlePageChanged = (page: number) =>
    setSearchPageOptions({ ...searchPageOptions, currentPage: page });

  const handleRowsPerPageChanged = (rowsPerPage: number) =>
    setSearchPageOptions({
      ...searchPageOptions,
      currentPage: 0,
      rowsPerPage: rowsPerPage,
    });

  const handleRawModeChanged = (rawMode: boolean) =>
    setSearchPageOptions({ ...searchPageOptions, rawMode: rawMode });

  const handleQuickDateRangeModeChange = (
    quickDateRangeMode: boolean,
    dateRange?: DateRange
  ) =>
    setSearchPageOptions({
      ...searchPageOptions,
      dateRangeQuickModeEnabled: quickDateRangeMode,
      // When the mode is changed, the date range may also need to be updated.
      // For example, if a custom date range is converted to Quick option 'All', then both start and end should be undefined.
      lastModifiedDateRange: dateRange,
    });

  const handleLastModifiedDateRangeChange = (dateRange?: DateRange) =>
    setSearchPageOptions({
      ...searchPageOptions,
      lastModifiedDateRange: dateRange,
    });

  const handleOwnerChange = (owner: OEQ.UserQuery.UserDetails) =>
    setSearchPageOptions({
      ...searchPageOptions,
      owner: { ...owner },
    });

  const handleOwnerClear = () =>
    setSearchPageOptions({
      ...searchPageOptions,
      owner: undefined,
    });

  const refinePanelControls: RefinePanelControl[] = [
    {
      title: collectionSelectorTitle,
      component: (
        <CollectionSelector
          onSelectionChange={handleCollectionSelectionChanged}
          value={searchPageOptions.collections}
        />
      ),
    },
    {
      title: dateModifiedSelectorTitle,
      component: (
        <DateRangeSelector
          onDateRangeChange={handleLastModifiedDateRangeChange}
          onQuickModeChange={handleQuickDateRangeModeChange}
          quickOptionDropdownLabel={quickOptionDropdown}
          dateRange={searchPageOptions.lastModifiedDateRange}
          quickModeEnabled={searchPageOptions.dateRangeQuickModeEnabled}
        />
      ),
    },
    {
      title: searchStrings.filterOwner.title,
      component: (
        <OwnerSelector
          onClearSelect={handleOwnerClear}
          onSelect={handleOwnerChange}
          value={searchPageOptions.owner}
        />
      ),
    },
  ];

  return (
    <Grid container spacing={2}>
      <Grid item xs={9}>
        <Grid container spacing={2}>
          <Grid item xs={12}>
            <SearchBar
              query={searchPageOptions.query ?? ""}
              rawMode={searchPageOptions.rawMode}
              onQueryChange={handleQueryChanged}
              onRawModeChange={handleRawModeChanged}
              doSearch={search}
            />
          </Grid>
          <Grid item xs={12}>
            <SearchResultList
              searchResultItems={pagedSearchResult.results}
              showSpinner={showSpinner}
              paginationProps={{
                count: pagedSearchResult.available,
                currentPage: searchPageOptions.currentPage,
                rowsPerPage: searchPageOptions.rowsPerPage,
                onPageChange: handlePageChanged,
                onRowsPerPageChange: handleRowsPerPageChanged,
              }}
              orderSelectProps={{
                value: searchPageOptions.sortOrder,
                onChange: handleSortOrderChanged,
              }}
            />
          </Grid>
        </Grid>
      </Grid>

      <Grid item xs={3}>
        <RefineSearchPanel controls={refinePanelControls} />
      </Grid>
    </Grid>
  );
};

export default SearchPage;
