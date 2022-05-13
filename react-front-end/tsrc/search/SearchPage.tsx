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
import { debounce, Drawer, Grid } from "@mui/material";
import * as OEQ from "@openequella/rest-api-client";
import * as A from "fp-ts/Array";
import { constant, pipe } from "fp-ts/function";
import * as M from "fp-ts/Map";
import * as O from "fp-ts/Option";
import { isEqual } from "lodash";
import * as React from "react";
import {
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useReducer,
  useRef,
  useState,
} from "react";
import { useHistory, useLocation, useParams } from "react-router";
import { getBaseUrl } from "../AppConfig";
import { DateRangeSelector } from "../components/DateRangeSelector";
import MessageInfo, { MessageInfoVariant } from "../components/MessageInfo";
import type { FieldValueMap } from "../components/wizard/WizardHelper";
import {
  ControlValue,
  isControlValueNonEmpty,
  isNonEmptyString,
} from "../components/wizard/WizardHelper";
import { AppRenderErrorContext } from "../mainui/App";
import {
  NEW_ADVANCED_SEARCH_PATH,
  NEW_SEARCH_PATH,
  routes,
} from "../mainui/routes";
import { templateDefaults, TemplateUpdateProps } from "../mainui/Template";
import {
  getAdvancedSearchByUuid,
  getAdvancedSearchesFromServer,
} from "../modules/AdvancedSearchModule";
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
  buildSelectionSessionSearchPageLink,
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
import { getMimeTypeFiltersFromServer } from "../modules/SearchFilterSettingsModule";
import {
  buildExportUrl,
  confirmExport,
  DisplayMode,
  searchItems,
  SearchOptions,
  SearchOptionsFields,
} from "../modules/SearchModule";
import { getSearchSettingsFromServer } from "../modules/SearchSettingsModule";
import { getCurrentUserDetails } from "../modules/UserModule";
import SearchBar from "../search/components/SearchBar";
import type { DateRange } from "../util/Date";
import { languageStrings } from "../util/langstrings";
import { OrdAsIs } from "../util/Ord";
import {
  generateAdvancedSearchCriteria,
  initialiseAdvancedSearch,
} from "./AdvancedSearchHelper";
import { AdvancedSearchPanel } from "./components/AdvancedSearchPanel";
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
import {
  mapSearchResultItems,
  SearchResultList,
} from "./components/SearchResultList";
import { SidePanel } from "./components/SidePanel";
import StatusSelector from "./components/StatusSelector";
import {
  defaultPagedSearchResult,
  defaultSearchPageOptions,
  generateQueryStringFromSearchPageOptions,
  generateSearchPageOptionsFromQueryString,
  getPartialSearchOptions,
  getRawModeFromStorage,
  SearchPageOptions,
  writeRawModeToStorage,
} from "./SearchPageHelper";
import { searchPageModeReducer } from "./SearchPageModeReducer";
import { reducer, SearchPageSearchResult } from "./SearchPageReducer";

// destructure strings import
const { searchpage: searchStrings } = languageStrings;
const { title: dateModifiedSelectorTitle, quickOptionDropdown } =
  searchStrings.lastModifiedDateSelector;
const { title: collectionSelectorTitle } = searchStrings.collectionSelector;
const { title: displayModeSelectorTitle } = searchStrings.displayModeSelector;

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

export const SearchPageRenderErrorContext = React.createContext<{
  /**
   * Function to handle errors thrown from Search page components.
   */
  handleError: (error: Error) => void;
}>({
  handleError: () => {},
});

interface AdvancedSearchParams {
  /**
   * ID of the currently selected Advanced Search. When it's defined, the component is in
   * Advanced Search mode, or normal Search page mode otherwise.
   */
  advancedSearchId?: string;
}

type SearchPageProps = TemplateUpdateProps & AdvancedSearchParams;

