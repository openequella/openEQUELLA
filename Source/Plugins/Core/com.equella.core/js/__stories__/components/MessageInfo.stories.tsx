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
import MessageInfo, {
  MessageInfoProps,
} from "../../tsrc/components/MessageInfo";

export default {
  title: "MessageInfo",
  component: MessageInfo,
  argTypes: { onClose: { action: "onClose" } },
} as Meta<MessageInfoProps>;

const sharedArgs = {
  open: true,
  title: "Lorem ipsum dolor sit amet, consectetur adipiscing elit",
};

export const VariantSuccess: Story<MessageInfoProps> = (args) => (
  <MessageInfo {...args} />
);
VariantSuccess.args = {
  ...sharedArgs,
  variant: "success",
};

export const VariantError: Story<MessageInfoProps> = (args) => (
  <MessageInfo {...args} />
);
VariantError.args = {
  ...sharedArgs,
  variant: "error",
};

export const VariantInfo: Story<MessageInfoProps> = (args) => (
  <MessageInfo {...args} />
);
VariantInfo.args = {
  ...sharedArgs,
  variant: "info",
};

export const VariantWarning: Story<MessageInfoProps> = (args) => (
  <MessageInfo {...args} />
);
VariantWarning.args = {
  ...sharedArgs,
  variant: "warning",
};
