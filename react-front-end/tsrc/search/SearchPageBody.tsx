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
import { debounce, Drawer, Grid, Hidden } from "@material-ui/core";
import * as OEQ from "@openequella/rest-api-client";
import { isEqual } from "lodash";
import * as React from "react";
import { useCallback, useContext, useMemo, useRef, useState } from "react";
import { useHistory } from "react-router";
import { getBaseUrl } from "../AppConfig";
import { DateRangeSelector } from "../components/DateRangeSelector";
import MessageInfo, { MessageInfoVariant } from "../components/MessageInfo";
import { AppContext } from "../mainui/App";
import { NEW_SEARCH_PATH, routes } from "../mainui/routes";
import { getAdvancedSearchIdFromLocation } from "../modules/AdvancedSearchModule";
import { Collection } from "../modules/CollectionsModule";
import { addFavouriteSearch } from "../modules/FavouriteModule";
import { GallerySearchResultItem } from "../modules/GallerySearchModule";
import {
  buildSelectionSessionAdvancedSearchLink,
  buildSelectionSessionRemoteSearchLink,
  buildSelectionSessionSearchPageLink,
  isSelectionSessionOpen,
} from "../modules/LegacySelectionSessionModule";
import { getRemoteSearchesFromServer } from "../modules/RemoteSearchModule";
import {
  Classification,
  SelectedCategories,
} from "../modules/SearchFacetsModule";
import {
  buildExportUrl,
  confirmExport,
  DisplayMode,
  SearchOptionsFields,
} from "../modules/SearchModule";
import { DateRange } from "../util/Date";
import { languageStrings } from "../util/langstrings";
import { AdvancedSearchSelector } from "./components/AdvancedSearchSelector";
import { AuxiliarySearchSelector } from "./components/AuxiliarySearchSelector";
import { CollectionSelector } from "./components/CollectionSelector";
import DisplayModeSelector from "./components/DisplayModeSelector";
import { FavouriteSearchDialog } from "./components/FavouriteSearchDialog";
import GallerySearchResult from "./components/GallerySearchResult";
import { MimeTypeFilterSelector } from "./components/MimeTypeFilterSelector";
import OwnerSelector from "./components/OwnerSelector";
import { RefinePanelControl } from "./components/RefineSearchPanel";
import { SearchAttachmentsSelector } from "./components/SearchAttachmentsSelector";
import SearchBar from "./components/SearchBar";
import {
  mapSearchResultItems,
  SearchResultList,
} from "./components/SearchResultList";
import { SidePanel } from "./components/SidePanel";
import StatusSelector from "./components/StatusSelector";
import { SearchContext } from "./Search";
import {
  defaultPagedSearchResult,
  defaultSearchPageHeaderConfig,
  defaultSearchPageOptions,
  defaultSearchPageRefinePanelConfig,
  generateExportErrorMessage,
  generateQueryStringFromSearchPageOptions,
  getPartialSearchOptions,
  SearchPageHeaderConfig,
  SearchPageOptions,
  SearchPageRefinePanelConfig,
  SearchPageSearchBarConfig,
  writeRawModeToStorage,
} from "./SearchPageHelper";
import { SearchPageSearchResult } from "./SearchPageReducer";
import * as O from "fp-ts/Option";
import { pipe } from "fp-ts/function";

const { searchpage: searchStrings } = languageStrings;
const { title: dateModifiedSelectorTitle, quickOptionDropdown } =
  searchStrings.lastModifiedDateSelector;
const { title: collectionSelectorTitle } = searchStrings.collectionSelector;
const { title: displayModeSelectorTitle } = searchStrings.displayModeSelector;

