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
import {
  WizardCalendar,
  WizardCalendarProps,
} from "../../../tsrc/components/wizard/WizardCalendar";

export default {
  title: "Component/Wizard/WizardCalendar",
  component: WizardCalendar,
  argTypes: {
    onChange: {
      action: "onChange called",
    },
  },
} as Meta<WizardCalendarProps>;

export const Normal: StoryFn<WizardCalendarProps> = (args) => (
  <WizardCalendar {...args} />
);
Normal.args = {
  id: "wizard-calendar-story",
  label: "Example",
  description: "This an example EditBox",
};

export const ValueSelected: StoryFn<WizardCalendarProps> = (args) => (
  <WizardCalendar {...args} />
);
ValueSelected.args = {
  ...Normal.args,
  values: ["2021-10-01", "2021-10-10"],
};

export const YearOnly: StoryFn<WizardCalendarProps> = (args) => (
  <WizardCalendar {...args} />
);
YearOnly.args = {
  ...Normal.args,
  dateFormat: "Y",
};

export const YearAndMonth: StoryFn<WizardCalendarProps> = (args) => (
  <WizardCalendar {...args} />
);
YearAndMonth.args = {
  ...Normal.args,
  dateFormat: "MY",
};
