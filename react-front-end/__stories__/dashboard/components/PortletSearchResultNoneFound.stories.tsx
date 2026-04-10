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
  PortletSearchResultNoneFound,
  PortletSearchResultNoneFoundProps,
} from "../../../tsrc/dashboard/components/PortletSearchResultNoneFound";

export default {
  title: "Dashboard/components/PortletSearchResultNoneFound",
  component: PortletSearchResultNoneFound,
  argTypes: {
    noneFoundMessage: {
      control: { type: "text" },
      description: "Message to display indicating that no results were found",
    },
  },
} as Meta<PortletSearchResultNoneFoundProps>;

const Template: StoryFn<PortletSearchResultNoneFoundProps> = (args) => (
  <PortletSearchResultNoneFound {...args} />
);

export const Basic = Template.bind({});
Basic.args = {
  noneFoundMessage: "No results found",
};

export const LongMessage = Template.bind({});
LongMessage.args = {
  noneFoundMessage:
    "Unfortunately, we couldn't find any items that match your search criteria. Please try adjusting your search terms or filters and try again.",
};
