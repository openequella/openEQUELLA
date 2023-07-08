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
import { flow, identity, pipe } from "fp-ts/function";
import * as M from "fp-ts/Map";
import * as RS from "fp-ts/ReadonlySet";
import * as S from "fp-ts/string";
import * as T from "fp-ts/Task";
import * as TE from "fp-ts/TaskEither";
import {
  BaseSecurityEntity,
  eqEntityById,
} from "../../../modules/ACLEntityModule";
import { pfTernary } from "../../../util/pointfree";

/**
 * Fetches entity details given a set of entity IDs.
 *
 * @param ids A set of entity IDs for which we want to fetch entity details.
 * @param resolveEntitiesProvider A function that takes an array of entity IDs and returns details of the entity.
 */
const getEntitiesTask = <T extends BaseSecurityEntity>(
  ids: ReadonlySet<string>,
  resolveEntitiesProvider: (ids: ReadonlyArray<string>) => Promise<T[]>
): TE.TaskEither<string, ReadonlySet<T>> => {
  const task = (ids: ReadonlySet<string>) =>
    pipe(
      TE.tryCatch(
        () => resolveEntitiesProvider(RS.toReadonlyArray(S.Ord)(ids)),
        String
      ),
      TE.chainNullableK(`Can't find entities by IDs: ${ids}`)(identity),
      TE.map(RS.fromReadonlyArray(eqEntityById<T>()))
    );

  return pipe(
    ids,
    pfTernary(RS.isEmpty, (_) => TE.right(new Set<T>()), task)
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
    ids: ReadonlyArray<string>
  ) => Promise<OEQ.UserQuery.RoleDetails[]>
): TE.TaskEither<string, ReadonlySet<OEQ.UserQuery.RoleDetails>> =>
  getEntitiesTask(roleIds, resolveRolesProvider);

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
    ids: ReadonlyArray<string>
  ) => Promise<OEQ.UserQuery.GroupDetails[]>
): TE.TaskEither<string, ReadonlySet<OEQ.UserQuery.GroupDetails>> =>
  getEntitiesTask(groupsIds, resolveGroupsProvider);

/**
 * For a given map of role ids, it resolves the associated role details and
 * returns a new map with the resolved role details.
 *
 * @param customRoles A map where the key is an LTI custom role ID
 *                    and the value is a set of oEQ role IDs associated with this identifier.
 * @param resolveRolesProvider A function that takes an array of role IDs and returns details of the roles.
 * @return a Map where the keys are the LTI role names, and the values are sets of full resolved oEQ roles - e.g. including names etc.
 *
 */
export const generateCustomRoles = (
  customRoles: Map<string, Set<string>>,
  resolveRolesProvider: (
    ids: ReadonlyArray<string>
  ) => Promise<OEQ.UserQuery.RoleDetails[]>
): T.Task<Map<string, Set<OEQ.UserQuery.RoleDetails>>> => {
  // It takes a LTI role ID and a set of role IDs,
  // and returns a Task that resolves with an array contains the LTI role ID and the resolved oEQ role details.
  // If any error happens it returns the lti role id with an empty set.
  const resolveRolesForLtiRole = (
    ltiRole: string,
    oeqRoleIds: ReadonlySet<string>
  ): T.Task<[string, ReadonlySet<OEQ.UserQuery.RoleDetails>]> =>
    pipe(
      getRolesTask(oeqRoleIds, resolveRolesProvider),
      TE.match(
        (e) => {
          console.warn(e);
          return [ltiRole, RS.empty];
        },
        (roleDetailsSet) => [ltiRole, roleDetailsSet]
      )
    );

  const task = (roles: Map<string, Set<string>>) =>
    pipe(
      roles,
      M.toArray(S.Ord),
      // Create an array of task to get oEQ roles details by ids for each lti role
      A.traverse(T.ApplicativePar)(([ltiRole, roleIds]) =>
        pipe(resolveRolesForLtiRole(ltiRole, roleIds))
      ),
      T.map(
        flow(
          // Remove empty elements
          A.filter(([_, oeqRoleSet]) => !RS.isEmpty(oeqRoleSet)),
          // Re-build the map
          (mappings) => new Map(mappings)
        )
      )
    );

  return pipe(
    customRoles,
    pfTernary(M.isEmpty, (_) => T.of(new Map()), task)
  );
};
