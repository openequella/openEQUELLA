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
import * as NEA from "fp-ts/NonEmptyArray";

export const emptyDashboardDetails: OEQ.Dashboard.DashboardDetails = {
  portlets: [],
};

export const privateSearchPortlet: OEQ.Dashboard.BasicPortlet = {
  commonDetails: {
    uuid: "01f33f25-f3e3-4bb3-898f-2fa2410273f5",
    name: "Private Search Portlet",
    column: 0,
    order: 1,
    isInstitutionWide: false,
    isClosed: false,
    isMinimised: false,
    canClose: false,
    canDelete: true,
    canEdit: true,
    canMinimise: true,
  },
  portletType: "search",
};

export const privateFavouritePortlet: OEQ.Dashboard.BasicPortlet = {
  commonDetails: {
    ...privateSearchPortlet.commonDetails,
    name: "Private Favourites Portlet",
    uuid: "58a60093-45fb-427e-80c1-9cc9faa765e9",
    column: 0,
    order: 1,
  },
  portletType: "favourites",
};

export const privateTasksPortlet: OEQ.Dashboard.BasicPortlet = {
  commonDetails: {
    ...privateSearchPortlet.commonDetails,
    name: "Private Tasks Portlet",
    uuid: "47d805e4-a87b-43f7-bcbf-a59e304a2af9",
    column: 0,
    order: 0,
  },
  portletType: "tasks",
};

export const publicHtmlPortlet: OEQ.Dashboard.FormattedTextPortlet = {
  commonDetails: {
    ...privateSearchPortlet.commonDetails,
    name: "Public HTML Portlet",
    uuid: "ca59801f-76e4-499b-adcd-afc2a40f4f5f",
    isInstitutionWide: true,
    canClose: true,
    canDelete: false,
    column: 1,
    order: 0,
  },
  portletType: "html",
  rawHtml: "<p>This is a <strong>public</strong> HTML portlet.</p>",
};

export const publicRecentContributionsPortlet: OEQ.Dashboard.RecentContributionsPortlet =
  {
    commonDetails: {
      ...publicHtmlPortlet.commonDetails,
      name: "Public Recent Contributions Portlet",
      uuid: "b21e5b0c-9215-441e-92bb-6c7a74b0ac8e",
      column: 1,
      order: 1,
    },
    portletType: "recent",
    query: "test",
    isShowTitleOnly: false,
  };

export const mockPortlets: NEA.NonEmptyArray<OEQ.Dashboard.BasicPortlet> = [
  privateSearchPortlet,
  privateFavouritePortlet,
  privateTasksPortlet,
  publicHtmlPortlet,
  publicRecentContributionsPortlet,
];
