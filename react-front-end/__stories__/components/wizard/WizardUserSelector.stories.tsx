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
import { Meta, Story } from "@storybook/react";
import * as React from "react";
import * as UserSearchMock from "../../../__mocks__/UserSearch.mock";
import {
  WizardUserSelector,
  WizardUserSelectorProps,
} from "../../../tsrc/components/wizard/WizardUserSelector";

export default {
  title: "Component/Wizard/WizardUserSelector",
  component: WizardUserSelector,
  argTypes: {
    onChange: {
      action: "onChange called",
    },
  },
} as Meta<WizardUserSelectorProps>;

export const NoUsers: Story<WizardUserSelectorProps> = (args) => (
  <WizardUserSelector {...args} />
);
NoUsers.args = {
  id: "wizard-userselector-story",
  label: "WizardUserSelector",
  description: "A user selector (single selection) with no users specified",
  users: new Set<string>([]),
  userListProvider: UserSearchMock.userDetailsProvider,
  resolveUsersProvider: UserSearchMock.resolveUsersProvider,
};

export const WithUsers: Story<WizardUserSelectorProps> = (args) => (
  <WizardUserSelector {...args} />
);
WithUsers.args = {
  ...NoUsers.args,
  description:
    "A User Selector (multi selection) with users specified - they should be nicely sorted",
  users: new Set<string>([
    UserSearchMock.users[1].id,
    UserSearchMock.users[2].id,
  ]),
  multiple: true,
};

export const ErrorOnResolvingUserIds: Story<WizardUserSelectorProps> = (
  args
) => <WizardUserSelector {...args} />;
ErrorOnResolvingUserIds.args = {
  ...WithUsers.args,
  description:
    "A User Selector which has failed to resolve the provided IDs into full user details",
  resolveUsersProvider: async () =>
    Promise.reject("This is an example failure..."),
};
