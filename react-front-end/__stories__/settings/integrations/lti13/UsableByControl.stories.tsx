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
import { action } from "@storybook/addon-actions";
import { Meta, StoryFn } from "@storybook/react";
import * as React from "react";
import { defaultACLEntityResolvers } from "../../../../__mocks__/ACLExpressionBuilder.mock";
import {
  aclEveryone,
  aclWithComplexSubExpression,
} from "../../../../__mocks__/ACLExpressionModule.mock";
import { searchGroups } from "../../../../__mocks__/GroupModule.mock";
import { searchRoles } from "../../../../__mocks__/RoleModule.mock";
import { listUsers } from "../../../../__mocks__/UserModule.mock";
import UsableByControlControl, {
  UsableByControlProps,
} from "../../../../tsrc/settings/Integrations/lti13/components/UsableByControl";

export default {
  title: "settings/Integrations/Lti13/UsableByControl",
  component: UsableByControlControl,
} as Meta<UsableByControlProps>;

export const Standard: StoryFn<UsableByControlProps> = (args) => (
  <UsableByControlControl {...args} />
);
Standard.args = {
  onChange: action("onChange"),
  value: aclEveryone,
  searchUserProvider: listUsers,
  searchGroupProvider: searchGroups,
  searchRoleProvider: searchRoles,
  aclEntityResolversProvider: defaultACLEntityResolvers,
};

export const ComplexACLExpression: StoryFn<UsableByControlProps> = (args) => (
  <UsableByControlControl {...args} />
);
ComplexACLExpression.args = {
  ...Standard.args,
  value: aclWithComplexSubExpression,
};
