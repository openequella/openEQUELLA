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
  basicPortlet,
  minimisedPortlet,
  noClosePortlet,
  noDeletePortlet,
  noEditPortlet,
  noMinimisePortlet,
} from "../../__mocks__/Dashboard.mock";
import PortletItem, {
  PortletItemProps,
} from "../../tsrc/dashboard/components/PortletItem";

export default {
  title: "Dashboard/PortletItem",
  component: PortletItem,
} as Meta<PortletItemProps>;

const commonProps: PortletItemProps = {
  portlet: basicPortlet,
  children: <div>Portlet content</div>,
};

export const Standard: StoryFn<PortletItemProps> = (args) => (
  <PortletItem {...args} />
);
Standard.args = commonProps;

export const Minimised: StoryFn<PortletItemProps> = (args) => (
  <PortletItem {...args} />
);
Minimised.args = {
  ...commonProps,
  portlet: minimisedPortlet,
};

export const noEdit: StoryFn<PortletItemProps> = (args) => (
  <PortletItem {...args} />
);
noEdit.args = {
  ...commonProps,
  portlet: noEditPortlet,
};

export const noDelete: StoryFn<PortletItemProps> = (args) => (
  <PortletItem {...args} />
);
noDelete.args = {
  ...commonProps,
  portlet: noDeletePortlet,
};

export const noClose: StoryFn<PortletItemProps> = (args) => (
  <PortletItem {...args} />
);
noClose.args = {
  ...commonProps,
  portlet: noClosePortlet,
};

export const noMinimise: StoryFn<PortletItemProps> = (args) => (
  <PortletItem {...args} />
);
noMinimise.args = {
  ...commonProps,
  portlet: noMinimisePortlet,
};

export const loading: StoryFn<PortletItemProps> = (args) => (
  <PortletItem {...args} />
);
loading.args = {
  ...commonProps,
  isLoading: true,
};

export const HighlightTransition: StoryFn<PortletItemProps> = () => {
  const [highlight, setHighlight] = React.useState(true);

  React.useEffect(() => {
    const timer = setTimeout(() => setHighlight(false), 500);
    return () => clearTimeout(timer);
  }, []);

  return <PortletItem {...commonProps} highlight={highlight} />;
};
