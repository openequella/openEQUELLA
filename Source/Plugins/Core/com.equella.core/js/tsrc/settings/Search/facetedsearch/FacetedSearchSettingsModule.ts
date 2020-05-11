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

import Axios from "axios";
import {
  BatchOperationResponse,
  groupErrorMessages,
} from "../../../api/BatchOperationResponse";

export interface Facet {
  /**
   * Name of a facet
   */
  name: string;
  /**
   * Schema node of a facet
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

export interface FacetWithFlags extends Facet {
  /**
   * A flag indicating a facet has been visually updated/created.
   */
  updated: boolean;
  /**
   * A flag indicating a facet has been visually deleted.
   */
  deleted: boolean;
}

const FACETED_SEARCH_API_URL = "api/settings/facetedsearch/classification";

export const getFacetsFromServer = (): Promise<Facet[]> =>
  Axios.get(FACETED_SEARCH_API_URL).then((res) => res.data);

/**
 * Remove the boolean flags and then save to the Server.
 */
export const batchUpdateOrAdd = (facets: FacetWithFlags[]) =>
  Axios.put<BatchOperationResponse[]>(
    FACETED_SEARCH_API_URL,
    facets.map((facet) => removeFlags(facet))
  ).then((res) => groupErrorMessages(res.data));

/**
 * Validate if trimmed name or schema node is empty.
 * Return true if they are invalid.
 */
export const validateFacetFields = (field: string): boolean => {
  return !field?.trim();
};

/**
 * Remove unneeded boolean fields.
 */
export const removeFlags = (facet: FacetWithFlags): Facet => {
  delete facet.deleted;
  delete facet.updated;
  return facet;
};

/**
 * Given a list of facets, return the highest order index.
 * If the list is empty then return -1.
 */
export const getHighestOrderIndex = (facets: FacetWithFlags[]) => {
  if (facets.length == 0) {
    return -1;
  }
  return Math.max(...facets.map((facet) => facet.orderIndex));
};
