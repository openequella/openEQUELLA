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
import { FacetedSearchClassificationCodec } from './gen/FacetedSearchSettings';
import { validate } from './Utils';

export interface FacetedSearchClassification {
  /**
   * ID of a facet; being undefined means this facet is dirty (i.e., not saved to the server).
   */
  id?: number;
  /**
   * Date time when a facet was created. Unnecessary for create or update.
   * Example: 2022-01-25T11:01:12.991+11:00
   */
  dateCreated?: string;
  /**
   * Latest date time when a facet was modified. Unnecessary for create or update.
   * Example: 2022-01-25T11:01:12.991+11:00
   */
  dateModified?: string;
  /**
   * Name of a facet.
   */
  name: string;
  /**
   * Schema node of a facet.
   */
  schemaNode: string;
  /**
   * The number of categories to display for a facet; Being undefined means the number is unlimited.
   */
  maxResults?: number;
  /**
   * Used to control the order of list of facets in the UI.
   */
  orderIndex: number;
}

const FACETED_SEARCH_SETTINGS_URL = '/settings/facetedsearch/classification';

/**
 * Return a list of the settings for all available faceted searches (classifications).
 *
 * @param apiBasePath Base URI to the oEQ institution and API
 */
export const getFacetedSearchSettings = (
  apiBasePath: string
): Promise<FacetedSearchClassification[]> =>
  GET<FacetedSearchClassification[]>(
    apiBasePath + FACETED_SEARCH_SETTINGS_URL,
    validate(t.array(FacetedSearchClassificationCodec))
  );

/**
 * Retrieve a faceted search setting (classification) by a provided ID
 *
 * @param apiBasePath Base URI to the oEQ institution and API
 * @param id Faceted search setting ID
 */
export const getFacetedSearchSettingById = (
  apiBasePath: string,
  id: number | string
): Promise<FacetedSearchClassification> =>
  GET<FacetedSearchClassification>(
    `${apiBasePath}${FACETED_SEARCH_SETTINGS_URL}/${id}`,
    validate(FacetedSearchClassificationCodec)
  );

/**
 * Update one or more faceted search settings (classifications) in a single request (batch).
 *
 * @param apiBasePath Base URI to the oEQ institution and API
 * @param facetedSearchClassifications The faceted search classification to be updated with the provided values.
 */
export const batchUpdateFacetedSearchSetting = (
  apiBasePath: string,
  facetedSearchClassifications: FacetedSearchClassification[]
): Promise<BatchOperationResponse[]> =>
  PUT<FacetedSearchClassification[], BatchOperationResponse[]>(
    apiBasePath + FACETED_SEARCH_SETTINGS_URL,
    facetedSearchClassifications
  );

/**
 * Delete one or more faceted search settings (classifications) in a single request (batch).
 *
 * @param apiBasePath Base URI to the oEQ institution and API
 * @param facetedSearchClassificationIds IDs of the faceted search classification to delete
 */
export const batchDeleteFacetedSearchSetting = (
  apiBasePath: string,
  facetedSearchClassificationIds: number[]
): Promise<BatchOperationResponse[]> =>
  DELETE<BatchOperationResponse[]>(apiBasePath + FACETED_SEARCH_SETTINGS_URL, {
    ids: facetedSearchClassificationIds,
  });
