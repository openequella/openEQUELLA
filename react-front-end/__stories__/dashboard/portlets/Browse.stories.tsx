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
import { privateBrowsePortlet } from "../../../__mocks__/Dashboard.mock";
import { getRootHierarchies } from "../../../__mocks__/Hierarchy.mock";
import {
  PortletBrowse,
  PortletBrowseProps,
} from "../../../tsrc/dashboard/portlet/PortletBrowse";

export default {
  title: "Dashboard/portlets/Browse",
  component: PortletBrowse,
} as Meta<PortletBrowseProps>;

const Template: StoryFn<PortletBrowseProps> = (args) => (
  <PortletBrowse {...args} />
);

const emptyGetRootHierarchiesProvider = async () => [];
const failingGetRootHierarchiesProvider = async () => {
  throw new Error("Get root hierarchies failed");
};

export const Standard = Template.bind({});
Standard.args = {
  cfg: privateBrowsePortlet,
  getRootHierarchiesProvider: getRootHierarchies,
  position: { order: 0, column: 0 },
};

export const NoResults = Template.bind({});
NoResults.args = {
  ...Standard.args,
  getRootHierarchiesProvider: emptyGetRootHierarchiesProvider,
};

export const ErrorGet = Template.bind({});
ErrorGet.args = {
  ...Standard.args,
  getRootHierarchiesProvider: failingGetRootHierarchiesProvider,
};

export const Loading = Template.bind({});
Loading.args = {
  ...Standard.args,
  getRootHierarchiesProvider: () =>
    new Promise((_) => {
      // Never resolves.
    }),
};
