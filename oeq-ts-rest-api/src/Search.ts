import * as Common from './Common';
import { is } from 'typescript-is';
import * as SearchResult from './SearchResult';
import {GET} from './AxiosInstance';

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
  status?: SearchResult.ItemStatus[];
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

export const isPagedSearchResultItem = (instance: unknown): boolean =>
  is<Common.PagedResult<SearchResult.Item>>(instance);

export const search = (searchPath: string, params?: SearchParams): Promise<Common.PagedResult<SearchResult.Item>> => {
  return GET<Common.PagedResult<SearchResult.Item>>(
    searchPath,
    isPagedSearchResultItem,
    params
  );
};
