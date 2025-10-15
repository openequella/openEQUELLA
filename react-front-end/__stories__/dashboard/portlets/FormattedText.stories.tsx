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
import { publicHtmlPortlet } from "../../../__mocks__/Dashboard.mock";
import {
  PortletFormattedText,
  PortletFormattedTextProps,
} from "../../../tsrc/dashboard/portlet/PortletFormattedText";

export default {
  title: "Dashboard/portlets/PortletFormattedText",
  component: PortletFormattedText,
} as Meta<PortletFormattedTextProps>;

export const Simple: StoryFn<PortletFormattedTextProps> = (args) => (
  <PortletFormattedText {...args} />
);
Simple.args = {
  cfg: {
    ...publicHtmlPortlet,
    rawHtml:
      "some plain text" +
      "<hr/>" +
      "<h3>Sample Formatted Text</h3>" +
      "<p>This is a sample formatted text portlet. You can configure it to display any HTML content you like.</p>" +
      "<hr/>" +
      "<h3>Example image:</h3><img src='./placeholder-500x500.png' alt='Placeholder Image'/>",
  },
};
