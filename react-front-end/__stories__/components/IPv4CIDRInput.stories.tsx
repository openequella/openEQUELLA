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
import type { Meta, Story } from "@storybook/react";
import IPv4CIDRInput, {
  IPv4CIDRInputProps,
} from "../../tsrc/components/IPv4CIDRInput";

export default {
  title: "component/IPv4CIDRInput",
  component: IPv4CIDRInput,
  argTypes: {
    onChange: { action: "onChange" },
  },
} as Meta<IPv4CIDRInputProps>;

export const Basic: Story<IPv4CIDRInputProps> = (args) => (
  <IPv4CIDRInput {...args} />
);
