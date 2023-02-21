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
import type { ACLEntityResolvers } from "../tsrc/modules/ACLEntityModule";
import { ACLExpression, generate } from "../tsrc/modules/ACLExpressionModule";
import {
  roleGuestRecipient,
  user100Recipient,
  user200Recipient,
} from "./ACLRecipientModule.mock";
import { findGroupById } from "./GroupModule.mock";
import { findRoleById } from "./RoleModule.mock";
import { findUserById } from "./UserModule.mock";

export const initialACLExpression: ACLExpression = {
  id: "root",
  operator: "OR",
  recipients: [user100Recipient],
  children: [],
};

export const initialACLExpressionWithValidChild: ACLExpression = {
  ...initialACLExpression,
  children: [
    {
      id: "test",
      operator: "AND",
      recipients: [user200Recipient, roleGuestRecipient],
      children: [],
    },
  ],
};
export const initialACLExpressionWithValidChildString = generate(
  initialACLExpressionWithValidChild
);

export const defaultACLEntityResolvers: ACLEntityResolvers = {
  resolveUserProvider: findUserById,
  resolveGroupProvider: findGroupById,
  resolveRoleProvider: findRoleById,
};
