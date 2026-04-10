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
import { SimpleTreeView } from "@mui/x-tree-view/SimpleTreeView";
import { Meta, StoryFn } from "@storybook/react";
import * as React from "react";
import {
  simpleTopic,
  topicWithChildren,
  topicWithHtmlDesc,
  topicWithShortAndLongDesc,
} from "../../__mocks__/Hierarchy.mock";
import HierarchyTopic, {
  HierarchyTopicProps,
} from "../../tsrc/hierarchy/components/HierarchyTopic";

export default {
  title: "Hierarchy/HierarchyTopic",
  component: HierarchyTopic,
} as Meta<HierarchyTopicProps>;

export const Simple: StoryFn<HierarchyTopicProps> = (args) => (
  <SimpleTreeView>
    <HierarchyTopic {...args} />
  </SimpleTreeView>
);
Simple.args = {
  topic: simpleTopic,
  expandedNodes: [],
};

export const WithShortDesc: StoryFn<HierarchyTopicProps> = (args) => (
  <SimpleTreeView>
    <HierarchyTopic {...args} />
  </SimpleTreeView>
);
WithShortDesc.args = {
  topic: topicWithShortAndLongDesc,
  expandedNodes: [],
};

export const WithHtmlShortDesc: StoryFn<HierarchyTopicProps> = (args) => (
  <SimpleTreeView>
    <HierarchyTopic {...args} />
  </SimpleTreeView>
);
WithHtmlShortDesc.args = {
  topic: topicWithHtmlDesc,
  expandedNodes: [],
};

export const WithChildren: StoryFn<HierarchyTopicProps> = (args) => (
  <SimpleTreeView>
    <HierarchyTopic {...args} />
  </SimpleTreeView>
);
WithChildren.args = {
  topic: topicWithChildren,
  expandedNodes: [],
};
