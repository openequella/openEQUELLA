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
import { absurd, flow, pipe, constant, identity } from "fp-ts/function";
import * as O from "fp-ts/Option";
import { Location } from "history";
import * as React from "react";
import { ReactNode } from "react";
import { Literal, match, Union, Unknown, when } from "runtypes";
import { TooltipIconButton } from "../components/TooltipIconButton";
import { nonDeletedStatuses } from "../modules/SearchModule";
import GallerySearchResult from "../search/components/GallerySearchResult";
import { SortOrderOptions } from "../search/components/SearchOrderSelect";
import SearchResult from "../search/components/SearchResult";
import { SearchPageSearchResult } from "../search/SearchPageReducer";
import { getISODateString } from "../util/Date";
import { languageStrings } from "../util/langstrings";
import { simpleMatch } from "../util/match";

export type MyResourcesType =
  | "Published"
  | "Drafts"
  | "Scrapbook"
  | "Moderation queue"
  | "Archive"
  | "All resources";

const ScrapbookLiteral = Literal("scrapbook");
const ModQueueLiteral = Literal("modqueue");

/**
 * Runtypes definition to represent the Legacy My resources types.
 */
export const LegacyMyResourcesRuntypes = Union(
  Literal("published"),
  Literal("draft"),
  ScrapbookLiteral,
  ModQueueLiteral,
  Literal("archived"),
  Literal("all")
);

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

// If query string 'searchOptions' exists in the given URL, return it in `Some`. Otherwise, return 'None'.
// This is mostly used to determine whether the URL is generated from New or Old UI.
const getSearchOptionsFromQueryParam = (location: Location): O.Option<string> =>
  pipe(
    location.search,
    O.fromNullable,
    O.chain((search) =>
      O.fromNullable(new URLSearchParams(search).get("searchOptions"))
    )
  );

// Get the Legacy My resources type from the given URL and then convert it to `MyResourcesType`.
const getMyResourcesTypeFromLegacyQueryParam = (
  location: Location
): MyResourcesType | undefined =>
  pipe(
    location.search,
    O.fromNullable,
    O.chain((search) =>
      O.fromNullable(new URLSearchParams(search).get("type"))
    ),
    O.filter(LegacyMyResourcesRuntypes.guard),
    O.map(
      LegacyMyResourcesRuntypes.match<MyResourcesType>(
        (published) => "Published",
        (draft) => "Drafts",
        (scrapbook) => "Scrapbook",
        (modqueue) => "Moderation queue",
        (archived) => "Archive",
        (all) => "All resources"
      )
    ),
    O.toUndefined
  );

/**
 * Given a URL generated from either Old UI or New UI, find out My resources type from query params.
 *
 * @param location The browser location which includes search query params.
 */
export const getMyResourcesTypeFromQueryParam = (
  location: Location
): MyResourcesType | undefined =>
  pipe(
    getSearchOptionsFromQueryParam(location),
    O.match(
      () => getMyResourcesTypeFromLegacyQueryParam(location),
      (_) => undefined // todo: support getting My resources type from query string. However, "myResourcesType" is not part of query string currently.
    )
  );

// Get Item status from query params of a URL generated from Old UI.
// Old UI uses 'mstatus' when the view is Moderation queue and uses 'status' when the view is All resources.
// And the two query strings can be both present in one URL. As a result, we need to firstly find out what
// My resources type is and then use the type to determine whether to use 'mstatus' or 'status'.
const getSubStatusFromLegacyQueryParam = (
  location: Location
): OEQ.Common.ItemStatus[] | undefined => {
  const getStatus = (
    params: URLSearchParams
  ): O.Option<OEQ.Common.ItemStatus[]> =>
    pipe(
      params.get("type"),
      O.fromNullable,
      O.filter(LegacyMyResourcesRuntypes.guard),
      O.map((t) => (t === "modqueue" ? "mstatus" : "status")),
      O.map((qs) => params.get(qs)),
      O.chain(O.fromNullable),
      O.map((status) => status.toUpperCase()),
      O.filter(OEQ.Common.ItemStatuses.guard),
      O.map((status) => [status])
    );

  return pipe(
    location.search,
    O.fromNullable,
    O.map((search) => new URLSearchParams(search)),
    O.chain(getStatus),
    O.toUndefined
  );
};

/**
 * Given a URL generated from either Old UI or New UI, find out Item status from query params and return it
 * as an array.
 *
 * @param location The browser location which includes search query params.
 */
