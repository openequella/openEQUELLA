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
import DeleteIcon from "@mui/icons-material/Delete";
import EditIcon from "@mui/icons-material/Edit";
import * as OEQ from "@openequella/rest-api-client";
import { SearchResultItem } from "@openequella/rest-api-client/dist/Search";
import * as A from "fp-ts/Array";
import * as E from "fp-ts/Either";
import { absurd, constant, flow, identity, pipe } from "fp-ts/function";
import * as J from "fp-ts/Json";
import * as O from "fp-ts/Option";
import * as R from "fp-ts/Record";
import * as S from "fp-ts/string";
import * as TE from "fp-ts/TaskEither";
import { Location } from "history";
import * as t from "io-ts";
import { MD5 } from "object-hash";
import * as React from "react";
import { ReactNode } from "react";
import { getBaseUrl } from "../AppConfig";
import { TooltipIconButton } from "../components/TooltipIconButton";
import { buildStorageKey } from "../modules/BrowserStorageModule";
import {
  LegacyMyResourcesCodec,
  LegacyMyResourcesTypes,
} from "../modules/LegacyContentModule";
import { getMimeTypeDefaultViewerDetails } from "../modules/MimeTypesModule";
import {
  nonDeletedStatuses,
  searchItemAttachments,
} from "../modules/SearchModule";
import {
  buildViewerConfigForAttachments,
  isViewerLightboxConfig,
  ViewerConfig,
  ViewerLightboxConfig,
  ViewerLinkConfig,
} from "../modules/ViewerModule";
import GallerySearchResult from "../search/components/GallerySearchResult";
import { SortOrderOptions } from "../search/components/SearchOrderSelect";
import SearchResult, {
  defaultActionButtonProps,
} from "../search/components/SearchResult";
import {
  DehydratedSearchPageOptions,
  DehydratedSearchPageOptionsCodec,
  SearchPageOptions,
} from "../search/SearchPageHelper";
import { SearchPageSearchResult } from "../search/SearchPageReducer";
import { DateRangeFromString, getISODateString } from "../util/Date";
import { languageStrings } from "../util/langstrings";
import { simpleMatch, simpleUnionMatch } from "../util/match";
import { pfSplitAt, pfTernaryTypeGuard } from "../util/pointfree";

export const PARAM_MYRESOURCES_TYPE = "myResourcesType";

export const MyResourcesTypeRuntypes = t.union([
  t.literal("Published"),
  t.literal("Drafts"),
  t.literal("Scrapbook"),
  t.literal("Moderation queue"),
  t.literal("Archive"),
  t.literal("All resources"),
]);

export type MyResourcesType = t.TypeOf<typeof MyResourcesTypeRuntypes>;

/**
 * Return a list of Item status that match the given MyResources type.
 *
 * @param resourceType MyResources type that may represent one or multiple Item statuses.
 */
