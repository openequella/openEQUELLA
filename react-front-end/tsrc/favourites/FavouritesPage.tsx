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

import { pipe } from "fp-ts/function";
import * as O from "fp-ts/Option";
import {
  type ReactNode,
  useCallback,
  useContext,
  useMemo,
  useState,
} from "react";
import ConfirmDialog from "../components/ConfirmDialog";
import { AppContext } from "../mainui/App";
import { NEW_FAVOURITES_PATH } from "../mainui/routes";
import type { TemplateUpdateProps } from "../mainui/Template";
import * as React from "react";
import {
  deleteFavouriteItem,
  FavouritesType,
  searchFavouriteItems,
} from "../modules/FavouriteModule";
import type { SearchOptions } from "../modules/SearchModule";
import { type InitialSearchConfig, Search } from "../search/Search";
import { SearchPageBody } from "../search/SearchPageBody";
import {
  SearchContext,
  type SearchContextProps,
  type SearchPageHeaderConfig,
  type SearchPageOptions,
  type SearchPageRefinePanelConfig,
  defaultSearchPageRefinePanelConfig,
  isListItems,
  isGalleryItems,
  defaultPagedSearchResult,
} from "../search/SearchPageHelper";
import type { SearchPageSearchResult } from "../search/SearchPageReducer";
import { languageStrings } from "../util/langstrings";
import FavouritesSelector from "./components/FavouritesSelector";
import * as OEQ from "@openequella/rest-api-client";
import { renderFavouriteItemsResult } from "./FavouritesItemHelper";

const { title } = languageStrings.favourites;
const { title: favouritesSelectorTitle } =
  languageStrings.favourites.favouritesSelector;
const { remove, removeAlert } = languageStrings.searchpage.favouriteItem;

const FavouritesPage = ({ updateTemplate }: TemplateUpdateProps) => {
  const { currentUser } = useContext(AppContext);
  const [bookmarkIdToRemove, setBookmarkIdToRemove] = useState<
    number | undefined
  >(undefined);

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
    (_: SearchContextProps) => (value: FavouritesType) => {
      setFavouritesType(value);
      //TODO: trigger a new search.
    };

  const favouritesPageHeaderConfig: SearchPageHeaderConfig = {
    enableCSVExportButton: false,
    enableShareSearchButton: false,
    enableFavouriteSearchButton: false,
  };

  const favouritesPageRefinePanelConfig = (
    searchContextProps: SearchContextProps,
  ): SearchPageRefinePanelConfig => {
    const defaultSearchConfig =
      favouritesType === "resources" ? defaultSearchPageRefinePanelConfig : {};

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

  /**
   * Provider used by Search to fetch favourite items.
   */
  const favouriteItemsSearchProvider = useCallback(
    async (
      searchOptions: SearchOptions,
    ): Promise<OEQ.Search.SearchResult<OEQ.Search.SearchResultItem>> =>
      pipe(
        // TODO: Remove once new GET API is available; current Search2 API needs `currentUser` for bookmark_owner must.
        O.fromNullable(currentUser),
        O.match(
          () => Promise.resolve(defaultPagedSearchResult),
          (user) => searchFavouriteItems(searchOptions, user),
        ),
      ),
    [currentUser],
  );

  const onRemoveItemFromFav = async (
    { search, searchState }: SearchContextProps,
    bookmarkId: number,
  ) => {
    await deleteFavouriteItem(bookmarkId);
    search(searchState.options);
    setBookmarkIdToRemove(undefined);
  };

  const renderCustomSearchResult = (
    searchResult: SearchPageSearchResult,
  ): ReactNode => {
    if (
      isListItems(searchResult.from, searchResult.content) ||
      isGalleryItems(searchResult.from, searchResult.content)
    ) {
      return renderFavouriteItemsResult(searchResult, setBookmarkIdToRemove);
    }
    // TODO: renderFavouriteSearchesResult
    return null;
  };

  return (
    <Search
      updateTemplate={updateTemplate}
      initialSearchConfig={initialSearchConfig}
      pageTitle={title}
      searchItemsProvider={
        favouritesType === "resources"
          ? favouriteItemsSearchProvider
          : undefined
      }
    >
      <SearchContext.Consumer>
        {(searchContextProps: SearchContextProps) => (
          <>
            <SearchPageBody
              pathname={NEW_FAVOURITES_PATH}
              headerConfig={favouritesPageHeaderConfig}
              refinePanelConfig={favouritesPageRefinePanelConfig(
                searchContextProps,
              )}
              customRenderSearchResults={renderCustomSearchResult}
              searchBarConfig={
                favouritesType === "searches"
                  ? { enableWildcardToggle: false }
                  : undefined
              }
            />
            {bookmarkIdToRemove && (
              <ConfirmDialog
                open={Boolean(bookmarkIdToRemove)}
                onConfirm={() =>
                  onRemoveItemFromFav(searchContextProps, bookmarkIdToRemove)
                }
                onCancel={() => setBookmarkIdToRemove(undefined)}
                title={remove}
                confirmButtonText={languageStrings.common.action.ok}
              >
                {removeAlert}
              </ConfirmDialog>
            )}
          </>
        )}
      </SearchContext.Consumer>
    </Search>
  );
};

export default FavouritesPage;
