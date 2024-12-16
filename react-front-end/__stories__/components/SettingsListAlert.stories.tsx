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

import SettingsListAlert, {
  SettingsListWarningProps,
} from "../../tsrc/components/SettingsListAlert";

export default {
  title: "Component/SettingsListAlert",
  component: SettingsListAlert,
} as Meta<SettingsListWarningProps>;

export const Warning: StoryFn<SettingsListWarningProps> = (args) => (
  <SettingsListAlert {...args} />
);
Warning.args = {
  severity: "warning",
  messages: ["This is a warning alert - check it out!"],
};

export const MultipleLines: StoryFn<SettingsListWarningProps> = (args) => (
  <SettingsListAlert {...args} />
);
MultipleLines.args = {
  severity: "warning",
  messages: ["This is a warning alert.", "Check it out!"],
};

export const Error: StoryFn<SettingsListWarningProps> = (args) => (
  <SettingsListAlert {...args} />
);
Error.args = {
  severity: "error",
  messages: ["This is a error alert - check it out!"],
};
