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
import { useParams } from "react-router-dom";
import { TooltipIconButton } from "../components/TooltipIconButton";
import { AppContext } from "../mainui/App";
import { HIERARCHY_PATH } from "../mainui/routes";
import { TemplateUpdateProps } from "../mainui/Template";
import {
  addKeyResource,
  defaultHierarchyAcl,
  deleteKeyResource,
  getHierarchy,
  getMyAcls,
} from "../modules/HierarchyModule";
import { itemEq } from "../modules/SearchModule";
import SearchResult from "../search/components/SearchResult";
import { InitialSearchConfig, Search } from "../search/Search";
import { SearchPageBody } from "../search/SearchPageBody";
import {
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

const HierarchyPage = ({ updateTemplate }: TemplateUpdateProps) => {
  const { appErrorHandler } = useContext(AppContext);
  const { compoundUuid } = useParams<{
    compoundUuid: string;
  }>();

  const [hierarchy, setHierarchy] = useState<
    OEQ.BrowseHierarchy.HierarchyTopic<OEQ.Search.SearchResultItem> | undefined
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
   * @param item the search result item to add/delete as a key resource.
   * @param isDelete true if the item is to be deleted, false if the item is to be added.
   */
  const updateKeyResource = (
    item: OEQ.Search.SearchResultItem,
    isDelete: boolean,
  ) => {
    const action = isDelete ? deleteKeyResource : addKeyResource;
    pipe(
      TE.tryCatch(
        () => action(compoundUuid, item.uuid, item.version),
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
  const mapSearchResultItem = (
    item: OEQ.Search.SearchResultItem,
    highlights: string[],
  ): React.ReactNode => {
    const isKeyResource = pipe(
      hierarchy?.keyResources ?? [],
      A.exists((resource) => itemEq.equals(item, resource)),
    );

    const title = isKeyResource ? removeKeyResourceText : addKeyResourceText;

    const updateKeyResourceButton = (
      <TooltipIconButton
        aria-label={title}
        title={title}
        onClick={() => updateKeyResource(item, isKeyResource)}
        size="small"
      >
        {isKeyResource ? <PushPin color="secondary" /> : <PushPinOutlined />}
      </TooltipIconButton>
    );

    return (
      <SearchResult
        key={`${item.uuid}/${item.version}`}
        item={item}
        highlights={highlights}
        customActionButtons={
          hierarchyAcls.MODIFY_KEY_RESOURCE
            ? [updateKeyResourceButton]
            : undefined
        }
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

    const isItems = (items: unknown): items is OEQ.Search.SearchResultItem[] =>
      from === "item-search";

    if (isItems(searchResults)) {
      return pipe(
        searchResults,
        A.map((item) => mapSearchResultItem(item, highlight)),
      );
    }

    //TODO: ADD image and video gallery mode

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
                      items={keyResources}
                      onPinIconClick={
                        hierarchyAcls.MODIFY_KEY_RESOURCE
                          ? (item) => updateKeyResource(item, true)
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
                  pathname={HIERARCHY_PATH}
                  headerConfig={{
                    enableCSVExportButton: false,
                    enableShareSearchButton: false,
                  }}
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
