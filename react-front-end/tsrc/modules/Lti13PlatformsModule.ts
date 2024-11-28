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
import * as OEQ from "@openequella/rest-api-client";
import * as A from "fp-ts/Array";
import * as EQ from "fp-ts/Eq";
import { constant, pipe } from "fp-ts/function";
import * as O from "fp-ts/Option";
import * as ORD from "fp-ts/Ord";
import * as R from "fp-ts/Record";
import * as S from "fp-ts/string";
import { shallowEqual } from "shallow-equal-object";
import { API_BASE_URL, getBaseUrl } from "../AppConfig";
import { languageStrings } from "../util/langstrings";

const {
  providerDetails: {
    toolUrl: toolUrlLabel,
    keysetUrl: keysetUrlinitiaLabel,
    initialLoginUrl: initialLoginUrlLabel,
    redirectionUrl: redirectionUrlLabel,
    contentSelectionUrl: contentSelectionUrlLabel,
  },
} = languageStrings.settings.integration.lti13PlatformsSettings;
const { ltiRoles: ltiRolesStrings } =
  languageStrings.settings.integration.lti13PlatformsSettings.createPage;

const baseUrl = getBaseUrl();

export interface ProviderDetail {
  readonly name: string;
  readonly value: string;
}

export const keysetUrlDetails = {
  name: keysetUrlinitiaLabel,
  value: `${baseUrl}.well-known/jwks.json`,
};

/**
 * LTI 1.3 provider details which can be used to configure LTI platform.
 */
export const providerDetails: { [key: string]: ProviderDetail } = {
  toolUrl: {
    name: toolUrlLabel,
    value: baseUrl,
  },
  keysetUrl: keysetUrlDetails,
  initialLoginUrl: {
    name: initialLoginUrlLabel,
    value: `${baseUrl}lti13/launch`,
  },
  redirectionUrlUrl: {
    name: redirectionUrlLabel,
    value: `${baseUrl}lti13/launch`,
  },
  contentSelectionUrl: {
    name: contentSelectionUrlLabel,
    value: baseUrl,
  },
};

/**
 * Explicit LTI 1.3 role IDs and names.
 * The following roles are written according to the LTI 1.3 standard.
 * Reference link: https://www.imsglobal.org/spec/lti/v1p3/#role-vocabularies
 */
export const ltiRoleIds = {
  system: {
    Administrator: ltiRolesStrings.system.Administrator,
    None: ltiRolesStrings.system.None,
    AccountAdmin: ltiRolesStrings.system.AccountAdmin,
    Creator: ltiRolesStrings.system.Creator,
    SysAdmin: ltiRolesStrings.system.SysAdmin,
    SysSupport: ltiRolesStrings.system.SysSupport,
    User: ltiRolesStrings.system.User,
  },
  institution: {
    Administrator: ltiRolesStrings.institution.Administrator,
    Faculty: ltiRolesStrings.institution.Faculty,
    Guest: ltiRolesStrings.institution.Guest,
    None: ltiRolesStrings.institution.None,
    Other: ltiRolesStrings.institution.Other,
    Staff: ltiRolesStrings.institution.Staff,
    Student: ltiRolesStrings.institution.Student,
    Alumni: ltiRolesStrings.institution.Alumni,
    Instructor: ltiRolesStrings.institution.Instructor,
    Learner: ltiRolesStrings.institution.Learner,
    Member: ltiRolesStrings.institution.Member,
    Mentor: ltiRolesStrings.institution.Mentor,
    Observer: ltiRolesStrings.institution.Observer,
    ProspectiveStudent: ltiRolesStrings.institution.ProspectiveStudent,
  },
  context: {
    Administrator: ltiRolesStrings.context.Administrator,
    ContentDeveloper: ltiRolesStrings.context.ContentDeveloper,
    Instructor: ltiRolesStrings.context.Instructor,
    Learner: ltiRolesStrings.context.Learner,
    Mentor: ltiRolesStrings.context.Mentor,
    Manager: ltiRolesStrings.context.Manager,
    Member: ltiRolesStrings.context.Member,
    Officer: ltiRolesStrings.context.Officer,
  },
};

/**
 * Defines detailed information for an LTI role,
 * including the name of the role and its Uniform Resource Name (URN).
 * Name and URN are written according to the LTI 1.3 standard.
 * Reference link: https://www.imsglobal.org/spec/lti/v1p3/#role-vocabularies
 */
