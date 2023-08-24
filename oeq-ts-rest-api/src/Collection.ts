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
import { GET } from './AxiosInstance';
import type {
  BaseEntity,
  BaseEntityReference,
  ListCommonParams,
  PagedResult,
} from './Common';
import { isPagedBaseEntity } from './Common';
import { CollectionCodec } from './gen/Collection';
import { PagedResultCodec } from './gen/Common';
import type {
  BaseEntitySecurity,
  DynamicRule,
  ItemMetadataSecurity,
  TargetListEntry,
} from './Security';
import { validate } from './Utils';

export interface CollectionSecurity extends BaseEntitySecurity {
  dynamicRules: DynamicRule[];
  metadata: Record<string, ItemMetadataSecurity>;
  statuses: Record<string, TargetListEntry[]>;
}

export interface Collection extends BaseEntity {
  schema: BaseEntityReference;
  workflow?: BaseEntityReference;
  reviewPeriod?: number;
  security: CollectionSecurity;
  filestoreId: string;
}

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
  params?: ListCommonParams
): Promise<PagedResult<BaseEntity>> => {
  // Only if the `full` param is specified do you get a whole Collection definition, otherwise
  // it's the bare minimum of BaseEntity.
  const validator = params?.full
    ? validate(PagedResultCodec(CollectionCodec))
    : isPagedBaseEntity;

  return GET<PagedResult<BaseEntity>>(
    apiBasePath + COLLECTION_ROOT_PATH,
    validator,
    params
  );
};
