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
import { stringify } from 'query-string';
import { GET, HEAD, POST } from './AxiosInstance';
import type { i18nString, ItemStatus } from './Common';
import { SearchResultCodec, SearchResultItemRawCodec } from './gen/Search';
import { convertDateFields, STANDARD_DATE_FIELDS, validate } from './Utils';

/**
 * Used for specifying must expressions such as `moderating:true`. Neither string should contain
 * any colons (or other exempt Lucene syntax characters).
 */
export type Must = [string, string[]];

export type SortOrder =
  | 'rank'
  | 'datemodified'
  | 'datecreated'
  | 'name'
  | 'rating'
  | 'task_lastaction'
  | 'task_submitted'
  | 'added_at';

interface SearchParamsBase {
  /**
   * Query string.
   */
  query?: string;
  /**
   * The first record of the search results to return.
   */
  start?: number;
  /**
   * The number of results to return.
   */
  length?: number;
  /**
   * List of collections.
   */
  collections?: string[];
  /**
   * The order of the search results.
   */
  order?: SortOrder;
  /**
   * Reverse the order of the search results.
   */
  reverseOrder?: boolean;
  /**
   * An advanced search UUID.
   */
  advancedSearch?: string;
  /**
   * A where clause.
   */
  whereClause?: string;
  /**
   * Item status.
   */
  status?: ItemStatus[];
  /**
   * A date before which items are modified. Date format (yyyy-MM-dd).
   */
  modifiedBefore?: string;
  /**
   * A date after which items are modified. Date format (yyyy-MM-dd).
   */
  modifiedAfter?: string;
  /**
   * An ID of a user.
   */
  owner?: string;
  /**
   * single dynamic collection uuid.
   */
  dynaCollection?: string;
  /**
   * Whether to include full attachment details in results. Including attachments incurs extra
   * processing and can slow down response times.
   */
  includeAttachments?: boolean;
  /**
   * A flag indicating whether to search attachments or not.
   */
  searchAttachments?: boolean;
  /**
   * List of MIME types to filter by.
   */
  mimeTypes?: string[];
  /**
   * Custom query in Lucene syntax.
   */
  customLuceneQuery?: string;
  /**
   * The UUID of a hierarchy topic.
   */
  hierarchy?: string;
}

/**
 * Type of query parameters that can be used in a search.
 */
export interface SearchParams extends SearchParamsBase {
  /**
   * List of search index key/value pairs to filter by. e.g. videothumb:true or realthumb:true.
   * If for example you wanted to use the above two in a query, you'd specify them as:
   *
   * ```typescript
   * const musts = [
   *   ["videothumb": ["true"]],
   *   ["realthumb": ["true"]],
   * ];
   * ```
   *
   * If you wanted to search on a list of UUIDs, you'd:
   *
   * ```typescript
   * const musts = [
   *   ["uuids",
   *     ["ab16b5f0-a12e-43f5-9d8b-25870528ad41",
   *      "24b977ec-4df4-4a43-8922-8ca6f82a296a"]],
   * ];
   * ```
   */
  musts?: Must[];
}

export interface WizardControlFieldValue {
  /**
   * A list of schema nodes targeted by a Wizard control.
   */
  schemaNodes: string[];
  /**
   * Values of a Wizard control.
   */
  values: string[];
  /**
   * Currently supported Query types.
   */
  queryType: 'DateRange' | 'Phrase' | 'Tokenised';
}

/**
 * Body of the search2 POST request for Advanced search.
 */
export interface AdvancedSearchParams {
  /**
   * A list of `WizardControlFieldValue` to build Advanced search criteria.
   */
  advancedSearchCriteria?: WizardControlFieldValue[];
}

/**
 * Provides the lower level implementation of SearchParams for sending directly to the server.
 */
interface SearchParamsProcessed extends SearchParamsBase {
  musts?: string[];
}

/**
 * Details of an additional field to display as part of search results - server side configurable in collections.
 */
export interface DisplayFields {
  /**
   * Type of a field.
   */
  type: string;
  /**
   * Name of a field.
   */
  name: i18nString;
  /**
   * Html code of a field.
   */
  html: i18nString;
}

/**
 * How an item should be displayed as configured per institution via the search result display template.
 */
export interface DisplayOptions {
  /**
   * The display mode for attachments when viewed from search result page.
   */
  attachmentType?: string;
  /**
   * True if thumbnail is prevented from displaying.
   */
  disableThumbnail: boolean;
  /**
   * True if the 'show attachments' icon is enabled for standard search result pages.
   */
  standardOpen: boolean;
  /**
   * True if the 'show attachments' icon is enabled for integration screen result pages.
   */
  integrationOpen: boolean;
}

/**
 * Summary of an attachment associated with an item returned in a search result.
 */
export interface Attachment {
  /**
   * Attachment type, e.g. "file", "url", "package", "scorm", etc.
   */
  attachmentType: string;
  /**
   * The unique ID of an attachment.
   */
  id: string;
  /**
   * The description of an attachment.
   */
  description?: string;

