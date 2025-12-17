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
import { SingleColumnIcon } from "../../tsrc/icons/SingleColumnIcon";
import { TwoColumnsEqualIcon } from "../../tsrc/icons/TwoColumnsEqualIcon";
import { TwoColumnsRatio1to2Icon } from "../../tsrc/icons/TwoColumnsRatio1to2Icon";
import { TwoColumnsRatio2to1Icon } from "../../tsrc/icons/TwoColumnsRatio2to1Icon";
import { SvgIconProps } from "@mui/material";

export default {
  title: "Icons/Dashboard Layout",
} as Meta<SvgIconProps>;

export const SingleColumn: StoryFn<SvgIconProps> = (args) => (
  <SingleColumnIcon {...args} />
);

export const TwoColumnsEqual: StoryFn<SvgIconProps> = (args) => (
  <TwoColumnsEqualIcon {...args} />
);

export const TwoColumnsRatio1to2: StoryFn<SvgIconProps> = (args) => (
  <TwoColumnsRatio1to2Icon {...args} />
);

export const TwoColumnsRatio2to1: StoryFn<SvgIconProps> = (args) => (
  <TwoColumnsRatio2to1Icon {...args} />
);
