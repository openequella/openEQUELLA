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
import * as A from "fp-ts/Array";
import * as RSET from "fp-ts/ReadonlySet";
import { pipe } from "fp-ts/function";
import * as S from "fp-ts/string";
import * as React from "react";
import * as GroupModuleMock from "../../../__mocks__/GroupModule.mock";
import * as GroupSearchMock from "../../../__mocks__/GroupSearch.mock";
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

export const Default: Story<GroupSearchProps> = (args) => (
  <GroupSearch {...args} />
);
Default.args = {
  selections: RSET.empty,
  listHeight: 150,
  search: GroupSearchMock.groupDetailsProvider,
};

export const MultiSelection: Story<GroupSearchProps> = (args) => (
  <GroupSearch {...args} />
);
MultiSelection.args = {
  ...Default.args,
  selections: pipe(
    GroupModuleMock.groups,
    A.filter((g) => pipe(g.name, S.includes("group"))),
    RSET.fromReadonlyArray(eqGroupById)
  ),
  enableMultiSelection: true,
};

export const GroupFilterEditable: Story<GroupSearchProps> = (args) => (
  <GroupSearch {...args} />
);
GroupFilterEditable.args = {
  ...Default.args,
  search: GroupSearchMock.groupDetailsProvider,
  groupFilterEditable: true,
  groupFilter: RSET.empty,
  groupSearch: GroupSearchMock.groupDetailsProvider,
  resolveGroupsProvider: GroupModuleMock.resolveGroups,
};
