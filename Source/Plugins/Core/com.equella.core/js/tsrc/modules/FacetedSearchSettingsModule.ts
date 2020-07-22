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
} from "../api/BatchOperationResponse";
import { encodeQuery } from "../util/encodequery";

export interface Facet {
  /**
   * ID of a facet; being undefined means this facet is dirty(i.e., not saved to the server).
   */
  id?: number;
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
 * * * Remove the flags and then save to the server.
 */
export const batchUpdateOrAdd = (facets: FacetWithFlags[]): Promise<string[]> =>
  Axios.put<BatchOperationResponse[]>(
    FACETED_SEARCH_API_URL,
    facets.map((facet) => removeFlags(facet))
  ).then((res) => groupErrorMessages(res.data));

export const batchDelete = (ids: string[]): Promise<string[]> =>
  Axios.delete<BatchOperationResponse[]>(
    `${FACETED_SEARCH_API_URL}/${encodeQuery({ ids: ids })}`
  ).then((res) => {
    return groupErrorMessages(res.data);
  });

/**
 * Validate if trimmed name or schema node is empty.
 * Return true if they are invalid.
 */
export const validateFacetFields = (field: string): boolean => {
  return !field.trim();
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
 * Given a list of facets, return the highest order index of non-deleted facets.
 * If the list is empty then return -1.
 */
export const getHighestOrderIndex = (facets: FacetWithFlags[]) => {
  if (facets.length == 0) {
    return -1;
  }
  return Math.max(
    ...facets.filter((facet) => !facet.deleted).map((facet) => facet.orderIndex)
  );
};

export const facetComparator = (target: FacetWithFlags) => {
  return (facet: FacetWithFlags) => facet === target;
};

/**
 * Reorder a list of facets, excluding deleted ones.
 * For example, in a array of five facets [f1, f2, f3, f4, f5] where indexes are from 0 - 4,
 * moving f2 to the end of the array results in that f2'index becomes 4 and indexes of f3, f4 and f5
 * become 1, 2 and 3, respectively. f1' index keeps 0.
 *
 * Given the same array, moving f5 to the second position of the array results in that
 * f5's index become 1 and indexes of f2, f3 and f4 become 2, 3 and 4, respectively. f1' index keeps 0.
 *
 * @param facets List of facets.
 * @param startIndex Current index of the dragged facet.
 * @param endIndex  New index of the dragged facet.
 */
export const reorder = (
  facets: FacetWithFlags[],
  startIndex: number,
  endIndex: number
): FacetWithFlags[] =>
  facets.map((facet) => {
    let newOrderIndex = 0;
    if (facet.deleted) {
      return { ...facet };
    }

    if (facet.orderIndex === startIndex) {
      newOrderIndex = endIndex;
    } else if (facet.orderIndex > startIndex && facet.orderIndex <= endIndex) {
      newOrderIndex = facet.orderIndex - 1;
    } else if (facet.orderIndex >= endIndex && facet.orderIndex < startIndex) {
      newOrderIndex = facet.orderIndex + 1;
    } else {
      return { ...facet };
    }

    return { ...facet, orderIndex: newOrderIndex, updated: true };
  });

/**
 * Remove a facet from the given list and update order indexes of facets that have a higher order index.
 * And return a new array which keeps the non-deleted facets and flags the deleted ones back to server.
 *
 * For example, given an array like [f1, f2, f3, f4], removing f2 results in decrementing
 * the order indexes of f3 and f4 by 1.
 *
 * @param facets List of facets
 * @param deletedOrderIndex Order index of the deleted facet.
 */
export const removeFacetFromList = (
  facets: FacetWithFlags[],
  deletedOrderIndex: number
): FacetWithFlags[] => {
  return facets
    .map((facet) => {
      if (facet.orderIndex === deletedOrderIndex) {
        return { ...facet, deleted: true };
      }

      if (facet.orderIndex > deletedOrderIndex) {
        return { ...facet, orderIndex: facet.orderIndex - 1, updated: true };
      }

      return { ...facet };
    })
    .filter((facet) => !facet.deleted || (facet.deleted && !!facet.id));
};