export const myResourcesTypeToItemStatus = (
  resourceType: MyResourcesType,
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

const buildURLSearchParams = (location: Location): O.Option<URLSearchParams> =>
  pipe(
    location.search,
    O.fromNullable,
    O.map((search) => new URLSearchParams(search)),
  );

const getParamFromLocation = (
  location: Location,
  queryString: string,
): O.Option<string> =>
  pipe(
    location,
    buildURLSearchParams,
    O.chain((params) => O.fromNullable(params.get(queryString))),
  );

// Get Legacy My resources type from query string. Invalid types will be logged in the console.
const getLegacyMyResourceType = (
  params: URLSearchParams,
): O.Option<LegacyMyResourcesTypes> =>
  pipe(
    params.get("type"),
    O.fromNullable,
    O.chainEitherK(
      flow(
        E.fromPredicate(
          LegacyMyResourcesCodec.is,
          (value) => `Invalid legacy my resources type: ${value}`,
        ),
        E.mapLeft(console.error),
      ),
    ),
  );

// If query string 'searchOptions' exists in the given URL, return it in `Some`. Otherwise, return 'None'.
const getSearchOptionsFromQueryParam = (
  location: Location,
): O.Option<DehydratedSearchPageOptions> => {
  const stringToDate: (dateString?: string) => O.Option<Date> = flow(
    O.fromNullable,
    O.map((dateString) => new Date(dateString)),
    O.filter((date) => !isNaN(date.getDate())),
  );

  // If field 'lastModifiedDateRange' exists in the provided JSON object, try to convert its value to a date range.
  // If the conversion fails or the field does not exist, return the original object.
  const processLastModifiedDateRange = (json: {
    [key: string]: unknown;
  }): { [key: string]: unknown } =>
    pipe(
      json,
      R.lookup("lastModifiedDateRange"),
      O.filter(DateRangeFromString.is),
      O.map(R.filterMap(stringToDate)),
      O.fold(
        () => json,
        (lastModifiedDateRange) => ({ ...json, lastModifiedDateRange }),
      ),
    );

  // Validate the parsed JSON which is expected to be a 'DehydratedSearchPageOptions'.
  const validateParsedObject: (
    data: J.Json,
  ) => E.Either<string | t.Errors, DehydratedSearchPageOptions> = flow(
    t.UnknownRecord.decode, // Type of the parsed result should be a record where keys and values are unknown.
    E.map(processLastModifiedDateRange),
    E.filterOrElseW(
      DehydratedSearchPageOptionsCodec.is,
      constant(
        "Parsed searchOptions is not a DehydratedSearchPageOptions - failed type check",
      ),
    ),
  );

  return pipe(
    getParamFromLocation(location, "searchOptions"),
    O.chainEitherK(
      flow(
        J.parse,
        E.mapLeft((error) => `Failed to parse searchOptions: ${error}`),
        E.chain(validateParsedObject),
        E.mapLeft(console.error),
      ),
    ),
  );
};

// Get the Legacy My resources type from the given params `MyResourcesType`.
const getMyResourcesTypeFromLegacyQueryParam = (
  params: URLSearchParams,
): O.Option<MyResourcesType> =>
  pipe(
    params,
    getLegacyMyResourceType,
    O.map(
      simpleUnionMatch<LegacyMyResourcesTypes, MyResourcesType>({
        published: constant("Published"),
        draft: constant("Drafts"),
        scrapbook: constant("Scrapbook"),
        modqueue: constant("Moderation queue"),
        archived: constant("Archive"),
        all: constant("All resources"),
        defaultValue: constant("Published"),
      }),
    ),
  );

// Get the 'My Resources' type from the given params `MyResourcesType`.
const getMyResourcesTypeFromNewUIQueryParam = (
  params: URLSearchParams,
): O.Option<MyResourcesType> =>
  pipe(
    params.get(PARAM_MYRESOURCES_TYPE),
    O.fromPredicate(MyResourcesTypeRuntypes.is),
  );

/**
 * Given a URL generated from either Old UI or New UI, find out My resources type from query params.
 *
 * @param location The browser location which includes search query params.
 */
export const getMyResourcesTypeFromQueryParam = (
  location: Location,
): MyResourcesType | undefined =>
  pipe(
    buildURLSearchParams(location),
    O.chain((params) =>
      pipe(
        getMyResourcesTypeFromNewUIQueryParam(params),
        O.alt(() => getMyResourcesTypeFromLegacyQueryParam(params)),
      ),
    ),
    O.toUndefined,
  );

// Get Item status from query params of a URL generated from Old UI.
// Old UI uses 'mstatus' when the view is Moderation queue and uses 'status' when the view is All resources.
// And the two query strings can be both present in one URL. As a result, we need to firstly find out what
// My resources type is and then use the type to determine whether to use 'mstatus' or 'status'.
const getSubStatusFromLegacyQueryParam = (
  location: Location,
): OEQ.Common.ItemStatus[] | undefined => {
  const getStatus = (params: URLSearchParams) =>
    pipe(
      params,
      getLegacyMyResourceType,
      O.map((t) => (t === "modqueue" ? "mstatus" : "status")),
      O.map((qs) => params.get(qs)),
      O.chain(O.fromNullable),
      O.map(S.toUpperCase),
      O.chainEitherK(
        flow(
          E.fromPredicate(
            OEQ.Codec.Common.ItemStatusCodec.is,
            (value) => `Invalid Item status: ${value}`,
          ),
          E.mapLeft(console.error),
        ),
      ),
      O.map((status) => [status]),
    );

  return pipe(
    buildURLSearchParams(location),
    O.chain(getStatus),
    O.toUndefined,
  );
};

/**
 * Given a URL generated from either Old UI or New UI, find out Item status from query params and return it
 * as an array.
 *
 * @param location The browser location which includes search query params.
 */
export const getSubStatusFromQueryParam = (
  location: Location,
): OEQ.Common.ItemStatus[] | undefined =>
  pipe(
    getSearchOptionsFromQueryParam(location),
    O.match(
      () => getSubStatusFromLegacyQueryParam(location),
      (options) => options.status,
    ),
  );

// Get sort order from query params of a URL generated from Old UI.
// Old UI uses 'modsort' when the view is Moderation queue,  uses 'sbsort' when the view is Scrapbook, and
// uses 'sort' for others. And the three query strings can be all present in one URL. As a result, we need
// to firstly find out what My resources type is and then use the type to determine which query string to
// use to get the sort order.
const getSortOrderFromLegacyQueryParam = (
  location: Location,
): OEQ.Search.SortOrder | undefined => {
  const getSortOrder = (
    params: URLSearchParams,
  ): O.Option<OEQ.Search.SortOrder> =>
    pipe(
      params,
      getLegacyMyResourceType,
      O.map(
        simpleUnionMatch<LegacyMyResourcesTypes, string>(
          {
            modqueue: constant("modsort"),
            scrapbook: constant("sbsort"),
          },
          constant("sort"),
        ),
      ),
      O.chain((queryString) => pipe(params.get(queryString), O.fromNullable)),
      // Need to translate the presentation values of Moderation specific sort orders to the real sorting values.
      O.map(
        simpleMatch({
          lastmod: constant("task_lastaction"),
          started: constant("task_submitted"),
          _: identity,
        }),
      ),
      O.chainEitherK(
        flow(
          E.fromPredicate(
            OEQ.Codec.Search.SortOrderCodec.is,
            (value) => `Invalid sort order: ${value}`,
          ),
          E.mapLeft(console.error),
        ),
      ),
    );

  return pipe(
    buildURLSearchParams(location),
    O.chain(getSortOrder),
    O.toUndefined,
  );
};

/**
 * Given a URL generated from either Old UI or New UI, find out the sort order from query params.
 *
 * @param location The browser location which includes search query params.
 */
export const getSortOrderFromQueryParam = (
  location: Location,
): OEQ.Search.SortOrder | undefined =>
  pipe(
    getSearchOptionsFromQueryParam(location),
    O.match(
      () => getSortOrderFromLegacyQueryParam(location),
      (options) => options.sortOrder,
    ),
  );
/**
 * Given a specific `MyResourceType` build the SortOrderOptions representing the options used in
 * UI for sorting in the related view.
 *
 * @param resourceType the type of resource to generate the options for
 */
export const sortOrderOptions = (
  resourceType: MyResourcesType,
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
  resourceType: MyResourcesType,
): OEQ.Search.SortOrder =>
  resourceType === "Moderation queue" ? "task_submitted" : "datemodified";

/**
 * Type definition for functions that render custom UI for SearchResult in My resources page.
 */
export type RenderFunc = (
  item: SearchResultItem,
  highlight: string[],
) => JSX.Element;

/**
 * Function to render a standard SearchResult.
 *
 * @param item A standard search result Item.
 * @param highlight A list of keywords to be highlighted.
 */
export const renderStandardResult: RenderFunc = (
  item: SearchResultItem,
  highlight: string[],
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
  highlight: string[],
) => {
  const { uuid, version, displayFields, moderationDetails } = item;

  return pipe(
    moderationDetails,
    O.fromNullable,
    O.chain(
      flow(
        ({ submittedDate }) => getISODateString(submittedDate),
        O.fromNullable,
      ),
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
    ),
  );
};

/**
 * Function taking Scrapbook event handlers and returning a new function to render custom UI for Scrapbook.
 *
 * The custom UI is a standard SearchResult with two action buttons for deleting and editing Scrapbook.
 *
 * @param onEdit Handler for editing the Scrapbook.
 * @param onDelete Handler for deleting the Scrapbook.
 * @param onClickTitle Handler for clicking the Scrapbook title.
 */
export const buildRenderScrapbookResult =
  (
    onEdit: (uuid: string) => void,
    onDelete: (uuid: string) => void,
    onClickTitle: (item: OEQ.Search.SearchResultItem) => void,
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
        customOnClickTitleHandler={() => onClickTitle(item)}
        actionButtonConfig={{
          ...defaultActionButtonProps,
          showAddToHierarchy: false,
        }}
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
  renderScrapbook: RenderFunc,
): ReactNode =>
  results.map((item) =>
    pipe(
      item.status,
      simpleMatch({
        personal: () => renderScrapbook,
        moderating: () => renderModeratingResult,
        _: () => renderStandardResult,
      }),
      (renderFunc) => renderFunc(item, highlight),
    ),
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
    searchResult: OEQ.Search.SearchResult<OEQ.Search.SearchResultItem>,
  ) => ReactNode,
): ReactNode =>
  searchPageSearchResult.from === "gallery-search" ? (
    <GallerySearchResult items={searchPageSearchResult.content.results} />
  ) : (
    renderList(searchPageSearchResult.content)
  );

