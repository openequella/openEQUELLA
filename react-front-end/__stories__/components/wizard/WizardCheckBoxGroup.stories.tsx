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
import { Meta, StoryFn } from "@storybook/react";
import {
  WizardCheckBoxGroup,
  WizardCheckBoxGroupProps,
} from "../../../tsrc/components/wizard/WizardCheckBoxGroup";

export default {
  title: "Component/Wizard/WizardCheckBoxGroup",
  component: WizardCheckBoxGroup,
  argTypes: {
    onSelect: {
      action: "onSelect called",
    },
  },
} as Meta<WizardCheckBoxGroupProps>;

export const Normal: StoryFn<WizardCheckBoxGroupProps> = (args) => (
  <WizardCheckBoxGroup {...args} />
);
Normal.args = {
  id: "wizard-checkboxgroup-story",
  label: "Example",
  description: "This an example of CheckBox Group",
  columns: 1,
  mandatory: true,
  options: [
    {
      text: "option1",
      value: "1",
    },
    {
      text: "option2",
      value: "2",
    },
    {
      text: "option3",
      value: "3",
    },
    {
      text: "option4",
      value: "4",
    },
    {
      text: "option5",
      value: "5",
    },
    {
      text: "option6",
      value: "6",
    },
    {
      text: "option7",
      value: "7",
    },
  ],
};

export const MultipleColumns: StoryFn<WizardCheckBoxGroupProps> = (args) => (
  <WizardCheckBoxGroup {...args} />
);
MultipleColumns.args = {
  ...Normal.args,
  columns: 4,
  description: "Options are displayed in 4 columns 2 rows",
};

export const ValueSelected: StoryFn<WizardCheckBoxGroupProps> = (args) => (
  <WizardCheckBoxGroup {...args} />
);
ValueSelected.args = {
  ...Normal.args,
  values: ["1", "3"],
  description: "Option 1 and 3 are initially selected.",
};
