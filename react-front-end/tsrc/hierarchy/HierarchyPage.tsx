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
import { PushPin, PushPinOutlined } from "@mui/icons-material";
import { Grid } from "@mui/material";
import * as OEQ from "@openequella/rest-api-client";
import * as A from "fp-ts/Array";
import { pipe } from "fp-ts/function";
import * as O from "fp-ts/Option";
import * as TE from "fp-ts/TaskEither";
import * as React from "react";
import { ReactNode, useContext, useEffect, useMemo, useState } from "react";
import { TooltipIconButton } from "../components/TooltipIconButton";
import { AppContext } from "../mainui/App";
import { NEW_HIERARCHY_PATH } from "../mainui/routes";
import { TemplateUpdateProps } from "../mainui/Template";
import {
  addKeyResource,
  defaultHierarchyAcl,
  deleteKeyResource,
  getHierarchy,
  getMyAcls,
} from "../modules/HierarchyModule";
import GallerySearchResult from "../search/components/GallerySearchResult";
import SearchResult, {
  defaultActionButtonProps,
} from "../search/components/SearchResult";
import { InitialSearchConfig, Search } from "../search/Search";
import { SearchPageBody } from "../search/SearchPageBody";
import {
  isGalleryItems,
  isListItems,
  SearchContext,
  SearchContextProps,
  SearchPageOptions,
  SearchPageRefinePanelConfig,
} from "../search/SearchPageHelper";
import { SearchPageSearchResult } from "../search/SearchPageReducer";
import { languageStrings } from "../util/langstrings";
import HierarchyPanel from "./components/HierarchyPanel";
import HierarchyPanelSkeleton from "./components/HierarchyPanelSkeleton";
import KeyResourcePanel from "./components/KeyResourcePanel";
import KeyResourcePanelSkeleton from "./components/KeyResourcePanelSkeleton";

const {
  addKeyResource: addKeyResourceText,
  removeKeyResource: removeKeyResourceText,
  hierarchyPageTitle,
} = languageStrings.hierarchy;

const refinePanelConfig: SearchPageRefinePanelConfig = {
  enableDisplayModeSelector: true,
  enableCollectionSelector: false,
  enableAdvancedSearchSelector: false,
  enableDateRangeSelector: true,
  enableRemoteSearchSelector: false,
  enableMimeTypeSelector: true,
  enableOwnerSelector: true,
  enableSearchAttachmentsSelector: false,
  enableItemStatusSelector: false,
};

interface HierarchyPageProps extends TemplateUpdateProps {
  /**
   * Compound UUID of the Hierarchy topic to be displayed in this page.
   */
  compoundUuid: string;
}

/**
 * This component controls how to render Hierarchy page, including:
 * 1. getting details of the topic;
 * 2. getting the hierarchy ACLs configured for the topic;
 * 3. controlling how to display Hierarchy panel;
 * 4. controlling how to display and update key resources;
 * 5. preparing functions for customising search result list.
 */
