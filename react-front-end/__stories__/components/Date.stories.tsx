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
import { Date as DateDisplay, DateProps } from "../../tsrc/components/Date";

export default {
  title: "DateDisplay",
  component: DateDisplay,
} as Meta<DateProps>;

export const dateDisplay: Story<DateProps> = (args) => (
  <DateDisplay {...args} />
);

dateDisplay.args = {
  displayRelative: false,
  date: new Date(),
};
