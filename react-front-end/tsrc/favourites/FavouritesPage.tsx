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
import { useCallback, useContext, useMemo, useState } from "react";
import { AppContext } from "../mainui/App";
import { NEW_FAVOURITES_PATH } from "../mainui/routes";
import { TemplateUpdateProps } from "../mainui/Template";
import * as React from "react";
import {
  FavouritesType,
  searchFavouriteItems,
} from "../modules/FavouriteModule";
import { SearchOptions } from "../modules/SearchModule";
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
} from "../search/SearchPageHelper";
import { languageStrings } from "../util/langstrings";
import FavouritesSelector from "./components/FavouritesSelector";
import * as OEQ from "@openequella/rest-api-client";

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
      ready: true,
      listInitialClassifications: false,
      customiseInitialSearchOptions,
    };
  }, []);

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

  const doListSearchForFavItems = useCallback(
    async (
      searchOptions: SearchOptions,
    ): Promise<OEQ.Search.SearchResult<OEQ.Search.SearchResultItem>> =>
      pipe(
        O.fromNullable(currentUser),
        O.match(
          () =>
            // fallback when currentUser is undefined
            Promise.resolve(defaultPagedSearchResult),
          (user) => searchFavouriteItems(searchOptions, user),
        ),
      ),
    [currentUser],
  );

  return (
    <Search
      updateTemplate={updateTemplate}
      initialSearchConfig={initialSearchConfig}
      pageTitle={title}
      doListSearch={doListSearchForFavItems}
    >
      <SearchContext.Consumer>
        {(searchContextProps: SearchContextProps) => (
          <SearchPageBody
            pathname={NEW_FAVOURITES_PATH}
            headerConfig={favouritesPageHeaderConfig}
            refinePanelConfig={favouritesPageRefinePanelConfig(
              searchContextProps,
            )}
            refreshOnFavouriteRemoved
          />
        )}
      </SearchContext.Consumer>
    </Search>
  );
};

export default FavouritesPage;
