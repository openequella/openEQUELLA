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
import * as OEQ from "@openequella/rest-api-client";
import { action } from "storybook/actions";
import { Meta, StoryFn } from "@storybook/react";
import * as A from "fp-ts/Array";
import * as RSET from "fp-ts/ReadonlySet";
import { pipe } from "fp-ts/function";
import * as S from "fp-ts/string";
import * as React from "react";
import * as GroupModuleMock from "../../../__mocks__/GroupModule.mock";
import { CheckboxMode } from "../../../tsrc/components/securityentitysearch/BaseSearch";
import GroupSearch, {
  GroupSearchProps,
} from "../../../tsrc/components/securityentitysearch/GroupSearch";
import { eqGroupById } from "../../../tsrc/modules/GroupModule";

export default {
  title: "component/SecurityEntitySearch/GroupSearch",
  component: GroupSearch,
  argTypes: {
    onChange: {
      action: "onChange called",
    },
  },
} as Meta<GroupSearchProps>;

const defaultCheckboxModeProps: CheckboxMode<OEQ.UserQuery.GroupDetails> = {
  type: "checkbox",
  onChange: action("onChange called"),
  selections: RSET.empty,
};

export const Default: StoryFn<GroupSearchProps> = (args) => (
  <GroupSearch {...args} />
);
Default.args = {
  mode: defaultCheckboxModeProps,
  listHeight: 150,
  search: GroupModuleMock.searchGroups,
};

export const MultiSelection: StoryFn<GroupSearchProps> = (args) => (
  <GroupSearch {...args} />
);
MultiSelection.args = {
  ...Default.args,
  mode: {
    ...defaultCheckboxModeProps,
    selections: pipe(
      GroupModuleMock.groups,
      A.filter((g) => pipe(g.name, S.includes("group"))),
      RSET.fromReadonlyArray(eqGroupById),
    ),
    enableMultiSelection: true,
  },
};

export const GroupFilterEditable: StoryFn<GroupSearchProps> = (args) => (
  <GroupSearch {...args} />
);
GroupFilterEditable.args = {
  ...Default.args,
  search: GroupModuleMock.searchGroups,
  groupFilterEditable: true,
  groupFilter: RSET.empty,
  groupSearch: GroupModuleMock.searchGroups,
  resolveGroupsProvider: GroupModuleMock.findGroupsByIds,
};

export const OneClickMode: StoryFn<GroupSearchProps> = (args) => (
  <GroupSearch {...args} />
);
OneClickMode.args = {
  ...Default.args,
  mode: {
    type: "one_click",
    onAdd: action("anAdd called"),
  },
};
