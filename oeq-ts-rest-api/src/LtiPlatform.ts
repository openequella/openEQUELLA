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
import { pipe } from 'fp-ts/function';
import { DELETE, GET, POST_void, PUT } from './AxiosInstance';
import { BatchOperationResponse } from './BatchOperationResponse';
import { LtiPlatformRawCodec } from './gen/LtiPlatform';
import { validate } from './Utils';
import * as t from 'io-ts';
import * as A from 'fp-ts/Array';
import * as M from 'fp-ts/Map';

export type UnknownUserHandling = 'ERROR' | 'GUEST' | 'CREATE';

export interface LtiPlatformBase {
  /**
   * ID of the learning platform which must be double URL encoded
   */
  platformId: string;
  /**
   * Client ID provided by the platform
   */
  clientId: string;
  /**
   * The platform's authentication request URL
   */
  authUrl: string;
  /**
   * JWKS keyset URL where to get the keys
   */
  keysetUrl: string;
  /**
   * Prefix added to the user ID from the LTI request
   */
  usernamePrefix?: string;
  /**
   * Suffix added to the user ID from the LTI request
   */
  usernameSuffix?: string;
  /**
   * How to handle unknown users by one of the three options - ERROR, GUEST OR CREATE
   */
  unknownUserHandling: UnknownUserHandling;
  /**
   * The ACL Expression to control access from this platform
   */
  allowExpression?: string;
  /**
   * `true` if the platform is enabled
   */
  enabled: boolean;
}

/**
 * Raw structure of LTI Platform returned from the API.
 */
export interface LtiPlatformRaw extends LtiPlatformBase {
  /**
   * A list of roles to be assigned to a LTI instructor role
   */
  instructorRoles: string[];
  /**
   * A list of roles to be assigned to a LTI role that is neither the instructor or in the list of custom roles
   */
  unknownRoles: string[];
  /**
   * Mappings from LTI roles to OEQ roles
   */
  customRoles: Record<string, string[]>;
  /**
   * The list of groups to be added to the user object If the unknown user handling is CREATE
   */
  unknownUserDefaultGroups?: string[];
}

/**
 * Structure of LTI platform which replace `Record` and `Array` with `Map` and `Set` respectively to match
 * the LTI platform data structure defined on server side.
 */
export interface LtiPlatform extends LtiPlatformBase {
  instructorRoles: Set<string>;
  unknownRoles: Set<string>;
  customRoles: Map<string, Set<string>>;
  unknownUserDefaultGroups?: Set<string>;
}

const convertToLtiPlatform = (platform: LtiPlatformRaw): LtiPlatform => ({
  ...platform,
  instructorRoles: new Set(platform.instructorRoles),
  unknownRoles: new Set(platform.unknownRoles),
  unknownUserDefaultGroups: new Set(platform.unknownUserDefaultGroups),
  customRoles: new Map(
    Object.entries(platform.customRoles).map(([key, value]) => [
      key,
      new Set(value),
    ])
  ),
});

export const convertToRawLtiPlatform = (
  platform: LtiPlatform
): LtiPlatformRaw => ({
  ...platform,
  instructorRoles: Array.from(platform.instructorRoles),
  unknownRoles: Array.from(platform.unknownRoles),
  unknownUserDefaultGroups: Array.from(platform.unknownUserDefaultGroups ?? []),
  customRoles: pipe(
    platform.customRoles,
    M.map<Set<string>, string[]>(Array.from),
    Object.fromEntries
  ),
});

const LTI_PLATFORM_PATH = '/ltiplatform';

/**
 * Retrieve one LTI platform by platform ID.
 *
 * @param apiBasePath Base URI to the oEQ institution and API
 * @param platformId LTI platform ID which must be double URL encoded
 */
export const getPlatformById = (
  apiBasePath: string,
  platformId: string
): Promise<LtiPlatform> =>
  GET(
    `${apiBasePath}${LTI_PLATFORM_PATH}/${platformId}`,
    validate(LtiPlatformRawCodec)
  ).then(convertToLtiPlatform);

/**
 * List all the LTI platforms for the institution.
 *
 * @param apiBasePath Base URI to the oEQ institution and API
 */
export const getAllPlatforms = (apiBasePath: string): Promise<LtiPlatform[]> =>
  GET(
    `${apiBasePath}${LTI_PLATFORM_PATH}`,
    validate(t.array(LtiPlatformRawCodec))
  ).then(A.map(convertToLtiPlatform));

/**
 * As Axios cannot directly send a POST request with data of type `Map` or `Set`, this function firstly processes Lti platform
 * data structure, and then use transformed structure to create a new LTI platform.
 *
 * @param apiBasePath Base URI to the oEQ institution and API
 * @param platform LTI platform details to be used to create a new platform
 */
export const createPlatform = (
  apiBasePath: string,
  platform: LtiPlatform
): Promise<void> =>
  POST_void(
    `${apiBasePath}${LTI_PLATFORM_PATH}`,
    convertToRawLtiPlatform(platform)
  );

/**
 * Similar to `createPlatform`, this function processes Lti platform data structure and then use transformed structure to
 * update an existing LTI platform.
 *
 * @param apiBasePath Base URI to the oEQ institution and API
 * @param platform LTI platform details to be used to update an existing platform
 */
export const updatePlatform = (
  apiBasePath: string,
  platform: LtiPlatform
): Promise<void> =>
  PUT(`${apiBasePath}${LTI_PLATFORM_PATH}`, convertToRawLtiPlatform(platform));

/**
 * Delete one LTI platform by platform ID.
 *
 * @param apiBasePath Base URI to the oEQ institution and API
 * @param platformId LTI platform ID which must be double URL encoded
 */
export const deletePlatformById = (
  apiBasePath: string,
  platformId: string
): Promise<void> => DELETE(`${apiBasePath}${LTI_PLATFORM_PATH}/${platformId}`);

/**
 * Bulk delete LTI platforms.
 *
 * @param apiBasePath Base URI to the oEQ institution and API
 * @param platformIds A list of LTI platform IDs
 */
export const deletePlatforms = (
  apiBasePath: string,
  platformIds: string[]
): Promise<BatchOperationResponse[]> =>
  DELETE(`${apiBasePath}${LTI_PLATFORM_PATH}`, { ids: platformIds });
