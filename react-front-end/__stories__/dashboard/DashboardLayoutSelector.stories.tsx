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
import * as React from "react";
import type { Meta, StoryFn } from "@storybook/react";
import { DashboardLayoutSelector } from "../../tsrc/dashboard/components/DashboardLayoutSelector";
import type { DashboardLayoutSelectorProps } from "../../tsrc/dashboard/components/DashboardLayoutSelector";

export default {
  title: "Dashboard/DashboardLayoutSelector",
  component: DashboardLayoutSelector,
  argTypes: {
    onChange: { action: "onChange" },
  },
} as Meta<DashboardLayoutSelectorProps>;

const Template: StoryFn<DashboardLayoutSelectorProps> = (args) => (
  <DashboardLayoutSelector {...args} />
);

export const UndefinedLayout: StoryFn<DashboardLayoutSelectorProps> =
  Template.bind({});
UndefinedLayout.args = {
  value: undefined,
};

export const SingleColumnLayoutSelected: StoryFn<DashboardLayoutSelectorProps> =
  Template.bind({});
SingleColumnLayoutSelected.args = {
  value: "SingleColumn",
};

export const TwoEqualColumnsLayoutSelected: StoryFn<DashboardLayoutSelectorProps> =
  Template.bind({});
TwoEqualColumnsLayoutSelected.args = {
  value: "TwoEqualColumns",
};

export const TwoColumnsRatio1to2LayoutSelected: StoryFn<DashboardLayoutSelectorProps> =
  Template.bind({});
TwoColumnsRatio1to2LayoutSelected.args = {
  value: "TwoColumnsRatio1to2",
};

export const TwoColumnsRatio2to1LayoutSelected: StoryFn<DashboardLayoutSelectorProps> =
  Template.bind({});
TwoColumnsRatio2to1LayoutSelected.args = {
  value: "TwoColumnsRatio2to1",
};
