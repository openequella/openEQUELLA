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
import { Meta, Story } from "@storybook/react";
import * as React from "react";
import {
  TooltipChip,
  TooltipChipProps,
} from "../../tsrc/components/TooltipChip";

export default {
  title: "component/TooltipChip",
  component: TooltipChip,
} as Meta<TooltipChipProps>;

export const Standard: Story<TooltipChipProps> = (args) => (
  <TooltipChip {...args} />
);
Standard.args = {
  title: "This is a Tooltip Chip",
  maxWidth: 150,
};

export const ReallyLongChip: Story<TooltipChipProps> = (args) => (
  <TooltipChip {...args} />
);
ReallyLongChip.args = {
  title:
    "This is a long long long long long long long long long long long long long long long long long long long long Tooltip Chip",
  maxWidth: 150,
};
