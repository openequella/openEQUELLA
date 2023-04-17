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
import * as EQ from "fp-ts/Eq";
import * as ORD from "fp-ts/Ord";
import * as RA from "fp-ts/ReadonlyArray";
import * as S from "fp-ts/string";
import { API_BASE_URL } from "../AppConfig";
import { findEntityById } from "./ACLEntityModule";

/**
 * Eq for `OEQ.UserQuery.RoleDetails` with equality based on the role's UUID.
 */
export const eqRoleById: EQ.Eq<OEQ.UserQuery.RoleDetails> = EQ.contramap(
  (role: OEQ.UserQuery.RoleDetails) => role.id
)(S.Eq);

/**
 * Ord for `OEQ.UserQuery.RoleDetails` with order based on the role's name.
 */
export const ordRole: ORD.Ord<OEQ.UserQuery.RoleDetails> = ORD.contramap(
  (r: OEQ.UserQuery.RoleDetails) => r.name
)(S.Ord);

/**
 * Lookup roles known in oEQ.
 *
 * @param ids An array of oEQ ids
 */
export const resolveRoles = async (
  ids: ReadonlyArray<string>
): Promise<OEQ.UserQuery.RoleDetails[]> =>
  (
    await OEQ.UserQuery.lookup(API_BASE_URL, {
      users: [],
      groups: [],
      roles: RA.toArray<string>(ids),
    })
  ).roles;

/**
 * Find a role's details by ID.
 *
 * @param roleId The unique ID of a role
 */
export const findRoleById = (roleId: string) =>
  findEntityById(roleId, resolveRoles);

/**
 * List roles known in oEQ.
 *
 * @param query A wildcard supporting string to filter the result based on name
 */
export const listRoles = async (
  query?: string
): Promise<OEQ.UserQuery.RoleDetails[]> =>
  (
    await OEQ.UserQuery.search(API_BASE_URL, {
      q: query,
      groups: false,
      users: false,
      roles: true,
    })
  ).roles;
