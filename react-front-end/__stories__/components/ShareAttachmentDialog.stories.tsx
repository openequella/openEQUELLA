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
import * as O from "fp-ts/Option";
import { Meta, StoryFn } from "@storybook/react";
import {
  ShareAttachmentDialog,
  ShareAttachmentDialogProps,
} from "../../tsrc/components/ShareAttachmentDialog";

export default {
  title: "component/ShareAttachmentDialog",
  component: ShareAttachmentDialog,
  argTypes: {
    closeDialog: { action: "on close dialog" },
  },
} as Meta<ShareAttachmentDialogProps>;

export const LinkOnly: StoryFn<ShareAttachmentDialogProps> = (
  args: ShareAttachmentDialogProps,
) => <ShareAttachmentDialog {...args} />;

LinkOnly.args = {
  open: true,
  src: "https://localhost/inst/items/1eeb3df5-3809-4655-925b-24d994e42ff6/1/image.jpg",
};

export const WithEmbedCode: StoryFn<ShareAttachmentDialogProps> = (
  args: ShareAttachmentDialogProps,
) => <ShareAttachmentDialog {...args} />;

WithEmbedCode.args = {
  ...LinkOnly.args,
  embedCode: O.of(
    `<img alt="placeholder-500x500.png" src="./placeholder-500x500.png"/>`,
  ),
};
