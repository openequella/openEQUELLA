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
import * as UserSearchMock from "../../__mocks__/UserSearch.mock";
import UserSearch, { UserSearchProps } from "../../tsrc/components/UserSearch";

export default {
  title: "component/UserSearch",
  component: UserSearch,
  argTypes: {
    onSelect: {
      action: "onSelect called",
    },
  },
} as Meta<UserSearchProps>;

export const Default: Story<UserSearchProps> = (args) => (
  <UserSearch {...args} />
);
Default.args = {
  listHeight: 150,
  userListProvider: UserSearchMock.userDetailsProvider,
};

export const GroupFilter: Story<UserSearchProps> = (args) => (
  <UserSearch {...args} />
);
GroupFilter.args = {
  ...Default.args,
  groupFilter: new Set([
    "35e0b3dc-f243-44be-9f9d-75b094c08a6c",
    "a47150a6-6d28-4de1-ba12-36fe440f5132",
  ]),
};