/**
 * Return a SearchPageOptions saved in browser session storage by steps listed below.
 *
 * 1. Gets a UUID from query string `newUIStateId`,
 * 2. Use the UUID as a key get the string value from session storage. The value should consist of two parts: a MD5 hash and a JSON string
 * from which the hash was generated.
 * 3. Since the MD5 hash has a fixed length (32 chars), split the string into two strings.
 * 4. Generate a new hash from the second string and compare it with the first string. If the two hashes are identical, the JSON string is
 * considered valid.
 * 5. Parse the JSON string.
 * 6. Check the parsed object. If it's an object and field `dateRangeQuickModeEnabled` exists in the object, which is mandatory in type
 * `SearchPageOptions`, the object is considered as a SearchPageOptions.
 * 7. All errors captured during above steps will be logged to console.
 *
 * NOTE: The use of the MD5 hash is to provide a lightweight runtime type validation for the data. It is deemed short-lived data and so
 * there's no need for full validation - just checks against corruption and tampering.
 *
 * @param location The browser location which includes search query params.
 */
export const getSearchPageOptionsFromStorage = (
  location: Location,
): SearchPageOptions | undefined => {
  const checkHash = ([hash, value]: [string, string]): E.Either<
    string,
    string
  > =>
    pipe(
      value,
      E.fromPredicate(
        (v) => hash === MD5(v),
        constant("SearchPageOptions hash check failed"),
      ),
    );

  const parse = flow(
    J.parse,
    E.mapLeft((error) => `Failed to parse data due to ${error}`),
    // Also convert the date strings to Date objects.
    E.map((maybeOptions) =>
      OEQ.Utils.convertDateFields(maybeOptions, ["start", "end"]),
    ),
  );

  return pipe(
    getParamFromLocation(location, "newUIStateId"),
    O.map(buildStorageKey),
    O.chain((key) => O.fromNullable(window.sessionStorage.getItem(key))),
    O.map(pfSplitAt(32)),
    O.chainEitherK(
      flow(
        checkHash,
        E.chain(parse),
        E.filterOrElse(
          (json: unknown): json is SearchPageOptions =>
            json !== null &&
            typeof json === "object" &&
            "dateRangeQuickModeEnabled" in json,
          constant("The parsed object is not SearchPageOptions"),
        ),
        E.mapLeft(console.error),
      ),
    ),
    O.toUndefined,
  );
};

