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
import { ListItemIcon, ListItemText, Menu, MenuItem } from "@mui/material";
import AddIcon from "@mui/icons-material/Add";
import CloudUploadIcon from "@mui/icons-material/CloudUpload";
import PagesIcon from "@mui/icons-material/Pages";
import * as OEQ from "@openequella/rest-api-client";
import * as A from "fp-ts/Array";
import { pipe } from "fp-ts/function";
import * as React from "react";
import { ReactNode, useContext, useMemo, useState } from "react";
import { useHistory } from "react-router";
import ConfirmDialog from "../components/ConfirmDialog";
import Lightbox, { LightboxProps } from "../components/Lightbox";
import MessageInfo from "../components/MessageInfo";
import { TooltipIconButton } from "../components/TooltipIconButton";
import { AppContext } from "../mainui/App";
import { NEW_MY_RESOURCES_PATH } from "../mainui/routes";
import { TemplateUpdateProps } from "../mainui/Template";
import type { FavouriteURL } from "../modules/FavouriteModule";
import {
  deleteScrapbook,
  openLegacyFileCreatingPage,
  openLegacyFileEditingPage,
  ScrapbookType,
} from "../modules/ScrapbookModule";
import { nonDeletedStatuses } from "../modules/SearchModule";
import type { ViewerLightboxConfig } from "../modules/ViewerModule";
import type { StatusSelectorProps } from "../search/components/StatusSelector";
import {
  InitialSearchConfig,
  Search,
  SearchPageHistoryState,
} from "../search/Search";
import { SearchPageBody } from "../search/SearchPageBody";
import {
  defaultSearchPageHeaderConfig,
  defaultSearchPageOptions,
  defaultSearchPageRefinePanelConfig,
  SearchContext,
  SearchContextProps,
  SearchPageHeaderConfig,
  SearchPageOptions,
  SearchPageRefinePanelConfig,
} from "../search/SearchPageHelper";
import type { SearchPageSearchResult } from "../search/SearchPageReducer";
import { languageStrings } from "../util/langstrings";
import { ModerationSearchResult } from "./components/ModerationSearchResult";
import { MyResourcesSelector } from "./components/MyResourcesSelector";
import type { MyResourcesType } from "./MyResourcesPageHelper";
import {
  buildRenderScrapbookResult,
  customUIForMyResources,
  defaultSortOrder,
  getMyResourcesTypeFromQueryParam,
  getSearchPageOptionsFromStorage,
  getSortOrderFromQueryParam,
  getSubStatusFromQueryParam,
  myResourcesTypeToItemStatus,
  PARAM_MYRESOURCES_TYPE,
  renderAllResources,
  sortOrderOptions,
  viewScrapbook,
} from "./MyResourcesPageHelper";

