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

import * as O from "fp-ts/Option";
import {
  type ReactNode,
  useCallback,
  useContext,
  useMemo,
  useState,
} from "react";
import { AppContext } from "../mainui/App";
import { pipe } from "fp-ts/function";
import { NEW_FAVOURITES_PATH } from "../mainui/routes";
import type { TemplateUpdateProps } from "../mainui/Template";
import * as React from "react";
import {
  FavouritesType,
  searchFavouriteItems,
} from "../modules/FavouriteModule";
import type { SearchOptions } from "../modules/SearchModule";
import GallerySearchResult from "../search/components/GallerySearchResult";
import { type InitialSearchConfig, Search } from "../search/Search";
import { SearchPageBody } from "../search/SearchPageBody";
import {
  SearchContext,
  type SearchContextProps,
  type SearchPageOptions,
  type SearchPageRefinePanelConfig,
  defaultSearchPageRefinePanelConfig,
  isListItems,
  isGalleryItems,
  defaultPagedSearchResult,
  isFavouriteSearches,
} from "../search/SearchPageHelper";
import type { SearchPageSearchResult } from "../search/SearchPageReducer";
import { languageStrings } from "../util/langstrings";
import FavouritesSelector from "./components/FavouritesSelector";
import {
  defaultFavouritesPageOptions,
  listFavouriteSearches,
  favouritesItemsResult,
  favouritesPageHeaderConfig,
  favouritesSearchesResult,
  favouritesSearchRefinePanelConfig,
} from "./FavouritesPageHelper";

const { title } = languageStrings.favourites;
const { title: favouritesSelectorTitle } =
  languageStrings.favourites.favouritesSelector;

const FavouritesPage = ({ updateTemplate }: TemplateUpdateProps) => {
  const { currentUser } = useContext(AppContext);

  const [favouritesType, setFavouritesType] =
    useState<FavouritesType>("resources");

  const initialSearchConfig = useMemo<InitialSearchConfig>(() => {
    const customiseInitialSearchOptions = (
      searchPageOptions: SearchPageOptions,
    ): SearchPageOptions => ({
      ...searchPageOptions,
      filterExpansion: false,
    });

    return {
      ready: currentUser !== undefined,
      listInitialClassifications: false,
      customiseInitialSearchOptions,
    };
  }, [currentUser]);

  const onFavouritesTypeChangeBuilder =
    ({ search }: SearchContextProps) =>
    (value: FavouritesType) => {
      setFavouritesType(value);
      search(defaultFavouritesPageOptions);
    };

  const favouritesPageRefinePanelConfig = (
    searchContextProps: SearchContextProps,
  ): SearchPageRefinePanelConfig => {
    const defaultSearchConfig =
      favouritesType === "resources"
        ? defaultSearchPageRefinePanelConfig
        : favouritesSearchRefinePanelConfig;

    return {
      ...defaultSearchConfig,
      enableAdvancedSearchSelector: false,
      enableDateRangeSelector: true,
      customRefinePanelControl: [
        {
          idSuffix: "FavouritesSelector",
          title: favouritesSelectorTitle,
          component: (
            <FavouritesSelector
              value={favouritesType}
              onChange={onFavouritesTypeChangeBuilder(searchContextProps)}
            />
          ),
          alwaysVisible: true,
        },
      ],
    };
  };

  // Provider used by Search to fetch favourite items.
  const favouriteItemsSearchProvider = useCallback(
    async (searchOptions: SearchOptions): Promise<SearchPageSearchResult> =>
      pipe(
        O.fromNullable(currentUser),
        O.match(
          () => Promise.resolve(defaultPagedSearchResult),
          (user) => searchFavouriteItems(searchOptions, user),
        ),
        async (result) => ({
          from: "item-search",
          content: await result,
        }),
      ),
    [currentUser],
  );

  // Refresh the search results when an item or search is removed from favourites.
  const buildOnFavouriteRemoved =
    ({ search, searchState }: SearchContextProps) =>
    () =>
      search(searchState.options);

  const renderCustomSearchResult =
    (searchContextProps: SearchContextProps) =>
    (pageResult: SearchPageSearchResult): ReactNode => {
      const {
        from,
        content: { results: searchResults, highlight },
      } = pageResult;

      if (isListItems(from, searchResults)) {
        return favouritesItemsResult(
          searchResults,
          highlight,
          buildOnFavouriteRemoved(searchContextProps),
        );
      } else if (isGalleryItems(from, searchResults)) {
        return <GallerySearchResult items={searchResults} />;
      } else if (isFavouriteSearches(from, searchResults)) {
        return favouritesSearchesResult(
          searchResults,
          highlight,
          buildOnFavouriteRemoved(searchContextProps),
        );
      }

      throw new TypeError("Unexpected display mode for favourites result");
    };

  return (
    <Search
      updateTemplate={updateTemplate}
      initialSearchConfig={initialSearchConfig}
      pageTitle={title}
      listModeSearchProvider={
        favouritesType === "resources"
          ? favouriteItemsSearchProvider
          : listFavouriteSearches
      }
    >
      <SearchContext.Consumer>
        {(searchContextProps: SearchContextProps) => (
          <SearchPageBody
            pathname={NEW_FAVOURITES_PATH}
            headerConfig={favouritesPageHeaderConfig}
            refinePanelConfig={favouritesPageRefinePanelConfig(
              searchContextProps,
            )}
            customRenderSearchResults={renderCustomSearchResult(
              searchContextProps,
            )}
            enableClassification={false}
            searchBarConfig={
              favouritesType === "searches"
                ? { enableWildcardToggle: false }
                : undefined
            }
          />
        )}
      </SearchContext.Consumer>
    </Search>
  );
};

export default FavouritesPage;
