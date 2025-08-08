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
import * as OEQ from "@openequella/rest-api-client";
import { memoize } from "lodash";
import { API_BASE_URL } from "../AppConfig";
import { getISODateString } from "../util/Date";
import { getFacetsFromServer } from "./FacetedSearchSettingsModule";
import {
  formatQuery,
  generateCategoryWhereQuery,
  SearchOptions,
} from "./SearchModule";

/**
 * Represents a Classification and its generated categories ready for display.
 */
export interface Classification {
  /**
   * The unique ID of a Classification.
   */
  id: number;
  /**
   * The name for this group of categories - typically one which has been configured on the system.
   */
  name: string;
  /**
   * The maximum number of categories which should be displayed - based on what was configured for
   * this classification. If `undefined` then the system default number of categories should be
   * displayed.
   */
  maxDisplay: number;
  /**
   * The actual list of categories for this classification. This will be the full list returned
   * from the server - as no paging is currently provided.
   */
  categories: OEQ.SearchFacets.Facet[];
  /**
   * The configured order in which this classification should be displayed.
   */
  orderIndex: number;
  /**
   * The configured schema node of this classification.
   */
  schemaNode: string;
}

/**
 * Represents a group which includes a list of categories and the Classification
 * ID and schema node which these categories belong to.
 */
export interface SelectedCategories {
  /**
   * The Classification's ID which the selected categories belong to.
   */
  id: number;
  /**
   * The schema node from which the selected categories are generated.
   * Whether it's undefined or not depends on the context of its usage.
   */
  schemaNode?: string;
  /**
   * A list of selected categories' terms.
   */
  categories: string[];
}
/**
 * Helper function to convert the commonly used `SearchOptions` into the params we need to
 * list facets. This is a memoized function, so that it can be used in an `Array.map()`
 * with reasonable performance. (Important seeing it's also doing some data conversion.)
 */
const convertSearchOptions: (
  options: SearchOptions,
) => OEQ.SearchFacets.SearchFacetsParams = memoize(
  (options: SearchOptions): OEQ.SearchFacets.SearchFacetsParams => {
    const {
      query,
      collections,
      lastModifiedDateRange,
      owner,
      status,
      rawMode,
      mimeTypes,
      musts,
      hierarchy,
    } = options;
    const searchFacetsParams: OEQ.SearchFacets.SearchFacetsParams = {
      nodes: [],
      query: formatQuery(!rawMode, query),
      modifiedAfter: getISODateString(lastModifiedDateRange?.start),
      modifiedBefore: getISODateString(lastModifiedDateRange?.end),
      owner: owner?.id,
      status,
      mimeTypes,
      musts,
      hierarchy,
    };
    return collections && collections.length > 0
      ? {
          ...searchFacetsParams,
          collections: collections.map((c) => c.uuid),
        }
      : searchFacetsParams;
  },
);

/**
 * Provides a list of categories as defined and filtered by the `options`.
 * Categories that have empty terms will be filtered out, as although the server generates
 * them, sending them back will generate a 500.
 *
 * @param options The control parameters for the generation of the categories
 */
export const listCategories = async (
  options: OEQ.SearchFacets.SearchFacetsParams,
): Promise<OEQ.SearchFacets.Facet[]> =>
  (await OEQ.SearchFacets.searchFacets(API_BASE_URL, options)).results.filter(
    (r) => r.term,
  );

/**
 * Uses the system's configured facets/classifications to generate a set of categories for
 * each. Thereby, generating All the classifications and categories for the system based on
 * configured facets.
 *
 * It is intended that this can be run alongside other search filters, and thereby provide
 * matching categories.
 *
 * The where clause used for generating one Classification's category list should only include
 * categories from other Classifications.
 *
 * @param options The standard options used for searching, as these also filter the generated categories
 */
export const listClassifications = async (
  options: SearchOptions,
): Promise<Classification[]> =>
  Promise.all(
    (await getFacetsFromServer()).map<Promise<Classification>>(
      async (settings, index) => ({
        // We know IDs won't be undefined here, but due to its type being number | undefined,
        // we have to do a nullish coalescing
        id: settings.id ?? index,
        name: settings.name,
        // Like ID, maxDisplay here won't be undefined. When it's 0 let it be 10 instead.
        maxDisplay: settings.maxResults || 10,
        orderIndex: settings.orderIndex,
        categories: await listCategories({
          ...convertSearchOptions(options),
          nodes: [settings.schemaNode],
          whereClause: generateCategoryWhereQuery(
            options.selectedCategories?.filter((c) => c.id !== settings.id),
          ),
        }),
        schemaNode: settings.schemaNode,
      }),
    ),
  );
