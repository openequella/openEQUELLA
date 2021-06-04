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
import type { DateRange } from "../util/Date";
import {
  defaultPagedSearchResult,
  defaultSearchPageOptions,
  generateQueryStringFromSearchPageOptions,
  generateSearchPageOptionsFromQueryString,
  getPartialSearchOptions,
} from "./SearchPageHelper";
import {
  buildExportUrl,
  confirmExport,
  DisplayMode,
  searchItems,
  SearchOptions,
  SearchOptionsFields,
} from "../modules/SearchModule";
import { pipe } from "fp-ts/function";
import { isEqual } from "lodash";
import * as React from "react";
import {
  useCallback,
  useEffect,
  useMemo,
  useRef,
  useReducer,
  useState,
} from "react";
import { useHistory, useLocation } from "react-router";
import { generateFromError } from "../api/errors";
import { AppConfig } from "../AppConfig";
import { DateRangeSelector } from "../components/DateRangeSelector";
import MessageInfo, { MessageInfoVariant } from "../components/MessageInfo";
import { routes } from "../mainui/routes";
import {
  templateDefaults,
  templateError,
  TemplateUpdateProps,
} from "../mainui/Template";
import { getAdvancedSearchesFromServer } from "../modules/AdvancedSearchModule";
import type { Collection } from "../modules/CollectionsModule";
import { addFavouriteSearch } from "../modules/FavouriteModule";
import {
  GallerySearchResultItem,
  imageGallerySearch,
  listImageGalleryClassifications,
  listVideoGalleryClassifications,
  videoGallerySearch,
} from "../modules/GallerySearchModule";
import {
  buildSelectionSessionAdvancedSearchLink,
  buildSelectionSessionRemoteSearchLink,
  isSelectionSessionInStructured,
  isSelectionSessionOpen,
  prepareDraggable,
} from "../modules/LegacySelectionSessionModule";
import { getRemoteSearchesFromServer } from "../modules/RemoteSearchModule";
import {
  Classification,
  listClassifications,
  SelectedCategories,
} from "../modules/SearchFacetsModule";
import {
  getMimeTypeFiltersFromServer,
  MimeTypeFilter,
} from "../modules/SearchFilterSettingsModule";
import { getSearchSettingsFromServer } from "../modules/SearchSettingsModule";
import { getCurrentUserDetails } from "../modules/UserModule";
import SearchBar from "../search/components/SearchBar";
import { languageStrings } from "../util/langstrings";
import { AuxiliarySearchSelector } from "./components/AuxiliarySearchSelector";
import { CollectionSelector } from "./components/CollectionSelector";
import DisplayModeSelector from "./components/DisplayModeSelector";
import { FavouriteSearchDialog } from "./components/FavouriteSearchDialog";
import GallerySearchResult from "./components/GallerySearchResult";
import { MimeTypeFilterSelector } from "./components/MimeTypeFilterSelector";
import OwnerSelector from "./components/OwnerSelector";
import { RefinePanelControl } from "./components/RefineSearchPanel";
import { SearchAttachmentsSelector } from "./components/SearchAttachmentsSelector";
import {
  mapSearchResultItems,
  SearchResultList,
} from "./components/SearchResultList";
import { SidePanel } from "./components/SidePanel";
import StatusSelector from "./components/StatusSelector";

// destructure strings import
const { searchpage: searchStrings } = languageStrings;
const {
  title: dateModifiedSelectorTitle,
  quickOptionDropdown,
} = searchStrings.lastModifiedDateSelector;
const { title: collectionSelectorTitle } = searchStrings.collectionSelector;
const { title: displayModeSelectorTitle } = searchStrings.displayModeSelector;

/**
 * Type of search options that are specific to Search page presentation layer.
 */
export interface SearchPageOptions extends SearchOptions {
  /**
   * Whether to enable Quick mode (true) or to use custom date pickers (false).
   */
  dateRangeQuickModeEnabled: boolean;
  /**
   * How to display the search results - also determines the type of results.
   */
  displayMode: DisplayMode;
}

