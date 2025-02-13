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
import { flow, pipe } from "fp-ts/function";
import * as M from "fp-ts/Map";
import * as O from "fp-ts/Option";
import { not } from "fp-ts/Predicate";
import * as RA from "fp-ts/ReadonlyArray";
import * as RS from "fp-ts/ReadonlySet";
import * as S from "fp-ts/string";
import * as SET from "fp-ts/Set";
import * as TE from "fp-ts/TaskEither";
import { sprintf } from "sprintf-js";
import type {
  CustomRole,
  CustomRolesDetailsMappings,
  CustomRolesMappings,
} from "../CustomRoleHelper";
import {
  BaseSecurityEntity,
  entityIds,
  eqEntityById,
} from "../../modules/ACLEntityModule";
import { getRoleNameByUrn } from "../../modules/Lti13PlatformsModule";
import { languageStrings } from "../../util/langstrings";
import { pfTernary } from "../../util/pointfree";

const { mismatchWarning, savingWarning } = languageStrings.securityEntity;

/**
 * Contains a set of entities and an optional warning message.
 * If the IDs of entity details that gets from server is not match with the initial IDs,
 * it will contain a warning message.
 *
 * The warning message is designed to alert users that one or some of the roles or groups in their
 * previous selections may have been deleted, so they cannot be retrieved from the server.
 * Users should be cautious when saving, as these role and group selections will be lost.
 */
export interface EntityResult<T extends BaseSecurityEntity> {
  entities: ReadonlySet<T>;
  warning?: string[];
}

/**
 * Contains a map of mappings, and an optional warning message in the form of a ReactNode.
 * If the IDs roles that gets from server is not match with the initial IDs,
 * it will contain warning messages.
 *
 * Check more details at {@link EntityResult}
 */
export interface CustomRoleMappingsResult {
  mappings: CustomRolesDetailsMappings;
  warnings?: string[];
}

/**
 * Generates a warning message indicates there are some entity details can't get from server.
 */
export const generateWarnMsgForMissingIds = (
  missingIds: ReadonlySet<string>,
  entityType: "role" | "group",
): string =>
  sprintf(
    mismatchWarning,
    entityType,
    pipe(missingIds, RS.toReadonlyArray(S.Ord), RA.intercalate(S.Monoid)(", ")),
  ) + savingWarning;

/**
 * Fetches entity details given a set of entity IDs.
 * If the number of entities gets from request is not match with the initial ids the return object
 * will contain a warning message.
 *
 * @param entityType The name of the entity type, such as `group` or `role`.
 * @param ids A set of entity IDs for which we want to fetch entity details.
 * @param resolveEntitiesProvider A function that takes an array of entity IDs and returns details of the entity.
 */
const getEntitiesTask = <T extends BaseSecurityEntity>(
  entityType: "role" | "group",
  ids: ReadonlySet<string>,
  resolveEntitiesProvider: (ids: ReadonlySet<string>) => Promise<T[]>,
): TE.TaskEither<string, EntityResult<T>> => {
  const task = (ids: ReadonlySet<string>) =>
    pipe(
      TE.tryCatch(
        () => resolveEntitiesProvider(ids),
        (e) => `Failed to get ${entityType}s by IDs: ${e}`,
      ),
      TE.map(RS.fromReadonlyArray(eqEntityById<T>())),
      TE.map((rolesSet) => ({
        entities: rolesSet,
        warning: pipe(
          ids,
          RS.difference(S.Eq)(entityIds(rolesSet)),
          O.fromPredicate(not(RS.isEmpty)),
          O.map((missingIds) => [
            generateWarnMsgForMissingIds(missingIds, entityType),
          ]),
          O.toUndefined,
        ),
      })),
    );

  return pipe(
    ids,
    pfTernary(
      RS.isEmpty,
      (_) =>
        TE.right({
          entities: new Set<T>(),
          warning: undefined,
        }),
      task,
    ),
  );
};

/**
 * Fetches role details given a set of role IDs.
 * If any error happens it returns an empty set.
 *
 * @param roleIds A set of role IDs for which we want to fetch role details.
 * @param resolveRolesProvider A function that takes an array of role IDs and returns details of the roles.
 */
export const getRolesTask = (
  roleIds: ReadonlySet<string>,
  resolveRolesProvider: (
    ids: ReadonlySet<string>,
  ) => Promise<OEQ.UserQuery.RoleDetails[]>,
): TE.TaskEither<string, EntityResult<OEQ.UserQuery.RoleDetails>> =>
  getEntitiesTask("role", roleIds, resolveRolesProvider);

