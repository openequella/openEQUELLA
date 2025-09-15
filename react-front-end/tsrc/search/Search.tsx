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
import * as OEQ from "@openequella/rest-api-client";
import { identity, pipe } from "fp-ts/function";
import * as O from "fp-ts/Option";
import * as React from "react";
import {
  ReactNode,
  useCallback,
  useContext,
  useEffect,
  useReducer,
  useState,
} from "react";
import { useHistory } from "react-router";
import { AppContext } from "../mainui/App";
import { templateDefaults, TemplateUpdateProps } from "../mainui/Template";
import { getAdvancedSearchesFromServer } from "../modules/AdvancedSearchModule";
import {
  imageGallerySearch,
  listImageGalleryClassifications,
  listVideoGalleryClassifications,
  videoGallerySearch,
} from "../modules/GallerySearchModule";
import {
  isSelectionSessionInStructured,
  prepareDraggable,
} from "../modules/LegacySelectionSessionModule";
import {
  Classification,
  listClassifications,
} from "../modules/SearchFacetsModule";
import { getMimeTypeFiltersFromServer } from "../modules/SearchFilterSettingsModule";
import {
  isLiveItem,
  searchItems,
  SearchOptions,
} from "../modules/SearchModule";
import { getSearchSettingsFromServer } from "../modules/SearchSettingsModule";
import { languageStrings } from "../util/langstrings";
import {
  defaultGeneralSearchSettings,
  defaultSearchPageOptions,
  GeneralSearchSettings,
  generateSearchPageOptionsFromLocation,
  getRawModeFromStorage,
  SearchContext,
  SearchPageOptions,
} from "./SearchPageHelper";
import { reducer, SearchPageSearchResult } from "./SearchPageReducer";

/**
 * Structure of data stored in browser history state, to capture the current state of SearchPage as well as
 * custom data required by certain Search components.
 */
export interface SearchPageHistoryState<T = unknown> {
  /**
   * SearchPageOptions to store in history
   */
  searchPageOptions: SearchPageOptions;
  /**
   * Custom data saved in the browser history.
   */
  customData?: {
    [key: string]: T;
  };
}

const { searchpage: searchStrings } = languageStrings;
const nop = () => {};

/**
 * Type definition for configuration of the initial search.
 */
export interface InitialSearchConfig {
  /**
   * Perform the initial search when the value is `true`.
   */
  ready: boolean;
  /**
   * `true` to list the initial classifications.
   */
  listInitialClassifications: boolean;
  /**
   * This function will be called before the initial search, allowing for any additional customisation
   * to the initial search options.
   *
   * @param searchPageOptions General SearchPageOptions for the initial search.
   * @param queryStringSearchOptions The SearchPageOptions transformed from query strings.
   */
  customiseInitialSearchOptions: (
    searchPageOptions: SearchPageOptions,
    queryStringSearchOptions?: SearchPageOptions,
  ) => SearchPageOptions;
}

const defaultInitialSearchConfig: InitialSearchConfig = {
  ready: true,
  listInitialClassifications: true,
  customiseInitialSearchOptions: identity,
};

interface SearchProps extends TemplateUpdateProps {
  /**
   * Child components which are dependent on SearchContext.
   */
  children: ReactNode;
  /**
   * Configuration for the initial search.
   */
  initialSearchConfig?: InitialSearchConfig;
  /**
   * Title of the page where this component is used.
   */
  pageTitle?: string;
  /**
   * A function that will be called to get a list of results with its search type.
   * This is used when the `displayMode` is `list`.
   * The default search type is "item-search" and the search function is `searchItems` from `SearchModule`.
   *
   * @param searchOptions The options for the search.
   */
  listModeSearchProvider?: (
    searchOptions: SearchOptions,
  ) => Promise<SearchPageSearchResult>;

  /**
   * Optional provider used only for gallery mode search.
   * This is used when the `displayMode` is `gallery-search`
   * Defaults to `searchItems`.
   *
   * @param searchOptions The options for the search.
   */
  galleryModeSearchProvider?: (
    searchOptions: SearchOptions,
  ) => Promise<OEQ.Search.SearchResult<OEQ.Search.SearchResultItem>>;
}

