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
import { validate } from './Utils';

export const ACL_SEARCH_COLLECTION = 'SEARCH_COLLECTION';
export const SEARCH_PAGE = 'SEARCH_PAGE';
export const HIERARCHY_PAGE = 'HIERARCHY_PAGE';
export const EDIT_SYSTEM_SETTINGS = 'EDIT_SYSTEM_SETTINGS';
export const MANAGE_CLOUD_PROVIDER = 'MANAGE_CLOUD_PROVIDER';
export const VIEW_HIERARCHY_TOPIC = 'VIEW_HIERARCHY_TOPIC';

/**
 * The unique ID of each system setting.
 */
export type SETTING =
  | 'loginnoticeeditor'
  | 'lti13platforms'
  | 'searching'
  | 'theme'
  | 'oidc';

const ACL_PRIVILEGE_CHECK_PATH = '/acl/privilegecheck';

/**
 * Given a list of non-entity privileges, return those granted to the current user.
 *
 * @param apiBasePath Base URI to the oEQ institution and API
 * @param privileges Privileges to check for the current user
 */
export const checkPrivilege = (
  apiBasePath: string,
  privileges: string[]
): Promise<string[]> =>
  GET(apiBasePath + ACL_PRIVILEGE_CHECK_PATH, validate(t.array(t.string)), {
    privilege: privileges,
  });

/**
 * Check if the provided privilege is granted to the current user for a specific system setting.
 *
 * @param apiBasePath Base URI to the oEQ institution and API
 * @param setting The setting to be checked against
 * @param privilege Privilege to check for the current user
 */
export const checkSettingPrivilege = (
  apiBasePath: string,
  setting: SETTING,
  privilege: string
): Promise<boolean> =>
  GET(
    apiBasePath + ACL_PRIVILEGE_CHECK_PATH + `/setting/${setting}`,
    validate(t.boolean),
    { privilege }
  );

/**
 * Check if the provided privilege is granted to the current user for a specific Hierarchy topic.
 *
 * @param apiBasePath Base URI to the oEQ institution and API
 * @param topic Compound UUID of the topic
 * @param privilege Privilege to check for the current user
 */
export const checkHierarchyPrivilege = (
  apiBasePath: string,
  topic: string,
  privilege: string
): Promise<boolean> =>
  GET(
    apiBasePath + ACL_PRIVILEGE_CHECK_PATH + `/hierarchy/${topic}`,
    validate(t.boolean),
    { privilege }
  );
