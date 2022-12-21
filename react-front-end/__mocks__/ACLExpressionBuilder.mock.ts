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
import { ACLExpression } from "../tsrc/modules/ACLExpressionModule";
import { user100RecipientWithName } from "./ACLRecipientModule.mock";

export const initialACLExpression: ACLExpression = {
  id: "root",
  operator: "OR",
  recipients: [
    {
      expression: "df950ee3-c5f2-4c09-90af-38bb9b73dc29",
      name: "Root User",
      type: "U",
    },
  ],
  children: [
    {
      id: "test",
      operator: "OR",
      recipients: [],
      children: [],
    },
  ],
};

export const initialACLExpressionWithValidChild: ACLExpression = {
  ...initialACLExpression,
  children: [
    {
      id: "test",
      operator: "AND",
      recipients: [user100RecipientWithName],

      children: [],
    },
  ],
};
