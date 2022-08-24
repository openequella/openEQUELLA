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
import * as React from "react";
import { createContext, useCallback, useContext, useState } from "react";
import { useHistory } from "react-router";
import { AppContext } from "../mainui/App";
import { NEW_MY_RESOURCES_PATH } from "../mainui/routes";
import { TemplateUpdateProps } from "../mainui/Template";
import { nonDeletedStatuses } from "../modules/SearchModule";
import { defaultSearchSettings } from "../modules/SearchSettingsModule";
import { languageStrings } from "../util/langstrings";
import { MyResourcesSelector } from "./components/MyResourcesSelector";
import type { StatusSelectorProps } from "./components/StatusSelector";
import { myResourcesTypeToItemStatus } from "./MyResourcesPageHelper";
import type { MyResourcesType } from "./MyResourcesPageHelper";
import {
  Search,
  SearchContext,
  SearchContextProps,
  SearchPageHistoryState,
} from "./Search";
import { SearchPageBody } from "./SearchPageBody";
import {
  defaultSearchPageHeaderConfig,
  defaultSearchPageOptions,
  defaultSearchPageRefinePanelConfig,
  SearchPageHeaderConfig,
  SearchPageOptions,
  SearchPageRefinePanelConfig,
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

  // The sub-statuses refer to Item statuses that can be selected from Moderation queue and All resources.
  const [subStatus, setSubStatus] = useState<OEQ.Common.ItemStatus[]>([]);

  const myResourcesInitialSearchOptions = useCallback(
    (searchPageOptions: SearchPageOptions): SearchPageOptions => ({
      ...searchPageOptions,
      owner: currentUser,
      status: myResourcesTypeToItemStatus(resourceType),
    }),
    [currentUser, resourceType]
  );

  const customSearchCriteria = (
    myResourcesType: MyResourcesType,
    { defaultSearchSort }: OEQ.SearchSettings.Settings = defaultSearchSettings
  ): SearchPageOptions => ({
    ...defaultSearchPageOptions,
    owner: currentUser,
    status: myResourcesTypeToItemStatus(myResourcesType),
    sortOrder: defaultSearchSort,
  });

  // Build an onChange event handler for MyResourcesSelector to do:
  // 1. Updating currently selected resources type and clear selected sub-statuses.
  // 2. Performing a search with custom search criteria.
  // 3. Saving currently selected resources type to the browser history.
  const onChangeBuilder =
    ({ search, searchSettings: { core } }: SearchContextProps) =>
    (value: MyResourcesType) => {
      setResourceType(value);
      setSubStatus([]);

      search(customSearchCriteria(value, core));

      history.replace({
        ...history.location,
        state: {
          ...history.location.state,
          customData: {
            myResourcesType: value,
          },
        },
      });
    };

  const searchPageHeaderConfig = ({
    searchSettings: { core },
  }: SearchContextProps): SearchPageHeaderConfig => ({
    ...defaultSearchPageHeaderConfig,
    newSearchConfig: {
      path: NEW_MY_RESOURCES_PATH,
      criteria: customSearchCriteria(resourceType, core),
    },
  });

  // Build custom configuration for Status selector.
  // 1. Turn on the Advanced mode to allow the selection of individual status.
  // 2. The available status options are "MODERATING", "REVIEW" and "REJECTED" for Moderation queue
  // and all statuses except "DELETED" for All resources.
  // 3. The 'onChange' handler includes all available statuses in the search criteria when no status is selected,
  // and it also updates the selected sub-status.
  const buildStatusSelectorCustomConfig = ({
    search,
    searchState: { options: searchPageOptions },
  }: SearchContextProps): StatusSelectorProps => {
    const options: OEQ.Common.ItemStatus[] =
      "Moderation queue" === resourceType
        ? ["MODERATING", "REVIEW", "REJECTED"]
        : nonDeletedStatuses;

    const onChange = (value: OEQ.Common.ItemStatus[]) => {
      search({
        ...searchPageOptions,
        status: value.length > 0 ? value : options,
      });
      setSubStatus(value);
    };

    return {
      advancedUse: true,
      options,
      onChange,
      value: subStatus,
    };
  };

  const searchPageRefinePanelConfig = (
    searchContextProps: SearchContextProps
  ): SearchPageRefinePanelConfig => ({
    ...defaultSearchPageRefinePanelConfig,
    enableDisplayModeSelector: resourceType !== "Moderation queue",
    enableCollectionSelector: resourceType !== "Scrapbook",
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
            onChange={onChangeBuilder(searchContextProps)}
          />
        ),
        alwaysVisible: true,
      },
    ],
    // Always enable Status selector for Moderation queue and All resources through the custom config,
    // so set `false` to 'enableItemStatusSelector' for other resource types.
    enableItemStatusSelector: false,
    statusSelectorCustomConfig: {
      alwaysEnabled: ["Moderation queue", "All resources"].includes(
        resourceType
      ),
      selectorProps: buildStatusSelectorCustomConfig(searchContextProps),
    },
  });

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
        {(searchContextProps: SearchContextProps) => (
          <SearchPageBody
            pathname={NEW_MY_RESOURCES_PATH}
            enableClassification={false}
            headerConfig={searchPageHeaderConfig(searchContextProps)}
            refinePanelConfig={searchPageRefinePanelConfig(searchContextProps)}
          />
        )}
      </SearchContext.Consumer>
    </Search>
  );
};

export default MyResourcesPage;
