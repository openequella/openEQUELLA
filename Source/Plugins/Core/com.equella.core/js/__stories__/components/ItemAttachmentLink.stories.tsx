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
import { Meta } from "@storybook/react";
import { Story } from "@storybook/react/dist/client/preview/types-6-0";
import * as React from "react";
import ItemAttachmentLink, {
  ItemAttachmentLinkProps,
} from "../../tsrc/components/ItemAttachmentLink";

export default {
  title: "component/ItemAttachmentLink",
  component: ItemAttachmentLink,
} as Meta<ItemAttachmentLinkProps>;

const githubAvatarUrl = "https://avatars2.githubusercontent.com/u/54074368";

export const linkToLightbox: Story<ItemAttachmentLinkProps> = (
  args: ItemAttachmentLinkProps
) => (
  <ItemAttachmentLink {...args}>
    <p>Click to show lightbox</p>
  </ItemAttachmentLink>
);
linkToLightbox.args = {
  description: "openEQUELLA GitHub avatar",
  mimeType: "image/png",
  viewerDetails: ["lightbox", githubAvatarUrl],
};

export const linkToURL: Story<ItemAttachmentLinkProps> = (
  args: ItemAttachmentLinkProps
) => (
  <ItemAttachmentLink {...args}>
    <p>Click to go to link</p>
  </ItemAttachmentLink>
);
linkToURL.args = {
  viewerDetails: ["link", githubAvatarUrl],
};
