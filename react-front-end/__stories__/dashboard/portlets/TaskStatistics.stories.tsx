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
import * as TE from "fp-ts/lib/TaskEither";
import * as React from "react";
import { privateTaskStatisticsPortlet } from "../../../__mocks__/Dashboard.mock";
import { neverReturn } from "../../../__mocks__/Utils";
import {
  failingGetAllWorkflowTrends,
  getAllWorkflowTrends,
  getEmptyWorkflowTrends,
} from "../../../__mocks__/WorkflowModule.mock";
import {
  PortletTaskStatistics,
  PortletTaskStatisticsProps,
} from "../../../tsrc/dashboard/portlet/PortletTaskStatistics";

export default {
  title: "Dashboard/portlets/PortletTaskStatistics",
  component: PortletTaskStatistics,
} as Meta<PortletTaskStatisticsProps>;

const Template: StoryFn<PortletTaskStatisticsProps> = (args) => (
  <PortletTaskStatistics {...args} />
);

export const Standard: StoryFn<PortletTaskStatisticsProps> = Template.bind({});
Standard.args = {
  cfg: privateTaskStatisticsPortlet,
  getAllWorkflowTrendsProvider: getAllWorkflowTrends,
  isManageWorkflowACLGrantedProvider: TE.right(true),
};

export const WithMonthlyTrend: StoryFn<PortletTaskStatisticsProps> =
  Template.bind({});
WithMonthlyTrend.args = {
  ...Standard.args,
  cfg: {
    ...privateTaskStatisticsPortlet,
    trend: "MONTH",
  },
};

export const EmptyResult: StoryFn<PortletTaskStatisticsProps> = Template.bind(
  {},
);
EmptyResult.args = {
  ...Standard.args,
  getAllWorkflowTrendsProvider: getEmptyWorkflowTrends,
};

export const NoPermission: StoryFn<PortletTaskStatisticsProps> = Template.bind(
  {},
);
NoPermission.args = {
  ...Standard.args,
  isManageWorkflowACLGrantedProvider: TE.left("No Permission"),
};

export const ErrorTrends = Template.bind({});
ErrorTrends.args = {
  ...Standard.args,
  getAllWorkflowTrendsProvider: failingGetAllWorkflowTrends,
};

export const Loading: StoryFn<PortletTaskStatisticsProps> = Template.bind({});
Loading.args = {
  ...Standard.args,
  isManageWorkflowACLGrantedProvider: neverReturn,
};

export const FetchingTrendData: StoryFn<PortletTaskStatisticsProps> =
  Template.bind({});
FetchingTrendData.args = {
  ...Standard.args,
  getAllWorkflowTrendsProvider: neverReturn,
};
