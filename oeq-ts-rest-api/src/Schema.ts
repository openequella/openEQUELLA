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
import type { BaseEntity, PagedResult } from './Common';
import { isPagedBaseEntity, ListCommonParams } from './Common';
import { PagedResultCodec } from './gen/Common';
import { EquellaSchemaCodec } from './gen/Schema';
import { validate } from './Utils';

export interface Citation {
  name: string;
  transformation: string;
}

export interface Schema extends BaseEntity {
  namePath: string;
  descriptionPath: string;
  /**
   * Typically a tree of objects representing an XML schema - so first entry is normally "xml".
   */
  definition: Record<string, unknown>;
}

export interface EquellaSchema extends Schema {
  citations: Citation[];
  exportTransformsMap: Record<string, string>;
  importTransformsMap: Record<string, string>;
  ownerUuid: string;
  serializedDefinition: string;
}

const SCHEMA_ROOT_PATH = '/schema';

/**
 * List all available schemas which the currently authenticated user has access to. Results can
 * be customised based on params, and if the `full` param is specified then the return value is
 * actually EquellaSchema with all details.
 *
 * @param apiBasePath Base URI to the oEQ institution and API
 * @param params Query parameters to customize (and/or page) result
 */
export const listSchemas = (
  apiBasePath: string,
  params?: ListCommonParams
): Promise<PagedResult<BaseEntity>> => {
  // Only if the `full` param is specified do you get a whole Schema definition, otherwise
  // it's the bare minimum of BaseEntity.
  const validator = params?.full
    ? validate(PagedResultCodec(EquellaSchemaCodec))
    : isPagedBaseEntity;

  return GET<PagedResult<BaseEntity>>(
    apiBasePath + SCHEMA_ROOT_PATH,
    validator,
    params
  );
};

/**
 * Get details of a specific schema as specified by the provided UUID.
 *
 * @param apiBasePath Base URI to the oEQ institution and API
 * @param uuid UUID of the schema to be retrieved.
 */
export const getSchema = (
  apiBasePath: string,
  uuid: string
): Promise<EquellaSchema> =>
  GET<EquellaSchema>(
    apiBasePath + `${SCHEMA_ROOT_PATH}/${uuid}`,
    validate(EquellaSchemaCodec)
  );
