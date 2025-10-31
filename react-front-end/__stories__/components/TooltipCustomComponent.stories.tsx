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
import { Fab } from "@mui/material";
import EditIcon from "@mui/icons-material/Edit";
import * as React from "react";
import SettingsToggleSwitch from "../../tsrc/components/SettingsToggleSwitch";
import {
  TooltipCustomComponent,
  TooltipCustomComponentProps,
} from "../../tsrc/components/TooltipCustomComponent";

export default {
  title: "component/TooltipCustomComponent",
  component: TooltipCustomComponent,
} as Meta<TooltipCustomComponentProps>;

export const TooltipToggleSwitch: StoryFn<TooltipCustomComponentProps> = (
  args,
) => (
  <TooltipCustomComponent {...args}>
    <SettingsToggleSwitch id="toggle" setValue={() => {}} />
  </TooltipCustomComponent>
);
TooltipToggleSwitch.args = {
  title: "This is a Tooltip Custom Component",
};

export const TooltipFabWithPositioningProps: StoryFn<
  TooltipCustomComponentProps
> = (args) => (
  <TooltipCustomComponent {...args}>
    <Fab color="secondary">
      <EditIcon />
    </Fab>
  </TooltipCustomComponent>
);
TooltipFabWithPositioningProps.args = {
  title: "Edit",
  sx: { position: "absolute", bottom: 24, right: 24 },
};
