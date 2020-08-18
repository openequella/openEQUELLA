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
import * as Common from './Common';
import { GET } from './AxiosInstance';
import * as Security from './Security';
import { is } from 'typescript-is';

export interface CollectionSecurity extends Security.BaseEntitySecurity {
  dynamicRules: Security.DynamicRule[];
  metadata: Record<string, Security.ItemMetadataSecurity>;
  statuses: Record<string, Security.TargetListEntry[]>;
}

export interface Collection extends Common.BaseEntity {
  schema: Common.BaseEntityReference;
  workflow?: Common.BaseEntityReference;
  reviewPeriod?: number;
  security: CollectionSecurity;
  filestoreId: string;
}

const isPagedCollection = (
  instance: unknown
): instance is Common.PagedResult<Collection> =>
  is<Common.PagedResult<Collection>>(instance);

const COLLECTION_ROOT_PATH = '/collection';

/**
 * List all available collections which the currently authenticated user has access to. Results can
 * be customised based on params, and if the `full` param is specified then the return value is
 * actually `Collection` with all details.
 *
 * @param apiBasePath Base URI to the oEQ institution and API
 * @param params Query parameters to customize (and/or page) result
 */
export const listCollections = (
  apiBasePath: string,
  params?: Common.ListCommonParams
): Promise<Common.PagedResult<Common.BaseEntity>> => {
  // Only if the `full` param is specified do you get a whole Collection definition, otherwise
  // it's the bare minimum of BaseEntity.
  const validator = params?.full ? isPagedCollection : Common.isPagedBaseEntity;

  return GET<Common.PagedResult<Common.BaseEntity>>(
    apiBasePath + COLLECTION_ROOT_PATH,
    validator,
    params
  );
};
