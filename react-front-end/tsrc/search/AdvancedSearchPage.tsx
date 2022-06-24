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
import { constFalse, pipe } from "fp-ts/function";
import * as O from "fp-ts/Option";
import * as React from "react";
import { createContext, useCallback, useEffect, useState } from "react";
import { useLocation } from "react-router";
import {
  extractDefaultValues,
  FieldValueMap,
} from "../components/wizard/WizardHelper";
import { NEW_ADVANCED_SEARCH_PATH } from "../mainui/routes";
import { TemplateUpdateProps } from "../mainui/Template";
import {
  getAdvancedSearchByUuid,
  getAdvancedSearchIdFromLocation,
} from "../modules/AdvancedSearchModule";
import {
  generateAdvancedSearchCriteria,
  confirmInitialFieldValueMap,
  isAdvSearchCriteriaSet,
} from "./AdvancedSearchHelper";
import { AdvancedSearchPanelRefactored } from "./components/AdvancedSearchPanelRefactored";
import { Search } from "./Search";
import { SearchPageBody } from "./SearchPageBody";
import {
  defaultSearchPageRefinePanelConfig,
  SearchPageOptions,
} from "./SearchPageHelper";

interface AdvancedSearchPageContextProps {
  /**
   * Function to update each control's value.
   */
  updateFieldValueMap: (fieldValueMap: FieldValueMap) => void;
  /**
   * Function to control whether the Advanced search panel is open.
   */
  openAdvancedSearchPanel: (open: boolean) => void;
  /**
   * `true` when the Advanced search definition is retrieved from server.
   */
  definitionRetrieved: boolean;
}

const nop = () => {};

export const AdvancedSearchPageContext =
  createContext<AdvancedSearchPageContextProps>({
    updateFieldValueMap: nop,
    openAdvancedSearchPanel: nop,
    definitionRetrieved: false,
  });

/**
 * This component controls how to render the Advanced search page, including:
 * 1. extracting the Advanced search ID from the current URL.
 * 2. preparing a function for the customised initial search.
 * 3. controlling the Advanced search panel.
 */
export const AdvancedSearchPage = ({ updateTemplate }: TemplateUpdateProps) => {
  const [advancedSearchDefinition, setAdvancedSearchDefinition] =
    useState<OEQ.AdvancedSearch.AdvancedSearchDefinition>();
  const [fieldValueMap, setFieldValueMap] = useState<FieldValueMap>();
  const [openAdvSearchPanel, setOpenAdvSearchPanel] = useState(true);

  const location = useLocation();

  const advancedSearchId = pipe(
    getAdvancedSearchIdFromLocation(location),
    O.fromNullable,
    O.getOrElseW(() => {
      throw new Error("Failed to extract Advanced search ID from the URL.");
    })
  );

  useEffect(() => {
    getAdvancedSearchByUuid(advancedSearchId).then(setAdvancedSearchDefinition);
  }, [advancedSearchId]);

  const initialise = useCallback(
    (
      searchPageOptions: SearchPageOptions,
      queryStringSearchOptions?: SearchPageOptions
    ): SearchPageOptions => {
      const initialFieldValueMap = pipe(
        advancedSearchDefinition,
        O.fromNullable,
        O.map((def) =>
          confirmInitialFieldValueMap(
            extractDefaultValues(def.controls),
            searchPageOptions,
            queryStringSearchOptions
          )
        ),
        O.getOrElse(() => searchPageOptions.advFieldValue)
      );
      const initialAdvancedSearchCriteria = pipe(
        initialFieldValueMap,
        O.fromNullable,
        O.map(generateAdvancedSearchCriteria),
        O.toUndefined
      );

      setFieldValueMap(initialFieldValueMap);

      return {
        ...searchPageOptions,
        collections: advancedSearchDefinition?.collections,
        advFieldValue: initialFieldValueMap,
        advancedSearchCriteria: initialAdvancedSearchCriteria,
      };
    },
    [advancedSearchDefinition]
  );

  const panel = (
    <AdvancedSearchPanelRefactored
      title={advancedSearchDefinition?.name}
      wizardControls={advancedSearchDefinition?.controls ?? []}
      values={fieldValueMap ?? new Map()}
    />
  );

  const accent = pipe(
    fieldValueMap,
    O.fromNullable,
    O.map(isAdvSearchCriteriaSet),
    O.getOrElse(constFalse)
  );

  const definitionRetrieved = advancedSearchDefinition !== undefined;

  return (
    <AdvancedSearchPageContext.Provider
      value={{
        updateFieldValueMap: setFieldValueMap,
        openAdvancedSearchPanel: setOpenAdvSearchPanel,
        definitionRetrieved,
      }}
    >
      <Search
        updateTemplate={updateTemplate}
        initialSearchConfig={{
          ready: definitionRetrieved,
          customiseInitialSearchOptions: initialise,
          listInitialClassifications: false,
        }}
      >
        <SearchPageBody
          pathname={`${NEW_ADVANCED_SEARCH_PATH}/${advancedSearchId}`}
          additionalPanels={openAdvSearchPanel ? [panel] : undefined}
          searchBarConfig={{
            advancedSearchFilter: {
              onClick: () => setOpenAdvSearchPanel(!openAdvSearchPanel),
              accent,
            },
          }}
          refinePanelConfig={{
            ...defaultSearchPageRefinePanelConfig,
            enableCollectionSelector: false,
          }}
          enableClassification={false}
        />
      </Search>
    </AdvancedSearchPageContext.Provider>
  );
};

export default AdvancedSearchPage;
