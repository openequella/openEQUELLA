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
import { constFalse, flow, pipe } from "fp-ts/function";
import * as O from "fp-ts/Option";
import * as E from "fp-ts/Either";
import * as TE from "fp-ts/TaskEither";
import * as React from "react";
import { useCallback, useContext, useEffect, useState } from "react";
import { useLocation } from "react-router";
import {
  extractDefaultValues,
  FieldValueMap,
} from "../components/wizard/WizardHelper";
import { AppContext } from "../mainui/App";
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
  AdvancedSearchPageContext,
} from "./AdvancedSearchHelper";
import { AdvancedSearchPanel } from "./components/AdvancedSearchPanel";
import { Search } from "./Search";
import { SearchPageBody } from "./SearchPageBody";
import {
  buildSearchPageNavigationConfig,
  defaultSearchPageHeaderConfig,
  defaultSearchPageRefinePanelConfig,
  SearchContext,
  SearchContextProps,
  SearchPageHeaderConfig,
  SearchPageOptions,
} from "./SearchPageHelper";

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
  const [advancedSearchId] = useState(
    getAdvancedSearchIdFromLocation(location),
  );
  const { appErrorHandler } = useContext(AppContext);

  useEffect(() => {
    const getDefinition = pipe(
      advancedSearchId,
      TE.fromNullable("Advanced search ID must not be empty"),
      TE.chain(
        flow(
          TE.tryCatchK(
            getAdvancedSearchByUuid,
            () =>
              `Failed to retrieve Advanced search definition for ${advancedSearchId}`,
          ),
        ),
      ),
    );

    (async () => {
      pipe(
        await getDefinition(),
        E.fold(appErrorHandler, setAdvancedSearchDefinition),
      );
    })();
  }, [advancedSearchId, appErrorHandler]);

  // Function to override `collections`, `advFieldValue` and `advancedSearchCriteria`
  // for the initial search in Advanced Search.
  const buildInitialAdvancedSearchOptions = useCallback(
    (
      searchPageOptions: SearchPageOptions,
      queryStringSearchOptions?: SearchPageOptions,
    ): SearchPageOptions => {
      const initialFieldValueMap = pipe(
        advancedSearchDefinition,
        O.fromNullable,
        O.map((def) =>
          confirmInitialFieldValueMap(
            extractDefaultValues(def.controls),
            searchPageOptions,
            queryStringSearchOptions,
          ),
        ),
        O.getOrElse(() => searchPageOptions.advFieldValue),
      );
      const initialAdvancedSearchCriteria = pipe(
        initialFieldValueMap,
        O.fromNullable,
        O.map(generateAdvancedSearchCriteria),
        O.toUndefined,
      );

      setFieldValueMap(initialFieldValueMap);

      return {
        ...searchPageOptions,
        collections: advancedSearchDefinition?.collections,
        advFieldValue: initialFieldValueMap,
        advancedSearchCriteria: initialAdvancedSearchCriteria,
      };
    },
    [advancedSearchDefinition],
  );

  const panel = (
    <AdvancedSearchPanel
      title={advancedSearchDefinition?.name}
      wizardControls={advancedSearchDefinition?.controls ?? []}
      values={fieldValueMap ?? new Map()}
    />
  );

  const accent = pipe(
    fieldValueMap,
    O.fromNullable,
    O.map(isAdvSearchCriteriaSet),
    O.getOrElse(constFalse),
  );

  const definitionRetrieved = advancedSearchDefinition !== undefined;

  const searchPageHeaderConfig = (
    options: SearchPageOptions,
  ): SearchPageHeaderConfig => ({
    ...defaultSearchPageHeaderConfig,
    enableCSVExportButton: false,
    newSearchConfig: {
      navigationTo: buildSearchPageNavigationConfig(options),
    },
  });

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
          customiseInitialSearchOptions: buildInitialAdvancedSearchOptions,
          listInitialClassifications: false,
        }}
      >
        <SearchContext.Consumer>
          {(searchContextProps: SearchContextProps) => (
            <SearchPageBody
              pathname={`${NEW_ADVANCED_SEARCH_PATH}/${advancedSearchId}`}
              headerConfig={searchPageHeaderConfig(
                searchContextProps.searchState.options,
              )}
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
                enableRemoteSearchSelector: false,
              }}
              enableClassification={false}
              customSearchCallback={() => setOpenAdvSearchPanel(false)}
            />
          )}
        </SearchContext.Consumer>
      </Search>
    </AdvancedSearchPageContext.Provider>
  );
};

export default AdvancedSearchPage;
