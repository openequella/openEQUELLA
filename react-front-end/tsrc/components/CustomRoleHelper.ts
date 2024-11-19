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
import { pipe } from "fp-ts/function";
import * as M from "fp-ts/Map";
import * as ORD from "fp-ts/Ord";
import * as S from "fp-ts/string";
import * as RS from "fp-ts/ReadonlySet";
import { roleIds } from "../modules/RoleModule";

/**
 * In server side it only saves the role ID, and the ID may not be readable,
 * but in client side it need to show a more meaningful name.
 * Thus this interface is used to represent the custom role with a more readable format.
 */
export interface CustomRole {
  /** Custom role which may not be presented in an readable and meaningful format. */
  role: string;
  /** A more meaningful and readable name for the role. */
  name?: string;
}

/**
 * Eq for `CustomRole` with equality based on the role's claim.
 */
export const customRoleEq: EQ.Eq<CustomRole> = EQ.contramap(
  (r: CustomRole) => r.role,
)(S.Eq);

/**
 * Ord for `CustomRole` with order based on the role's name and claim.
 */
export const customRoleOrd: ORD.Ord<CustomRole> = ORD.contramap(
  (r: CustomRole) => r.name ?? r.role,
)(S.Ord);

export type CustomRolesMapping = Map<
  CustomRole,
  Set<OEQ.UserQuery.RoleDetails>
>;

/**
 * Transform CustomRolesMapping to a map contains the bare minimal info required on server.
 * It will transform the key(custom role) to a string and the value(oEQ role) to a set of UUID.
 */
export const transformCustomRoleMapping = (
  maps: CustomRolesMapping,
): Map<string, Set<OEQ.Common.UuidString>> =>
  pipe(
    maps,
    M.reduceWithIndex(customRoleOrd)(
      new Map<string, Set<OEQ.Common.UuidString>>(),
      (customRole, resultMap, oeqRoles) => {
        const uuids = pipe(oeqRoles, RS.fromSet, roleIds, RS.toSet);
        return pipe(resultMap, M.upsertAt(S.Eq)(customRole.role, uuids));
      },
    ),
  );
