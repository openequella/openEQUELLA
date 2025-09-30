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
import type { Meta, StoryFn } from "@storybook/react";
import * as React from "react";
import { mockPortlets } from "../../__mocks__/Dashboard.mock";
import {
  PortletContainer,
  PortletContainerProps,
} from "../../tsrc/dashboard/portlet/PortletContainer";

export default {
  title: "Dashboard/PortletContainer",
  component: PortletContainer,
  args: {
    portlets: mockPortlets,
    layout: "SingleColumn",
  },
} as Meta<PortletContainerProps>;

export const SingleColumn: StoryFn<PortletContainerProps> = (args) => (
  <PortletContainer {...args} />
);

SingleColumn.args = {
  layout: "SingleColumn",
};

export const TwoEqualColumns: StoryFn<PortletContainerProps> = (args) => (
  <PortletContainer {...args} />
);

TwoEqualColumns.args = {
  layout: "TwoEqualColumns",
};

export const TwoColumnsRatio1to2: StoryFn<PortletContainerProps> = (args) => (
  <PortletContainer {...args} />
);

TwoColumnsRatio1to2.args = {
  layout: "TwoColumnsRatio1to2",
};

export const TwoColumnsRatio2to1: StoryFn<PortletContainerProps> = (args) => (
  <PortletContainer {...args} />
);

TwoColumnsRatio2to1.args = {
  layout: "TwoColumnsRatio2to1",
};
