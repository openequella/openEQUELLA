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
  WizardRadioButtonGroup,
  WizardRadioButtonGroupProps,
} from "../../../tsrc/components/wizard/WizardRadioButtonGroup";

export default {
  title: "Component/Wizard/WizardRadioButtonGroup",
  component: WizardRadioButtonGroup,
  argTypes: {
    onSelect: {
      action: "onSelect called",
    },
  },
} as Meta<WizardRadioButtonGroupProps>;

export const Normal: StoryFn<WizardRadioButtonGroupProps> = (args) => (
  <WizardRadioButtonGroup {...args} />
);
Normal.args = {
  id: "wizard-radiobuttongroup-story",
  label: "Example",
  description: "This an example of RadioButton Group",
  columns: 1,
  mandatory: true,
  options: [
    {
      text: "first option",
      value: "1",
    },
    {
      text: "second option",
      value: "2",
    },
    {
      text: "third option",
      value: "3",
    },
    {
      text: "fourth option",
      value: "4",
    },
  ],
};

export const MultipleColumns: StoryFn<WizardRadioButtonGroupProps> = (args) => (
  <WizardRadioButtonGroup {...args} />
);
MultipleColumns.args = {
  ...Normal.args,
  columns: 2,
  description: "Options are displayed in 2 columns 2 rows",
};

export const ValueSelected: StoryFn<WizardRadioButtonGroupProps> = (args) => (
  <WizardRadioButtonGroup {...args} />
);
ValueSelected.args = {
  ...Normal.args,
  value: "2",
  description: "Option 2 is initially selected.",
};