/**
 * Structure of data stored in browser history state, to capture the current state of SearchPage
 */
interface SearchPageHistoryState {
  /**
   * SearchPageOptions to store in history
   */
  searchPageOptions: SearchPageOptions;
  /**
   * Open/closed state of refine expansion panel
   */
  filterExpansion: boolean;
}

/**
 * The types of SearchResultItem that we support within an `OEQ.Search.SearchResult`.
 */
type SearchPageSearchResult =
  | {
      from: "item-search";
      content: OEQ.Search.SearchResult<OEQ.Search.SearchResultItem>;
    }
  | {
      from: "gallery-search";
      content: OEQ.Search.SearchResult<GallerySearchResultItem>;
    };

type Action =
  | { type: "init" }
  | { type: "search"; options: SearchPageOptions; scrollToTop: boolean }
  | {
      type: "search-complete";
      result: SearchPageSearchResult;
      classifications: Classification[];
    }
  | { type: "error"; cause: Error };

type State =
  | { status: "initialising" }
  | {
      status: "searching";
      options: SearchPageOptions;
      previousResult?: SearchPageSearchResult;
      previousClassifications?: Classification[];
      scrollToTop: boolean;
    }
  | {
      status: "success";
      result: SearchPageSearchResult;
      classifications: Classification[];
    }
  | { status: "failure"; cause: Error };

const reducer = (state: State, action: Action): State => {
  switch (action.type) {
    case "init":
      return { status: "initialising" };
    case "search":
      const prevResults =
        state.status === "success"
          ? {
              previousResult: state.result,
              previousClassifications: state.classifications,
            }
          : {};
      return {
        status: "searching",
        options: action.options,
        scrollToTop: action.scrollToTop,
        ...prevResults,
      };
    case "search-complete":
      return {
        status: "success",
        result: action.result,
        classifications: action.classifications,
      };
    case "error":
      return { status: "failure", cause: action.cause };
    default:
      throw new TypeError("Unexpected action passed to reducer!");
  }
};

