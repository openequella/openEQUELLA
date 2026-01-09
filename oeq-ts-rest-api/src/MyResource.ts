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
import { MyResourcesCategoryCodec } from './gen/MyResource';
import { validate } from './Utils';

/**
 * IDs returned by GET /search/myresources for each MyResources category.
 */
type MyResourcesCategoryId =
  | 'published'
  | 'draft'
  | 'scrapbook'
  | 'modqueue'
  | 'archived'
  | 'all';

/**
 * Display names for each MyResources category.
 */
export type MyResourcesCategoryName =
  | 'Published'
  | 'Drafts'
  | 'Scrapbook'
  | 'Moderation queue'
  | 'Archive'
  | 'All resources';

/**
 * Sub‑category IDs under the "Moderation queue" type.
 */
type ModerationQueueSubCategoryId = 'moderating' | 'review' | 'rejected';

/**
 * Display names for sub‑category under the "Moderation queue" type.
 */
export type ModerationQueueSubCategoryName =
  | 'In moderation'
  | 'Under review'
  | 'Rejected';

/**
 * Type representing a sub-category for MyResourcesCategory especially used for "Moderation queue".
 */
export interface MyResourcesSubCategory {
  /**
   * Display name of the sub-category (e.g. "In moderation").
   */
  name: ModerationQueueSubCategoryName;
  /**
   * Unique identifier of the sub-category (e.g. "moderating").
   */
  id: ModerationQueueSubCategoryId;
  /**
   * Number of results available in this sub-category.
   */
  count: number;
}

/**
 * Type representing a single My Resources category entry as returned by GET /search/myresources.
 */
export interface MyResourcesCategory {
  /**
   * Display name of the category (e.g. "Published").
   */
  name: MyResourcesCategoryName;
  /**
   * Unique identifier of the category (e.g. "published").
   */
  id: MyResourcesCategoryId;
  /**
   * Number of results available under this category.
   */
  count: number;
  /**
   * Link to execute this category.
   */
  links: string;
  /**
   * Optional list of sub-searches, present for category "Moderation queue".
   */
  subSearches?: MyResourcesSubCategory[];
}

/**
 * Get a list of all My Resources categories for the current user.
 *
 * @param apiBasePath Base URI to the oEQ institution and API.
 */
export const getMyResourceCategories = (
  apiBasePath: string
): Promise<MyResourcesCategory[]> =>
  GET<MyResourcesCategory[]>(
    `${apiBasePath}/search/myresources`,
    validate(t.array(MyResourcesCategoryCodec))
  );
