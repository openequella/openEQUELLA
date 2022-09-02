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
import DeleteIcon from "@material-ui/icons/Delete";
import EditIcon from "@material-ui/icons/Edit";
import * as OEQ from "@openequella/rest-api-client";
import { SearchResultItem } from "@openequella/rest-api-client/dist/Search";
import { absurd, flow, pipe } from "fp-ts/function";
import * as O from "fp-ts/Option";
import { Location } from "history";
import { ReactNode } from "react";
import * as React from "react";
import { TooltipIconButton } from "../components/TooltipIconButton";
import { nonDeletedStatuses } from "../modules/SearchModule";
import { getISODateString } from "../util/Date";
import { languageStrings } from "../util/langstrings";
import { simpleMatch } from "../util/match";
import GallerySearchResult from "../search/components/GallerySearchResult";
import SearchResult from "../search/components/SearchResult";
import { SearchPageSearchResult } from "../search/SearchPageReducer";

export type MyResourcesType =
  | "Published"
  | "Drafts"
  | "Scrapbook"
  | "Moderation queue"
  | "Archive"
  | "All resources";

/**
 * Return a list of Item status that match the given MyResources type.
 *
 * @param resourceType MyResources type that may represent one or multiple Item statuses.
 */
export const myResourcesTypeToItemStatus = (
  resourceType: MyResourcesType
): OEQ.Common.ItemStatus[] => {
  switch (resourceType) {
    case "Published":
      return ["LIVE", "REVIEW"];
    case "Drafts":
      return ["DRAFT"];
    case "Scrapbook":
      return ["PERSONAL"];
    case "Moderation queue":
      return ["MODERATING", "REJECTED", "REVIEW"];
    case "Archive":
      return ["ARCHIVED"];
    case "All resources":
      return nonDeletedStatuses;
    default:
      return absurd(resourceType);
  }
};

export const getMyResourcesTypeFromLegacyQueryParam = (
  location: Location
): MyResourcesType | undefined =>
  pipe(
    location.search,
    O.fromNullable,
    O.chain((search) =>
      O.fromNullable(new URLSearchParams(search).get("type"))
    ),
    O.map(
      simpleMatch<MyResourcesType>({
        published: () => "Published",
        draft: () => "Drafts",
        scrapbook: () => "Scrapbook",
        modqueue: () => "Moderation queue",
        archived: () => "Archive",
        all: () => "All resources",
        _: (resourcesType) => {
          throw new TypeError(
            `Unknown Legacy My resources type [${resourcesType}]`
          );
        },
      })
    ),
    O.toUndefined
  );

/**
 * Type definition for functions that render custom UI for SearchResult in My resources page.
 */
export type RenderFunc = (
  item: SearchResultItem,
  highlight: string[]
) => JSX.Element;

/**
 * Return the default sort order for My resources page.
 * todo: Return "Submitted" for Moderation queue when working on OEQ-1343.
 */
export const defaultSortOrder = (_: MyResourcesType): OEQ.Search.SortOrder =>
  "datemodified";

/**
 * Function to render a standard SearchResult.
 *
 * @param item A standard search result Item.
 * @param highlight A list of keywords to be highlighted.
 */
export const renderStandardResult: RenderFunc = (
  item: SearchResultItem,
  highlight: string[]
) => (
  <SearchResult
    key={`${item.uuid}/${item.version}`}
    item={item}
    highlights={highlight}
  />
);

/**
 * Function to render custom UI for Items in moderation. The UI is a standard SearchResult
 * with the Item submitted date being displayed as a Display Field.
 *
 * @param item A standard search result Item and its status must be 'moderating'.
 * @param highlight A list of keywords to be highlighted.
 */
export const renderModeratingResult: RenderFunc = (
  item: OEQ.Search.SearchResultItem,
  highlight: string[]
) => {
  const { uuid, version, displayFields, moderationDetails } = item;

  return pipe(
    moderationDetails,
    O.fromNullable,
    O.chain(
      flow(
        ({ submittedDate }) => getISODateString(submittedDate),
        O.fromNullable
      )
    ),
    O.map((submittedDate) => ({
      ...item,
      displayFields: [
        ...displayFields,
        {
          type: "node",
          name: languageStrings.myResources.moderating.since,
          html: submittedDate,
        },
      ],
    })),
    O.getOrElse(() => item),
    (processedItem) => (
      <SearchResult
        key={`${uuid}/${version}`}
        item={processedItem}
        highlights={highlight}
      />
    )
  );
};

/**
 * Function taking Scrapbook event handlers and returning a new function to render custom UI for Scrapbook.
 *
 * The custom UI is a standard SearchResult with two action buttons for deleting and editing Scrapbook.
 *
 * @param onEdit Handler for editing the Scrapbook.
 * @param onDelete Handler for deleting the Scrapbook.
 */
export const buildRenderScrapbookResult =
  (
    onEdit: (uuid: string) => void,
    onDelete: (uuid: string) => void
  ): RenderFunc =>
  (item: OEQ.Search.SearchResultItem, highlight: string[]) =>
    (
      <SearchResult
        key={`${item.uuid}/${item.version}`}
        item={item}
        highlights={highlight}
        customActionButtons={[
          <TooltipIconButton
            title={languageStrings.common.action.edit}
            onClick={() => onEdit(item.uuid)}
          >
            <EditIcon />
          </TooltipIconButton>,
          <TooltipIconButton
            title={languageStrings.common.action.delete}
            onClick={() => onDelete(item.uuid)}
          >
            <DeleteIcon />
          </TooltipIconButton>,
        ]}
      />
    );

/**
 * Function to render a list of Item displayed in All resources.
 *
 * For Scrapbooks, use the provided function to render each SearchResult because the interactions with
 * Scrapbook involves API calls and React state update.
 *
 * For Items where status is 'moderating',use the function 'moderatingSearchResult' to display the submitted date
 * as a Display Field.
 *
 * For Items in other statuses, render the standard SearchResult.
 *
 * @param results A List of standard search result Items.
 * @param highlight A list of keywords to be highlighted.
 * @param renderScrapbook Function provided to render a customised SearchResult for Scrapbook
 */
export const renderAllResources = (
  { results, highlight }: OEQ.Search.SearchResult<OEQ.Search.SearchResultItem>,
  renderScrapbook: RenderFunc
): ReactNode =>
  results.map((item) =>
    pipe(
      item.status,
      simpleMatch({
        personal: () => renderScrapbook,
        moderating: () => renderModeratingResult,
        _: () => renderStandardResult,
      }),
      (renderFunc) => renderFunc(item, highlight)
    )
  );

/**
 * Function to render custom UI for My resources views where the list of Search result Items needs UI customisation.
 * Each of these views must supply their own functions for the UI customisation.
 *
 * Galleries in My resources currently does not need UI customisation.
 *
 * @param searchPageSearchResult The list of Search result Items which is to be rendered in either the standard list or in the galleries.
 * @param renderList Function supplied to render custom UI for Search result Items in a specific My resources view.
 */
export const customUIForMyResources = (
  searchPageSearchResult: SearchPageSearchResult,
  renderList: (
    searchResult: OEQ.Search.SearchResult<OEQ.Search.SearchResultItem>
  ) => ReactNode
): ReactNode =>
  searchPageSearchResult.from === "gallery-search" ? (
    <GallerySearchResult items={searchPageSearchResult.content.results} />
  ) : (
    renderList(searchPageSearchResult.content)
  );
