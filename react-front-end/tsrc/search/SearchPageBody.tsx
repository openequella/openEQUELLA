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
import { debounce, Drawer, Grid, useMediaQuery } from "@mui/material";
import type { Theme } from "@mui/material/styles";
import * as OEQ from "@openequella/rest-api-client";
import { constant, identity, pipe } from "fp-ts/function";
import * as A from "fp-ts/Array";
import * as O from "fp-ts/Option";
import * as T from "fp-ts/Task";
import * as TO from "fp-ts/TaskOption";
import { isEqual } from "lodash";
import * as React from "react";
import {
  ReactNode,
  useCallback,
  useContext,
  useMemo,
  useRef,
  useState,
} from "react";
import { useHistory } from "react-router";
import { getBaseUrl } from "../AppConfig";
import { DateRangeSelector } from "../components/DateRangeSelector";
import MessageInfo, { MessageInfoVariant } from "../components/MessageInfo";
import { AppContext } from "../mainui/App";
import { routes } from "../mainui/routes";
import { getAdvancedSearchIdFromLocation } from "../modules/AdvancedSearchModule";
import { Collection } from "../modules/CollectionsModule";
import { addFavouriteSearch, FavouriteURL } from "../modules/FavouriteModule";
import {
  buildSelectionSessionAdvancedSearchLink,
  buildSelectionSessionRemoteSearchLink,
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
import { validateGrouping } from "../util/TextUtils";
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
import StatusSelector, {
  StatusSelectorProps,
} from "./components/StatusSelector";
import {
  buildSearchPageNavigationConfig,
  defaultPagedSearchResult,
  defaultSearchPageHeaderConfig,
  defaultSearchPageOptions,
  defaultSearchPageRefinePanelConfig,
  generateExportErrorMessage,
  generateQueryStringFromSearchPageOptions,
  getPartialSearchOptions,
  isGalleryItems,
  isListItems,
  navigateTo,
  SearchContext,
  SearchPageHeaderConfig,
  SearchPageNavigationConfig,
  SearchPageOptions,
  SearchPageRefinePanelConfig,
  SearchPageSearchBarConfig,
  writeRawModeToStorage,
} from "./SearchPageHelper";
import { SearchPageSearchResult } from "./SearchPageReducer";

const { searchpage: searchStrings } = languageStrings;
const { title: dateModifiedSelectorTitle, quickOptionDropdown } =
  searchStrings.lastModifiedDateSelector;
const { title: collectionSelectorTitle } = searchStrings.collectionSelector;
const { title: displayModeSelectorTitle } = searchStrings.displayModeSelector;

interface SnackBarDetails {
  message: string;
  variant?: MessageInfoVariant;
}

export interface SearchPageBodyProps {
  /**
   * URL path representing where the component is used.
   */
  pathname: string;
  /**
   * Any panel that will be rendered in between SearchBar and SearchResultList.
   */
  additionalPanels?: React.JSX.Element[];
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
  /**
   * Function to render custom UI for a list of SearchResultItem or GallerySearchResultItem.
   */
  customRenderSearchResults?: (
    searchResult: SearchPageSearchResult,
  ) => ReactNode;
  /**
   * Function to customise the URL which is used when saving favourites.
   */
  customFavouriteUrl?: (url: FavouriteURL) => FavouriteURL;
  /**
   * Flag which takes precedence over state to control whether to show the spinner.
   */
  customShowSpinner?: boolean;
  /**
   * Function to get the list of remote searches from the server.
   */
  getRemoteSearchesProvider?: () => Promise<OEQ.Common.BaseEntitySummary[]>;
  /**
   * The title of the search result list.
   */
  searchResultTitle?: string;
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
  customRenderSearchResults,
  customFavouriteUrl = identity,
  customShowSpinner = false,
  getRemoteSearchesProvider = getRemoteSearchesFromServer,
  searchResultTitle,
}: SearchPageBodyProps) => {
  const {
    enableCSVExportButton,
    enableShareSearchButton,
    additionalHeaders,
    customSortingOptions,
    newSearchConfig,
  } = headerConfig;

  const {
    customRefinePanelControl = [],
    enableAdvancedSearchSelector,
    enableCollectionSelector,
    enableDateRangeSelector,
    enableDisplayModeSelector,
    enableItemStatusSelector,
    enableRemoteSearchSelector,
    enableMimeTypeSelector,
    enableOwnerSelector,
    enableSearchAttachmentsSelector,
    statusSelectorCustomConfig = { alwaysEnabled: false },
  } = refinePanelConfig;

  const isMdUp = useMediaQuery<Theme>((theme) => theme.breakpoints.up("md"));

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
  const searchBarRef = useRef<HTMLDivElement>(null);

  const [snackBar, setSnackBar] = useState<SnackBarDetails>({
    message: "",
  });
  const [alreadyDownloaded, setAlreadyDownloaded] = useState<boolean>(false);
  const [showRefinePanel, setShowRefinePanel] = useState<boolean>(false);
  // Set default value to `true` to ensure component is rendered if it's enabled and then the API request can be sent to get the real data.
  const [hasRemoteSearches, setHasRemoteSearches] = useState<boolean>(true);
  const [showFavouriteSearchDialog, setShowFavouriteSearchDialog] =
    useState<boolean>(false);
  const [filterExpansion, setFilterExpansion] = useState(
    searchPageOptions?.filterExpansion ??
      defaultSearchPageOptions?.filterExpansion,
  );
  const exportLinkRef = useRef<HTMLAnchorElement>(null);

  const doSearch = useCallback(
    (searchPageOptions: SearchPageOptions, scrollToSearchBar = true) => {
      const callback = () => {
        // Save the value of wildcard mode to LocalStorage.
        writeRawModeToStorage(searchPageOptions.rawMode);
        if (scrollToSearchBar) {
          // Scroll to the top of the search bar.
          // It calculates top distance of the search bar, subtract 64px for the header height,
          // and an extra 10px to avoid the header's shadow. Result is the scroll distance.
          const distance =
            (searchBarRef.current?.getBoundingClientRect().top ?? 0) - 74;
          window.scrollBy({ top: distance });
        }
        // Allow downloading new search result.
        setAlreadyDownloaded(false);
        customSearchCallback?.();
      };

      search(searchPageOptions, enableClassification, callback);
    },
    [search, enableClassification, customSearchCallback],
  );

  const navigationWithHistory = (config: SearchPageNavigationConfig) =>
    navigateTo(config, history);

  const handleAdvancedSearchChanged = (
    advancedSearch: OEQ.Common.BaseEntitySummary | null,
  ) =>
    pipe(
      O.fromNullable(advancedSearch),
      O.map(({ uuid }) => ({
        path: routes.NewAdvancedSearch.to(uuid),
        selectionSessionPathBuilder: () =>
          buildSelectionSessionAdvancedSearchLink(
            uuid,
            searchPageOptions.externalMimeTypes,
          ),
      })),
      // Go to the normal Search page if none is selected.
      O.getOrElse(() => buildSearchPageNavigationConfig(searchPageOptions)),
      navigationWithHistory,
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
      // Apply custom new search criteria.
      ...newSearchConfig?.criteria,
    });
    setFilterExpansion(false);

    newSearchConfig?.callback?.();
    pipe(
      newSearchConfig?.navigationTo,
      O.fromNullable,
      O.map(navigationWithHistory),
    );
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
      searchPageOptions,
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
        }),
      );
  };

  const handleLastModifiedDateRangeChange = (dateRange?: DateRange) =>
    doSearch({
      ...searchPageOptions,
      lastModifiedDateRange: dateRange,
      selectedCategories: undefined,
    });

  const handleMimeTypeFilterChange = (
    filters: OEQ.SearchFilterSettings.MimeTypeFilter[],
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
      debounce((query: string) => {
        if (validateGrouping(query)) {
          doSearch({
            ...searchPageOptions,
            query: query,
            currentPage: 0,
            selectedCategories: undefined,
          });
        }
      }, 500),
    [doSearch, searchPageOptions],
  );

  const handleQuickDateRangeModeChange = (
    quickDateRangeMode: boolean,
    dateRange?: DateRange,
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
      false,
    );

  const handleSaveFavouriteSearch = async (name: string): Promise<void> =>
    await pipe(
      {
        path: pathname,
        params: new URLSearchParams(
          generateQueryStringFromSearchPageOptions(searchPageOptions),
        ),
      },
      customFavouriteUrl,
      (url) => TO.tryCatch(() => addFavouriteSearch(name, url)),
      TO.match<SnackBarDetails, OEQ.Favourite.FavouriteSearchModel>(
        constant({
          message: searchStrings.favouriteSearch.saveSearchFailedText,
          variant: "error",
        }),
        constant({
          message: searchStrings.favouriteSearch.saveSearchConfirmationText,
        }),
      ),
      T.map(setSnackBar),
    )();

  const handleSearchAttachmentsChange = (searchAttachments: boolean) => {
    doSearch({
      ...searchPageOptions,
      searchAttachments: searchAttachments,
    });
  };

  const handleSelectedCategoriesChange = (
    selectedCategories: SelectedCategories[],
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
      currentPage: 0,
    });
  };

  const handleStatusChange = (status: OEQ.Common.ItemStatus[]) =>
    doSearch({
      ...searchPageOptions,
      status: [...status],
      selectedCategories: undefined,
    });

  const handleSortOrderChanged = (order: OEQ.Search.SortOrder) =>
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
      getPartialSearchOptions(searchPageOptions, fields),
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
      getPartialSearchOptions(searchPageOptions, fields),
    );

    // Field 'selectedCategories' is a bit different. Once a classification is selected, the category will persist in searchPageOptions.
    // What we really care is if we have got any category that has any classification selected.
    const isClassificationSelected: boolean =
      searchPageOptions.selectedCategories?.some(
        ({ categories }: SelectedCategories) => categories.length > 0,
      ) ?? false;

    return isQueryOrFiltersSet || isClassificationSelected;
  };

  const buildStatusSelector = () =>
    pipe(
      statusSelectorCustomConfig.selectorProps,
      O.fromNullable,
      O.getOrElse<StatusSelectorProps>(() => ({
        value: searchPageOptions.status,
        onChange: handleStatusChange,
      })),
      (props) => <StatusSelector {...props} />,
    );

  const refinePanelControls: RefinePanelControl[] =
    customRefinePanelControl.concat([
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
                uuid === getAdvancedSearchIdFromLocation(history.location),
            )}
          />
        ),
        disabled:
          advancedSearches.length === 0 || !enableAdvancedSearchSelector,
        alwaysVisible: true,
      },
      {
        idSuffix: "RemoteSearchSelector",
        title: searchStrings.remoteSearchSelector.title,
        component: (
          <AuxiliarySearchSelector
            label={searchStrings.remoteSearchSelector.label}
            auxiliarySearchesSupplier={async () => {
              const result = await getRemoteSearchesProvider();
              setHasRemoteSearches(A.isNonEmpty(result));
              return result;
            }}
            urlGeneratorForRouteLink={routes.RemoteSearch.to}
            urlGeneratorForMuiLink={buildSelectionSessionRemoteSearchLink}
          />
        ),
        disabled: enableRemoteSearchSelector ? !hasRemoteSearches : true,
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
        component: buildStatusSelector(),
        disabled:
          !statusSelectorCustomConfig?.alwaysEnabled &&
          (!enableItemStatusSelector ||
            !searchSettings.core?.searchingShowNonLiveCheckbox),
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
    ]);

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

  const defaultRenderSearchResults = (
    searchPageSearchResult: SearchPageSearchResult,
  ): ReactNode => {
    const {
      from,
      content: { results: searchResults },
    } = searchPageSearchResult;

    if (searchResults.length < 1) {
      return null;
    }

    if (isListItems(from, searchResults)) {
      return mapSearchResultItems(searchResults, highlights);
    } else if (isGalleryItems(from, searchResults)) {
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
        <Grid
          size={{
            sm: 12,
            md: 8,
          }}
        >
          <Grid container spacing={2}>
            <Grid size={12}>
              <SearchBar
                ref={searchBarRef}
                query={searchPageOptions.query ?? ""}
                wildcardMode={!searchPageOptions.rawMode}
                onQueryChange={handleQueryChanged}
                onWildcardModeChange={handleWildcardModeChanged}
                doSearch={() => doSearch(searchPageOptions)}
                advancedSearchFilter={searchBarConfig?.advancedSearchFilter}
              />
            </Grid>
            {additionalPanels?.map((panel, index) => (
              <Grid size={12} key={`additionalPanel-${index}`}>
                {panel}
              </Grid>
            ))}
            <Grid size={12}>
              <SearchResultList
                title={searchResultTitle}
                showSpinner={
                  customShowSpinner ||
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
                {pipe(
                  searchResult(),
                  customRenderSearchResults ?? defaultRenderSearchResults,
                )}
              </SearchResultList>
            </Grid>
          </Grid>
        </Grid>
        {isMdUp && <Grid size={{ md: 4 }}>{renderSidePanel()}</Grid>}
      </Grid>
      <MessageInfo
        open={!!snackBar.message}
        onClose={() => setSnackBar({ message: "" })}
        title={snackBar.message}
        variant={snackBar.variant ?? "success"}
      />
      {!isMdUp && (
        <Drawer
          sx={{ display: { md: "none", xs: "block" } }}
          open={showRefinePanel}
          anchor="right"
          onClose={() => setShowRefinePanel(false)}
          PaperProps={{ style: { width: "50%" } }}
        >
          {renderSidePanel()}
        </Drawer>
      )}

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
