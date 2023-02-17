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
import type { BaseEntitySummary } from './Common';
import { AdvancedSearchDefinitionCodec } from './gen/AdvancedSearch';
import { BaseEntitySummaryCodec } from './gen/Common';
import { validate } from './Utils';
import type { WizardControl } from './WizardControl';

/**
 * Definition of an Advanced Search.
 */
export interface AdvancedSearchDefinition {
  /**
   * Name of the Advanced Search.
   */
  name?: string;
  /**
   * Description of the Advanced Search.
   */
  description?: string;
  /**
   * A list of Collections configured to limit the search result.
   */
  collections: BaseEntitySummary[];
  /**
   * All the Wizard Controls of the Advanced Search.
   */
  controls: WizardControl[];
}

const ADV_SEARCH_SETTINGS_ROOT_PATH = '/settings/advancedsearch/';

/**
 * List all Advanced Searches which the currently authenticated user has access to.
 */
export const listAdvancedSearches = (
  apiBasePath: string
): Promise<BaseEntitySummary[]> =>
  GET(
    apiBasePath + ADV_SEARCH_SETTINGS_ROOT_PATH,
    validate(t.array(BaseEntitySummaryCodec))
  );

/**
 * Retrieve an Advanced search's definition by UUID.
 *
 * @param apiBasePath Base URI to the oEQ institution and API
 * @param uuid UUID of an Advanced search.
 */
export const getAdvancedSearchByUuid = (
  apiBasePath: string,
  uuid: string
): Promise<AdvancedSearchDefinition> =>
  GET(
    apiBasePath + ADV_SEARCH_SETTINGS_ROOT_PATH + uuid,
    validate(AdvancedSearchDefinitionCodec)
  );
