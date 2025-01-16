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
import { Meta, StoryFn } from "@storybook/react";
import * as React from "react";
import { findRolesByIds, searchRoles } from "../../__mocks__/RoleModule.mock";
import CustomRolesMappingControl, {
  CustomRolesMappingControlProps,
} from "../../tsrc/components/CustomRolesMappingControl";
import { GUEST_USER_ROLE_ID } from "../../tsrc/modules/ACLRecipientModule";

export default {
  title: "component/CustomRolesMappingControl",
  component: CustomRolesMappingControl,
  argTypes: { onChange: { action: "On change" } },
} as Meta<CustomRolesMappingControlProps>;

const defaultProps: CustomRolesMappingControlProps = {
  initialMappings: new Map(),
  searchRolesProvider: searchRoles,
  findRolesByIdsProvider: findRolesByIds,
  onChange: () => {},
};

export const Standard: StoryFn<CustomRolesMappingControlProps> = (args) => (
  <CustomRolesMappingControl {...args} />
);
Standard.args = defaultProps;

export const WithSelections: StoryFn<CustomRolesMappingControlProps> = (
  args,
) => <CustomRolesMappingControl {...args} />;
WithSelections.args = {
  ...defaultProps,
  initialMappings: new Map([["1", new Set([GUEST_USER_ROLE_ID])]]),
};

export const WithWarningMessage: StoryFn<CustomRolesMappingControlProps> = (
  args,
) => <CustomRolesMappingControl {...args} />;
WithWarningMessage.args = {
  ...defaultProps,
  initialMappings: new Map([["1", new Set(["non-existent-role-id"])]]),
};

export const WithCustomStrings: StoryFn<CustomRolesMappingControlProps> = (
  args,
) => <CustomRolesMappingControl {...args} />;
WithCustomStrings.args = {
  ...defaultProps,
  title: "Custom title",
  description: "Custom description",
};
