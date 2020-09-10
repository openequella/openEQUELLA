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

import {
  Classification,
  listClassifications,
  SelectedCategories,
} from "../modules/SearchFacetsModule";
import { languageStrings } from "../util/langstrings";
import { Card, CardContent, Grid, Typography } from "@material-ui/core";
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
import { CategorySelector } from "./components/CategorySelector";
import {
  RefinePanelControl,
  RefineSearchPanel,
} from "./components/RefineSearchPanel";
import { SearchAttachmentsSelector } from "./components/SearchAttachmentsSelector";
import { SearchResultList } from "./components/SearchResultList";
import { CollectionSelector } from "./components/CollectionSelector";
import { Collection } from "../modules/CollectionsModule";
import { useHistory } from "react-router";
import { DateRange, DateRangeSelector } from "../components/DateRangeSelector";
import OwnerSelector from "./components/OwnerSelector";
import StatusSelector from "./components/StatusSelector";

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
    OEQ.Search.SearchResult<OEQ.Search.SearchResultItem>
  >(defaultPagedSearchResult);
  const [showSpinner, setShowSpinner] = useState<boolean>(false);
  const [searchSettings, setSearchSettings] = useState<SearchSettings>();
  const [classifications, setClassifications] = useState<Classification[]>([]);
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
      setSearchSettings(settings);
      handleSortOrderChanged(
        searchPageOptions.sortOrder ?? settings.defaultSearchSort
      );
    });
  }, []);

  const isInitialSearch = useRef(true);
  // Trigger a search when Search options get changed, but skip the initial values.
  useEffect(() => {
    if (isInitialSearch.current) {
      isInitialSearch.current = false;
    } else {
      search();
    }
  }, [searchPageOptions]);

  // When Search options get changed, also update the Classification list.
  useEffect(() => {
    if (!isInitialSearch.current) {
      listClassifications(searchPageOptions).then((classifications) =>
        setClassifications(classifications)
      );
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
      .then((items: OEQ.Search.SearchResult<OEQ.Search.SearchResultItem>) => {
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
      selectedCategories: undefined,
    });

  const handleCollectionSelectionChanged = (collections: Collection[]) => {
    setSearchPageOptions({
      ...searchPageOptions,
      collections: collections,
      currentPage: 0,
      selectedCategories: undefined,
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
      selectedCategories: undefined,
    });

  const handleLastModifiedDateRangeChange = (dateRange?: DateRange) =>
    setSearchPageOptions({
      ...searchPageOptions,
      lastModifiedDateRange: dateRange,
      selectedCategories: undefined,
    });

  const handleClearSearchOptions = () =>
    setSearchPageOptions({
      ...defaultSearchPageOptions,
      sortOrder: searchSettings?.defaultSearchSort,
    });

  const handleOwnerChange = (owner: OEQ.UserQuery.UserDetails) =>
    setSearchPageOptions({
      ...searchPageOptions,
      owner: { ...owner },
      selectedCategories: undefined,
    });

  const handleOwnerClear = () =>
    setSearchPageOptions({
      ...searchPageOptions,
      owner: undefined,
      selectedCategories: undefined,
    });

  const handleStatusChange = (status: OEQ.Common.ItemStatus[]) =>
    setSearchPageOptions({
      ...searchPageOptions,
      status: [...status],
      selectedCategories: undefined,
    });

  const handleSearchAttachmentsChange = (searchAttachments: boolean) => {
    setSearchPageOptions({
      ...searchPageOptions,
      searchAttachments: searchAttachments,
    });
  };

  const handleSelectedCategoriesChange = (
    selectedCategories: SelectedCategories[]
  ) => {
    const getSchemaNode = (id: number) => {
      const node = classifications.find((c) => c.id === id)?.schemaNode;
      if (!node) {
        throw new Error(`Unable to find schema node for classification ${id}.`);
      }
      return node;
    };

    setSearchPageOptions({
      ...searchPageOptions,
      selectedCategories: selectedCategories.map((c) => ({
        ...c,
        schemaNode: getSchemaNode(c.id),
      })),
    });
  };

  const refinePanelControls: RefinePanelControl[] = [
    {
      idSuffix: "CollectionSelector",
      title: collectionSelectorTitle,
      component: (
        <CollectionSelector
          onSelectionChange={handleCollectionSelectionChanged}
          value={searchPageOptions.collections}
        />
      ),
      disabled: false,
    },
    {
      idSuffix: "DateRangeSelector",
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
      // Before Search settings are retrieved, do not show.
      disabled: searchSettings?.searchingDisableDateModifiedFilter ?? true,
    },
    {
      idSuffix: "OwnerSelector",
      title: searchStrings.filterOwner.title,
      component: (
        <OwnerSelector
          onClearSelect={handleOwnerClear}
          onSelect={handleOwnerChange}
          value={searchPageOptions.owner}
        />
      ),
      disabled: searchSettings?.searchingDisableOwnerFilter ?? true,
    },
    {
      idSuffix: "StatusSelector",
      title: searchStrings.statusSelector.title,
      component: (
        <StatusSelector
          onChange={handleStatusChange}
          value={searchPageOptions.status}
        />
      ),
      disabled: !searchSettings?.searchingShowNonLiveCheckbox ?? true,
    },
    {
      idSuffix: "SearchAttachmentsSelector",
      title: searchStrings.searchAttachmentsSelector.title,
      component: (
        <SearchAttachmentsSelector
          value={searchPageOptions.searchAttachments}
          onChange={handleSearchAttachmentsChange}
        />
      ),
      disabled: false,
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
              onClearSearchOptions={handleClearSearchOptions}
            />
          </Grid>
        </Grid>
      </Grid>

      <Grid item xs={3}>
        <Grid container direction="column" spacing={2}>
          <Grid item>
            <RefineSearchPanel controls={refinePanelControls} />
          </Grid>
          {classifications.length > 0 &&
            classifications.some((c) => c.categories.length > 0) && (
              <Grid item>
                <Card>
                  <CardContent>
                    <Typography variant="h5">
                      {languageStrings.searchpage.categorySelector.title}
                    </Typography>
                    <CategorySelector
                      classifications={classifications}
                      onSelectedCategoriesChange={
                        handleSelectedCategoriesChange
                      }
                      selectedCategories={searchPageOptions.selectedCategories}
                    />
                  </CardContent>
                </Card>
              </Grid>
            )}
        </Grid>
      </Grid>
    </Grid>
  );
};

export default SearchPage;
