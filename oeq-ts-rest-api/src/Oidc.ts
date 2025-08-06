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
import * as O from 'fp-ts/Option';
import { GET, PUT } from './AxiosInstance';
import type { UuidString } from './Common';
import { toRecord, fromRecord } from './fp-ts-extended/Map.extended';
import { fromStringArray, toStringArray } from './fp-ts-extended/Set.extended';
import { IdentityProviderResponseCodec } from './gen/Oidc';
import { validate } from './Utils';

const OIDC_CONFIG_PATH = '/oidc/config';

export type IdentityProviderPlatform = 'AUTH0' | 'ENTRA_ID' | 'OKTA';

/**
 * Data structure for the common details of an Identity Provider, containing fields
 * with primitive types.
 **/
interface CommonDetailsBase {
  /**
   * One of the supported Identity Provider: {@link IdentityProviderPlatform}
   */
  platform: IdentityProviderPlatform;
  /**
   * The issuer identifier for the OpenID Connect provider. This value should match the 'iss'
   * claim in the JWTs issued by this provider.
   */
  issuer: string;
  /**
   * ID of an OAuth2 client registered in the selected Identity Provider, used specifically in
   * the Authorization Code flow
   */
  authCodeClientId: string;
  /**
   * Secret key used specifically in the Authorization Code flow. Being optional as the value is
   * unknown on client, and it's not required on server if it is already configured.
   */
  authCodeClientSecret?: string;
  /**
   * The URL used to initiate the OAuth2 authorisation process
   */
  authUrl: string;
  /**
   * The URL where the OAuth2 client's public keys are located
   */
  keysetUrl: string;
  /**
   * The URL used to obtain an access token from the selected Identity Provider
   */
  tokenUrl: string;
  /**
   * Custom claim used to retrieve a meaningful username from an ID token
   */
  usernameClaim?: string;
  /**
   * Attribute configured on an IdP to provide the correct ID for a user
   */
  userIdAttribute?: string;
  /**
   * Whether the Identity Provider configuration is enabled
   */
  enabled: boolean;
}

/**
 * Full structure of an Identity Provider where types are looser in order to be handled by axios.
 */
interface CommonDetailsRaw extends CommonDetailsBase {
  /**
   * A list of default OEQ roles to assign to the logged-in user.
   */
  defaultRoles: UuidString[];
  /**
   * Optional configuration for custom roles assigned to the logged-in user.
   */
  roleConfig?: {
    /**
     * Custom claim used to retrieve meaningful roles from an ID token.
     */
    roleClaim: string;
    /**
     * A mapping between IdP roles and OEQ roles where one IdP role can map to multiple OEQ roles.
     */
    customRoles: Record<string, UuidString[]>;
  };
}

/**
 * Full structure of an Identity Provider, including default roles and custom role configuration.
 */
export interface CommonDetails extends CommonDetailsBase {
  /**
   * A list of default OEQ roles to assign to the logged-in user.
   */
  defaultRoles: Set<UuidString>;
  /**
   * Optional configuration for custom roles assigned to the logged-in user.
   */
  roleConfig?: {
    /**
     * Custom claim used to retrieve meaningful roles from an ID token.
     */
    roleClaim: string;
    /**
     * A mapping between IdP roles and OEQ roles where one IdP role can map to multiple OEQ roles.
     */
    customRoles: Map<string, Set<UuidString>>;
  };
}

/**
 * API details configured for the use of an Identity Provider's REST APIs.
 */
export interface RestApiDetails {
  /**
   * The API endpoint for the Identity Provider, use for operations such as search for users.
   */
  apiUrl: string;
  /**
   *  Client ID used to get an Authorisation Token to use with the Identity Provider's API.
   */
  apiClientId: string;
  /**
   * Client Secret used with `apiClientId` to get an Authorization Token to use with the Identity Provider's API.
   */
  apiClientSecret?: string;
}

interface EntraId extends CommonDetails, RestApiDetails {
  platform: 'ENTRA_ID';
}

interface Auth0 extends CommonDetails, RestApiDetails {
  platform: 'AUTH0';
}

interface Okta extends CommonDetails, RestApiDetails {
  platform: 'OKTA';
}

export type IdentityProvider = EntraId | Auth0 | Okta;

/**
 * Data structure for the response of an Identity Provider where common details are consolidated in one field and secret values are excluded.
 */
interface IdentityProviderResponse {
  commonDetails: CommonDetailsRaw;
  /**
   * The API endpoint for the Identity Provider, use for operations such as search for users.
   */
  apiUrl: string;
  /**
   *  Client ID used to get an Authorisation Token to use with the Identity Provider's API.
   */
  apiClientId: string;
}

const toIdentityProviderRaw = (idp: IdentityProvider): CommonDetailsRaw => ({
  ...idp,
  defaultRoles: toStringArray(idp.defaultRoles),
  roleConfig: pipe(
    idp.roleConfig,
    O.fromNullable,
    O.map(({ roleClaim, customRoles }) => ({
      roleClaim,
      customRoles: toRecord(customRoles, toStringArray),
    })),
    O.toUndefined
  ),
});

const toIdentityProvider = ({
  commonDetails,
  ...platformSpecific
}: IdentityProviderResponse): IdentityProvider => ({
  ...commonDetails,
  defaultRoles: fromStringArray(commonDetails.defaultRoles),
  roleConfig: pipe(
    commonDetails.roleConfig,
    O.fromNullable,
    O.map(({ roleClaim, customRoles }) => ({
      roleClaim,
      customRoles: fromRecord(customRoles, fromStringArray),
    })),
    O.toUndefined
  ),
  ...platformSpecific,
  // Because server does not return the client secret, set it to undefined.
  apiClientSecret: undefined,
});

/**
 * Retrieve the Identity Provider configuration.
 *
 * @param apiBasePath Base URI to the oEQ institution and API
 */
export const getIdentityProvider = (
  apiBasePath: string
): Promise<IdentityProvider> =>
  GET(
    `${apiBasePath}${OIDC_CONFIG_PATH}`,
    validate(IdentityProviderResponseCodec)
  ).then(toIdentityProvider);

/**
 * Update the Identity Provider configuration.
 *
 * @param apiBasePath Base URI to the oEQ institution and API
 * @param idp The configuration of an Identity Provider.
 */
export const updateIdentityProvider = (
  apiBasePath: string,
  idp: IdentityProvider
): Promise<void> =>
  PUT(`${apiBasePath}${OIDC_CONFIG_PATH}`, toIdentityProviderRaw(idp));
