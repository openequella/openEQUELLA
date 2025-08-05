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

import { useMemo, useState } from "react";
import { NEW_FAVOURITES_PATH } from "../mainui/routes";
import { TemplateUpdateProps } from "../mainui/Template";
import * as React from "react";
import { FavouritesType } from "../modules/FavouriteModule";
import { InitialSearchConfig, Search } from "../search/Search";
import { SearchPageBody } from "../search/SearchPageBody";
import {
  SearchContext,
  SearchContextProps,
  SearchPageHeaderConfig,
  SearchPageOptions,
  SearchPageRefinePanelConfig,
  defaultSearchPageRefinePanelConfig,
} from "../search/SearchPageHelper";
import { languageStrings } from "../util/langstrings";
import FavouritesSelector from "./components/FavouritesSelector";

const { title } = languageStrings.favourites;
const { title: favouritesSelectorTitle } =
  languageStrings.favourites.favouritesSelector;

const FavouritesPage = ({ updateTemplate }: TemplateUpdateProps) => {
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

  return (
    <Search
      updateTemplate={updateTemplate}
      initialSearchConfig={initialSearchConfig}
      pageTitle={title}
    >
      <SearchContext.Consumer>
        {(searchContextProps: SearchContextProps) => (
          <SearchPageBody
            pathname={NEW_FAVOURITES_PATH}
            headerConfig={favouritesPageHeaderConfig}
            refinePanelConfig={favouritesPageRefinePanelConfig(
              searchContextProps,
            )}
          />
        )}
      </SearchContext.Consumer>
    </Search>
  );
};

export default FavouritesPage;
