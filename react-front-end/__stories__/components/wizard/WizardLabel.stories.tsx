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
import { Typography } from "@mui/material";
import { Meta, Story } from "@storybook/react";
import * as React from "react";
import {
  WizardLabel,
  WizardLabelProps,
} from "../../../tsrc/components/wizard/WizardLabel";

export default {
  title: "Component/Wizard/WizardLabel",
  component: WizardLabel,
} as Meta<WizardLabelProps>;

export const FullySpecified: Story<WizardLabelProps> = (args) => (
  <WizardLabel {...args} />
);
FullySpecified.args = {
  label: "A label",
  description: "This is the description",
  mandatory: true,
};

export const Optional: Story<WizardLabelProps> = (args) => (
  <WizardLabel {...args} />
);
Optional.args = {
  label: "Optional",
  description: "This field is _not_ mandatory",
  mandatory: false,
};

export const NoDescription: Story<WizardLabelProps> = (args) => (
  <WizardLabel {...args} />
);
NoDescription.args = {
  ...FullySpecified.args,
  description: undefined,
};

export const NoLabel: Story<WizardLabelProps> = (args) => (
  <WizardLabel {...args} />
);
NoLabel.args = {
  ...FullySpecified.args,
  label: undefined,
};

export const NoDescriptionOrLabel: Story<WizardLabelProps> = (args) => (
  <>
    <WizardLabel {...args} />
    <br />
    <br />
    <Typography variant="caption" color="textSecondary">
      All you should see is this text - as there is nothing from the actual
      component (above).
    </Typography>
  </>
);
NoDescriptionOrLabel.args = {
  ...FullySpecified.args,
  label: undefined,
  description: undefined,
};
