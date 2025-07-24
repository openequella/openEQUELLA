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
import { groups } from "../../../../__mocks__/GroupModule.mock";
import { eqGroupById, groupIds } from "../../../../tsrc/modules/GroupModule";
import UnknownUserHandlingControl, {
  UnknownUserHandlingControlProps,
} from "../../../../tsrc/settings/Integrations/lti13/components/UnknownUserHandlingControl";

export default {
  title: "settings/Integrations/Lti13/UnknownUserHandlingControl",
  component: UnknownUserHandlingControl,
} as Meta<UnknownUserHandlingControlProps>;

export const DefaultOption: StoryFn<UnknownUserHandlingControlProps> = (
  args,
) => <UnknownUserHandlingControl {...args} />;
DefaultOption.args = {
  onChange: action("onChange"),
  selection: "ERROR",
};

export const CreateOption: StoryFn<UnknownUserHandlingControlProps> = (
  args,
) => <UnknownUserHandlingControl {...args} />;
CreateOption.args = {
  ...DefaultOption.args,
  selection: "CREATE",
};

export const CreateOptionWithSelectedGroups: StoryFn<
  UnknownUserHandlingControlProps
> = (args) => <UnknownUserHandlingControl {...args} />;
CreateOptionWithSelectedGroups.args = {
  ...DefaultOption.args,
  selection: "CREATE",
  groups: pipe(groups, RS.fromReadonlyArray(eqGroupById), groupIds),
};
