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
import { Meta } from "@storybook/react";
import * as React from "react";
import * as UserModuleMock from "../../__mocks__/UserModule.mock";
import OwnerSelector, {
  OwnerSelectorProps,
} from "../../tsrc/search/components/OwnerSelector";

export default {
  title: "Search/OwnerSelector",
  component: OwnerSelector,
} as Meta<OwnerSelectorProps>;

const commonParams = {
  onClearSelect: action("onClearSelect"),
  onSelect: action("onSelect"),
  userListProvider: UserModuleMock.listUsers,
};

export const NoSelectedUser = () => <OwnerSelector {...commonParams} />;

export const SelectedUser = () => (
  <OwnerSelector {...commonParams} value={UserModuleMock.users[0]} />
);
