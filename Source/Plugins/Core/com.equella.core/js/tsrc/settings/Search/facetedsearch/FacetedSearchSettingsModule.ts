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
import { encodeQuery } from "../../../util/encodequery";
import * as lodash from "lodash";

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
 * Firstly, update order index and the 'updated' flag of the dragged facet.
 * Secondly, remove this facet from the list.
 * Thirdly, update properties of facets in the dragged and dropped range.
 * Lastly, insert the dragged facet to the list at its new place.
 *
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
): FacetWithFlags[] => {
  const filterFacetsByOrderRange = (facet: FacetWithFlags) => {
    if (startIndex < endIndex) {
      return facet.orderIndex > startIndex && facet.orderIndex <= endIndex;
    }
    return facet.orderIndex >= endIndex && facet.orderIndex < startIndex;
  };
  // Deep copy to avoid mutating objects of the original array.
  const copiedFacets: FacetWithFlags[] = lodash.cloneDeep(facets);
  // Deleted facets do not need reordering so leave them alone for now.
  const deletedFacets = copiedFacets.filter((facets) => facets.deleted);
  const nonDeletedFacets = copiedFacets.filter((facet) => !facet.deleted);
  // Update the dragged facet.
  const draggedFacet = nonDeletedFacets[startIndex];
  draggedFacet.updated = true;
  draggedFacet.orderIndex = draggedFacet.orderIndex + (endIndex - startIndex);
  // Remove it from its original place.
  nonDeletedFacets.splice(startIndex, 1);
  // Update facets within the range
  nonDeletedFacets
    .filter((facet) => filterFacetsByOrderRange(facet))
    .forEach((facet) => {
      facet.updated = true;
      facet.orderIndex =
        facet.orderIndex -
        Math.abs(endIndex - startIndex) / (endIndex - startIndex);
    });
  // Insert the dragged one to the list at its new place.
  nonDeletedFacets.splice(endIndex, 0, draggedFacet);
  // Combine deleted and non-deleted and return.
  return nonDeletedFacets.concat(deletedFacets);
};
