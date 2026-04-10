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
  itemWithAttachment,
  itemWithBookmark,
  itemWithLongDescription,
  normalItemWithoutName,
  itemNotInKeyResource,
} from "../../../__mocks__/SearchResult.mock";
import {
  PortletSearchResultList,
  PortletSearchResultListProps,
} from "../../../tsrc/dashboard/components/PortletSearchResultList";

export default {
  title: "Dashboard/components/PortletSearchResultList",
  component: PortletSearchResultList,
  argTypes: {
    hideDescription: {
      control: { type: "boolean" },
      description:
        "If true, hides the description field and displays title only",
    },
  },
} as Meta<PortletSearchResultListProps>;

const Template: StoryFn<PortletSearchResultListProps> = (args) => (
  <PortletSearchResultList {...args} />
);

const mockResults = [
  itemWithAttachment,
  normalItemWithoutName,
  itemNotInKeyResource,
  itemWithBookmark,
];

export const Default = Template.bind({});
Default.args = {
  results: mockResults,
};

export const TitleOnly = Template.bind({});
TitleOnly.args = {
  results: mockResults,
  hideDescription: true,
};

export const EmptyList = Template.bind({});
EmptyList.args = {
  results: [],
};

export const SingleItem = Template.bind({});
SingleItem.args = {
  results: [itemWithAttachment],
};

export const LongDescription = Template.bind({});
LongDescription.args = {
  results: [itemWithLongDescription],
};

export const HtmlDescription = Template.bind({});
HtmlDescription.args = {
  results: [
    {
      ...itemWithLongDescription,
      description: "<h1>This is a bold statement.</h1>",
    },
  ],
};
