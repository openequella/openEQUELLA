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
import { action } from "storybook/actions";
import { Meta, StoryFn } from "@storybook/react";
import * as React from "react";
import { defaultACLEntityResolvers } from "../../__mocks__/ACLExpressionBuilder.mock";
import { complexExpressionACLExpression } from "../../__mocks__/ACLExpressionModule.mock";
import { searchGroups } from "../../__mocks__/GroupModule.mock";
import { searchRoles } from "../../__mocks__/RoleModule.mock";
import { listUsers } from "../../__mocks__/UserModule.mock";
import { generate } from "../../tsrc/modules/ACLExpressionModule";
import ACLExpressionBuilderDialog, {
  ACLExpressionBuilderDialogProps,
} from "../../tsrc/components/ACLExpressionBuilderDialog";

export default {
  title: "Component/ACLExpressionBuilderDialog",
  component: ACLExpressionBuilderDialog,
} as Meta<ACLExpressionBuilderDialogProps>;

export const Standard: StoryFn<ACLExpressionBuilderDialogProps> = (args) => (
  <ACLExpressionBuilderDialog {...args} />
);
Standard.args = {
  open: true,
  onClose: action("onClose"),
  value: generate(complexExpressionACLExpression),
  searchUserProvider: listUsers,
  searchGroupProvider: searchGroups,
  searchRoleProvider: searchRoles,
  aclEntityResolversProvider: defaultACLEntityResolvers,
};
