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
 * This module matches oEQ REST endpoint '/hierarchy' and provides functions to update key resource.
 */

import { DELETE, GET, POST_void } from './AxiosInstance';
import { HierarchyTopicAclCodec } from './gen/Hierarchy';
import { validate } from './Utils';

const HIERARCHY_ROOT_PATH = '/hierarchy';

/**
 * Provides the ACLs for a hierarchy topic.
 */
export interface HierarchyTopicAcl {
  /**
   * Whether the user has permission to view the topic.
   */
  VIEW_HIERARCHY_TOPIC: boolean;
  /**
   * Whether the user has permission to edit the topic.
   */
  EDIT_HIERARCHY_TOPIC: boolean;
  /**
   * Whether the user has permission to modify the key resource of the topic.
   */
  MODIFY_KEY_RESOURCE: boolean;
}

/**
 * Retrieve all ACLs for the hierarchy topic.
 *
 * @param apiBasePath Base URI to the oEQ institution and API.
 * @param compoundUuid Compound UUID for topic.
 */
export const getMyAcls = (
  apiBasePath: string,
  compoundUuid: string
): Promise<HierarchyTopicAcl> =>
  GET<HierarchyTopicAcl>(
    apiBasePath + HIERARCHY_ROOT_PATH + `/${compoundUuid}/my-acls`,
    validate(HierarchyTopicAclCodec)
  );

/**
 * Add a key resource to a hierarchy topic
 *
 * @param apiBasePath Base URI to the oEQ institution and API.
 * @param compoundUuid Topic compound UUID.
 * @param itemUuid The item UUID.
 * @param itemVersion The item version. 0 for latest version.
 */
export const addKeyResource = (
  apiBasePath: string,
  compoundUuid: string,
  itemUuid: string,
  itemVersion: number
): Promise<void> =>
  POST_void<void>(
    apiBasePath +
      HIERARCHY_ROOT_PATH +
      `/${compoundUuid}/keyresource/${itemUuid}/${itemVersion}`
  );

/**
 * Delete a key resource from a hierarchy topic
 *
 * @param apiBasePath Base URI to the oEQ institution and API.
 * @param compoundUuid Topic compound UUID.
 * @param itemUuid The item UUID.
 * @param itemVersion The item version.
 */
export const deleteKeyResource = (
  apiBasePath: string,
  compoundUuid: string,
  itemUuid: string,
  itemVersion: number
): Promise<void> =>
  DELETE<void>(
    apiBasePath +
      HIERARCHY_ROOT_PATH +
      `/${compoundUuid}/keyresource/${itemUuid}/${itemVersion}`
  );
