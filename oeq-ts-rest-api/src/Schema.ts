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
import { is } from 'typescript-is';

export interface Citation {
  name: string;
  transformation: string;
}

export interface Schema extends Common.BaseEntity {
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

/**
 * Helper function for a standard validator for EquellaSchema instances via typescript-is.
 *
 * @param instance An instance to validate.
 */
export const isEquellaSchema = (instance: unknown): instance is EquellaSchema =>
  is<EquellaSchema>(instance);

/**
 * Helper function for a standard validator for EquellaSchema instances wrapped in a PagedResult
 * via typescript-is.
 *
 * @param instance An instance to validate.
 */
export const isPagedEquellaSchema = (
  instance: unknown
): instance is Common.PagedResult<EquellaSchema> =>
  is<Common.PagedResult<EquellaSchema>>(instance);

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
  params?: Common.ListCommonParams
): Promise<Common.PagedResult<Common.BaseEntity>> => {
  // Only if the `full` param is specified do you get a whole Schema definition, otherwise
  // it's the bare minimum of BaseEntity.
  const validator = params?.full
    ? isPagedEquellaSchema
    : Common.isPagedBaseEntity;

  return GET<Common.PagedResult<Common.BaseEntity>>(
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
    isEquellaSchema
  );
