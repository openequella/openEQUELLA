import * as Common from './Common';
import { is } from 'typescript-is';
import {GET} from './AxiosInstance';

/**
 * Type of query parameters that can be used in a search.
 */
export interface SearchParams {
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
}

export enum ItemStatus {
  DRAFT = "DRAFT",
  LIVE = "LIVE",
  REJECTED = "REJECTED",
  MODERATING = "MODERATING",
  ARCHIVED = "ARCHIVED",
  SUSPENDED = "SUSPENDED",
  DELETED = "DELETED",
  REVIEW = "REVIEW",
  PERSONAL = "PERSONAL",
}

/**
 * Type of item's display fields.
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
 * Type of item's display options.
 */
export interface DisplayOptions {
  /**
   * The display mode for attachments when viewed from search result page.
   */
  attachmentType: string;
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
 * Type of search result attachment.
 */
export interface SearchResultAttachment {
  /**
   * Attachment type.
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
   * Links to the attachment.
   */
  links: Record<string, string>;
}

/**
 * Type of search result item.
 */
export interface SearchResultItem {
  /**
   * Item's unique ID.
   */
  uuid: string;
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
   * The date when item is created.
   */
  createdDate: string;
  /**
   * The last date when item is modified.
   */
  modifiedDate: string;
  /**
   * The ID of item's collection.
   */
  collectionId: string;
  /**
   * The number of item's comments.
   */
  commentCount: number;
  /**
   * Item's attachments.
   */
  attachments: SearchResultAttachment[];
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
   * Links to an item.
   */
  links: Record<string, string>;
}

const SEARCH2_API_PATH = '/search2';

/**
 * Communicate with REST endpoint 'search2' to do a search with specified search criteria.
 */
export const search = (apiBasePath: string, params?: SearchParams): Promise<Common.PagedResult<SearchResultItem>> => {
  return GET<Common.PagedResult<SearchResultItem>>(
    apiBasePath + SEARCH2_API_PATH,
    (data) => is<Common.PagedResult<SearchResultItem>>(data),
    params
  );
};