/**
 * Build a TaskEither to retrieve viewer configuration for the provided Scrapbook. If the Scrapbook
 * has multiple attachments, only use the first attachment's viewer configuration.
 *
 * @param item Details of the scrapbook item for which to retrieve viewer configuration.
 */
export const getScrapbookViewerConfig = (
  item: OEQ.Search.SearchResultItem,
): TE.TaskEither<string, O.Option<ViewerConfig>> => {
  const { uuid, version } = item;

  return pipe(
    TE.tryCatch(
      () => searchItemAttachments(uuid, version),
      (error) => `Failed to search attachments: ${error}`,
    ),
    TE.chainOptionK(() => `Scrapbook ${uuid} does not have any attachment`)(
      A.head,
    ),
    TE.chain((attachment) =>
      TE.tryCatch(
        () =>
          buildViewerConfigForAttachments(
            item,
            [attachment],
            getMimeTypeDefaultViewerDetails,
          ),
        (error) => `Failed to build viewer configuration: ${error}`,
      ),
    ),
    TE.map(
      flow(
        A.head,
        O.chain(({ viewerConfig }) => O.fromNullable(viewerConfig)),
      ),
    ),
  );
};

/**
 * Function to view Scrapbook, which essentially is to view the Scrapbook's attachments.
 *
 * As the thumbnail detail is generated from the first viewable attachment, we can use `attachmentType`, which is part
 * of the thumbnail detail, to determine whether the Scrapbook is a file or a Webpage.
 *
 * If the attachment type is `html` the Scrapbook must be a Webpage. So we construct the Webpage specific URL and open
 * the page in a new tab.
 *
 * For other attachment types, we firstly need to get the viewer configuration, and then depending on the configuration
 * we either show the attachment in Lightbox or open a new tab.
 *
 * @param item Details of the scrapbook item to be viewed.
 * @param viewInLightbox Function to show the Scrapbook in Lightbox.
 */
export const viewScrapbook = async (
  item: OEQ.Search.SearchResultItem,
  viewInLightbox: (config: ViewerLightboxConfig) => void,
): Promise<void> => {
  const { uuid, version, thumbnailDetails } = item;
  const viewInNewTab = (url: string) => window.open(url, "_blank");
  const viewFileScrapbook = async () =>
    pipe(
      await getScrapbookViewerConfig(item)(),
      E.fold(
        console.error,
        flow(
          O.map(
            pfTernaryTypeGuard(
              isViewerLightboxConfig,
              viewInLightbox,
              ({ url }: ViewerLinkConfig) => {
                viewInNewTab(url);
              },
            ),
          ),
        ),
      ),
    );

  thumbnailDetails?.attachmentType === "html"
    ? viewInNewTab(`${getBaseUrl()}items/${uuid}/${version}/viewpages.jsp`)
    : await viewFileScrapbook();
};
