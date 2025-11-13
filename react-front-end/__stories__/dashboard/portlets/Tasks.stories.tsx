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
import * as OEQ from "@openequella/rest-api-client";
import { Meta, StoryFn } from "@storybook/react";
import * as React from "react";
import { BrowserRouter } from "react-router-dom";
import { getTaskAndNotificationCountsResp } from "../../../__mocks__/TaskModule.mock";
import {
  PortletTasks,
  PortletTasksProps,
} from "../../../tsrc/dashboard/portlet/PortletTasks";

export default {
  title: "Dashboard/portlets/PortletTasks",
  component: PortletTasks,
  decorators: [
    (Story) => (
      <BrowserRouter>
        <Story />
      </BrowserRouter>
    ),
  ],
} as Meta<PortletTasksProps>;

const Template: StoryFn<PortletTasksProps> = (args) => (
  <PortletTasks {...args} />
);

// Mock task providers with different scenarios
const mockTasksProvider = async (): Promise<OEQ.Task.TaskFilterCount[]> =>
  getTaskAndNotificationCountsResp;

const emptyTasksProvider = async (): Promise<OEQ.Task.TaskFilterCount[]> => [];

const onlyTasksProvider = async (): Promise<OEQ.Task.TaskFilterCount[]> =>
  getTaskAndNotificationCountsResp.filter(
    (item) => item.id === "taskall" || item.parent === "taskall",
  );

const onlyNotificationsProvider = async (): Promise<
  OEQ.Task.TaskFilterCount[]
> =>
  getTaskAndNotificationCountsResp.filter(
    (item) => item.id === "noteall" || item.parent === "noteall",
  );

const noParentTasksProvider = async (): Promise<OEQ.Task.TaskFilterCount[]> =>
  getTaskAndNotificationCountsResp.filter(
    (item) => item.id !== "taskall" && item.id !== "noteall",
  );

const highCountsProvider = async (): Promise<OEQ.Task.TaskFilterCount[]> =>
  getTaskAndNotificationCountsResp.map((item) => ({
    ...item,
    count:
      item.count > 0 ? item.count * 100 : Math.floor(Math.random() * 50) + 1,
  }));

const failingTasksProvider = async (): Promise<OEQ.Task.TaskFilterCount[]> => {
  throw new Error("Failed to fetch task counts");
};

const slowTasksProvider = async (): Promise<OEQ.Task.TaskFilterCount[]> => {
  await new Promise((resolve) => setTimeout(resolve, 3000));
  return getTaskAndNotificationCountsResp;
};

// Base portlet configuration
const basePortletConfig: OEQ.Dashboard.BasicPortlet = {
  commonDetails: {
    uuid: "tasks-story-uuid",
    name: "Tasks",
    column: 0,
    order: 0,
    isInstitutionWide: false,
    isClosed: false,
    isMinimised: false,
    canClose: true,
    canDelete: true,
    canEdit: true,
    canMinimise: true,
  },
  portletType: "tasks",
};

export const Default = Template.bind({});
Default.args = {
  cfg: basePortletConfig,
  tasksProvider: mockTasksProvider,
  position: { order: 0, column: 0 },
};

export const Minimised = Template.bind({});
Minimised.args = {
  ...Default.args,
  cfg: {
    ...basePortletConfig,
    commonDetails: {
      ...basePortletConfig.commonDetails,
      isMinimised: true,
    },
  },
};

export const OnlyTasks = Template.bind({});
OnlyTasks.args = {
  ...Default.args,
  tasksProvider: onlyTasksProvider,
  cfg: {
    ...basePortletConfig,
    commonDetails: {
      ...basePortletConfig.commonDetails,
      name: "Tasks Only",
    },
  },
};

export const OnlyNotifications = Template.bind({});
OnlyNotifications.args = {
  ...Default.args,
  tasksProvider: onlyNotificationsProvider,
  cfg: {
    ...basePortletConfig,
    commonDetails: {
      ...basePortletConfig.commonDetails,
      name: "Notifications Only",
    },
  },
};

export const HighCounts = Template.bind({});
HighCounts.args = {
  ...Default.args,
  tasksProvider: highCountsProvider,
  cfg: {
    ...basePortletConfig,
    commonDetails: {
      ...basePortletConfig.commonDetails,
      name: "High Count Examples",
    },
  },
};

export const NoResults = Template.bind({});
NoResults.args = {
  ...Default.args,
  tasksProvider: emptyTasksProvider,
};

export const MissingParentError = Template.bind({});
MissingParentError.args = {
  ...Default.args,
  tasksProvider: noParentTasksProvider,
  cfg: {
    ...basePortletConfig,
    commonDetails: {
      ...basePortletConfig.commonDetails,
      name: "Missing Parent Filter Error",
    },
  },
};

export const ErrorOnLoad = Template.bind({});
ErrorOnLoad.args = {
  ...Default.args,
  tasksProvider: failingTasksProvider,
};

export const SlowLoading = Template.bind({});
SlowLoading.args = {
  ...Default.args,
  tasksProvider: slowTasksProvider,
};
