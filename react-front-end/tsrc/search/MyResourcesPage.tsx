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
import * as React from "react";
import { createContext, useCallback, useContext, useState } from "react";
import { useHistory } from "react-router";
import { AppContext } from "../mainui/App";
import { NEW_MY_RESOURCES_PATH } from "../mainui/routes";
import { TemplateUpdateProps } from "../mainui/Template";
import { languageStrings } from "../util/langstrings";
import { MyResourcesSelector } from "./components/MyResourcesSelector";
import { myResourcesTypeToItemStatus } from "./MyResourcesPageHelper";
import type { MyResourcesType } from "./MyResourcesPageHelper";
import { Search, SearchContext, SearchPageHistoryState } from "./Search";
import { SearchPageBody } from "./SearchPageBody";
import {
  defaultSearchPageRefinePanelConfig,
  SearchPageOptions,
} from "./SearchPageHelper";

interface MyResourcesPageContextProps {
  onChange: (resourceType: MyResourcesType) => void;
}

export const MyResourcesPageContext =
  createContext<MyResourcesPageContextProps>({
    onChange: () => {},
  });

const { title } = languageStrings.myResources;

/**
 * This component controls how to render My resources page, including:
 * 1. controlling what Refine panel controls to be displayed.
 * 2. controlling My resources selector.
 * 3. preparing a function for the customised initial search.
 */
export const MyResourcesPage = ({ updateTemplate }: TemplateUpdateProps) => {
  const { currentUser } = useContext(AppContext);
  const history = useHistory<SearchPageHistoryState<MyResourcesType>>();
  const [resourceType, setResourceType] = useState<MyResourcesType>(
    // Todo: support getting the resource type from query string. Data saved in browser history
    // takes precedence over query params. But if both are undefined, default to 'Published'.
    history.location.state?.customData?.["myResourcesType"] ?? "Published"
  );

  const myResourcesInitialSearchOptions = useCallback(
    (searchPageOptions: SearchPageOptions): SearchPageOptions => ({
      ...searchPageOptions,
      owner: currentUser,
      status: myResourcesTypeToItemStatus(resourceType),
    }),
    [currentUser, resourceType]
  );

  const scrapbookSelected = resourceType === "Scrapbook";

  return (
    <Search
      updateTemplate={updateTemplate}
      initialSearchConfig={{
        ready: true,
        listInitialClassifications: true,
        customiseInitialSearchOptions: myResourcesInitialSearchOptions,
      }}
      pageTitle={title}
    >
      <SearchContext.Consumer>
        {({ search, searchState }) => (
          <SearchPageBody
            pathname={NEW_MY_RESOURCES_PATH}
            enableClassification={false}
            refinePanelConfig={{
              ...defaultSearchPageRefinePanelConfig,
              enableItemStatusSelector: [
                "All resources",
                "Moderation queue",
              ].includes(resourceType),
              enableDisplayModeSelector: !scrapbookSelected,
              enableCollectionSelector: !scrapbookSelected,
              enableAdvancedSearchSelector: false,
              enableRemoteSearchSelector: false,
              enableOwnerSelector: false,
              customRefinePanelControl: [
                {
                  idSuffix: "MyResourcesSelector",
                  title,
                  component: (
                    <MyResourcesSelector
                      value={resourceType}
                      onChange={(resourcesType) => {
                        setResourceType(resourcesType);

                        // Trigger a search with Item statuses that match the new resource type.
                        search({
                          ...searchState.options,
                          status: myResourcesTypeToItemStatus(resourcesType),
                        });

                        // Also save the selected My resources type in the browser history.
                        history.replace({
                          ...history.location,
                          state: {
                            ...history.location.state,
                            customData: {
                              myResourcesType: resourcesType,
                            },
                          },
                        });
                      }}
                    />
                  ),
                  alwaysVisible: true,
                },
              ],
            }}
          />
        )}
      </SearchContext.Consumer>
    </Search>
  );
};

export default MyResourcesPage;
