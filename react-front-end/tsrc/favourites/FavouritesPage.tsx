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
import { ReactNode, useCallback, useContext, useMemo, useState } from "react";
import { AppContext } from "../mainui/App";
import { NEW_FAVOURITES_PATH } from "../mainui/routes";
import { TemplateUpdateProps } from "../mainui/Template";
import * as React from "react";
import {
  deleteFavouriteItem,
  FavouritesType,
  searchFavouriteItems,
} from "../modules/FavouriteModule";
import { SearchOptions } from "../modules/SearchModule";
import { RemoveFromFavouritesConfirmDialog } from "../search/components/FavouriteItemDialog";
import { InitialSearchConfig, Search } from "../search/Search";
import { SearchPageBody } from "../search/SearchPageBody";
import {
  SearchContext,
  SearchContextProps,
  SearchPageHeaderConfig,
  SearchPageOptions,
  SearchPageRefinePanelConfig,
  defaultSearchPageRefinePanelConfig,
  defaultPagedSearchResult,
  isListItems,
  isGalleryItems,
} from "../search/SearchPageHelper";
import type { SearchPageSearchResult } from "../search/SearchPageReducer";
import { languageStrings } from "../util/langstrings";
import FavouritesSelector from "./components/FavouritesSelector";
import * as OEQ from "@openequella/rest-api-client";
import { renderFavouriteItemsResult } from "./FavouritesItemHelper";

const { title, error } = languageStrings.favourites;
const { title: favouritesSelectorTitle } =
  languageStrings.favourites.favouritesSelector;

const FavouritesPage = ({ updateTemplate }: TemplateUpdateProps) => {
  const { currentUser, appErrorHandler } = useContext(AppContext);
  const [bookmarkIdToRemove, setBookmarkIdToRemove] = useState<
    number | undefined
  >(undefined);
  const [isRemoveDialogOpen, setIsRemoveDialogOpen] = useState<boolean>(false);

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
   *
   * Dependency notes:
   * - currentUser: triggers refetch when user changes.
   * - appErrorHandler: sourced from context and may change if the provider updates;
   *   include it to avoid stale closures and satisfy exhaustive-deps.
   *   If your AppContext guarantees it is stable, it's still safe to keep here.
   */
  const favouriteItemsSearchProvider = useCallback(
    async (
      searchOptions: SearchOptions,
    ): Promise<OEQ.Search.SearchResult<OEQ.Search.SearchResultItem>> =>
      pipe(
        O.fromNullable(currentUser),
        O.match(
          () => {
            // fallback when currentUser is undefined
            appErrorHandler(error.noLoggedInUserFound);
            return Promise.resolve(defaultPagedSearchResult);
          },
          (user) => searchFavouriteItems(searchOptions, user),
        ),
      ),
    [currentUser, appErrorHandler],
  );

  const handleRemoveItemFromFavourites = useCallback((bookmarkId: number) => {
    setBookmarkIdToRemove(bookmarkId);
    setIsRemoveDialogOpen(true);
  }, []);

  const removeItemFromFavourites = useCallback(
    async ({ search, searchState }: SearchContextProps) => {
      if (bookmarkIdToRemove === undefined) {
        return;
      }
      await deleteFavouriteItem(bookmarkIdToRemove);
      search(searchState.options);
      setIsRemoveDialogOpen(false);
      setBookmarkIdToRemove(undefined);
    },
    [bookmarkIdToRemove],
  );

  const renderCustomSearchResult = (
    searchResult: SearchPageSearchResult,
  ): ReactNode => {
    if (
      isListItems(searchResult.from, searchResult.content) ||
      isGalleryItems(searchResult.from, searchResult.content)
    ) {
      return renderFavouriteItemsResult(
        searchResult,
        handleRemoveItemFromFavourites,
      );
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
            />
            {isRemoveDialogOpen && (
              <RemoveFromFavouritesConfirmDialog
                open={isRemoveDialogOpen}
                onConfirm={() => removeItemFromFavourites(searchContextProps)}
                onCancel={() => {
                  setBookmarkIdToRemove(undefined);
                  setIsRemoveDialogOpen(false);
                }}
              />
            )}
          </>
        )}
      </SearchContext.Consumer>
    </Search>
  );
};

export default FavouritesPage;
