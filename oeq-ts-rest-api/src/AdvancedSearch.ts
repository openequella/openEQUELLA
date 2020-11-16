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
import { UuidString } from './Common';

/**
 * A summary representation of an Advanced search.
 */
interface AdvancedSearchSummary {
  /**
   * The unique system identifier for this entity.
   */
  uuid: UuidString;
  /**
   * A human readable name for this advanced search in the default locale.
   */
  name: string;
}

const isAdvancedSearchSummary = (
  instance: unknown
): instance is AdvancedSearchSummary[] => is<AdvancedSearchSummary[]>(instance);

const ADV_SEARCH_SETTINGS_ROOT_PATH = '/settings/advancedsearch/';

/**
 * List all Advanced Searches which the currently authenticated user has access to.
 */
export const listAdvancedSearches = (
  apiBasePath: string
): Promise<AdvancedSearchSummary[]> =>
  GET(apiBasePath + ADV_SEARCH_SETTINGS_ROOT_PATH, isAdvancedSearchSummary);
