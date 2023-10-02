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
import * as React from "react";
import {
  brokenFileDetails,
  equellaItemDetails,
  fileDetails,
  htmlDetails,
  linkDetails,
  resourceFileDetails,
  resourceHtmlDetails,
  resourceLinkDetails,
} from "../../__mocks__/OEQThumb.mock";
import OEQThumb, { OEQThumbProps } from "../../tsrc/components/OEQThumb";

export default {
  title: "Component/OEQThumb",
  component: OEQThumb,
  argTypes: {
    showPlaceholder: { boolean: false },
  },
} as Meta<OEQThumbProps>;

export const file: StoryFn<OEQThumbProps> = (args) => <OEQThumb {...args} />;
file.args = {
  details: fileDetails,
};

export const brokenFile: StoryFn<OEQThumbProps> = (args) => (
  <OEQThumb {...args} />
);
brokenFile.args = {
  details: brokenFileDetails,
};

export const customResource: StoryFn<OEQThumbProps> = (args) => (
  <OEQThumb {...args} />
);
customResource.args = {
  details: resourceFileDetails,
};

export const link: StoryFn<OEQThumbProps> = (args) => <OEQThumb {...args} />;
link.args = {
  details: linkDetails,
};

export const resourceLink: StoryFn<OEQThumbProps> = (args) => (
  <OEQThumb {...args} />
);
resourceLink.args = {
  details: resourceLinkDetails,
};

export const equellaItem: StoryFn<OEQThumbProps> = (args) => (
  <OEQThumb {...args} />
);
equellaItem.args = {
  details: equellaItemDetails,
};

export const html: StoryFn<OEQThumbProps> = (args) => <OEQThumb {...args} />;
html.args = {
  details: htmlDetails,
};

export const resourceHtml: StoryFn<OEQThumbProps> = (args) => (
  <OEQThumb {...args} />
);
resourceHtml.args = {
  details: resourceHtmlDetails,
};

export const placeHolder: StoryFn<OEQThumbProps> = (args) => (
  <OEQThumb {...args} />
);
placeHolder.args = {
  details: undefined,
};