/**
 * Fetches group details given a set of group IDs.
 * If any error happens it returns an empty set.
 *
 * @param groupsIds A set of group IDs for which we want to fetch group details.
 * @param resolveGroupsProvider A function that takes an array of group IDs and returns details of the groups.
 */
export const getGroupsTask = (
  groupsIds: ReadonlySet<string>,
  resolveGroupsProvider: (
    ids: ReadonlySet<string>,
  ) => Promise<OEQ.UserQuery.GroupDetails[]>,
): TE.TaskEither<string, EntityResult<OEQ.UserQuery.GroupDetails>> =>
  getEntitiesTask("group", groupsIds, resolveGroupsProvider);

/**
 * For a given map of role ids, it resolves the associated role details and
 * returns a new map with the resolved role details.
 *
 * @param customRoles A map where the key is an custom role ID
 *                    and the value is a set of oEQ role IDs associated with this identifier.
 * @param resolveRolesProvider A function that takes an array of role IDs and returns details of the roles.
 * @return a Map where the keys are the custom role names, and the values are sets of full resolved oEQ roles - e.g. including names etc.
 */
export const generateCustomRoles = (
  customRoles: CustomRolesMappings,
  resolveRolesProvider: (
    ids: ReadonlySet<string>,
  ) => Promise<OEQ.UserQuery.RoleDetails[]>,
): TE.TaskEither<string, CustomRoleMappingsResult> => {
  // Raw mapping result for each LTI role
  type CustomRoleMappingResult = [
    string,
    EntityResult<OEQ.UserQuery.RoleDetails>,
  ];

  // It takes a custom role ID and a set of role IDs, and
  // returns a Task that resolves with an array contains the custom role ID and
  // the resolved oEQ role details with possible warning message.
  const resolveRolesForCustomRole = (
    customRole: string,
    oeqRoleIds: ReadonlySet<string>,
  ): TE.TaskEither<string, CustomRoleMappingResult> =>
    pipe(
      getRolesTask(oeqRoleIds, resolveRolesProvider),
      TE.map((roleDetailsSetWithMessage) => [
        customRole,
        roleDetailsSetWithMessage,
      ]),
    );

  // This function creates a JSX Element warning message for the provided custom role.
  const generateWarnMsgForCustomRole = ([
    customRole,
    { warning },
  ]: CustomRoleMappingResult): O.Option<string> =>
    pipe(
      warning,
      O.fromNullable,
      O.map((m) => `Custom role: ${customRole} - ${m}`),
    );

  // generate the final Map structure from rawMappings
  const generateMappings: (
    rawMappings: ReadonlyArray<CustomRoleMappingResult>,
  ) => CustomRolesDetailsMappings = flow(
    // Get role set
    RA.map<
      CustomRoleMappingResult,
      [CustomRole, Set<OEQ.UserQuery.RoleDetails>]
    >(([ltiRole, roleDetailsWithWarningMessage]) => [
      {
        role: ltiRole,
        name: getRoleNameByUrn(ltiRole),
      },
      pipe(roleDetailsWithWarningMessage.entities, RS.toSet),
    ]),
    // Remove empty elements
    RA.filter(([_, oeqRoleSet]) => !SET.isEmpty(oeqRoleSet)),
    // Build map
    (mappings) => new Map(mappings),
  );

  // This function first maps the input role Map to a TaskEither array with each TaskEither resolving
  // to a raw mapping. Then, the array of raw mappings is used to generate warning messages and final mappings
  const task = (
    roles: Map<string, Set<string>>,
  ): TE.TaskEither<string, CustomRoleMappingsResult> =>
    pipe(
      roles,
      M.toArray(S.Ord),
      // create a TE array.
      A.map(([ltiRole, roleIds]) =>
        resolveRolesForCustomRole(ltiRole, roleIds),
      ),
      TE.sequenceArray,
      TE.map((rawMappings) => ({
        mappings: generateMappings(rawMappings),
        warnings: pipe(
          rawMappings,
          RA.map(generateWarnMsgForCustomRole),
          RA.compact,
          O.fromPredicate(not(RA.isEmpty)),
          O.map(RA.toArray),
          O.toUndefined,
        ),
      })),
    );

  return pipe(
    customRoles,
    pfTernary(M.isEmpty, (_) => TE.of({ mappings: new Map() }), task),
  );
};
