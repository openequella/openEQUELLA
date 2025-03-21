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
import { rootHierarchies } from "../../__mocks__/Hierarchy.mock";
import HierarchyTree, {
  HierarchyTreeProps,
} from "../../tsrc/hierarchy/components/HierarchyTree";

export default {
  title: "Hierarchy/HierarchyTree",
  component: HierarchyTree,
} as Meta<HierarchyTreeProps>;

export const Standard: StoryFn<HierarchyTreeProps> = (args) => (
  <HierarchyTree {...args} />
);
Standard.args = {
  hierarchies: rootHierarchies,
};

export const OnlyShowTitle: StoryFn<HierarchyTreeProps> = (args) => (
  <HierarchyTree {...args} />
);
OnlyShowTitle.args = {
  hierarchies: rootHierarchies,
  onlyShowTitle: true,
};

export const DisableTitleLink: StoryFn<HierarchyTreeProps> = (args) => (
  <HierarchyTree {...args} />
);
DisableTitleLink.args = {
  hierarchies: rootHierarchies,
  disableTitleLink: true,
};

export const CustomActionButtons: StoryFn<HierarchyTreeProps> = (args) => (
  <HierarchyTree {...args} />
);
CustomActionButtons.args = {
  hierarchies: rootHierarchies,
  customActionBuilder: (uuid) => <button>Custom Action: {uuid}</button>,
};