export const getSubStatusFromQueryParam = (
  location: Location
): OEQ.Common.ItemStatus[] | undefined =>
  pipe(
    getSearchOptionsFromQueryParam(location),
    O.match(
      () => getSubStatusFromLegacyQueryParam(location),
      (_) => undefined // todo: support getting sub status from query string.
    )
  );

// Get sort order from query params of a URL generated from Old UI.
// Old UI uses 'modsort' when the view is Moderation queue,  uses 'sbsort' when the view is Scrapbook, and
// uses 'sort' for others. And the three query strings can be all present in one URL. As a result, we need
// to firstly find out what My resources type is and then use the type to determine which query string to
// use to get the sort order.
const getSortOrderFromLegacyQueryParam = (
  location: Location
): OEQ.Search.SortOrder | undefined => {
  const getSortOrder = (
    params: URLSearchParams
  ): O.Option<OEQ.Search.SortOrder> =>
    pipe(
      params.get("type"),
      O.fromNullable,
      O.filter(LegacyMyResourcesRuntypes.guard),
      O.map(
        match(
          when(ScrapbookLiteral, constant("sbsort")),
          when(ModQueueLiteral, constant("modsort")),
          when(Unknown, constant("sort"))
        )
      ),
      O.chain((queryString) => pipe(params.get(queryString), O.fromNullable)),
      // Need to translate the presentation values of Moderation specific sort orders to the real sorting values.
      O.map(
        simpleMatch({
          lastmod: constant("task_lastaction"),
          started: constant("task_submitted"),
          _: identity,
        })
      ),
      O.filter(OEQ.Search.SortOrderRunTypes.guard)
    );

  return pipe(
    location.search,
    O.fromNullable,
    O.map((search) => new URLSearchParams(search)),
    O.chain(getSortOrder),
    O.toUndefined
  );
};

/**
 * Given a URL generated from either Old UI or New UI, find out the sort order from query params.
 *
 * @param location The browser location which includes search query params.
 */
export const getSortOrderFromQueryParam = (
  location: Location
): OEQ.Search.SortOrder | undefined =>
  pipe(
    getSearchOptionsFromQueryParam(location),
    O.match(
      () => getSortOrderFromLegacyQueryParam(location),
      (_) => undefined // todo: Sort order should come from query string 'searchOptions' for free before this function is called.
    )
  );
/**
 * Given a specific `MyResourceType` build the SortOrderOptions representing the options used in
 * UI for sorting in the related view.
 *
 * @param resourceType the type of resource to generate the options for
 */
export const sortOrderOptions = (
  resourceType: MyResourcesType
): SortOrderOptions => {
  const {
    dateCreated,
    lastAction,
    lastModified,
    relevance,
    submitted,
    title,
    userRating,
  } = languageStrings.myResources.sortOptions;

  switch (resourceType) {
    case "Moderation queue":
      return new Map<OEQ.Search.SortOrder, string>([
        ["task_submitted", submitted],
        ["task_lastaction", lastAction],
        ["name", title],
        ["datemodified", lastModified],
        ["datecreated", dateCreated],
      ]);
    case "Scrapbook":
      return new Map<OEQ.Search.SortOrder, string>([
        ["datemodified", lastModified],
        ["datecreated", dateCreated],
        ["name", title],
      ]);
    default:
      return new Map<OEQ.Search.SortOrder, string>([
        ["rank", relevance],
        ["datemodified", lastModified],
        ["datecreated", dateCreated],
        ["name", title],
        ["rating", userRating],
      ]);
  }
};

/**
 * Return the default sort order for My resources page, depending on the specific `resourceType`.
 */
export const defaultSortOrder = (
  resourceType: MyResourcesType
): OEQ.Search.SortOrder =>
  resourceType === "Moderation queue" ? "task_submitted" : "datemodified";

/**
 * Type definition for functions that render custom UI for SearchResult in My resources page.
 */
export type RenderFunc = (
  item: SearchResultItem,
  highlight: string[]
) => JSX.Element;

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
  (item: OEQ.Search.SearchResultItem, highlight: string[]) => {
    const { uuid, version } = item;
    const key = `${uuid}/${version}`;

    return (
      <SearchResult
        key={key}
        item={item}
        highlights={highlight}
        customActionButtons={[
          <TooltipIconButton
            title={languageStrings.common.action.edit}
            onClick={() => onEdit(key)}
          >
            <EditIcon />
          </TooltipIconButton>,
          <TooltipIconButton
            title={languageStrings.common.action.delete}
            onClick={() => onDelete(uuid)}
          >
            <DeleteIcon />
          </TooltipIconButton>,
        ]}
      />
    );
  };

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
