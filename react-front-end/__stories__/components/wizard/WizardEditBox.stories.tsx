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
  WizardEditBox,
  WizardEditBoxProps,
} from "../../../tsrc/components/wizard/WizardEditBox";

export default {
  title: "Component/Wizard/WizardEditBox",
  component: WizardEditBox,
  argTypes: {
    onChange: {
      action: "onChange called",
    },
  },
} as Meta<WizardEditBoxProps>;

export const Normal: Story<WizardEditBoxProps> = (args) => (
  <WizardEditBox {...args} />
);
Normal.args = {
  id: "wizard-editbox-story",
  label: "Example",
  description: "This an example EditBox",
};

export const Mandatory: Story<WizardEditBoxProps> = (args) => (
  <WizardEditBox {...args} mandatory />
);
Mandatory.args = { ...Normal.args };

export const Multiline: Story<WizardEditBoxProps> = (args) => (
  <WizardEditBox {...args} />
);
Multiline.args = { ...Normal.args, description: "With three rows", rows: 3 };

export const NoDescription: Story<WizardEditBoxProps> = (args) => (
  <WizardEditBox {...args} />
);
NoDescription.args = { ...Normal.args, description: undefined };

export const NoTitle: Story<WizardEditBoxProps> = (args) => (
  <WizardEditBox {...args} />
);
NoTitle.args = {
  ...Normal.args,
  description: "No title, only this description",
  label: undefined,
};

export const NoDescriptionOrTitle: Story<WizardEditBoxProps> = (args) => (
  <div>
    <WizardEditBox {...args} />
    <br />
    <br />
    <Typography variant="caption" color="textSecondary">
      This may seem odd, but it's a valid configuration in the Admin Console so
      we need to check it works.
    </Typography>
  </div>
);
NoDescriptionOrTitle.args = {
  ...Normal.args,
  label: undefined,
  description: undefined,
};