const HierarchyPage = ({
  updateTemplate,
  compoundUuid,
}: HierarchyPageProps) => {
  const { appErrorHandler } = useContext(AppContext);

  const [hierarchy, setHierarchy] = useState<
    | OEQ.BrowseHierarchy.HierarchyTopic<OEQ.BrowseHierarchy.KeyResource>
    | undefined
  >();
  const [hierarchyAcls, setHierarchyAcls] =
    useState<OEQ.Hierarchy.HierarchyTopicAcl>(defaultHierarchyAcl);

  const [needUpdateHierarchy, setNeedUpdateHierarchy] = useState(true);
  const [showSearchResult, setShowSearchResult] = useState(false);

  // Get hierarchy
  useEffect(() => {
    if (needUpdateHierarchy) {
      pipe(
        TE.tryCatch(
          () => getHierarchy(compoundUuid),
          (e) => `Failed to get hierarchy: ${e}`,
        ),
        TE.match(appErrorHandler, (h) => {
          setHierarchy(h);
          setNeedUpdateHierarchy(false);
          setShowSearchResult(h.summary.showResults);
        }),
      )();
    }
  }, [appErrorHandler, compoundUuid, needUpdateHierarchy]);

  // Get hierarchy ACLs
  useEffect(() => {
    pipe(
      TE.tryCatch(
        () => getMyAcls(compoundUuid),
        (e) => `Failed to get hierarchy ACLs: ${e}`,
      ),
      TE.match(appErrorHandler, setHierarchyAcls),
    )();
  }, [appErrorHandler, compoundUuid]);

  /**
   * Add/Delete a key resource to the hierarchy and then update the `needUpdateHierarchy` flag.
   *
   * @param itemUuid the UUID of the item to be added/deleted.
   * @param itemVersion the version of the item to be added/deleted.
   * @param isDelete true if the item is to be deleted, false if the item is to be added.
   */
  const updateKeyResource = (
    itemUuid: OEQ.Common.UuidString,
    itemVersion: number,
    isDelete: boolean,
  ) => {
    const action = isDelete ? deleteKeyResource : addKeyResource;
    pipe(
      TE.tryCatch(
        () => action(compoundUuid, itemUuid, itemVersion),
        (e) => `Failed to update key resource: ${e}`,
      ),
      TE.match(appErrorHandler, (_) => setNeedUpdateHierarchy(true)),
    )();
  };

  const initialSearchConfig = useMemo<InitialSearchConfig>(() => {
    const customiseInitialSearchOptions = (
      searchPageOptions: SearchPageOptions,
    ): SearchPageOptions => ({
      ...searchPageOptions,
      filterExpansion: true,
      hierarchy: compoundUuid,
    });

    return {
      // Wait for the hierarchy query since it needs key resource info to render the search result (the pin button).
      ready: hierarchy !== undefined,
      listInitialClassifications: true,
      customiseInitialSearchOptions,
    };
  }, [compoundUuid, hierarchy]);

  /**
   * Generate a search result item with update key resource button.
   *
   * @param item the search result item to map over.
   * @param highlights a list of highlight terms.
   */
  const mapListItemResult = (
    item: OEQ.Search.SearchResultItem,
    highlights: string[],
  ): React.ReactNode => {
    const { uuid: itemUuid, version: itemVersion } = item;

    const isKeyResource = pipe(
      hierarchy?.keyResources ?? [],
      A.exists((resource) => {
        const { version, uuid } = resource.item;
        return itemUuid === uuid && itemVersion === version;
      }),
    );

    const title = isKeyResource ? removeKeyResourceText : addKeyResourceText;

    const updateKeyResourceButton = (
      <TooltipIconButton
        aria-label={title}
        title={title}
        onClick={() => updateKeyResource(itemUuid, itemVersion, isKeyResource)}
        size="small"
      >
        {isKeyResource ? <PushPin color="secondary" /> : <PushPinOutlined />}
      </TooltipIconButton>
    );

    return (
      <SearchResult
        key={`${itemUuid}/${itemVersion}`}
        item={item}
        highlights={highlights}
        customActionButtons={
          hierarchyAcls.MODIFY_KEY_RESOURCE
            ? [updateKeyResourceButton]
            : undefined
        }
        actionButtonConfig={{
          ...defaultActionButtonProps,
          showAddToHierarchy: false,
        }}
      />
    );
  };

  const customSearchResultBuilder = (
    searchResult: SearchPageSearchResult,
  ): ReactNode => {
    // If there is no hierarchy,then there is no search result that needs to be displayed.
    if (hierarchy === undefined) {
      return null;
    }

    const {
      from,
      content: { results: searchResults, highlight },
    } = searchResult;

    if (isListItems(from, searchResults)) {
      return pipe(
        searchResults,
        A.map((item) => mapListItemResult(item, highlight)),
      );
    } else if (isGalleryItems(from, searchResults)) {
      return <GallerySearchResult items={searchResults} />;
    }

    throw new TypeError("Unexpected display mode for hierarchy result");
  };

  return (
    <Search
      updateTemplate={updateTemplate}
      initialSearchConfig={initialSearchConfig}
      pageTitle={hierarchyPageTitle}
    >
      <Grid container spacing={2}>
        <Grid item xs={12}>
          {hierarchy ? (
            <HierarchyPanel hierarchy={hierarchy} />
          ) : (
            <HierarchyPanelSkeleton />
          )}
        </Grid>

        {pipe(
          O.fromNullable(hierarchy),
          O.fold(
            () => (
              <Grid item xs={12}>
                <KeyResourcePanelSkeleton />
              </Grid>
            ),
            (h) =>
              pipe(
                h.keyResources,
                O.fromPredicate(A.isNonEmpty),
                O.map((keyResources) => (
                  <Grid item xs={12}>
                    <KeyResourcePanel
                      keyResources={keyResources}
                      onPinIconClick={
                        hierarchyAcls.MODIFY_KEY_RESOURCE
                          ? ({ item }) =>
                              updateKeyResource(item.uuid, item.version, true)
                          : undefined
                      }
                    />
                  </Grid>
                )),
                O.toNullable,
              ),
          ),
        )}

        {showSearchResult && (
          <Grid item xs={12}>
            <SearchContext.Consumer>
              {(_: SearchContextProps) => (
                <SearchPageBody
                  pathname={`${NEW_HIERARCHY_PATH}/${compoundUuid}`}
                  headerConfig={{ enableCSVExportButton: false }}
                  enableClassification
                  refinePanelConfig={refinePanelConfig}
                  customRenderSearchResults={customSearchResultBuilder}
                  searchResultTitle={hierarchy?.summary.searchResultSectionName}
                />
              )}
            </SearchContext.Consumer>
          </Grid>
        )}
      </Grid>
    </Search>
  );
};

export default HierarchyPage;