export interface LtiRoleDetails {
  name: string;
  urn: string;
}

/**
 * Generate an array that includes the role name, and role URN for each role.
 *
 * @params roles The role ids.
 * @params urnPrefix The prefix of role URN.
 */
const mapRoles = (
  roleIds: Record<string, string>,
  urnPrefix: string,
): LtiRoleDetails[] =>
  pipe(
    roleIds,
    R.collect(S.Ord)((roleId, roleName) => ({
      name: roleName,
      urn: `${urnPrefix}${roleId}`,
    })),
  );

/**
 * An array that includes the role name, and role URN for each role.
 */
export const ltiRoles: LtiRoleDetails[] = [
  ...mapRoles(
    ltiRoleIds.system,
    "http://purl.imsglobal.org/vocab/lis/v2/system/person#",
  ),
  ...mapRoles(
    ltiRoleIds.institution,
    "http://purl.imsglobal.org/vocab/lis/v2/institution/person#",
  ),
  ...mapRoles(
    ltiRoleIds.context,
    "http://purl.imsglobal.org/vocab/lis/v2/membership#",
  ),
];

export const defaultSelectedRoleUrn =
  "http://purl.imsglobal.org/vocab/lis/v2/system/person#Administrator";

/**
 * Get the corresponding role name by provided URN.
 */
export const getRoleNameByUrn = (roleUrn: string): string =>
  pipe(
    ltiRoles,
    A.findFirst(({ urn }) => roleUrn === urn),
    O.map(({ name }) => name),
    O.getOrElse(constant(roleUrn)),
  );

/**
 * Ord for `OEQ.LtiPlatform.LtiPlatform` with order based on the platform's name.
 */
export const platformOrd: ORD.Ord<OEQ.LtiPlatform.LtiPlatform> = ORD.contramap(
  (p: OEQ.LtiPlatform.LtiPlatform) => p.name,
)(S.Ord);

/**
 * Eq for `OEQ.LtiPlatform.LtiPlatform` with equality based on the `shallowEqual`.
 */
export const platformEq = EQ.fromEquals(
  (a: OEQ.LtiPlatform.LtiPlatform, b: OEQ.LtiPlatform.LtiPlatform) =>
    shallowEqual(a, b),
);

/**
 * Get platform by ID.
 */
export const getPlatform = (
  platformId: string,
): Promise<OEQ.LtiPlatform.LtiPlatform> =>
  OEQ.LtiPlatform.getPlatformById(API_BASE_URL, platformId);

/**
 * Provide all platforms in a list.
 */
export const getPlatforms = (): Promise<OEQ.LtiPlatform.LtiPlatform[]> =>
  OEQ.LtiPlatform.getAllPlatforms(API_BASE_URL);

/**
 * Update enabled status for platforms.
 *
 * @params enabledStatus An array of platform id with the new value of enabled status.
 */
export const updateEnabledPlatforms = (
  enabledStatus: OEQ.LtiPlatform.LtiPlatformEnabledStatus[],
): Promise<OEQ.BatchOperationResponse.BatchOperationResponse[]> =>
  OEQ.LtiPlatform.updateEnabledPlatforms(API_BASE_URL, enabledStatus);

/**
 * Create a new platform.
 */
export const createPlatform = (
  platform: OEQ.LtiPlatform.LtiPlatform,
): Promise<void> => OEQ.LtiPlatform.createPlatform(API_BASE_URL, platform);

/**
 * Update an existing platform.
 */
export const updatePlatform = (
  platform: OEQ.LtiPlatform.LtiPlatform,
): Promise<void> => OEQ.LtiPlatform.updatePlatform(API_BASE_URL, platform);

/**
 * Delete a list of LTI platforms.
 *
 * @param platformIds Array of platform identifiers to be deleted.
 */
export const deletePlatforms = (
  platformIds: string[],
): Promise<OEQ.BatchOperationResponse.BatchOperationResponse[]> =>
  OEQ.LtiPlatform.deletePlatforms(API_BASE_URL, platformIds);

/**
 * Rotate Key pair for an platform by ID and return the new key pair ID.
 */
export const rotateKeyPair = (platformId: string): Promise<string> =>
  OEQ.LtiPlatform.rotateKeyPair(API_BASE_URL, platformId);
