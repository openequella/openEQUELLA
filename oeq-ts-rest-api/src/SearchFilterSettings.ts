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
import { DELETE, GET, PUT } from './AxiosInstance';
import type { BatchOperationResponse } from './BatchOperationResponse';
import { MimeTypeFilterCodec } from './gen/SearchFilterSettings';
import { validate } from './Utils';

export interface MimeTypeFilter {
  /**
   * The unique ID a MIME type filter. It's generated on the Server.
   * So it can be null if the filter is created but not saved.
   */
  id?: string;
  /**
   * The name of a MIME type filter.
   */
  name: string;
  /**
   * A list of MIME types belonging to a MIME type filter.
   */
  mimeTypes: string[];
}

const SEARCH_FILTER_SETTINGS_URL = '/settings/search/filter';

/**
 * Retrieve search filters.
 *
 * @param apiBasePath Base URI to the oEQ institution and API
 */
export const getSearchFilterSettings = (
  apiBasePath: string
): Promise<MimeTypeFilter[]> =>
  GET<MimeTypeFilter[]>(
    apiBasePath + SEARCH_FILTER_SETTINGS_URL,
    validate(t.array(MimeTypeFilterCodec))
  );

/**
 * Update one or more search filters in a single request (batch).
 *
 * @param apiBasePath Base URI to the oEQ institution and API
 * @param searchFilterSettings The filters to be updated with the provided values.
 */
export const batchUpdateSearchFilterSetting = (
  apiBasePath: string,
  searchFilterSettings: MimeTypeFilter[]
): Promise<BatchOperationResponse[]> =>
  PUT<MimeTypeFilter[], BatchOperationResponse[]>(
    apiBasePath + SEARCH_FILTER_SETTINGS_URL,
    searchFilterSettings
  );

/**
 * Delete one or more filters in a single request (batch).
 *
 * @param apiBasePath Base URI to the oEQ institution and API
 * @param searchFilterSettingsIds IDs of the filters to delete
 */
export const batchDeleteSearchFilterSetting = (
  apiBasePath: string,
  searchFilterSettingsIds: string[]
): Promise<BatchOperationResponse[]> =>
  DELETE<BatchOperationResponse[]>(apiBasePath + SEARCH_FILTER_SETTINGS_URL, {
    ids: searchFilterSettingsIds,
  });
