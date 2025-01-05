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
import { searchRoles } from "../../__mocks__/RoleModule.mock";
import CustomRolesMappingControl, {
  CustomRolesMappingControlProps,
} from "../../tsrc/components/CustomRolesMappingControl";

export default {
  title: "component/CustomRoleMappingControl",
  component: CustomRolesMappingControl,
  argTypes: { onChange: { action: "On change" } },
} as Meta<CustomRolesMappingControlProps>;

const defaultProps = {
  open: true,
  initialRoleMappings: new Map(),
  searchRoleProvider: searchRoles,
};

export const Standard: StoryFn<CustomRolesMappingControlProps> = (args) => (
  <CustomRolesMappingControl {...args} />
);
Standard.args = defaultProps;

export const WithCustomStrings: StoryFn<CustomRolesMappingControlProps> = (
  args,
) => <CustomRolesMappingControl {...args} />;
WithCustomStrings.args = {
  ...defaultProps,
  title: "Custom title",
  description: "Custom description",
};
