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
import * as t from 'io-ts';
import { GET, POST } from './AxiosInstance';
import type { UuidString } from './Common';
import { SearchResultCodec, UserDetailsCodec } from './gen/UserQuery';
import { validate } from './Utils';

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

export interface FilteredParams {
  /** Wildcard supporting text string to filter results by. */
  q?: string;
  /** A list of group UUIDs to filter the search by. */
  byGroups?: UuidString[];
}

export interface LookupParams {
  /** List of User IDs to lookup. */
  users: string[];
  /** List of Group IDs to lookup. */
  groups: string[];
  /** List of Role IDs to lookup. */
  roles: string[];
}

const isSearchResult = validate(SearchResultCodec);

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
  GET<SearchResult>(
    apiBasePath + USERQUERY_ROOT_PATH + '/search',
    isSearchResult,
    params
  );

/**
 * Searches for users, but filters the results based on the byGroups parameter.
 *
 * @param apiBasePath Base URI to the oEQ institution and API
 * @param params Query parameters to customize result
 */
export const filtered = (
  apiBasePath: string,
  params: FilteredParams
): Promise<UserDetails[]> =>
  GET<UserDetails[]>(
    apiBasePath + USERQUERY_ROOT_PATH + '/filtered',
    validate(t.array(UserDetailsCodec)),
    params
  );

/**
 * Lookup users and related entities (i.e. groups and roles) by id.
 *
 * @param apiBasePath Base URI to the oEQ institution and API
 * @param params Query parameters to customize result
 */
export const lookup = (
  apiBasePath: string,
  params: LookupParams
): Promise<SearchResult> =>
  POST<LookupParams, SearchResult>(
    apiBasePath + USERQUERY_ROOT_PATH + '/lookup',
    isSearchResult,
    params
  );