export interface SearchPageBodyProps {
  /**
   * URL path representing where the component is used.
   */
  pathname: string;
  /**
   * Any panel that will be rendered in between SearchBar and SearchResultList.
   */
  additionalPanels?: JSX.Element[];
  /**
   * Configuration for Search page headers, including whether to enable some common components(e.g. Share search),
   * custom sorting options and custom components.
   */
  headerConfig?: SearchPageHeaderConfig;
  /**
   * Configuration for whether to enable filters of the Refine search panel.
   */
  refinePanelConfig?: SearchPageRefinePanelConfig;
  /**
   * `true` to enable the faceted search.
   */
  enableClassification?: boolean;
  /**
   * Configuration for any custom components in Search bar. Currently, only support enabling/disabling the
   * Advanced search filter.
   */
  searchBarConfig?: SearchPageSearchBarConfig;
  /**
   * Customised callback fired after a search is complete.
   */
  customSearchCallback?: () => void;
}

/**
 * This component is focused on UI structure of the Search page and it must be
 * used as a child component of component `Search`.
 *
 * 1. Controlling how to display major components - SearchBar, SearchResultList, Refine search panel and Classification panel.
 * 2. Creating event handlers for all the child Search components.
 * 3. Supporting the display of custom components.
 * 4. Supporting UI requirements specific to the Selection Session.
 */
