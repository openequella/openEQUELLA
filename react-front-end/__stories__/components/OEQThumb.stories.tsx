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
  brokenFileAttachment,
  resourceFileAttachment,
  equellaItemAttachment,
  fileAttachment,
  htmlAttachment,
  linkAttachment,
  resourceLinkAttachment,
  resourceHtmlAttachment,
} from "../../__mocks__/OEQThumb.mock";
import OEQThumb, { OEQThumbProps } from "../../tsrc/components/OEQThumb";

export default {
  title: "Component/OEQThumb",
  component: OEQThumb,
} as Meta<OEQThumbProps>;

export const fileAttachmentStory: Story<OEQThumbProps> = (args) => (
  <OEQThumb {...args} />
);

fileAttachmentStory.args = {
  attachment: fileAttachment,
  showPlaceholder: false,
};

export const brokenFileAttachmentStory: Story<OEQThumbProps> = (args) => (
  <OEQThumb {...args} />
);

brokenFileAttachmentStory.args = {
  attachment: brokenFileAttachment,
  showPlaceholder: false,
};

export const customResourceAttachmentStory: Story<OEQThumbProps> = (args) => (
  <OEQThumb {...args} />
);

customResourceAttachmentStory.args = {
  attachment: resourceFileAttachment,
  showPlaceholder: false,
};

export const linkAttachmentStory: Story<OEQThumbProps> = (args) => (
  <OEQThumb {...args} />
);

linkAttachmentStory.args = {
  attachment: linkAttachment,
  showPlaceholder: false,
};

export const resourceLinkAttachmentStory: Story<OEQThumbProps> = (args) => (
  <OEQThumb {...args} />
);

resourceLinkAttachmentStory.args = {
  attachment: resourceLinkAttachment,
  showPlaceholder: false,
};

export const equellaItemAttachmentStory: Story<OEQThumbProps> = (args) => (
  <OEQThumb {...args} />
);

equellaItemAttachmentStory.args = {
  attachment: equellaItemAttachment,
  showPlaceholder: false,
};

export const htmlAttachmentStory: Story<OEQThumbProps> = (args) => (
  <OEQThumb {...args} />
);

htmlAttachmentStory.args = {
  attachment: htmlAttachment,
  showPlaceholder: false,
};

export const resourceHtmlAttachmentStory: Story<OEQThumbProps> = (args) => (
  <OEQThumb {...args} />
);

resourceHtmlAttachmentStory.args = {
  attachment: resourceHtmlAttachment,
  showPlaceholder: false,
};
