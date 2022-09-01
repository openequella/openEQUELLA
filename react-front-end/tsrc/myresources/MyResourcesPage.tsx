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
import * as A from "fp-ts/Array";
import * as React from "react";
import { ReactNode, useContext, useMemo, useState } from "react";
import { useHistory } from "react-router";
import { AppContext } from "../mainui/App";
import { NEW_MY_RESOURCES_PATH } from "../mainui/routes";
import { TemplateUpdateProps } from "../mainui/Template";
import { nonDeletedStatuses } from "../modules/SearchModule";
import type { StatusSelectorProps } from "../search/components/StatusSelector";
import {
  Search,
  SearchContext,
  SearchContextProps,
  SearchPageHistoryState,
} from "../search/Search";
import { SearchPageBody } from "../search/SearchPageBody";
import {
  defaultSearchPageHeaderConfig,
  defaultSearchPageOptions,
  defaultSearchPageRefinePanelConfig,
  SearchPageHeaderConfig,
  SearchPageOptions,
  SearchPageRefinePanelConfig,
} from "../search/SearchPageHelper";
import type { SearchPageSearchResult } from "../search/SearchPageReducer";
import { languageStrings } from "../util/langstrings";
import { MyResourcesSelector } from "./components/MyResourcesSelector";
import type { MyResourcesType } from "./MyResourcesPageHelper";
import {
  customUIForMyResources,
  defaultSortOrder,
  myResourcesTypeToItemStatus,
  renderAllResources,
  buildRenderScrapbookResult,
} from "./MyResourcesPageHelper";

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

  const initialSearchConfig = useMemo(
    () => ({
      ready: true,
      listInitialClassifications: true,
      customiseInitialSearchOptions: (
        searchPageOptions: SearchPageOptions
      ): SearchPageOptions => ({
        ...searchPageOptions,
        owner: currentUser,
        status: myResourcesTypeToItemStatus(resourceType),
        // The sort order in My resources initial search is a bit different. If no sort order
        // is present in either the browser history or query string, we use the custom default
        // sort order rather than the one configured in Search settings.
        sortOrder:
          // todo: support getting the sort order from query string.
          history.location.state?.searchPageOptions?.sortOrder ??
          defaultSortOrder(resourceType),
      }),
    }),
    [
      currentUser,
      history.location.state?.searchPageOptions?.sortOrder,
      resourceType,
    ]
  );

  const customSearchCriteria = (
    myResourcesType: MyResourcesType = resourceType
  ): SearchPageOptions => ({
    ...defaultSearchPageOptions,
    owner: currentUser,
    status: myResourcesTypeToItemStatus(myResourcesType),
    sortOrder: defaultSortOrder(resourceType),
  });

  // Build an onChange event handler for MyResourcesSelector to do:
  // 1. Updating currently selected resources type and clear selected sub-statuses.
  // 2. Performing a search with custom search criteria.
  // 3. Saving currently selected resources type to the browser history.
  const onChangeBuilder =
    ({ search }: SearchContextProps) =>
    (value: MyResourcesType) => {
      setResourceType(value);
      setSubStatus([]);

      search(customSearchCriteria(value));

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

  // Return a function to render custom UI for the SearchResult in Scrapbook, Moderation queue
  // and All resources. For others, return undefined.
  const customSearchResultBuilder = ():
    | ((searchResult: SearchPageSearchResult) => ReactNode)
    | undefined => {
    const renderScrapbook = buildRenderScrapbookResult(
      () => {}, // todo: Open the legacy page for editing Scrapbook.
      () => {} // todo: Delete the Scrapbook and do a search again.
    );

    switch (resourceType) {
      // todo: OEQ-1009: custom UI for Scrapbook
      case "Scrapbook":
        return undefined;
      // todo: OEQ-990: custom UI for Moderation queue
      case "Moderation queue":
        return undefined;
      case "All resources":
        return (result: SearchPageSearchResult) =>
          customUIForMyResources(
            result,
            (items: OEQ.Search.SearchResult<OEQ.Search.SearchResultItem>) =>
              renderAllResources(items, renderScrapbook)
          );
      default:
        return undefined;
    }
  };

  const searchPageHeaderConfig: SearchPageHeaderConfig = {
    ...defaultSearchPageHeaderConfig,
    newSearchConfig: {
      path: NEW_MY_RESOURCES_PATH,
      criteria: customSearchCriteria(),
    },
  };

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
        status: A.isNonEmpty(value) ? value : options,
      });
      setSubStatus(value);
    };

    return {
      advancedMode: { options },
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
      initialSearchConfig={initialSearchConfig}
      pageTitle={title}
    >
      <SearchContext.Consumer>
        {(searchContextProps: SearchContextProps) => (
          <SearchPageBody
            pathname={NEW_MY_RESOURCES_PATH}
            enableClassification={false}
            headerConfig={searchPageHeaderConfig}
            refinePanelConfig={searchPageRefinePanelConfig(searchContextProps)}
            customRenderSearchResults={customSearchResultBuilder()}
          />
        )}
      </SearchContext.Consumer>
    </Search>
  );
};

export default MyResourcesPage;