  /**
   * Whether or not the attachment has been determined to be broken by the server.
   */
  brokenAttachment: boolean;
  /**
   * True if an attachment can be previewed.
   */
  preview: boolean;
  /**
   * MimeType of file based attachment
   */
  mimeType?: string;
  /**
   * True if a file attachment has a generated thumbnail in filestore
   */
  hasGeneratedThumb?: boolean;
  /**
   * Links to the attachment.
   */
  links: {
    /**
     * The URL for viewing this attachment.
     */
    view: string;
    /**
     * The URL for viewing this attachment's thumbnail.
     */
    thumbnail: string;
    /**
     * The ID of the attachment on an external system - as determined by the `attachmentType`.
     * For example, for `custom/youtube` this is the YouTube video ID.
     */
    externalId?: string;
  };
  /**
   * If a file attachment, the path for the represented file
   */
  filePath?: string;
}

/**
 * Status of Item's DRM.
 */
export interface DrmStatus {
  /**
   * `true` if DRM terms have been accepted.
   */
  termsAccepted: boolean;
  /**
   * `true` if user is authorised to access Item or accept DRM.
   */
  isAuthorised: boolean;
  /**
   * `true` to allow viewing the Item summary page without accepting the terms.
   */
  isAllowSummary: boolean;
}

/**
 * Provides details to assist with displaying a thumbnail for a search result, based on the
 * attachment that is designated to be used as the basis for the thumbnail of this item (typically
 * the first attachment).
 */
export interface ThumbnailDetails {
  /**
   * The broad indicator of attachment type which drives the content of the other properties.
   * Example values are `file`, `link`, `custom/xyz`.
   */
  attachmentType: string;
  /**
   * Mostly used when `attachmentType` is `file` but also when `custom/resource`.
   */
  mimeType?: string;
  /**
   * If the server has generated a specific thumbnail for this item, then this will provide the URL
   * for it.
   */
  link?: string;
}

/**
 * Shared properties of raw and transformed Bookmark.
 */
export interface BookmarkBase {
  /**
   * Unique ID of the bookmark.
   */
  id: number;
  /**
   * Tags associated with this bookmark.
   */
  tags: string[];
}

/**
 * Bookmark as it is returned in the API
 */
interface BookmarkRaw extends BookmarkBase {
  /** The date when bookmark was added. */
  addedAt: string;
}

/**
 * Full details of a Bookmark.
 */
export interface Bookmark extends BookmarkBase {
  /** The date when bookmark was added. */
  addedAt: Date;
}

/**
 * Shared properties or raw and transformed search result item
 */
interface SearchResultItemBase {
  /**
   * Item's unique ID.
   */
  uuid: string;
  /**
   * Item's version.
   */
  version: number;
  /**
   * Item's name.
   */
  name?: i18nString;
  /**
   * Item's description.
   */
  description?: i18nString;
  /**
   * Item's status
   */
  status: string;
  /**
   * The ID of item's collection.
   */
  collectionId: string;
  /**
   * The number of item's comments.
   */
  commentCount?: number;
  /**
   * Item's star rating.
   */
  starRatings: number;
  /**
   * Item's attachments. Will not be present if `includeAttachments` in search params is `false` or
   * if the item has none.
   */
  attachments?: Attachment[];
  /**
   * How many attachments this item has - present regardless of `includeAttachments`.
   */
  attachmentCount: number;
  /**
   * Item's thumbnail.
   */
  thumbnail: string;
  /**
   * Details for a thumbnail to represent this item - if available (depends on the item having
   * attachments).
   */
  thumbnailDetails?: ThumbnailDetails;
  /**
   * Item's display fields.
   */
  displayFields: DisplayFields[];
  /**
   * Item's display options.
   */
  displayOptions?: DisplayOptions;
  /**
   * Indicates if a search term was found inside attachment content
   */
  keywordFoundInAttachment: boolean;
  /**
   * Links to an item.
   */
  links: {
    /**
     * The URL for viewing this item.
     */
    view: string;
    /**
     * The REST API path used to get this item's details.
     */
    self: string;
  };
  /**
   * True if this version is the latest version.
   */
  isLatestVersion: boolean;
  /**
   * Item's DRM Status. Absent if item is not under DRM control
   */
  drmStatus?: DrmStatus;
}

/**
 * Search result item as it is returned by the API
 */
export interface SearchResultItemRaw extends SearchResultItemBase {
  /**
   * The date when item is created.
   */
  createdDate: string;
  /**
   * The last date when item is modified.
   */
  modifiedDate: string;
  /**
   * Details of an Item's moderation.
   */
  moderationDetails?: {
    /**
     * When was the last moderation action performed.
     */
    lastActionDate: string;
    /**
     * When was the Item submitted to moderation.
     */
    submittedDate: string;
    /**
     * Message for why the Item was rejected.
     */
    rejectionMessage?: string;
  };
  /**
   * Bookmark linking to this Item.
   */
  bookmark?: BookmarkRaw;
}

/**
 * Type of search result item.
 */
