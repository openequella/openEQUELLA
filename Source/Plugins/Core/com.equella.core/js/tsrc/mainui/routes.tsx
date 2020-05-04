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
import * as React from "react";
import { RouteComponentProps } from "react-router";
import { LocationDescriptor } from "history";
import { TemplateUpdate } from "./Template";
import SearchCourse from "../course/SearchCourse";
import EditCourse from "../course/EditCourse";
import ThemePage from "../theme/ThemePage";
import CloudProviderListPage from "../cloudprovider/CloudProviderListPage";
import { Bridge } from "../api/bridge";
import SearchPageSettings from "../settings/Search/SearchPageSettings";
import SettingsPage from "../settings/SettingsPage";
import SearchFilterPage from "../settings/Search/searchfilter/SearchFilterSettingsPage";
import ContentIndexSettings from "../settings/Search/ContentIndexSettings";
import LoginNoticeConfigPage from "../loginnotice/LoginNoticeConfigPage";

declare const bridge: Bridge;

export interface OEQRouteComponentProps<T = any>
  extends RouteComponentProps<T> {
  updateTemplate(edit: TemplateUpdate): void;
  redirect(to: LocationDescriptor): void;
  setPreventNavigation(b: boolean): void;
  refreshUser(): void;
}

export interface OEQRoute {
  component?:
    | React.ComponentType<OEQRouteComponentProps<any>>
    | React.ComponentType<any>;
  render?: (props: OEQRouteComponentProps<any>) => React.ReactNode;
  path?: string;
  exact?: boolean;
  sensitive?: boolean;
  strict?: boolean;
  to?: any;
}

export const routes = {
  Courses: { path: "/page/course", exact: true, component: SearchCourse },
  NewCourse: { path: "/page/course/new", exact: true, component: EditCourse },
  EditCourse: {
    path: "/page/course/:uuid",
    to: function (uuid: string) {
      return "/page/course/" + uuid;
    },
    render: (p: OEQRouteComponentProps<any>) => (
      <EditCourse {...p} uuid={p.match.params.uuid} />
    ),
  },
  Settings: {
    path: "(/access/settings.do|/page/settings)",
    to: "/page/settings",
    component: SettingsPage,
  },
  Search: {
    path: "/page/search",
    render: (p: OEQRouteComponentProps<any>) => <bridge.SearchPage {...p} />,
  },
  SearchSettings: {
    path: "/page/searchsettings",
    render: (p: OEQRouteComponentProps<any>) => <SearchPageSettings {...p} />,
  },

  SearchFilterSettings: {
    path: "/page/searchfiltersettings",
    render: (p: OEQRouteComponentProps<any>) => <SearchFilterPage {...p} />,
  },

  ContentIndexSettings: {
    path: "/page/contentindexsettings",
    render: (p: OEQRouteComponentProps<any>) => <ContentIndexSettings {...p} />,
  },
  ViewItem: {
    to: function (uuid: string, version: number) {
      return `/items/${uuid}/${version}/`;
    },
  },
  ThemeConfig: { path: "/page/themeconfiguration", component: ThemePage },
  LoginNoticeConfig: {
    path: "/page/loginconfiguration",
    component: LoginNoticeConfigPage,
  },
  CloudProviders: {
    path: "/page/cloudprovider",
    component: CloudProviderListPage,
  },
  Notifications: {
    to: "/access/notifications.do",
  },
  TaskList: {
    to: "/access/tasklist.do",
  },
  Logout: {
    // lack of '/' is significant
    to: "logon.do?logout=true",
  },
  UserPreferences: {
    to: "/access/user.do",
  },
};