export const SearchPageBody = ({
  pathname,
  additionalPanels,
  searchBarConfig,
  headerConfig = defaultSearchPageHeaderConfig,
  refinePanelConfig = defaultSearchPageRefinePanelConfig,
  enableClassification = true,
  customSearchCallback,
}: SearchPageBodyProps) => {
  const {
    enableCSVExportButton,
    enableShareSearchButton,
    additionalHeaders,
    customSortingOptions,
  } = headerConfig;

  const {
    enableAdvancedSearchSelector,
    enableCollectionSelector,
    enableDateRangeSelector,
    enableDisplayModeSelector,
    enableItemStatusSelector,
    enableRemoteSearchSelector,
    enableMimeTypeSelector,
    enableOwnerSelector,
    enableSearchAttachmentsSelector,
  } = refinePanelConfig;

  const {
    search,
    searchState: state,
    searchSettings,
    searchPageErrorHandler,
  } = useContext(SearchContext);

  const { options: searchPageOptions } = state;
  const { advancedSearches } = searchSettings;

  const { currentUser } = useContext(AppContext);
  const history = useHistory();

  const [snackBar, setSnackBar] = useState<{
    message: string;
    variant?: MessageInfoVariant;
  }>({
    message: "",
  });
  const [alreadyDownloaded, setAlreadyDownloaded] = useState<boolean>(false);
  const [showRefinePanel, setShowRefinePanel] = useState<boolean>(false);
  const [showFavouriteSearchDialog, setShowFavouriteSearchDialog] =
    useState<boolean>(false);
  const [filterExpansion, setFilterExpansion] = useState(
    searchPageOptions?.filterExpansion ??
      defaultSearchPageOptions?.filterExpansion
  );
  const exportLinkRef = useRef<HTMLAnchorElement>(null);

  const doSearch = useCallback(
    (searchPageOptions: SearchPageOptions, scrollToTop = true) => {
      const callback = () => {
        // Save the value of wildcard mode to LocalStorage.
        writeRawModeToStorage(searchPageOptions.rawMode);
        // scroll back up to the top of the page
        if (scrollToTop) window.scrollTo(0, 0);
        // Allow downloading new search result.
        setAlreadyDownloaded(false);
        customSearchCallback?.();
      };

      search(searchPageOptions, enableClassification, callback);
    },
    [enableClassification, search, customSearchCallback]
  );

  /**
   * Depending on the whether in the context of a Selection Session, this function will
   * use the appropriate method to navigate to the provided `normalPath`.
   *
   * @param normalPath The path which the page will be navigated to.
   * @param selectionSessionPathBuilder Function to convert the supplied path to a Selection Session specific path.
   */
  const navigateTo = (
    normalPath: string,
    selectionSessionPathBuilder: () => string
  ) => {
    isSelectionSessionOpen()
      ? window.open(selectionSessionPathBuilder(), "_self")
      : history.push(normalPath);
  };

  const exitAdvancedSearch = () => {
    navigateTo(NEW_SEARCH_PATH, () =>
      buildSelectionSessionSearchPageLink(searchPageOptions.externalMimeTypes)
    );
  };

  const handleAdvancedSearchChanged = (
    advancedSearch: OEQ.Common.BaseEntitySummary | null
  ) =>
    pipe(
      O.fromNullable(advancedSearch),
      O.map(({ uuid }) =>
        navigateTo(routes.NewAdvancedSearch.to(uuid), () =>
          buildSelectionSessionAdvancedSearchLink(
            uuid,
            searchPageOptions.externalMimeTypes
          )
        )
      ),
      O.getOrElse(() => exitAdvancedSearch())
    );

  const handleClearSearchOptions = () => {
    doSearch({
      ...defaultSearchPageOptions,
      sortOrder: searchSettings.core?.defaultSearchSort,
      externalMimeTypes: isSelectionSessionOpen()
        ? searchPageOptions.externalMimeTypes
        : undefined,
      // As per requirements for persistence of rawMode, it is _not_ reset for New Searches
      rawMode: searchPageOptions.rawMode,
    });
    setFilterExpansion(false);

    exitAdvancedSearch();
  };

  const handleCollapsibleFilterClick = () => {
    setFilterExpansion(!filterExpansion);
  };

  const handleCollectionSelectionChanged = (collections: Collection[]) => {
    doSearch({
      ...searchPageOptions,
      collections: collections,
      currentPage: 0,
      selectedCategories: undefined,
    });
  };

  const handleCopySearch = () => {
    //base institution urls have a trailing / that we need to get rid of
    const instUrl = getBaseUrl().slice(0, -1);
    const searchUrl = `${instUrl}${pathname}?${generateQueryStringFromSearchPageOptions(
      searchPageOptions
    )}`;
    navigator.clipboard
      .writeText(searchUrl)
      .then(() => {
        setSnackBar({ message: searchStrings.shareSearchConfirmationText });
      })
      .catch(searchPageErrorHandler);
  };

  const handleDisplayModeChanged = (mode: DisplayMode) =>
    doSearch({ ...searchPageOptions, displayMode: mode });

  const handleExport = () => {
    if (searchPageOptions.collections?.length !== 1) {
      setSnackBar({
        message: searchStrings.export.collectionLimit,
        variant: "warning",
      });
      return;
    }

    confirmExport(searchPageOptions)
      .then(() => {
        // All checks pass so manually trigger a click on the export link.
        exportLinkRef.current?.click();
        // Do not allow exporting the same search result again until searchPageOptions gets changed.
        setAlreadyDownloaded(true);
      })
      .catch((error: OEQ.Errors.ApiError) =>
        setSnackBar({
          message: generateExportErrorMessage(error),
          variant: "warning",
        })
      );
  };

  const handleLastModifiedDateRangeChange = (dateRange?: DateRange) =>
    doSearch({
      ...searchPageOptions,
      lastModifiedDateRange: dateRange,
      selectedCategories: undefined,
    });

  const handleMimeTypeFilterChange = (
    filters: OEQ.SearchFilterSettings.MimeTypeFilter[]
  ) =>
    doSearch({
      ...searchPageOptions,
      mimeTypeFilters: filters,
      mimeTypes: filters.flatMap((f) => f.mimeTypes),
      currentPage: 0,
      selectedCategories: undefined,
    });

  const handleOwnerChange = (owner: OEQ.UserQuery.UserDetails) =>
    doSearch({
      ...searchPageOptions,
      owner: { ...owner },
      selectedCategories: undefined,
    });

  const handleOwnerClear = () =>
    doSearch({
      ...searchPageOptions,
      owner: undefined,
      selectedCategories: undefined,
    });

  const handlePageChanged = (page: number) =>
    doSearch({ ...searchPageOptions, currentPage: page });

  const handleQueryChanged = useMemo(
    () =>
      debounce(
        (query: string) =>
          doSearch({
            ...searchPageOptions,
            query: query,
            currentPage: 0,
            selectedCategories: undefined,
          }),
        500
      ),
    [doSearch, searchPageOptions]
  );

  const handleQuickDateRangeModeChange = (
    quickDateRangeMode: boolean,
    dateRange?: DateRange
  ) =>
    doSearch({
      ...searchPageOptions,
      dateRangeQuickModeEnabled: quickDateRangeMode,
      // When the mode is changed, the date range may also need to be updated.
      // For example, if a custom date range is converted to Quick option 'All', then both start and end should be undefined.
      lastModifiedDateRange: dateRange,
      selectedCategories: undefined,
    });

  const handleRowsPerPageChanged = (rowsPerPage: number) =>
    doSearch(
      {
        ...searchPageOptions,
        currentPage: 0,
        rowsPerPage: rowsPerPage,
      },
      false
    );

  const handleSaveFavouriteSearch = (name: string) => {
    // We only need pathname and query strings.
    const url = `${pathname}?${generateQueryStringFromSearchPageOptions(
      searchPageOptions
    )}`;

    return addFavouriteSearch(name, url).then(() =>
      setSnackBar({
        message: searchStrings.favouriteSearch.saveSearchConfirmationText,
      })
    );
  };

  const handleSearchAttachmentsChange = (searchAttachments: boolean) => {
    doSearch({
      ...searchPageOptions,
      searchAttachments: searchAttachments,
    });
  };

  const handleSelectedCategoriesChange = (
    selectedCategories: SelectedCategories[]
  ) => {
    const getSchemaNode = (id: number) => {
      const node =
        state.status === "success" &&
        state.classifications.find((c) => c.id === id)?.schemaNode;
      if (!node) {
        throw new Error(`Unable to find schema node for classification ${id}.`);
      }
      return node;
    };

    doSearch({
      ...searchPageOptions,
      selectedCategories: selectedCategories.map((c) => ({
        ...c,
        schemaNode: getSchemaNode(c.id),
      })),
    });
  };

  const handleStatusChange = (status: OEQ.Common.ItemStatus[]) =>
    doSearch({
      ...searchPageOptions,
      status: [...status],
      selectedCategories: undefined,
    });

  const handleSortOrderChanged = (order: OEQ.SearchSettings.SortOrder) =>
    doSearch({ ...searchPageOptions, sortOrder: order });

  const handleWildcardModeChanged = (wildcardMode: boolean) =>
    // `wildcardMode` is a presentation concept, in the lower levels its inverse is the value for `rawMode`.
    doSearch({ ...searchPageOptions, rawMode: !wildcardMode });

  /**
   * Determines if any collapsible filters have been modified from their defaults
   */
  const areCollapsibleFiltersSet = (): boolean => {
    const fields: SearchOptionsFields[] = [
      "lastModifiedDateRange",
      "owner",
      "status",
      "searchAttachments",
      "mimeTypeFilters",
    ];
    return !isEqual(
      getPartialSearchOptions(defaultSearchPageOptions, fields),
      getPartialSearchOptions(searchPageOptions, fields)
    );
  };

  /**
   * Determines if any search criteria has been set, including classifications, query and all filters.
   */
  const isCriteriaSet = (): boolean => {
    const fields: SearchOptionsFields[] = [
      "lastModifiedDateRange",
      "owner",
      "status",
      "searchAttachments",
      "collections",
      "mimeTypeFilters",
    ];

    const isQueryOrFiltersSet = !isEqual(
      getPartialSearchOptions(defaultSearchPageOptions, fields),
      getPartialSearchOptions(searchPageOptions, fields)
    );

    // Field 'selectedCategories' is a bit different. Once a classification is selected, the category will persist in searchPageOptions.
    // What we really care is if we have got any category that has any classification selected.
    const isClassificationSelected: boolean =
      searchPageOptions.selectedCategories?.some(
        ({ categories }: SelectedCategories) => categories.length > 0
      ) ?? false;

    return isQueryOrFiltersSet || isClassificationSelected;
  };

  const refinePanelControls: RefinePanelControl[] = [
    {
      idSuffix: "DisplayModeSelector",
      title: displayModeSelectorTitle,
      component: (
        <DisplayModeSelector
          onChange={handleDisplayModeChanged}
          value={searchPageOptions.displayMode ?? "list"}
          disableImageMode={
            searchSettings.core?.searchingDisableGallery ?? false
          }
          disableVideoMode={
            searchSettings.core?.searchingDisableVideos ?? false
          }
        />
      ),
      disabled: !enableDisplayModeSelector,
      alwaysVisible: true,
    },
    {
      idSuffix: "CollectionSelector",
      title: collectionSelectorTitle,
      component: (
        <CollectionSelector
          onSelectionChange={handleCollectionSelectionChanged}
          value={searchPageOptions.collections}
        />
      ),
      disabled: !enableCollectionSelector,
      alwaysVisible: true,
    },
    {
      idSuffix: "AdvancedSearchSelector",
      title: searchStrings.advancedSearchSelector.title,
      component: (
        <AdvancedSearchSelector
          advancedSearches={advancedSearches}
          onSelectionChange={handleAdvancedSearchChanged}
          value={advancedSearches.find(
            ({ uuid }) =>
              uuid === getAdvancedSearchIdFromLocation(history.location)
          )}
        />
      ),
      disabled: advancedSearches.length === 0 || !enableAdvancedSearchSelector,
      alwaysVisible: true,
    },
    {
      idSuffix: "RemoteSearchSelector",
      title: searchStrings.remoteSearchSelector.title,
      component: (
        <AuxiliarySearchSelector
          auxiliarySearchesSupplier={getRemoteSearchesFromServer}
          urlGeneratorForRouteLink={routes.RemoteSearch.to}
          urlGeneratorForMuiLink={buildSelectionSessionRemoteSearchLink}
        />
      ),
      disabled: !enableRemoteSearchSelector,
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
      disabled:
        !enableDateRangeSelector ||
        (searchSettings.core?.searchingDisableDateModifiedFilter ?? true),
    },
    {
      idSuffix: "MIMETypeSelector",
      title: searchStrings.mimeTypeFilterSelector.title,
      component: (
        <MimeTypeFilterSelector
          value={searchPageOptions.mimeTypeFilters}
          onChange={handleMimeTypeFilterChange}
          filters={searchSettings.mimeTypeFilters}
        />
      ),
      disabled:
        !enableMimeTypeSelector ||
        searchPageOptions.displayMode !== "list" ||
        searchSettings.mimeTypeFilters.length === 0 ||
        !!searchPageOptions.externalMimeTypes,
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
      disabled:
        !enableOwnerSelector ||
        (searchSettings.core?.searchingDisableOwnerFilter ?? true),
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
      disabled:
        !enableItemStatusSelector ||
        (!searchSettings.core?.searchingShowNonLiveCheckbox ?? true),
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
      disabled: !enableSearchAttachmentsSelector,
    },
  ];

  const renderSidePanel = () => {
    const getClassifications = (): Classification[] => {
      const orEmpty = (c?: Classification[]) => c ?? [];

      switch (state.status) {
        case "success":
          return orEmpty(state.classifications);
        case "searching":
          return orEmpty(state.previousClassifications);
      }

      return [];
    };

    return (
      <SidePanel
        refinePanelProps={{
          controls: refinePanelControls,
          onChangeExpansion: handleCollapsibleFilterClick,
          panelExpanded: filterExpansion ?? false,
          showFilterIcon: areCollapsibleFiltersSet(),
          onClose: () => setShowRefinePanel(false),
        }}
        classificationsPanelProps={
          enableClassification
            ? {
                classifications: getClassifications(),
                onSelectedCategoriesChange: handleSelectedCategoriesChange,
                selectedCategories: searchPageOptions.selectedCategories,
              }
            : undefined
        }
      />
    );
  };

  const searchResult = (): SearchPageSearchResult => {
    const defaultResult: SearchPageSearchResult = {
      from: "item-search",
      content: defaultPagedSearchResult,
    };
    const orDefault = (r?: SearchPageSearchResult) => r ?? defaultResult;

    switch (state.status) {
      case "success":
        return orDefault(state.result);
      case "searching":
        return orDefault(state.previousResult);
    }

    return defaultResult;
  };

  const renderSearchResults = (): React.ReactNode | null => {
    const {
      from,
      content: { results: searchResults },
    } = searchResult();

    if (searchResults.length < 1) {
      return null;
    }

    const isListItems = (
      items: unknown
    ): items is OEQ.Search.SearchResultItem[] => from === "item-search";

    const isGalleryItems = (
      items: unknown
    ): items is GallerySearchResultItem[] => from === "gallery-search";

    if (isListItems(searchResults)) {
      return mapSearchResultItems(searchResults, highlights);
    } else if (isGalleryItems(searchResults)) {
      return <GallerySearchResult items={searchResults} />;
    }

    throw new TypeError("Unexpected type for searchResults");
  };

  const {
    content: { available: totalCount, highlight: highlights },
  } = searchResult();

  return (
    <>
      <Grid container spacing={2}>
        <Grid item sm={12} md={8}>
          <Grid container spacing={2}>
            <Grid item xs={12}>
              <SearchBar
                query={searchPageOptions.query ?? ""}
                wildcardMode={!searchPageOptions.rawMode}
                onQueryChange={handleQueryChanged}
                onWildcardModeChange={handleWildcardModeChanged}
                doSearch={() => doSearch(searchPageOptions)}
                advancedSearchFilter={searchBarConfig?.advancedSearchFilter}
              />
            </Grid>
            {additionalPanels?.map((panel, index) => (
              <Grid item xs={12} key={`additionalPanel-${index}`}>
                {panel}
              </Grid>
            ))}
            <Grid item xs={12}>
              <SearchResultList
                showSpinner={
                  state.status === "initialising" ||
                  state.status === "searching"
                }
                paginationProps={{
                  count: totalCount,
                  currentPage: searchPageOptions.currentPage,
                  rowsPerPage: searchPageOptions.rowsPerPage,
                  onPageChange: handlePageChanged,
                  onRowsPerPageChange: handleRowsPerPageChanged,
                }}
                orderSelectProps={{
                  value: searchPageOptions.sortOrder,
                  onChange: handleSortOrderChanged,
                  customSortingOptions,
                }}
                refineSearchProps={{
                  showRefinePanel: () => setShowRefinePanel(true),
                  isCriteriaSet: isCriteriaSet(),
                }}
                onClearSearchOptions={handleClearSearchOptions}
                onCopySearchLink={handleCopySearch}
                onSaveSearch={() => setShowFavouriteSearchDialog(true)}
                exportProps={{
                  isExportPermitted:
                    (enableCSVExportButton &&
                      currentUser?.canDownloadSearchResult) ??
                    false,
                  linkRef: exportLinkRef,
                  exportLinkProps: {
                    url: buildExportUrl(searchPageOptions),
                    onExport: handleExport,
                    alreadyExported: alreadyDownloaded,
                  },
                }}
                useShareSearchButton={enableShareSearchButton}
                additionalHeaders={additionalHeaders}
              >
                {renderSearchResults()}
              </SearchResultList>
            </Grid>
          </Grid>
        </Grid>
        <Hidden smDown>
          <Grid item md={4}>
            {renderSidePanel()}
          </Grid>
        </Hidden>
      </Grid>
      <MessageInfo
        open={!!snackBar.message}
        onClose={() => setSnackBar({ message: "" })}
        title={snackBar.message}
        variant={snackBar.variant ?? "success"}
      />
      <Hidden mdUp>
        <Drawer
          open={showRefinePanel}
          anchor="right"
          onClose={() => setShowRefinePanel(false)}
          PaperProps={{ style: { width: "50%" } }}
        >
          {renderSidePanel()}
        </Drawer>
      </Hidden>

      {showFavouriteSearchDialog && (
        <FavouriteSearchDialog
          open={showFavouriteSearchDialog}
          closeDialog={() => setShowFavouriteSearchDialog(false)}
          onConfirm={handleSaveFavouriteSearch}
        />
      )}
    </>
  );
};
