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
import type {
  ACLEntityResolversMulti,
  ACLEntityResolvers,
} from "../tsrc/modules/ACLEntityModule";
import { ACLExpression, generate } from "../tsrc/modules/ACLExpressionModule";
import { user100Recipient } from "./ACLRecipientModule.mock";
import {
  findGroupById,
  searchGroups,
  findGroupsByIds,
} from "./GroupModule.mock";
import { findRoleById, searchRoles, findRolesByIds } from "./RoleModule.mock";
import {
  findUserById,
  getTokens,
  listUsers,
  resolveUsers,
} from "./UserModule.mock";

export const initialACLExpression: ACLExpression = {
  id: "root",
  operator: "OR",
  recipients: [user100Recipient],
  children: [],
};
export const initialACLExpressionString = generate(initialACLExpression);

/**
 *  ```
 *  {
 *   id: "root",
 *   operator: "OR",
 *   recipients: [user100Recipient],
 *   children: [
 *     {
 *       id: "test",
 *       operator: "AND",
 *       recipients: [user200Recipient, roleGuestRecipient],
 *       children: [],
 *     },
 *   ],
 * };
 *  ```
 */
export const initialACLExpressionWithValidChildString =
  "U:20483af2-fe56-4499-a54b-8d7452156895 U:f9ec8b09-cf64-44ff-8a0a-08a8f2f9272a R:TLE_GUEST_USER_ROLE AND OR";

export const defaultACLEntityResolvers: ACLEntityResolvers = {
  resolveUserProvider: findUserById,
  resolveGroupProvider: findGroupById,
  resolveRoleProvider: findRoleById,
};

export const defaultACLEntityResolversMulti: ACLEntityResolversMulti = {
  resolveUsersProvider: resolveUsers,
  resolveGroupsProvider: findGroupsByIds,
  resolveRolesProvider: findRolesByIds,
};

export const defaultACLExpressionProps = {
  searchUserProvider: listUsers,
  searchGroupProvider: searchGroups,
  searchRoleProvider: searchRoles,
  resolveGroupsProvider: findGroupsByIds,
  aclEntityResolversProvider: defaultACLEntityResolvers,
  ssoTokensProvider: getTokens,
};