const SearchPage = ({ updateTemplate, advancedSearchId }: SearchPageProps) => {
  console.debug("START: <SearchPage>");

  const history = useHistory();
  const location = useLocation();

  // Retrieve any AdvancedSearchId from the Router
  const { advancedSearchId: advancedSearchIdParam } =
    useParams<AdvancedSearchParams>();

  // If an Advanced Search ID has been provided, use that otherwise check to see if one
  // was passed in by the Router.
  advancedSearchId = advancedSearchId ?? advancedSearchIdParam;

  const [state, dispatch] = useReducer(reducer, { status: "initialising" });
  const [searchPageModeState, searchPageModeDispatch] = useReducer(
    searchPageModeReducer,
    { mode: "normal" }
  );

  // Function to navigate Search page or Advanced search page to another page. If in Selection Session,
  // call the provided path builder to generate a Selection Session specific path.
  const navigateTo = (
    normalPath: string,
    selectionSessionPathBuilder: () => string
  ) => {
    isSelectionSessionOpen()
      ? window.open(selectionSessionPathBuilder(), "_self")
      : history.push(normalPath);
  };

  const exitAdvancedSearchMode = () => {
    searchPageModeDispatch({ type: "useNormal" });
    navigateTo(NEW_SEARCH_PATH, () =>
      buildSelectionSessionSearchPageLink(searchPageOptions.externalMimeTypes)
    );
  };

  const defaultSearchPageHistory: SearchPageHistoryState = {
    searchPageOptions: defaultSearchPageOptions,
    filterExpansion: false,
  };
  const searchPageHistoryState: SearchPageHistoryState | undefined = history
    .location.state as SearchPageHistoryState;
  const [searchPageOptions, setSearchPageOptions] = useState<SearchPageOptions>(
    // If the user has gone 'back' to this page, then use their previous options. Otherwise
    // we start fresh - i.e. if a new navigation to Search Page.
    searchPageHistoryState?.searchPageOptions ?? {
      ...defaultSearchPageHistory.searchPageOptions,
      rawMode: getRawModeFromStorage(),
    }
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
    mimeTypeFilters: OEQ.SearchFilterSettings.MimeTypeFilter[];
  }>({
    core: undefined,
    mimeTypeFilters: [],
  });

  const [advancedSearches, setAdvancedSearches] = useState<
    OEQ.Common.BaseEntitySummary[]
  >([]);

  const [currentUser, setCurrentUser] =
    React.useState<OEQ.LegacyContent.CurrentUserDetails>();

  const [showRefinePanel, setShowRefinePanel] = useState<boolean>(false);
  const [showFavouriteSearchDialog, setShowFavouriteSearchDialog] =
    useState<boolean>(false);
  const [alreadyDownloaded, setAlreadyDownloaded] = useState<boolean>(false);
  const exportLinkRef = useRef<HTMLAnchorElement>(null);

  const { appErrorHandler } = useContext(AppRenderErrorContext);
  const searchPageErrorHandler = useCallback(
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

  const pathname = pipe(
    advancedSearchId,
    O.fromNullable,
    O.map((id) => `${NEW_ADVANCED_SEARCH_PATH}/${id}`),
    O.getOrElse(constant(NEW_SEARCH_PATH))
  );

  /**
   * Error display -> similar to onError hook, however in the context of reducer need to do manually.
   */
  useEffect(() => {
    if (state.status === "failure") {
      appErrorHandler(state.cause);
    }
  }, [state, appErrorHandler]);

  /**
   * Page initialisation -> Update the page title, retrieve Search settings and trigger first
   * search.
   */
  useEffect(() => {
    if (state.status !== "initialising") {
      return;
    }

    const timerId = "sp-init";
    console.debug("SearchPage: useEffect - initialising");
    console.time(timerId);

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
      getAdvancedSearchesFromServer(),
      advancedSearchId
        ? getAdvancedSearchByUuid(advancedSearchId)
        : Promise.resolve(undefined),
    ])
      .then(
        ([
          searchSettings,
          mimeTypeFilters,
          queryStringSearchOptions,
          currentUserDetails,
          advancedSearches,
          advancedSearchDefinition,
        ]) => {
          setSearchSettings({
            core: searchSettings,
            mimeTypeFilters: mimeTypeFilters,
          });
          setAdvancedSearches(advancedSearches);
          setCurrentUser(currentUserDetails);

          const [initialAdvSearchFieldValueMap, initialAdvancedSearchCriteria] =
            pipe(
              advancedSearchDefinition,
              O.fromNullable,
              O.map((def) =>
                initialiseAdvancedSearch(
                  def,
                  searchPageModeDispatch,
                  searchPageOptions,
                  queryStringSearchOptions
                )
              ),
              O.getOrElseW(() => [])
            );

          // This is the SearchPageOptions for the first searching, not the one created in the first rendering.
          const initialSearchPageOptions = pipe(
            queryStringSearchOptions,
            O.fromNullable,
            O.map<SearchPageOptions, SearchPageOptions>((options) => ({
              ...options,
              dateRangeQuickModeEnabled: false,
              sortOrder: options.sortOrder ?? searchSettings.defaultSearchSort,
              advancedSearchCriteria: initialAdvancedSearchCriteria,
              advFieldValue: initialAdvSearchFieldValueMap,
              collections:
                advancedSearchDefinition?.collections ?? options.collections,
            })),
            O.getOrElse<SearchPageOptions>(() => ({
              ...searchPageOptions,
              collections:
                advancedSearchDefinition?.collections ??
                searchPageOptions.collections,
              sortOrder:
                searchPageOptions.sortOrder ?? searchSettings.defaultSearchSort,
              advancedSearchCriteria: initialAdvancedSearchCriteria,
            }))
          );

          search(initialSearchPageOptions);
        }
      )
      .catch((e) => {
        searchPageErrorHandler(e);
      });

    console.timeEnd(timerId);
  }, [
    dispatch,
    searchPageErrorHandler,
    location,
    search,
    searchPageOptions,
    state.status,
    updateTemplate,
    advancedSearchId,
    searchPageModeDispatch,
  ]);

  /**
   * Searching -> Executing the search (including for classifications) and returning the results.
   */
  useEffect(() => {
    if (state.status === "searching") {
      const timerId = "sp-searching";
      console.debug("SearchPage: useEffect - searching");
      console.time(timerId);

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
            return {
              from: "item-search",
              content: await searchItems({
                ...options,
                includeAttachments: false,
              }),
            };
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

      if (searchPageModeState.mode === "advSearch") {
        searchPageModeDispatch({
          type: "hideAdvSearchPanel",
        });
      }

      setSearchPageOptions(state.options);
      (async () => {
        try {
          const searchResult: SearchPageSearchResult = await doSearch(
            state.options
          );
          // Do not list classifications in Advanced search mode.
          const classifications: Classification[] = !advancedSearchId
            ? await getClassifications(state.options)
            : [];

          dispatch({
            type: "search-complete",
            result: { ...searchResult },
            classifications,
          });

          // Update history
          history.replace({
            ...history.location,
            state: { searchPageOptions: state.options, filterExpansion },
          });
          // Save the value of wildcard mode to LocalStorage.
          writeRawModeToStorage(state.options.rawMode);
          // scroll back up to the top of the page
          if (state.scrollToTop) window.scrollTo(0, 0);
          // Allow downloading new search result.
          setAlreadyDownloaded(false);
        } catch (error: unknown) {
          searchPageErrorHandler(
            error instanceof Error
              ? error
              : new Error(`Failed to perform a search: ${error}`)
          );
        }
        console.timeEnd(timerId);
      })();
    }
  }, [
    dispatch,
    filterExpansion,
    searchPageErrorHandler,
    history,
    state,
    advancedSearchId,
    searchPageModeState.mode,
  ]);

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

  const handleAdvancedSearchChanged = (
    selection: OEQ.Common.BaseEntitySummary | null
  ) => {
    if (selection) {
      const { uuid } = selection;
      navigateTo(routes.NewAdvancedSearch.to(uuid), () =>
        buildSelectionSessionAdvancedSearchLink(
          uuid,
          searchPageOptions.externalMimeTypes
        )
      );
    } else {
      exitAdvancedSearchMode();
    }
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
      // As per requirements for persistence of rawMode, it is _not_ reset for New Searches
      rawMode: searchPageOptions.rawMode,
    });
    setFilterExpansion(false);

    if (searchPageModeState.mode === "advSearch") {
      exitAdvancedSearchMode();
    }
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
          const { badRequest, unauthorised, notFound } =
            searchStrings.export.errorMessages;
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

  const handleMimeTypeFilterChange = (
    filters: OEQ.SearchFilterSettings.MimeTypeFilter[]
  ) =>
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

  const handleSubmitAdvancedSearch = async (
    advFieldValue: FieldValueMap,
    overrideHide = false
  ) => {
    searchPageModeDispatch({
      type: "setQueryValues",
      values: advFieldValue,
      overrideHide,
    });

    search({
      ...searchPageOptions,
      advancedSearchCriteria: generateAdvancedSearchCriteria(advFieldValue),
      advFieldValue,
      currentPage: 0,
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

  const isAdvSearchCriteriaSet = (queryValues: FieldValueMap): boolean => {
    const isAnyFieldSet = pipe(
      queryValues,
      // Some controls like Calendar may have an empty string as their default values which should be
      // filtered out.
      M.map(A.filter(isNonEmptyString)),
      M.values<ControlValue>(OrdAsIs),
      A.some(isControlValueNonEmpty)
    );
    const isValueMapNotEmpty = !M.isEmpty(queryValues);

    return isValueMapNotEmpty && isAnyFieldSet;
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
          onSelectionChange={handleCollectionSelectionChanged}
          value={searchPageOptions.collections}
        />
      ),
      disabled: searchPageModeState.mode === "advSearch",
      alwaysVisible: true,
    },
    {
      idSuffix: "AdvancedSearchSelector",
      title: searchStrings.advancedSearchSelector.title,
      component: (
        <AdvancedSearchSelector
          advancedSearches={advancedSearches}
          onSelectionChange={handleAdvancedSearchChanged}
          value={advancedSearches.find(({ uuid }) => uuid === advancedSearchId)}
        />
      ),
      disabled: advancedSearches.length === 0,
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
      disabled: searchPageModeState.mode === "advSearch",
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
        classificationsPanelProps={
          // When in advanced search mode, hide classifications panel
          searchPageModeState.mode !== "advSearch"
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
    <SearchPageRenderErrorContext.Provider
      value={{ handleError: searchPageErrorHandler }}
    >
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
                advancedSearchFilter={
                  // Only show if we're in advanced search mode
                  searchPageModeState.mode === "advSearch"
                    ? {
                        onClick: () =>
                          searchPageModeDispatch({
                            type: "toggleAdvSearchPanel",
                          }),
                        accent: isAdvSearchCriteriaSet(
                          searchPageModeState.queryValues
                        ),
                      }
                    : undefined
                }
              />
            </Grid>
            {searchPageModeState.mode === "advSearch" &&
              searchPageModeState.isAdvSearchPanelOpen && (
                <Grid item xs={12}>
                  <AdvancedSearchPanel
                    title={searchPageModeState.definition.name}
                    wizardControls={searchPageModeState.definition.controls}
                    values={searchPageModeState.queryValues}
                    onSubmit={handleSubmitAdvancedSearch}
                    onClear={() => handleSubmitAdvancedSearch(new Map(), true)}
                    onClose={() =>
                      searchPageModeDispatch({ type: "hideAdvSearchPanel" })
                    }
                  />
                </Grid>
              )}
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
        <Grid item md={4} sx={{ display: { xs: "none", md: "block" } }}>
          {renderSidePanel()}
        </Grid>
      </Grid>
      <MessageInfo
        open={!!snackBar.message}
        onClose={() => setSnackBar({ message: "" })}
        title={snackBar.message}
        variant={snackBar.variant ?? "success"}
      />
      <Drawer
        sx={{ display: { md: "none", xs: "block" } }}
        open={showRefinePanel}
        anchor="right"
        onClose={() => setShowRefinePanel(false)}
        PaperProps={{ style: { width: "50%" } }}
      >
        {renderSidePanel()}
      </Drawer>

      {showFavouriteSearchDialog && (
        <FavouriteSearchDialog
          open={showFavouriteSearchDialog}
          closeDialog={() => setShowFavouriteSearchDialog(false)}
          onConfirm={handleSaveFavouriteSearch}
        />
      )}
    </SearchPageRenderErrorContext.Provider>
  );
};

export default SearchPage;
