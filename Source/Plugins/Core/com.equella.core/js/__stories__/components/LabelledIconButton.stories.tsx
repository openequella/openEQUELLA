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
import AddCircleIcon from "@material-ui/icons/AddCircle";
import { Meta, Story } from "@storybook/react";
import React from "react";
import {
  LabelledIconButton,
  LabelledIconButtonProps,
} from "../../tsrc/components/LabelledIconButton";

export default {
  title: "component/LabelledIconButton",
  component: LabelledIconButton,
  argTypes: {
    onClick: { action: "onClick triggered" },
  },
} as Meta<LabelledIconButtonProps>;

export const addButton: Story<LabelledIconButtonProps> = (
  args: LabelledIconButtonProps
) => <LabelledIconButton {...args} />;

addButton.args = {
  icon: <AddCircleIcon />,
  buttonText: "Add an item",
  color: "primary",
};
