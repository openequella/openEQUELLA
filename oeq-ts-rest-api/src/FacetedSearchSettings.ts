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
import type { BatchOperationResponse } from './BatchOperationResponse';
import { DELETE, GET, PUT } from './AxiosInstance';
import { is } from 'typescript-is';

export interface FacetedSearchClassification {
  /**
   * ID of a facet; being undefined means this facet is dirty(i.e., not saved to the server).
   */
  id?: number;
  /**
   * Date time when a facet was created. Not required for batch create or update.
   */
  dateCreated?: string;
  /**
   * Latest date time when a facet was modified. Not required for batch create or update.
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
   * The number of category of a facet; Being undefined means the number is unlimited.
   */
  maxResults?: number;
  /**
   * Used for re-ordering facets.
   */
  orderIndex: number;
}

const FACETED_SEARCH_SETTINGS_URL = '/settings/facetedsearch/classfication';

/**
 * Retrieve faceted search settings
 *
 * @param apiBasePath Base URI to the oEQ institution and API
 */
export const getFacetedSearchSettings = (
  apiBasePath: string
): Promise<FacetedSearchClassification[]> =>
  GET<FacetedSearchClassification[]>(
    apiBasePath + FACETED_SEARCH_SETTINGS_URL,
    (data): data is FacetedSearchClassification[] =>
      is<FacetedSearchClassification[]>(data)
  );

/**
 * Update one or more faceted search classifications in a single request (batch).
 *
 * @param apiBasePath Base URI to the oEQ institution and API
 * @param facetSearchClassifications The faceted search classification to be updated with the provided values.
 */
export const batchUpdateFacetedSearchSetting = (
  apiBasePath: string,
  facetSearchClassifications: FacetedSearchClassification[]
): Promise<BatchOperationResponse[]> =>
  PUT<FacetedSearchClassification[], BatchOperationResponse[]>(
    apiBasePath + FACETED_SEARCH_SETTINGS_URL,
    facetSearchClassifications
  );

/**
 * Delete one or more faceted search classifications in a single request (batch).
 *
 * @param apiBasePath Base URI to the oEQ institution and API
 * @param facetSearchClassificationIds IDs of the faceted search classification to delete
 */
export const batchDeleteFacetedSearchSetting = (
  apiBasePath: string,
  facetSearchClassificationIds: string[]
): Promise<BatchOperationResponse[]> =>
  DELETE<BatchOperationResponse[]>(apiBasePath + FACETED_SEARCH_SETTINGS_URL, {
    ids: facetSearchClassificationIds,
  });
