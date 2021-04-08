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
import { is } from 'typescript-is';
import { GET } from './AxiosInstance';
import * as Common from './Common';
import * as Utils from './Utils';

/**
 * Used for specifying must expressions such as `moderating:true`. Neither string should contain
 * any colons (or other exempt Lucene syntax characters).
 */
export type Must = [string, string[]];

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
  order?: string;
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
  status?: Common.ItemStatus[];
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
   * A flag indicating whether to search attachments or not.
   */
  searchAttachments?: boolean;
  /**
   * List of MIME types to filter by.
   */
  mimeTypes?: string[];
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
  name: Common.i18nString;
  /**
   * Html code of a field.
   */
  html: Common.i18nString;
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
  name?: Common.i18nString;
  /**
   * Item's description.
   */
  description?: Common.i18nString;
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
   * Item's attachments.
   */
  attachments?: Attachment[];
  /**
   * Item's thumbnail.
   */
  thumbnail: string;
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
   * ID of Bookmark linking to this Item.
   */
  bookmarkId?: number;
  /**
   * True if this version is the latest version.
   */
  isLatestVersion: boolean;
}

/**
 * Search result item as it is returned by the API
 */
interface SearchResultItemRaw extends SearchResultItemBase {
  /**
   * The date when item is created.
   */
  createdDate: string;
  /**
   * The last date when item is modified.
   */
  modifiedDate: string;
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
  const containsColon = (s: string): boolean => s.match(':') !== null;
  const noColonsPresent = !containsColon(field) && !values.some(containsColon);
  const noEmptyValues =
    values.length > 0 && !values.some((v) => v.trim().length < 1);
  return field.trim().length > 0 && noEmptyValues && noColonsPresent;
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

const processMusts = (musts?: Must[]): string[] | undefined => {
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

/**
 * Communicate with REST endpoint 'search2' to do a search with specified search criteria.
 *
 * @param apiBasePath Base URI to the oEQ institution and API.
 * @param params Query parameters as search criteria.
 */
export const search = (
  apiBasePath: string,
  params?: SearchParams
): Promise<SearchResult<SearchResultItem>> => {
  return GET<SearchResult<SearchResultItemRaw>>(
    apiBasePath + SEARCH2_API_PATH,
    (data): data is SearchResult<SearchResultItemRaw> =>
      is<SearchResult<SearchResultItemRaw>>(data),
    processSearchParams(params)
  ).then((data) =>
    Utils.convertDateFields<SearchResult<SearchResultItem>>(data, [
      'createdDate',
      'modifiedDate',
    ])
  );
};
