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
import * as A from 'fp-ts/Array';
import { pipe } from 'fp-ts/function';
import * as O from 'fp-ts/Option';
import * as t from 'io-ts';
import { DELETE, GET, POST_void, PUT } from './AxiosInstance';
import type { BatchOperationResponse } from './BatchOperationResponse';
import { toRecord, fromRecord } from './fp-ts-extended/Map.extended';
import { fromStringArray, toStringArray } from './fp-ts-extended/Set.extended';
import { LtiPlatformRawCodec } from './gen/LtiPlatform';
import { validate } from './Utils';

export type UnknownUserHandling = 'ERROR' | 'GUEST' | 'CREATE';

export interface LtiPlatformBase {
  /**
   * ID of the learning platform.
   */
  platformId: string;
  /**
   * Name of the learning platform.
   */
  name: string;
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
   * The claim used to retrieve username from the LTI request.
   */
  usernameClaim?: string;
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
   * The activated key pair ID (Readonly)
   */
  kid?: string;
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
  /**
   * A list of roles to be assigned to a LTI instructor role
   */
  instructorRoles: Set<string>;
  /**
   * A list of roles to be assigned to a LTI role that is neither the instructor or in the list of custom roles
   */
  unknownRoles: Set<string>;
  /**
   * Mappings from LTI roles to OEQ roles
   */
  customRoles: Map<string, Set<string>>;
  /**
   * The list of groups to be added to the user object If the unknown user handling is CREATE
   */
  unknownUserDefaultGroups?: Set<string>;
}

/**
 * Data structure used for setting enabled status for LTI platform.
 */
export interface LtiPlatformEnabledStatus {
  /**
   * ID of the LTI platform.
   */
  platformId: string;
  /**
   * New value of the enabled status.
   */
  enabled: boolean;
}

const convertToLtiPlatform = (platform: LtiPlatformRaw): LtiPlatform => ({
  ...platform,
  instructorRoles: fromStringArray(platform.instructorRoles),
  unknownRoles: fromStringArray(platform.unknownRoles),
  unknownUserDefaultGroups: pipe(
    O.fromNullable(platform.unknownUserDefaultGroups),
    O.map(fromStringArray),
    O.toUndefined
  ),
  customRoles: fromRecord(platform.customRoles, fromStringArray),
});

export const convertToRawLtiPlatform = (
  platform: LtiPlatform
): LtiPlatformRaw => ({
  ...platform,
  instructorRoles: toStringArray(platform.instructorRoles),
  unknownRoles: toStringArray(platform.unknownRoles),
  unknownUserDefaultGroups: pipe(
    O.fromNullable(platform.unknownUserDefaultGroups),
    O.map(toStringArray),
    O.toUndefined
  ),
  customRoles: toRecord(platform.customRoles, toStringArray),
});

// Helper function to encode the provided text twice.
const doubleEncoded = (text: string): string =>
  encodeURIComponent(encodeURIComponent(text));

const LTI_PLATFORM_PATH = '/ltiplatform';

/**
 * Retrieve one LTI platform by platform ID.
 *
 * @param apiBasePath Base URI to the oEQ institution and API
 * @param platformId ID of the learning platform
 */
export const getPlatformById = (
  apiBasePath: string,
  platformId: string
): Promise<LtiPlatform> =>
  GET(
    `${apiBasePath}${LTI_PLATFORM_PATH}/${doubleEncoded(platformId)}`,
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
 * Create a new LTI platform with the provided LTI platform details.
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
    // As Axios cannot directly send a POST request with data of type `Map` or `Set`, we need to transform
    // `Map` to `Record` and `Set` to `Array`.
    convertToRawLtiPlatform(platform)
  );

/**
 * update an existing LTI platform with the provided LTI platform details.
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
 * @param platformId ID of the learning platform
 */
export const deletePlatformById = (
  apiBasePath: string,
  platformId: string
): Promise<void> =>
  DELETE(`${apiBasePath}${LTI_PLATFORM_PATH}/${doubleEncoded(platformId)}`);

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

/**
 * Bulk update enabled status for LTI platforms.
 *
 * @param apiBasePath Base URI to the oEQ institution and API
 * @param idWithStatus A list of LTI platform IDs with enabled status
 */
export const updateEnabledPlatforms = (
  apiBasePath: string,
  idWithStatus: LtiPlatformEnabledStatus[]
): Promise<BatchOperationResponse[]> =>
  PUT(`${apiBasePath}${LTI_PLATFORM_PATH}/enabled`, idWithStatus);

/**
 * Rotate key pair for one LTI platform by platform ID and get the new key pair ID.
 *
 * @param apiBasePath Base URI to the oEQ institution and API
 * @param platformId ID of the learning platform
 */
export const rotateKeyPair = (
  apiBasePath: string,
  platformId: string
): Promise<string> =>
  GET(
    `${apiBasePath}${LTI_PLATFORM_PATH}/${doubleEncoded(
      platformId
    )}/rotated-keys`,
    validate(t.string)
  );
