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
/**
 * Params to pass to a call for search facets.
 */
import { is } from 'typescript-is';
import { GET } from './AxiosInstance';
import { UuidString } from './Common';
import { Must, processMusts } from './Search';
import { asCsvList } from './Utils';

interface SearchFacetsParamsBase {
  /**
   * List of XML nodes to search.
   */
  nodes: string[];
  /**
   * Currently unused server side. (Kept to ensure a match to the Swagger doco.)
   */
  nestLevel?: string;
  /**
   * A query string for the filter the resultant 'terms' by.
   */
  q?: string;
  /**
   * The number of term combinations to search for, a higher number will return more results and
   * more accurate counts, but will take longer. Default 10.
   */
  breadth?: number;
  /**
   * Collections to filter by.
   */
  collections?: UuidString[];
  /**
   * An Apache Lucene syntax where clause to enable more powerful filtering. More information can
   * be found at the documentation site in the
   * [REST API Guide](https://openequella.github.io/guides/RestAPIGuide.html).
   */
  where?: string;
  /**
   * An ISO date format (yyyy-MM-dd)
   */
  modifiedAfter?: string;
  /**
   * An ISO date format (yyyy-MM-dd)
   */
  modifiedBefore?: string;
  /**
   * An ID (not a username) of a user.
   */
  owner?: UuidString;
  /**
   * If `true` then includes items that are not live.
   */
  showall?: boolean;
  /**
   * A list of MIME types to filter items based on their attachments matching the specified types.
   */
  mimeTypes?: string[];
}

/**
 * Parameters which can be passed to a facet search.
 */
export interface SearchFacetsParams extends SearchFacetsParamsBase {
  /**
   * List of search index key/value pairs to filter by. e.g. videothumb:true or realthumb:true.
   * If for example you wanted to use the above two in a query, you'd specify them as:
   *
   * ```typescript
   * const musts = [
   *   ["videothumb": ["true"]],
   *   ["realthumb": ["true"]],
   * ];
   * ```
   *
   * If you wanted to search on a list of UUIDs, you'd:
   *
   * ```typescript
   * const musts = [
   *   ["uuids",
   *     ["ab16b5f0-a12e-43f5-9d8b-25870528ad41",
   *      "24b977ec-4df4-4a43-8922-8ca6f82a296a"]],
   * ];
   * ```
   */
  musts?: Must[];
}

/**
 * Provides the lower level implementation of SearchFacetsParams for sending directly to the server.
 */
interface SearchFacetsParamsProcessed extends SearchFacetsParamsBase {
  musts?: string[];
}

/**
 * Represents an individual facet returned in the results. Sometimes also called a category.
 *
 * Caution: The is based of the server side class of `com.tle.web.api.search.interfaces.beans.FacetBean`
 * which extends `com.tle.web.api.interfaces.beans.AbstractExtendableBean`. That means at runtime
 * there can be additional fields dynamically added.
 */
export interface Facet {
  /**
   * The derived facet.
   */
  term: string;
  /**
   * How many items match this `term` based on the applied filters (date, owner, collection, etc.)
   */
  count: number;
  /**
   * Additional nested facets - but not currently supported by the server.
   */
  innerFacets?: Facet[];
}

/**
 * Simple container for the results of a facet search. Rather redundant, but had to follow what was
 * `com.tle.web.api.search.interfaces.beans.FacetSearchBean`.
 */
export interface SearchFacetsResult {
  results: Facet[];
}

const processSearchFacetsParams = (
  params?: SearchFacetsParams
): SearchFacetsParamsProcessed | undefined =>
  params ? { ...params, musts: processMusts(params.musts) } : undefined;

const SEARCH_FACETS_API_PATH = '/search/facet';

export const searchFacets = (
  apiBasePath: string,
  params: SearchFacetsParams
): Promise<SearchFacetsResult> =>
  GET<SearchFacetsResult>(
    apiBasePath + SEARCH_FACETS_API_PATH,
    (data): data is SearchFacetsResult => is<SearchFacetsResult>(data),
    {
      ...processSearchFacetsParams(params),
      nodes: asCsvList<string>(params.nodes),
      collections: asCsvList<string>(params.collections),
    }
  );
