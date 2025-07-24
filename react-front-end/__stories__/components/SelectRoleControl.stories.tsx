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
import { pipe } from "fp-ts/function";
import * as RS from "fp-ts/ReadonlySet";
import * as React from "react";
import { eqRoleById, roleIds } from "../../tsrc/modules/RoleModule";
import SelectRoleControl, {
  SelectRoleControlProps,
} from "../../tsrc/components/SelectRoleControl";
import {
  searchRoles,
  findRolesByIds,
  roles,
} from "../../__mocks__/RoleModule.mock";

export default {
  title: "Component/SelectRoleControl",
  component: SelectRoleControl,
} as Meta<SelectRoleControlProps>;

export const Empty: StoryFn<SelectRoleControlProps> = (args) => (
  <SelectRoleControl {...args} />
);
Empty.args = {
  primaryText: "primary Text",
  secondaryText: "Secondary Text",
  onChange: action("onChange"),
  value: new Set(),
  searchRolesProvider: searchRoles,
  findRolesByIdsProvider: findRolesByIds,
};

export const WithValue: StoryFn<SelectRoleControlProps> = (args) => (
  <SelectRoleControl {...args} />
);
WithValue.args = {
  ...Empty.args,
  value: pipe(roles, RS.fromReadonlyArray(eqRoleById), roleIds),
};
