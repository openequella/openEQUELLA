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
import { contramap, Eq } from "fp-ts/Eq";
import { flow, pipe } from "fp-ts/function";
import * as ORD from "fp-ts/Ord";
import * as RA from "fp-ts/ReadonlyArray";
import * as RSET from "fp-ts/ReadonlySet";
import * as S from "fp-ts/string";
import { API_BASE_URL } from "../AppConfig";
import { findEntityById } from "./ACLEntityModule";

/**
 * Eq for `OEQ.UserQuery.GroupDetails` with equality based on the user's UUID.
 */
export const eqGroupById: Eq<OEQ.UserQuery.GroupDetails> = contramap(
  (group: OEQ.UserQuery.GroupDetails) => group.id,
)(S.Eq);

/**
 * Ord for `OEQ.UserQuery.GroupDetails` with order based on the group's name.
 */
export const ordGroup: ORD.Ord<OEQ.UserQuery.GroupDetails> = ORD.contramap(
  (g: OEQ.UserQuery.GroupDetails) => g.name,
)(S.Ord);

/**
 * Given a set of `OEQ.UserQuery.GroupDetails`, return a set of UUIDs for all the groups.
 */
export const groupIds: (
  a: ReadonlySet<OEQ.UserQuery.GroupDetails>,
) => ReadonlySet<string> = flow(RSET.map(S.Eq)(({ id }) => id));

/**
 * Ord for `OEQ.UserQuery.GroupDetails` with ordering rule based on the group's name.
 */
export const groupOrd = ORD.contramap(
  (u: OEQ.UserQuery.GroupDetails) => u.name,
)(S.Ord);

/**
 * Lookup groups known in oEQ.
 *
 * @param ids An array of oEQ ids
 */
export const findGroupsByIds = async (
  ids: ReadonlySet<string>,
): Promise<OEQ.UserQuery.GroupDetails[]> =>
  (
    await OEQ.UserQuery.lookup(API_BASE_URL, {
      users: [],
      groups: pipe(ids, RSET.toReadonlyArray<string>(S.Ord), RA.toArray),
      roles: [],
    })
  ).groups;

/**
 * Find a group's details by ID.
 *
 * @param groupId The unique ID of a role
 */
export const findGroupById = (groupId: string) =>
  findEntityById(groupId, findGroupsByIds);

/**
 * List groups known in oEQ.
 *
 * @param query A wildcard supporting string to filter the result based on name
 * @param groupFilter A list of group UUIDs to filter the search by
 */
export const searchGroups = async (
  query?: string,
  groupFilter?: ReadonlySet<string>,
): Promise<OEQ.UserQuery.GroupDetails[]> =>
  await OEQ.UserQuery.filteredGroups(API_BASE_URL, {
    q: query,
    byGroups: groupFilter ? Array.from<string>(groupFilter) : undefined,
  });
