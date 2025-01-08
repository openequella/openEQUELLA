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
import { findEntityById } from "../tsrc/modules/ACLEntityModule";
import {
  GUEST_USER_ROLE_ID,
  LOGGED_IN_USER_ROLE_ID,
} from "../tsrc/modules/ACLRecipientModule";
import { LOGGED_IN_USER_ROLE_NAME } from "./ACLRecipientModule.mock";
import { entityDetailsProvider } from "./SecurityEntitySearch.mock";

/**
 * A list of roles to test with role search.
 */
export const roles: OEQ.UserQuery.RoleDetails[] = [
  {
    id: LOGGED_IN_USER_ROLE_ID,
    name: LOGGED_IN_USER_ROLE_NAME,
  },
  {
    id: GUEST_USER_ROLE_ID,
    name: "Guest User Role",
  },
  {
    id: "62ed85d3-278a-46f5-8ee4-391a45f97899",
    name: "Teachers",
  },
  {
    id: "1ffbf760-2970-48d7-ab9f-62e95a64d07e",
    name: "Systems Administrators",
  },
  {
    id: "dc97436d-8e52-40db-abc6-ca198cbe6dae",
    name: "Content Administrators",
  },
  {
    id: "ffff7e1d-bf77-464f-bd41-de89d44a9cc6",
    name: "Student",
  },
  {
    id: "fda99983-9eda-440a-ac68-0f746173fdcb",
    name: "role100",
  },
  {
    id: "1de3a6df-dc81-4a26-b69e-e61f8474594a",
    name: "role200",
  },
];

/**
 * Helper function to inject into component for role retrieval by an array of ids.
 *
 * @param ids A list of role IDs to lookup, should be one of those in `roles`
 */
export const findRolesByIds = async (
  ids: ReadonlySet<string>,
): Promise<OEQ.UserQuery.RoleDetails[]> =>
  Promise.resolve(roles.filter(({ id }) => ids.has(id)));

/**
 * Helper function for role retrieval by role id.
 *
 * @param id oEQ id
 */
export const findRoleById = (id: string) => findEntityById(id, findRolesByIds);

/**
 * Helper function to inject into component for role retrieval.
 *
 * @param query A simple string to filter by (no wildcard support)
 */
export const searchRoles = async (
  query?: string,
): Promise<OEQ.UserQuery.RoleDetails[]> =>
  entityDetailsProvider(
    roles,
    (r: OEQ.UserQuery.RoleDetails, q: string) => r.name.search(q) === 0,
    query,
  );
