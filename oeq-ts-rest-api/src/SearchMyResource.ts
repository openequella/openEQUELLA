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
import { GET } from './AxiosInstance';
import { MyResourceSearchTypeCodec } from './gen/SearchMyResource';
import { validate } from './Utils';

/**
 * IDs returned by GET /search/myresources for each myresources type
 */
type MyResourceSearchTypeId =
  | 'published'
  | 'draft'
  | 'scrapbook'
  | 'modqueue'
  | 'archived'
  | 'all';

/**
 * Display names for each myresources search types.
 */
export type MyResourceSearchTypeName =
  | 'Published'
  | 'Drafts'
  | 'Scrapbook'
  | 'Moderation queue'
  | 'Archive'
  | 'All resources';

/**
 * Sub‑search IDs under the "Moderation queue" type.
 */
type MyResourceModerationSubSearchId = 'moderating' | 'review' | 'rejected';

/**
 * Display names for sub‑searches under the "Moderation queue" type.
 */
export type MyResourceModerationSubSearchName =
  | 'In moderation'
  | 'Under review'
  | 'Rejected';

/**
 * Type representing a sub-search under "Moderation queue" type.
 */
export interface MyResourceModeratingSubSearch {
  /**
   * Display name of the sub-search (e.g. "In moderation").
   */
  name: MyResourceModerationSubSearchName;
  /**
   * Unique identifier of the sub-search (e.g. "modqueue_moderating").
   */
  id: MyResourceModerationSubSearchId;
  /**
   * Number of results available in this sub-search.
   */
  count: number;
}

/**
 * Type representing a single My Resources search type entry as returned by GET /search/myresources.
 */
export interface MyResourceSearchType {
  /**
   * Display name of the search type (e.g. "Published").
   */
  name: MyResourceSearchTypeName;
  /**
   * Unique identifier of the search type (e.g. "published").
   */
  id: MyResourceSearchTypeId;
  /**
   * Number of results available under this search type.
   */
  count: number;
  /**
   * Link to execute this search type.
   */
  links: string;
  /**
   * Optional list of sub-searches, present for type "Moderation queue".
   */
  subSearches?: MyResourceModeratingSubSearch[];
}

/**
 * Get a list of all My Resources search types for the current user.
 *
 * @param apiBasePath Base URI to the oEQ institution and API.
 */
export const getMyResourceSearchTypes = (
  apiBasePath: string
): Promise<MyResourceSearchType[]> =>
  GET<MyResourceSearchType[]>(
    `${apiBasePath}/search/myresources`,
    validate(t.array(MyResourceSearchTypeCodec))
  );