const SearchPage = ({ updateTemplate }: TemplateUpdateProps) => {
  const history = useHistory();
  const location = useLocation();

  const [state, dispatch] = useReducer(reducer, { status: "initialising" });
  const defaultSearchPageHistory: SearchPageHistoryState = {
    searchPageOptions: defaultSearchPageOptions,
    filterExpansion: false,
  };
  const searchPageHistoryState: SearchPageHistoryState | undefined = history
    .location.state as SearchPageHistoryState;
  const [searchPageOptions, setSearchPageOptions] = useState<SearchPageOptions>(
    // If the user has gone 'back' to this page, then use their previous options. Otherwise
    // we start fresh - i.e. if a new navigation to Search Page.
    searchPageHistoryState?.searchPageOptions ??
      defaultSearchPageHistory.searchPageOptions
  );
  const [filterExpansion, setFilterExpansion] = useState(
    searchPageHistoryState?.filterExpansion ??
      defaultSearchPageHistory.filterExpansion
  );
  const [snackBar, setSnackBar] = useState<{
    message: string;
    variant?: MessageInfoVariant;
  }>({
    message: "",
  });

  const [searchSettings, setSearchSettings] = useState<{
    core: OEQ.SearchSettings.Settings | undefined;
    mimeTypeFilters: MimeTypeFilter[];
  }>({
    core: undefined,
    mimeTypeFilters: [],
  });

  const [
    currentUser,
    setCurrentUser,
  ] = React.useState<OEQ.LegacyContent.CurrentUserDetails>();

  const [showRefinePanel, setShowRefinePanel] = useState<boolean>(false);
  const [
    showFavouriteSearchDialog,
    setShowFavouriteSearchDialog,
  ] = useState<boolean>(false);
  const [alreadyDownloaded, setAlreadyDownloaded] = useState<boolean>(false);
  const exportLinkRef = useRef<HTMLAnchorElement>(null);

  const handleError = useCallback(
    (error: Error) => {
      dispatch({ type: "error", cause: error });
    },
    [dispatch]
  );

  const search = useCallback(
    (searchPageOptions: SearchPageOptions, scrollToTop = true): void =>
      dispatch({
        type: "search",
        options: { ...searchPageOptions },
        scrollToTop,
      }),
    [dispatch]
  );

  /**
   * Error display -> similar to onError hook, however in the context of reducer need to do manually.
   */
  useEffect(() => {
    if (state.status === "failure") {
      updateTemplate(templateError(generateFromError(state.cause)));
    }
  }, [state, updateTemplate]);

  /**
   * Page initialisation -> Update the page title, retrieve Search settings and trigger first
   * search.
   */
  useEffect(() => {
    if (state.status !== "initialising") {
      return;
    }

    updateTemplate((tp) => ({
      ...templateDefaults(searchStrings.title)(tp),
    }));

    Promise.all([
      getSearchSettingsFromServer(),
      getMimeTypeFiltersFromServer(),
      // If the search options are available from browser history, ignore those in the query string.
      (location.state as SearchPageHistoryState)
        ? Promise.resolve(undefined)
        : generateSearchPageOptionsFromQueryString(location),
      getCurrentUserDetails(),
    ])
      .then(
        ([
          searchSettings,
          mimeTypeFilters,
          queryStringSearchOptions,
          currentUserDetails,
        ]) => {
          setSearchSettings({
            core: searchSettings,
            mimeTypeFilters: mimeTypeFilters,
          });
          setCurrentUser(currentUserDetails);
          search(
            queryStringSearchOptions
              ? {
                  ...queryStringSearchOptions,
                  dateRangeQuickModeEnabled: false,
                  sortOrder:
                    queryStringSearchOptions.sortOrder ??
                    searchSettings.defaultSearchSort,
                }
              : {
                  ...searchPageOptions,
                  sortOrder:
                    searchPageOptions.sortOrder ??
                    searchSettings.defaultSearchSort,
                }
          );
        }
      )
      .catch((e) => {
        handleError(e);
      });
  }, [
    dispatch,
    handleError,
    location,
    search,
    searchPageOptions,
    state.status,
    updateTemplate,
  ]);

  /**
   * Searching -> Executing the search (including for classifications) and returning the results.
   */
  useEffect(() => {
    if (state.status === "searching") {
      const gallerySearch = async (
        search: typeof imageGallerySearch | typeof videoGallerySearch,
        options: SearchPageOptions
      ): Promise<SearchPageSearchResult> => ({
        from: "gallery-search",
        content: await search({
          ...options,
          // `mimeTypeFilters` should be ignored in gallery modes
          mimeTypeFilters: undefined,
        }),
      });

      const doSearch = async (
        options: SearchPageOptions
      ): Promise<SearchPageSearchResult> => {
        switch (options.displayMode) {
          case "gallery-image":
            return gallerySearch(imageGallerySearch, options);
          case "gallery-video":
            return gallerySearch(videoGallerySearch, options);
          case "list":
            return { from: "item-search", content: await searchItems(options) };
          default:
            throw new TypeError("Unexpected `displayMode` for searching");
        }
      };

      // Depending on what display mode we're in, determine which function we use to list
      // the classifications to match the search.
      const getClassifications: (
        _: SearchOptions
      ) => Promise<Classification[]> = pipe(
        state.options.displayMode,
        (mode) => {
          switch (mode) {
            case "gallery-image":
              return listImageGalleryClassifications;
            case "gallery-video":
              return listVideoGalleryClassifications;
            case "list":
              return listClassifications;
            default:
              throw new TypeError(
                "Unexpected `displayMode` for determining classifications listing function"
              );
          }
        }
      );

      setSearchPageOptions(state.options);
      Promise.all([doSearch(state.options), getClassifications(state.options)])
        .then(
          ([result, classifications]: [
            SearchPageSearchResult,
            Classification[]
          ]) => {
            dispatch({
              type: "search-complete",
              result: { ...result },
              classifications: [...classifications],
            });
            // Update history
            history.replace({
              ...history.location,
              state: { searchPageOptions: state.options, filterExpansion },
            });
            // scroll back up to the top of the page
            if (state.scrollToTop) window.scrollTo(0, 0);
            // Allow downloading new search result.
            setAlreadyDownloaded(false);
          }
        )
        .catch(handleError);
    }
  }, [dispatch, filterExpansion, handleError, history, state]);

  // In Selection Session, once a new search result is returned, make each
  // new search result Item draggable. Could probably merge into 'searching'
  // effect, however this is only required while selection sessions still
  // involve legacy content.
  useEffect(() => {
    if (
      state.status === "success" &&
      state.result.from === "item-search" &&
      isSelectionSessionInStructured()
    ) {
      state.result.content.results.forEach(
        ({ uuid }: OEQ.Search.SearchResultItem) => {
          prepareDraggable(uuid);
        }
      );
    }
  }, [state]);

  const handleSortOrderChanged = (order: OEQ.SearchSettings.SortOrder) =>
    search({ ...searchPageOptions, sortOrder: order });

  const handleQueryChanged = useMemo(
    () =>
      debounce(
        (query: string) =>
          search({
            ...searchPageOptions,
            query: query,
            currentPage: 0,
            selectedCategories: undefined,
          }),
        500
      ),
    [searchPageOptions, search]
  );

  const handleDisplayModeChanged = (mode: DisplayMode) =>
    search({ ...searchPageOptions, displayMode: mode });

  const handleCollectionSelectionChanged = (collections: Collection[]) => {
    search({
      ...searchPageOptions,
      collections: collections,
      currentPage: 0,
      selectedCategories: undefined,
    });
  };

  const handleCollapsibleFilterClick = () => {
    setFilterExpansion(!filterExpansion);
  };

  const handlePageChanged = (page: number) =>
    search({ ...searchPageOptions, currentPage: page });

  const handleRowsPerPageChanged = (rowsPerPage: number) =>
    search(
      {
        ...searchPageOptions,
        currentPage: 0,
        rowsPerPage: rowsPerPage,
      },
      false
    );

  const handleWildcardModeChanged = (wildcardMode: boolean) =>
    // `wildcardMode` is a presentation concept, in the lower levels its inverse is the value for `rawMode`.
    search({ ...searchPageOptions, rawMode: !wildcardMode });

  const handleQuickDateRangeModeChange = (
    quickDateRangeMode: boolean,
    dateRange?: DateRange
  ) =>
    search({
      ...searchPageOptions,
      dateRangeQuickModeEnabled: quickDateRangeMode,
      // When the mode is changed, the date range may also need to be updated.
      // For example, if a custom date range is converted to Quick option 'All', then both start and end should be undefined.
      lastModifiedDateRange: dateRange,
      selectedCategories: undefined,
    });

  const handleLastModifiedDateRangeChange = (dateRange?: DateRange) =>
    search({
      ...searchPageOptions,
      lastModifiedDateRange: dateRange,
      selectedCategories: undefined,
    });

  const handleClearSearchOptions = () => {
    search({
      ...defaultSearchPageOptions,
      sortOrder: searchSettings.core?.defaultSearchSort,
      externalMimeTypes: isSelectionSessionOpen()
        ? searchPageOptions.externalMimeTypes
        : undefined,
    });
    setFilterExpansion(false);
  };

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
      .catch((error: OEQ.Errors.ApiError) => {
        const generateExportErrorMessage = (
          error: OEQ.Errors.ApiError
        ): string => {
          const {
            badRequest,
            unauthorised,
            notFound,
          } = searchStrings.export.errorMessages;
          switch (error.status) {
            case 400:
              return badRequest;
            case 403:
              return unauthorised;
            case 404:
              return notFound;
            default:
              return error.message;
          }
        };
        setSnackBar({
          message: generateExportErrorMessage(error),
          variant: "warning",
        });
      });
  };

  const handleCopySearch = () => {
    //base institution urls have a trailing / that we need to get rid of
    const instUrl = AppConfig.baseUrl.slice(0, -1);
    const searchUrl = `${instUrl}${
      location.pathname
    }?${generateQueryStringFromSearchPageOptions(searchPageOptions)}`;

    navigator.clipboard
      .writeText(searchUrl)
      .then(() => {
        setSnackBar({ message: searchStrings.shareSearchConfirmationText });
      })
      .catch(() => handleError);
  };

  const handleSaveFavouriteSearch = (name: string) => {
    // We only need pathname and query strings.
    const url = `${
      location.pathname
    }?${generateQueryStringFromSearchPageOptions(searchPageOptions)}`;

    return addFavouriteSearch(name, url)
      .then(() =>
        setSnackBar({
          message: searchStrings.favouriteSearch.saveSearchConfirmationText,
        })
      )
      .catch(handleError);
  };

  const handleMimeTypeFilterChange = (filters: MimeTypeFilter[]) =>
    search({
      ...searchPageOptions,
      mimeTypeFilters: filters,
      mimeTypes: filters.flatMap((f) => f.mimeTypes),
      currentPage: 0,
      selectedCategories: undefined,
    });

  const handleOwnerChange = (owner: OEQ.UserQuery.UserDetails) =>
    search({
      ...searchPageOptions,
      owner: { ...owner },
      selectedCategories: undefined,
    });

  const handleOwnerClear = () =>
    search({
      ...searchPageOptions,
      owner: undefined,
      selectedCategories: undefined,
    });

  const handleStatusChange = (status: OEQ.Common.ItemStatus[]) =>
    search({
      ...searchPageOptions,
      status: [...status],
      selectedCategories: undefined,
    });

  const handleSearchAttachmentsChange = (searchAttachments: boolean) => {
    search({
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

    search({
      ...searchPageOptions,
      selectedCategories: selectedCategories.map((c) => ({
        ...c,
        schemaNode: getSchemaNode(c.id),
      })),
    });
  };

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
      disabled: false,
      alwaysVisible: true,
    },
    {
      idSuffix: "CollectionSelector",
      title: collectionSelectorTitle,
      component: (
        <CollectionSelector
          onError={handleError}
          onSelectionChange={handleCollectionSelectionChanged}
          value={searchPageOptions.collections}
        />
      ),
      disabled: false,
      alwaysVisible: true,
    },
    {
      idSuffix: "AdvancedSearchSelector",
      title: searchStrings.advancedSearchSelector.title,
      component: (
        <AuxiliarySearchSelector
          auxiliarySearchesSupplier={getAdvancedSearchesFromServer}
          urlGeneratorForRouteLink={routes.AdvancedSearch.to}
          urlGeneratorForMuiLink={buildSelectionSessionAdvancedSearchLink}
        />
      ),
      disabled: false,
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
      disabled: searchSettings.core?.searchingDisableDateModifiedFilter ?? true,
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
      disabled: searchSettings.core?.searchingDisableOwnerFilter ?? true,
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
      disabled: !searchSettings.core?.searchingShowNonLiveCheckbox ?? true,
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
          panelExpanded: filterExpansion,
          showFilterIcon: areCollapsibleFiltersSet(),
          onClose: () => setShowRefinePanel(false),
        }}
        classificationsPanelProps={{
          classifications: getClassifications(),
          onSelectedCategoriesChange: handleSelectedCategoriesChange,
          selectedCategories: searchPageOptions.selectedCategories,
        }}
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
      return mapSearchResultItems(searchResults, handleError, highlights);
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
                doSearch={() => search(searchPageOptions)}
              />
            </Grid>
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
                    currentUser?.canDownloadSearchResult ?? false,
                  linkRef: exportLinkRef,
                  exportLinkProps: {
                    url: buildExportUrl(searchPageOptions),
                    onExport: handleExport,
                    alreadyExported: alreadyDownloaded,
                  },
                }}
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

export default SearchPage;
