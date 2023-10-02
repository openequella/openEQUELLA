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
import * as UserModuleMock from "../../../__mocks__/UserModule.mock";
import { users } from "../../../__mocks__/UserModule.mock";
import {
  WizardUserSelector,
  WizardUserSelectorProps,
} from "../../../tsrc/components/wizard/WizardUserSelector";
import { GroupFilter } from "../securityentitysearch/UserSearch.stories";

export default {
  title: "Component/Wizard/WizardUserSelector",
  component: WizardUserSelector,
  argTypes: {
    onChange: {
      action: "onChange called",
    },
  },
} as Meta<WizardUserSelectorProps>;

export const NoUsers: StoryFn<WizardUserSelectorProps> = (args) => (
  <WizardUserSelector {...args} />
);
NoUsers.args = {
  id: "wizard-userselector-story",
  label: "WizardUserSelector",
  description: "A user selector (single selection) with no users specified",
  users: new Set<string>([]),
  userListProvider: UserModuleMock.listUsers,
  resolveUsersProvider: UserModuleMock.resolveUsers,
};

export const WithUsers: StoryFn<WizardUserSelectorProps> = (args) => (
  <WizardUserSelector {...args} />
);
WithUsers.args = {
  ...NoUsers.args,
  description:
    "A User Selector (multi selection) with users specified - they should be nicely sorted",
  users: new Set<string>([users[1].id, users[2].id]),
  multiple: true,
};

export const WithGroupFilter: StoryFn<WizardUserSelectorProps> = (args) => (
  <WizardUserSelector {...args} />
);
WithGroupFilter.args = {
  ...NoUsers.args,
  description:
    "A user selector (single selection) with no users specified but with a group filter active",
  groupFilter: GroupFilter.args?.groupFilter,
  resolveGroupsProvider: GroupFilter.args?.resolveGroupsProvider,
};

export const ErrorOnResolvingUserIds: StoryFn<WizardUserSelectorProps> = (
  args,
) => <WizardUserSelector {...args} />;
ErrorOnResolvingUserIds.args = {
  ...WithUsers.args,
  description:
    "A User Selector which has failed to resolve the provided IDs into full user details",
  resolveUsersProvider: async () =>
    Promise.reject("This is an example failure..."),
};
