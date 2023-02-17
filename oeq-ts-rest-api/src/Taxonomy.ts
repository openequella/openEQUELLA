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
import { PagedResult } from './Common';
import { PagedResultCodec } from './gen/Common';
import { TermCodec } from './gen/Taxonomy';
import { validate } from './Utils';

/**
 * Restrictions applied to Taxonomy term selection.
 */
export type SelectionRestriction =
  | 'TOP_LEVEL_ONLY'
  | 'LEAF_ONLY'
  | 'UNRESTRICTED';

/**
 * Formats which are used to search for a term.
 */
export type TermStorageFormat = 'FULL_PATH' | 'LEAF_ONLY';

export interface Term {
  /**
   * Term of a taxonomy.
   */
  term: string;
  /**
   * The term including its parent terms.
   */
  fullTerm: string;
  /**
   * Whether the term is readonly.
   */
  readonly: boolean;
  /**
   * Index of the term in the taxonomy.
   */
  index: number;
  /**
   * UUID of the term. Optional because not all endpoints returning it.
   */
  uuid?: string;
  /**
   * UUID of the parent term. Optional because not all endpoints returning it.
   */
  parentUuid?: string;
  /**
   * Extra data associated with the term, which could be either a HTML fragment displayed
   * in the Taxonomy term Pop-up Browser or a custom key/value pair.
   */
  data?: Record<string, string>;
}

const TAXONOMY_ROOT_PATH = '/taxonomy/';

/**
 * Search for child terms by UUID and path.
 * For example, a taxonomy has a term which has a nested term (e.g. `term1\term2`).
 * To get `term2`, the path should be `term1`.
 * To get `term1`, the path is just an empty string.
 *
 * @param apiBasePath Base URI to the oEQ institution and API.
 * @param uuid UUID of the taxonomy.
 * @param path Path Parent path of child terms.
 */
export const getTaxonomyChildTerms = (
  apiBasePath: string,
  uuid: string,
  path: string
): Promise<Term[]> =>
  GET(
    `${apiBasePath}${TAXONOMY_ROOT_PATH}${uuid}/term`,
    validate(t.array(TermCodec)),
    {
      path: path,
    }
  );

/**
 * Search for terms of a taxonomy.
 *
 * @param apiBasePath Base URI to the oEQ institution and API.
 * @param uuid UUID of the taxonomy.
 * @param q Query string.
 * @param restriction Restriction applied to the search result.
 * @param limit The maxinum number of terms in a search result.
 * @param searchfullterm Whether to search full term.
 */
export const searchTaxonomyTerms = (
  apiBasePath: string,
  uuid: string,
  q: string,
  restriction: SelectionRestriction = 'UNRESTRICTED',
  limit = 20,
  searchfullterm = false
): Promise<PagedResult<Term>> =>
  GET(
    `${apiBasePath}${TAXONOMY_ROOT_PATH}${uuid}/search`,
    validate(PagedResultCodec(TermCodec)),
    {
      q,
      restriction,
      limit,
      searchfullterm,
    }
  );
