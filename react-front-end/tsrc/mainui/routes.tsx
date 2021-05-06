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
import { LocationDescriptor } from "history";
import * as React from "react";
import { TemplateUpdate } from "./Template";

const ThemePage = React.lazy(() => import("../theme/ThemePage"));
const CloudProviderListPage = React.lazy(
  () => import("../cloudprovider/CloudProviderListPage")
);
const SearchPageSettings = React.lazy(
  () => import("../settings/Search/SearchPageSettings")
);
const SettingsPage = React.lazy(() => import("../settings/SettingsPage"));
const SearchFilterPage = React.lazy(
  () => import("../settings/Search/searchfilter/SearchFilterSettingsPage")
);
const LoginNoticeConfigPage = React.lazy(
  () => import("../loginnotice/LoginNoticeConfigPage")
);
const FacetedSearchSettingsPage = React.lazy(
  () => import("../settings/Search/facetedsearch/FacetedSearchSettingsPage")
);
const ContentIndexSettings = React.lazy(
  () => import("../settings/Search/ContentIndexSettings")
);

export interface BaseOEQRouteComponentProps {
  updateTemplate: (edit: TemplateUpdate) => void;
  redirect: (to: LocationDescriptor) => void;
  setPreventNavigation: (b: boolean) => void;
  refreshUser: () => void;
  isReloadNeeded: boolean;
}

type ToFunc = (uuid: string) => string;
type ToVersionFunc = (uuid: string, version: number) => string;

export interface OEQRouteNewUI {
  component?: React.ComponentType<BaseOEQRouteComponentProps>;
  render?: (props: BaseOEQRouteComponentProps) => React.ReactNode;
  path: string;
}

interface OEQRouteTo<T = string | ToFunc | ToVersionFunc> {
  to: T;
}

interface Routes {
  AdvancedSearch: OEQRouteTo<ToFunc>;
  CloudProviders: OEQRouteNewUI;
  ContentIndexSettings: OEQRouteNewUI;
  FacetedSearchSetting: OEQRouteNewUI;
  LoginNoticeConfig: OEQRouteNewUI;
  Logout: OEQRouteTo<string>;
  Notifications: OEQRouteTo<string>;
  RemoteSearch: OEQRouteTo<ToFunc>;
  SearchFilterSettings: OEQRouteNewUI;
  SearchSettings: OEQRouteNewUI;
  Settings: OEQRouteNewUI & OEQRouteTo<string>;
  TaskList: OEQRouteTo<string>;
  ThemeConfig: OEQRouteNewUI;
  UserPreferences: OEQRouteTo<string>;
  ViewItem: OEQRouteTo<ToVersionFunc>;
}

/**
 * Type guard for when needing to dynamically determine what kind of route is being used.
 *
 * @param route the potential route to check
 */
export const isNewUIRoute = (route: unknown): route is OEQRouteNewUI =>
  (route as OEQRouteNewUI).component !== undefined ||
  (route as OEQRouteNewUI).render !== undefined;

/**
 * Simple validator to allow direct use of an expected to URL route - considering they're hardcoded
 * if an expected one is missing - or of the wrong type - then we have a code issue.
 *
 * @param to route to validate
 */
export const legacyPageUrl = (to?: string | ToFunc | ToVersionFunc): string => {
  if (typeof to === "string") {
    return to;
  }

  throw new TypeError("Expected legacy page URL is undefined");
};

export const routes: Routes = {
  AdvancedSearch: {
    to: (uuid: string) => `/advanced/searching.do?in=P${uuid}&editquery=true`,
  },
  CloudProviders: {
    path: "/page/cloudprovider",
    component: CloudProviderListPage,
  },
  ContentIndexSettings: {
    path: "/page/contentindexsettings",
    component: ContentIndexSettings,
  },
  FacetedSearchSetting: {
    path: "/page/facetedsearchsettings",
    component: FacetedSearchSettingsPage,
  },
  LoginNoticeConfig: {
    path: "/page/loginconfiguration",
    component: LoginNoticeConfigPage,
  },
  Logout: {
    // lack of '/' is significant
    to: "logon.do?logout=true",
  },
  Notifications: {
    to: "/access/notifications.do",
  },
  RemoteSearch: {
    // `uc` parameter comes from sections code (AbstractRootSearchSection.Model.java). Setting it to
    // true clears out the Session State for Remote Repository pages. This replicates the behaviour
    // for links inside the 'Within' dropdown in the legacy UI.
    // See com.tle.web.searching.section.SearchQuerySection.forwardToRemote
    to: (uuid: string) => `/access/z3950.do?.repository=${uuid}&uc=true`,
  },
  Settings: {
    path: "(/access/settings.do|/page/settings)",
    to: "/page/settings",
    component: SettingsPage,
  },
  SearchFilterSettings: {
    path: "/page/searchfiltersettings",
    component: SearchFilterPage,
  },
  SearchSettings: {
    path: "/page/searchsettings",
    component: SearchPageSettings,
  },
  TaskList: {
    to: "/access/tasklist.do",
  },
  ThemeConfig: { path: "/page/themeconfiguration", component: ThemePage },
  UserPreferences: {
    to: "/access/user.do",
  },
  ViewItem: {
    to: (uuid: string, version: number) => `/items/${uuid}/${version}/`,
  },
};
