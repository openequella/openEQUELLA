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

import { useMemo } from "react";
import { NEW_FAVOURITES_PATH } from "../mainui/routes";
import { TemplateUpdateProps } from "../mainui/Template";
import * as React from "react";
import { InitialSearchConfig, Search } from "../search/Search";
import { SearchPageBody } from "../search/SearchPageBody";
import { SearchPageOptions } from "../search/SearchPageHelper";
import { languageStrings } from "../util/langstrings";

const { title } = languageStrings.favourites;

const FavouritesPage = ({ updateTemplate }: TemplateUpdateProps) => {
  const initialSearchConfig = useMemo<InitialSearchConfig>(() => {
    const customiseInitialSearchOptions = (
      searchPageOptions: SearchPageOptions,
    ): SearchPageOptions => ({
      ...searchPageOptions,
      filterExpansion: true,
    });

    return {
      ready: true,
      listInitialClassifications: false,
      customiseInitialSearchOptions,
    };
  }, []);

  return (
    <Search
      updateTemplate={updateTemplate}
      initialSearchConfig={initialSearchConfig}
      pageTitle={title}
    >
      <SearchPageBody pathname={NEW_FAVOURITES_PATH} />
    </Search>
  );
};

export default FavouritesPage;
