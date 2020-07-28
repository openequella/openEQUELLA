import { GET } from './AxiosInstance';
import { is } from 'typescript-is';
import {UuidString} from "./Common";

export interface UserDetails {
  id: UuidString;
  username: string;
  firstName: string;
  lastName: string;
  email?: string;
}

export interface GroupDetails {
  id: UuidString;
  name: string;
}

export interface RoleDetails {
  id: UuidString;
  name: string;
}

/**
 * Results for the Search operation, where lists will be populated based on Search criteria. If
 * the Search criteria results in no results for one of the lists, then they will be returned as
 * an empty list.
 *
 * Based on com.tle.web.api.users.LookupQueryResult
 */
export interface SearchResult {
  users: UserDetails[];
  groups: GroupDetails[];
  roles: RoleDetails[];
}

export interface SearchParams {
  /** Wildcard supporting text string to filter results by. */
  q?: string;
  /** Include groups in the results. */
  groups: boolean;
  /** Include roles in the results. */
  roles: boolean;
  /** Include users in the results. */
  users: boolean;
}

const isSearchResult = (instance: unknown): instance is SearchResult =>
  is<SearchResult>(instance);

const USERQUERY_ROOT_PATH = '/userquery';

/**
 * Search for users and related entities (i.e. groups and roles).
 *
 * @param apiBasePath Base URI to the oEQ institution and API
 * @param params Query parameters to customize result
 */
export const search = (
  apiBasePath: string,
  params: SearchParams
): Promise<SearchResult> =>
  GET<SearchResult>( apiBasePath + USERQUERY_ROOT_PATH + '/search', isSearchResult, params);