const {
  title,
  scrapbook: {
    addScrapbook,
    createFile,
    createPage,
    deleteDialogTitle,
    deleteDialogContent,
  },
} = languageStrings.myResources;

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
    // Data saved in browser history takes precedence over query params.
    // But if both are undefined, default to 'Published'.
    history.location.state?.customData?.["myResourcesType"] ??
      getMyResourcesTypeFromQueryParam(history.location) ??
      "Published",
  );

  // The sub-statuses refer to Item statuses that can be selected from Moderation queue and All resources.
  const [subStatus, setSubStatus] = useState<OEQ.Common.ItemStatus[]>(
    history.location.state?.searchPageOptions?.status ??
      getSubStatusFromQueryParam(history.location) ??
      [],
  );

  // Anchor used to control where and when to show the menu for creating new Scrapbook.
  const [anchorEl, setAnchorEl] = React.useState<HTMLElement | null>(null);

  // State to control whether to show a confirmation dialog for deleting a Scrapbook.
  // The 'string' stored here is actually a UUID of the scrapbook item to delete.
  const [scrapbookToBeDeleted, setScrapbookToBeDeleted] = useState<string>();

  const [lightBoxProps, setLightBoxProps] = useState<LightboxProps>();
  // Used to control whether to show a spinner while retrieving Scrapbook viewer configuration.
  const [showSpinner, setShowsSpinner] = useState(false);

  const initialSearchConfig = useMemo<InitialSearchConfig>(() => {
    const optionsFromStorage = getSearchPageOptionsFromStorage(
      history.location,
    );

    const customiseInitialSearchOptions = (
      searchPageOptions: SearchPageOptions,
    ): SearchPageOptions =>
      optionsFromStorage ?? {
        ...searchPageOptions,
        owner: currentUser,
        status: A.isNonEmpty(subStatus)
          ? subStatus
          : myResourcesTypeToItemStatus(resourceType),
        // The sort order in My resources initial search is a bit different. If no sort order
        // is present in either the browser history or query string, we use the custom default
        // sort order rather than the one configured in Search settings.
        sortOrder:
          history.location.state?.searchPageOptions?.sortOrder ??
          getSortOrderFromQueryParam(history.location) ??
          defaultSortOrder(resourceType),
      };

    return {
      ready: currentUser !== undefined,
      listInitialClassifications: true,
      customiseInitialSearchOptions,
    };
  }, [currentUser, history.location, resourceType, subStatus]);

  const customSearchCriteria = (
    myResourcesType: MyResourcesType,
  ): SearchPageOptions => ({
    ...defaultSearchPageOptions,
    owner: currentUser,
    status: myResourcesTypeToItemStatus(myResourcesType),
    sortOrder: defaultSortOrder(myResourcesType),
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

  const onScrapbookDelete = (
    uuid: string,
    {
      search,
      searchState: { options },
      searchPageErrorHandler,
    }: SearchContextProps,
  ) =>
    deleteScrapbook(uuid)
      .then(() => {
        search(options);
      })
      .catch(searchPageErrorHandler)
      .finally(() => setScrapbookToBeDeleted(undefined));

  // Return a function to render custom UI for the SearchResult in Scrapbook, Moderation queue
  // and All resources. For others, return undefined.
  const customSearchResultBuilder = ({
    searchState: { options },
  }: SearchContextProps):
    | ((searchResult: SearchPageSearchResult) => ReactNode)
    | undefined => {
    const renderScrapbook = buildRenderScrapbookResult(
      (key: string) => openLegacyFileEditingPage(key, history, options),
      setScrapbookToBeDeleted,
      (item) => {
        const openLightbox = ({ config }: ViewerLightboxConfig) =>
          setLightBoxProps({
            open: true,
            onClose: () => setLightBoxProps(undefined),
            config,
          });

        setShowsSpinner(true);
        viewScrapbook(item, openLightbox).finally(() => setShowsSpinner(false));
      },
    );

    switch (resourceType) {
      case "Scrapbook":
        return (result: SearchPageSearchResult) =>
          customUIForMyResources(
            result,
            ({
              results,
              highlight,
            }: OEQ.Search.SearchResult<OEQ.Search.SearchResultItem>) =>
              results.map((item) => renderScrapbook(item, highlight)),
          );
      case "Moderation queue":
        return (searchResult: SearchPageSearchResult) =>
          searchResult.from === "item-search" ? (
            <ModerationSearchResult result={searchResult.content} />
          ) : (
            <MessageInfo
              title="Sorry, that display type is not supported - list view only."
              variant="info"
              open
              onClose={() => {}}
            />
          );
      case "All resources":
        return (result: SearchPageSearchResult) =>
          customUIForMyResources(
            result,
            (items: OEQ.Search.SearchResult<OEQ.Search.SearchResultItem>) =>
              renderAllResources(items, renderScrapbook),
          );
      default:
        return undefined;
    }
  };

  const searchPageHeaderConfig: SearchPageHeaderConfig = {
    ...defaultSearchPageHeaderConfig,
    newSearchConfig: {
      criteria: customSearchCriteria(resourceType),
      callback: () => setSubStatus([]),
    },
    enableShareSearchButton: false,
    // Downloading CSV is a feature belonging to Search page so disable it in My resources.
    enableCSVExportButton: false,
    customSortingOptions: sortOrderOptions(resourceType),
    additionalHeaders:
      resourceType === "Scrapbook"
        ? [
            <TooltipIconButton
              title={addScrapbook}
              onClick={({ currentTarget }) => setAnchorEl(currentTarget)}
            >
              <AddIcon />
            </TooltipIconButton>,
          ]
        : undefined,
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

  /**
   * Customises the favourite URL by including a parameter to specify the `My Resources` type
   * so that it is persisted in the favourite.
   */
  const customFavouriteUrl = ({ path, params }: FavouriteURL): FavouriteURL => {
    const updatedParams = new URLSearchParams(params);
    updatedParams.set(PARAM_MYRESOURCES_TYPE, resourceType);
    updatedParams.sort();

    return {
      path,
      params: updatedParams,
    };
  };

  const searchPageRefinePanelConfig = (
    searchContextProps: SearchContextProps,
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
        resourceType,
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
          <>
            <SearchPageBody
              pathname={NEW_MY_RESOURCES_PATH}
              enableClassification={false}
              headerConfig={searchPageHeaderConfig}
              refinePanelConfig={searchPageRefinePanelConfig(
                searchContextProps,
              )}
              customRenderSearchResults={customSearchResultBuilder(
                searchContextProps,
              )}
              customFavouriteUrl={customFavouriteUrl}
              customShowSpinner={showSpinner}
            />
            {scrapbookToBeDeleted && (
              <ConfirmDialog
                open
                title={deleteDialogTitle}
                onCancel={() => setScrapbookToBeDeleted(undefined)}
                onConfirm={() =>
                  onScrapbookDelete(scrapbookToBeDeleted, searchContextProps)
                }
                confirmButtonText={languageStrings.common.action.ok}
              >
                {deleteDialogContent}
              </ConfirmDialog>
            )}
            <Menu
              open={anchorEl !== null}
              anchorEl={anchorEl}
              onClick={() => setAnchorEl(null)}
            >
              {pipe(
                [
                  ["file", createFile, <CloudUploadIcon />],
                  ["page", createPage, <PagesIcon />],
                ],
                A.map<
                  [ScrapbookType, string, React.JSX.Element],
                  React.JSX.Element
                >(([scrapbookType, text, icon]) => (
                  <MenuItem
                    key={scrapbookType}
                    onClick={() =>
                      openLegacyFileCreatingPage(
                        scrapbookType,
                        history,
                        searchContextProps.searchState.options,
                      )
                    }
                  >
                    <ListItemIcon>{icon}</ListItemIcon>
                    <ListItemText>{text}</ListItemText>
                  </MenuItem>
                )),
              )}
            </Menu>
            {lightBoxProps && <Lightbox {...lightBoxProps} />}
          </>
        )}
      </SearchContext.Consumer>
    </Search>
  );
};

export default MyResourcesPage;