const defaultListModeSearch = async (
  options: SearchOptions,
): Promise<SearchPageSearchResult> => ({
  from: "item-search",
  content: await searchItems({
    ...options,
    includeAttachments: false,
  }),
});

/**
 * This component is responsible for managing the state of search and performing general initialisation tasks.
 * It will use Context to allow child components to access the state, the current user, the error handler
 * and the search settings.
 */
export const Search = ({
  updateTemplate,
  children,
  initialSearchConfig = defaultInitialSearchConfig,
  pageTitle = searchStrings.title,
  listModeSearchProvider = defaultListModeSearch,
  galleryModeSearchProvider = searchItems,
}: SearchProps) => {
  const history = useHistory<SearchPageHistoryState>();
  const searchPageHistoryState: SearchPageHistoryState | undefined =
    history.location.state;

  const [searchState, dispatch] = useReducer(reducer, {
    status: "initialising",
    options: searchPageHistoryState?.searchPageOptions ?? {
      ...defaultSearchPageOptions,
      rawMode: getRawModeFromStorage(),
    },
  });
  const { options: searchPageOptions } = searchState;

  const [searchSettings, setSearchSettings] = useState<GeneralSearchSettings>(
    defaultGeneralSearchSettings,
  );

  const { appErrorHandler } = useContext(AppContext);
  const searchPageErrorHandler = useCallback(
    (error: Error) => {
      dispatch({ type: "error", cause: error });
    },
    [dispatch],
  );

  const search = useCallback(
    (
      searchPageOptions: SearchPageOptions,
      updateClassifications = true,
      callback: () => void = nop,
    ): void => {
      dispatch({
        type: "search",
        options: searchPageOptions,
        updateClassifications,
        callback,
      });
    },
    [dispatch],
  );

  /**
   * Error display -> similar to onError hook, however in the context of reducer need to do manually.
   */
  useEffect(() => {
    if (searchState.status === "failure") {
      appErrorHandler(searchState.cause);
    }
  }, [searchState, appErrorHandler]);

  /**
   * Page initialisation -> Update the page title, retrieve Search settings and trigger first
   * search.
   */
  useEffect(() => {
    if (searchState.status !== "initialising") {
      return;
    }

    const timerId = "sp-init";
    console.debug("SearchPage: useEffect - initialising");
    console.time(timerId);

    const { ready, customiseInitialSearchOptions, listInitialClassifications } =
      initialSearchConfig;

    updateTemplate((tp) => ({
      ...templateDefaults(pageTitle)(tp),
    }));

    Promise.all([
      getSearchSettingsFromServer(),
      getMimeTypeFiltersFromServer(),
      // If the search options are available from browser history, ignore those in the query string.
      searchPageHistoryState
        ? Promise.resolve(undefined)
        : generateSearchPageOptionsFromLocation(history.location),
      getAdvancedSearchesFromServer(),
    ])
      .then(
        ([
          searchSettings,
          mimeTypeFilters,
          queryStringSearchOptions,
          advancedSearches,
        ]) => {
          setSearchSettings({
            core: searchSettings,
            mimeTypeFilters,
            advancedSearches,
          });

          const mergeSearchPageOptions = (options: SearchPageOptions) =>
            customiseInitialSearchOptions(options, queryStringSearchOptions);

          // This is the SearchPageOptions for the first searching, not the one created in the first rendering.
          const initialSearchPageOptions = pipe(
            queryStringSearchOptions,
            O.fromNullable,
            O.map<SearchPageOptions, SearchPageOptions>((options) => ({
              ...options,
              dateRangeQuickModeEnabled: false,
              sortOrder: options.sortOrder ?? searchSettings.defaultSearchSort,
            })),
            O.getOrElse<SearchPageOptions>(() => ({
              ...searchPageOptions,
              sortOrder:
                searchPageOptions.sortOrder ?? searchSettings.defaultSearchSort,
            })),
            mergeSearchPageOptions,
          );

          ready && search(initialSearchPageOptions, listInitialClassifications);
        },
      )
      .catch(searchPageErrorHandler);

    console.timeEnd(timerId);
  }, [
    dispatch,
    searchPageErrorHandler,
    search,
    searchPageOptions,
    searchState.status,
    updateTemplate,
    initialSearchConfig,
    pageTitle,
    history.location,
    searchPageHistoryState,
  ]);

  /**
   * Searching -> Executing the search (including for classifications) and returning the results.
   */
  useEffect(() => {
    if (searchState.status === "searching") {
      const timerId = "sp-searching";
      console.debug("SearchPage: useEffect - searching");
      console.time(timerId);

      const { options, updateClassifications, callback } = searchState;

      const gallerySearch = async (
        search: typeof imageGallerySearch | typeof videoGallerySearch,
        options: SearchPageOptions,
      ): Promise<SearchPageSearchResult> => ({
        from: "gallery-search",
        content: await search(
          {
            ...options,
            // `mimeTypeFilters` should be ignored in gallery modes
            mimeTypeFilters: undefined,
          },
          galleryModeSearchProvider,
        ),
      });

      const doSearch = async (
        searchPageOptions: SearchPageOptions,
      ): Promise<SearchPageSearchResult> => {
        switch (searchPageOptions.displayMode) {
          case "gallery-image":
            return gallerySearch(imageGallerySearch, searchPageOptions);
          case "gallery-video":
            return gallerySearch(videoGallerySearch, searchPageOptions);
          case "list":
            return listModeSearchProvider(searchPageOptions);
          default:
            throw new TypeError("Unexpected `displayMode` for searching");
        }
      };

      // Depending on what display mode we're in, determine which function we use to list
      // the classifications to match the search.
      const getClassifications: (
        _: SearchOptions,
      ) => Promise<Classification[]> = pipe(options.displayMode, (mode) => {
        switch (mode) {
          case "gallery-image":
            return listImageGalleryClassifications;
          case "gallery-video":
            return listVideoGalleryClassifications;
          case "list":
            return listClassifications;
          default:
            throw new TypeError(
              "Unexpected `displayMode` for determining classifications listing function",
            );
        }
      });

      (async () => {
        try {
          const searchResult: SearchPageSearchResult = await doSearch(options);
          const classifications: Classification[] = updateClassifications
            ? await getClassifications(options)
            : [];

          dispatch({
            type: "search-complete",
            result: { ...searchResult },
            classifications,
          });

          // Save searchPageOptions in the browser history.
          history.replace({
            ...history.location,
            state: {
              ...searchPageHistoryState,
              searchPageOptions: options,
            },
          });

          // Run provided callback.
          callback?.();
        } catch (error: unknown) {
          searchPageErrorHandler(
            error instanceof Error
              ? error
              : new Error(`Failed to perform a search: ${error}`),
          );
        }
        console.timeEnd(timerId);
      })();
    }
  }, [
    dispatch,
    searchPageErrorHandler,
    history,
    searchState,
    searchPageHistoryState,
    listModeSearchProvider,
    galleryModeSearchProvider,
  ]);

  // In Selection Session, once a new search result is returned, make each
  // new search result Item draggable. Could probably merge into 'searching'
  // effect, however this is only required while selection sessions still
  // involve legacy content.
  useEffect(() => {
    if (
      searchState.status === "success" &&
      searchState.result.from === "item-search" &&
      isSelectionSessionInStructured()
    ) {
      searchState.result.content.results
        .filter(isLiveItem)
        .forEach(({ uuid }: OEQ.Search.SearchResultItem) => {
          prepareDraggable(uuid);
        });
    }
  }, [searchState]);

  return (
    <SearchContext.Provider
      value={{
        search,
        searchState,
        searchSettings,
        searchPageErrorHandler,
      }}
    >
      {children}
    </SearchContext.Provider>
  );
};