export interface SearchResultItem extends SearchResultItemBase {
  /**
   * The date when item is created.
   */
  createdDate: Date;
  /**
   * The last date when item is modified.
   */
  modifiedDate: Date;
  /**
   * Details of an Item's moderation.
   */
  moderationDetails?: {
    /**
     * When was the last moderation action performed.
     */
    lastActionDate: Date;
    /**
     * When was the Item submitted to moderation.
     */
    submittedDate: Date;
    /**
     * Message for why the Item was rejected.
     */
    rejectionMessage?: string;
  };
  /**
   * Bookmark linking to this Item.
   */
  bookmark?: Bookmark;
}

/**
 * Represents the results for a search query.
 */
export interface SearchResult<T> {
  /**
   * The starting offset into the total search results
   */
  start: number;
  /**
   * How many results can be found in `results`
   */
  length: number;
  /**
   * The maximum number of results available for paging
   */
  available: number;
  /**
   * The individual items which match the search
   */
  results: T[];
  /**
   * List of words to use to highlight when displaying the results
   */
  highlight: string[];
}

const isMustValid = ([field, values]: Must): boolean => {
  const isValidFieldName = field.trim().length > 0 && !field.includes(':');
  const isValidValues =
    values.length > 0 && values.every((v) => v.trim().length > 0);

  return isValidFieldName && isValidValues;
};

// convert one
const convertMust = ([field, values]: Must): string[] =>
  values.map((v) => `${field}:${v}`);

// convert many
const convertMusts = (musts: Must[]): string[] =>
  musts.reduce(
    (prev: string[], must: Must) => prev.concat(convertMust(must)),
    []
  );

export const processMusts = (musts?: Must[]): string[] | undefined => {
  if (!musts) {
    return;
  }
  musts.forEach((m) => {
    if (!isMustValid(m)) {
      throw new TypeError('Provided must specification is invalid: ' + m);
    }
  });
  return convertMusts(musts);
};

const processSearchParams = (
  params?: SearchParams
): SearchParamsProcessed | undefined =>
  params ? { ...params, musts: processMusts(params.musts) } : undefined;

const SEARCH2_API_PATH = '/search2';
const ADVANCED_API_PATH = `${SEARCH2_API_PATH}/advanced`;
const EXPORT_PATH = `${SEARCH2_API_PATH}/export`;

const searchResultValidator = validate(
  SearchResultCodec(SearchResultItemRawCodec)
);

const processRawSearchResult = (data: SearchResult<SearchResultItemRaw>) =>
  convertDateFields<SearchResult<SearchResultItem>>(data, STANDARD_DATE_FIELDS);

/**
 * Communicate with REST endpoint 'search2' to do a search with specified search criteria.
 *
 * @param apiBasePath Base URI to the oEQ institution and API.
 * @param params Query parameters as search criteria.
 */
export const search = (
  apiBasePath: string,
  params?: SearchParams
): Promise<SearchResult<SearchResultItem>> =>
  GET<SearchResult<SearchResultItemRaw>>(
    apiBasePath + SEARCH2_API_PATH,
    searchResultValidator,
    processSearchParams(params)
  ).then(processRawSearchResult);

/**
 * Communicate with POST endpoint 'search2' to do a search with large number of search criteria.
 *
 * @param apiBasePath Base URI to the oEQ institution and API.
 * @param params Query parameters as search criteria.
 */
export const searchWithPOST = (
  apiBasePath: string,
  params?: SearchParams
): Promise<SearchResult<SearchResultItem>> =>
  POST<SearchParamsProcessed, SearchResult<SearchResultItemRaw>>(
    apiBasePath + SEARCH2_API_PATH,
    searchResultValidator,
    processSearchParams(params)
  ).then(processRawSearchResult);

/**
 * Communicate with the variation of REST endpoint 'GET search2' which handles a POST request ('POST search2/advanced').
 * General search criteria and Advanced search criteria are both supported.
 *
 * @param apiBasePath Base URI to the oEQ institution and API.
 * @param advancedParams Advanced parameters (e.g. Advanced search criteria).
 * @param normalParams Query parameters as general search criteria.
 */
export const searchWithAdvancedParams = (
  apiBasePath: string,
  advancedParams: AdvancedSearchParams,
  normalParams?: SearchParams
): Promise<SearchResult<SearchResultItem>> =>
  POST<AdvancedSearchParams, SearchResult<SearchResultItemRaw>>(
    apiBasePath + ADVANCED_API_PATH,
    searchResultValidator,
    advancedParams,
    processSearchParams(normalParams)
  ).then(processRawSearchResult);

/**
 * Build a full URL for downloading a search result.
 * @param apiBasePath Base URI to the oEQ institution and API.
 * @param params Query parameters as search criteria.
 */
export const buildExportUrl = (
  apiBasePath: string,
  params: SearchParams
): string => apiBasePath + EXPORT_PATH + '?' + stringify(params);

/**
 * Communicate with REST endpoint 'search2/export' to confirm if an export request is valid.
 *
 * @param apiBasePath Base URI to the oEQ institution and API.
 * @param params Query parameters as search criteria.
 */
export const confirmExportRequest = (
  apiBasePath: string,
  params: SearchParams
): Promise<boolean> => HEAD(apiBasePath + EXPORT_PATH, params);
