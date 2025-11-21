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
import type { Decorator, Meta, StoryFn } from "@storybook/react";
import * as React from "react";
import { dashboardDetailsWithLayout } from "../../../__mocks__/Dashboard.mock";
import {
  DashboardPageContext,
  defaultDashboardPageContextValue,
} from "../../../tsrc/dashboard/DashboardPageContext";
import { DashboardLayout } from "../../../tsrc/dashboard/editor/DashboardLayout";

export default {
  title: "Dashboard/editor/DashboardLayout",
  component: DashboardLayout,
  excludeStories: ["buildDashboardPageContextDecorator"],
} as Meta;

export const buildDashboardPageContextDecorator =
  (details?: OEQ.Dashboard.DashboardDetails): Decorator =>
  (Story) => (
    <DashboardPageContext.Provider
      value={{
        ...defaultDashboardPageContextValue,
        dashboardDetails: details,
      }}
    >
      {Story()}
    </DashboardPageContext.Provider>
  );

export const WithDashboardDetails: StoryFn = () => <DashboardLayout />;
WithDashboardDetails.decorators = [
  buildDashboardPageContextDecorator(dashboardDetailsWithLayout()),
];

export const NoDashboardDetails: StoryFn = () => <DashboardLayout />;
NoDashboardDetails.decorators = [buildDashboardPageContextDecorator(undefined)];
