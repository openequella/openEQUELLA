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
import * as RoleModuleMock from "../../../__mocks__/RoleModule.mock";
import * as RoleSearchMock from "../../../__mocks__/RoleSearch.mock";
import RoleSearch, {
  RoleSearchProps,
} from "../../../tsrc/components/securityentitysearch/RoleSearch";
import { eqRoleById } from "../../../tsrc/modules/RoleModule";

export default {
  title: "component/SecurityEntitySearch/RoleSearch",
  component: RoleSearch,
  argTypes: {
    onChange: {
      action: "onChange called",
    },
  },
} as Meta<RoleSearchProps>;

export const Default: Story<RoleSearchProps> = (args) => (
  <RoleSearch {...args} />
);
Default.args = {
  selections: RSET.empty,
  listHeight: 150,
  roleListProvider: RoleSearchMock.roleDetailsProvider,
};

export const MultiSelection: Story<RoleSearchProps> = (args) => (
  <RoleSearch {...args} />
);
MultiSelection.args = {
  ...Default.args,
  selections: pipe(
    RoleModuleMock.roles,
    A.filter((g) => pipe(g.name, S.includes("role"))),
    RSET.fromReadonlyArray(eqRoleById)
  ),
  enableMultiSelection: true,
};
