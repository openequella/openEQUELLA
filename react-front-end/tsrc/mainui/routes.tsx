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
import { FAVOURITES_TYPE_PARAM } from "../favourites/FavouritesPageHelper";
import { FavouritesType } from "../modules/FavouriteModule";
import {
  isEditSystemSettingsGranted,
  isHierarchyPageACLGranted,
  isManageCloudProviderACLGranted,
  isSearchPageACLGranted,
  isViewHierarchyTopicACLGranted,
  RequiredPermissionCheck,
} from "../modules/SecurityModule";
import AdvancedSearchPage from "../search/AdvancedSearchPage";
import { TemplateUpdate } from "./Template";

const ThemePage = React.lazy(() => import("../theme/ThemePage"));
const LtiPlatformsSettingsPage = React.lazy(
  () => import("../settings/Integrations/lti13/Lti13PlatformsSettings"),
);
const CreateLti13PlatformPage = React.lazy(
  () => import("../settings/Integrations/lti13/components/CreateLti13Platform"),
);
const EditLti13PlatformPage = React.lazy(
  () => import("../settings/Integrations/lti13/components/EditLti13Platform"),
);
const CloudProviderListPage = React.lazy(
  () => import("../cloudprovider/CloudProviderListPage"),
);
const SearchPage = React.lazy(() => import("../search/SearchPage"));
const SearchPageSettings = React.lazy(
  () => import("../settings/Search/SearchPageSettings"),
);
const SettingsPage = React.lazy(() => import("../settings/SettingsPage"));
const SearchFilterPage = React.lazy(
  () => import("../settings/Search/searchfilter/SearchFilterSettingsPage"),
);
const LoginNoticeConfigPage = React.lazy(
  () => import("../loginnotice/LoginNoticeConfigPage"),
);
const FacetedSearchSettingsPage = React.lazy(
  () => import("../settings/Search/facetedsearch/FacetedSearchSettingsPage"),
);
const ContentIndexSettings = React.lazy(
  () => import("../settings/Search/ContentIndexSettings"),
);
const OidcSettingsPage = React.lazy(
  () => import("../settings/Integrations/oidc/OidcSettings"),
);
const MyResourcesPage = React.lazy(
  () => import("../myresources/MyResourcesPage"),
);
const BrowseHierarchyPage = React.lazy(
  () => import("../hierarchy/BrowseHierarchyPage"),
);
const RootHierarchyPage = React.lazy(
  () => import("../hierarchy/RootHierarchyPage"),
);
const DashboardPage = React.lazy(() => import("../dashboard/DashboardPage"));
const FavouritesPage = React.lazy(() => import("../favourites/FavouritesPage"));

export interface BaseOEQRouteComponentProps {
  updateTemplate: (edit: TemplateUpdate) => void;
  redirect: (to: LocationDescriptor) => void;
  setPreventNavigation: (b: boolean) => void;
  isReloadNeeded: boolean;
}

type ToFunc = (uuid: string) => string;
type ToVersionFunc = (uuid: string, version: number) => string;

export interface OEQRouteNewUI {
  /**
   * React component to be rendered for the route.
   */
  component: React.ComponentType<BaseOEQRouteComponentProps>;
  /**
   * The path which this route matches to.
   */
  path: string;
  /**
   * Optional function to check if the current user has permission to access the page which this
   * route points to. When none, authentication is required for the access. To make this route
   * publicly available, make this function always return a Promise of `true`.
   */
  permissionChecks?: RequiredPermissionCheck[];
}

interface OEQRouteTo<T = string | ToFunc | ToVersionFunc> {
  to: T;
}

interface Routes {
  BrowseHierarchy: OEQRouteNewUI;
  CloudProviders: OEQRouteNewUI;
  ContentIndexSettings: OEQRouteNewUI;
  CreateLti13Platform: OEQRouteNewUI;
  Dashboard: OEQRouteNewUI;
  EditLti13Platform: OEQRouteNewUI & OEQRouteTo<ToFunc>;
  FacetedSearchSetting: OEQRouteNewUI;
  Favourites: OEQRouteNewUI & {
    to: (favouritesType: FavouritesType) => string;
  };
  Hierarchy: OEQRouteNewUI & OEQRouteTo<ToFunc>;
  LoginNoticeConfig: OEQRouteNewUI;
  Logout: OEQRouteTo<string>;
  Lti13PlatformsSettings: OEQRouteNewUI;
  MyResources: OEQRouteNewUI;
  NewAdvancedSearch: OEQRouteNewUI & OEQRouteTo<ToFunc>;
  Notifications: OEQRouteTo<string>;
  OidcSettings: OEQRouteNewUI;
  OldAdvancedSearch: OEQRouteTo<ToFunc>; // Need this route to support using Advanced Search in Selection Session.
  OldHierarchy: OEQRouteTo<ToFunc>;
  RemoteSearch: OEQRouteTo<ToFunc>;
  SearchFilterSettings: OEQRouteNewUI;
  SearchPage: OEQRouteNewUI;
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
  (route as OEQRouteNewUI).component !== undefined;

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

// The component of Search UI varies, depending on the path and whether New Search UI is enabled.
// So only export their paths.
export const OLD_SEARCH_PATH = "/searching.do";
export const NEW_SEARCH_PATH = "/page/search";

export const NEW_ADVANCED_SEARCH_PATH = "/page/advancedsearch";

export const NEW_FAVOURITES_PATH = "/page/favourites";

export const NEW_MY_RESOURCES_PATH = "/page/myresources";
export const OLD_MY_RESOURCES_PATH = "/access/myresources.do";

export const NEW_HIERARCHY_PATH = "/page/hierarchy";
export const OLD_HIERARCHY_PATH = "/hierarchy.do";

export const NEW_DASHBOARD_PATH = "/page/home";
export const OLD_DASHBOARD_PATH = "/home.do";

export const routes: Routes = {
  BrowseHierarchy: {
    path: "/page/hierarchies",
    component: BrowseHierarchyPage,
    permissionChecks: [isHierarchyPageACLGranted],
  },
  CloudProviders: {
    path: "/page/cloudprovider",
    component: CloudProviderListPage,
    permissionChecks: [isManageCloudProviderACLGranted],
  },
  ContentIndexSettings: {
    path: "/page/contentindexsettings",
    component: ContentIndexSettings,
    permissionChecks: [isEditSystemSettingsGranted("searching")],
  },
  CreateLti13Platform: {
    path: "/page/createLti13Platform",
    component: CreateLti13PlatformPage,
    permissionChecks: [isEditSystemSettingsGranted("lti13platforms")],
  },
  Dashboard: {
    path: NEW_DASHBOARD_PATH,
    component: DashboardPage,
  },
  EditLti13Platform: {
    // normally platform ID will be an URL which need to be encoded first
    to: (platformId: string) => `/page/editLti13Platform/${btoa(platformId)}`,
    path: `/page/editLti13Platform/:platformIdBase64`,
    component: EditLti13PlatformPage,
    permissionChecks: [isEditSystemSettingsGranted("lti13platforms")],
  },
  FacetedSearchSetting: {
    path: "/page/facetedsearchsettings",
    component: FacetedSearchSettingsPage,
    permissionChecks: [isEditSystemSettingsGranted("searching")],
  },
  Hierarchy: {
    path: `${NEW_HIERARCHY_PATH}/:compoundUuid`,
    to: (compoundUuid: string) => `${NEW_HIERARCHY_PATH}/${compoundUuid}`,
    component: RootHierarchyPage,
    permissionChecks: [
      isHierarchyPageACLGranted,
      isViewHierarchyTopicACLGranted,
    ],
  },
  LoginNoticeConfig: {
    path: "/page/loginconfiguration",
    component: LoginNoticeConfigPage,
    permissionChecks: [isEditSystemSettingsGranted("loginnoticeeditor")],
  },
  Logout: {
    // lack of '/' is significant
    to: "logon.do?logout=true",
  },
  Lti13PlatformsSettings: {
    path: "/page/lti13platforms",
    component: LtiPlatformsSettingsPage,
    permissionChecks: [isEditSystemSettingsGranted("lti13platforms")],
  },
  OidcSettings: {
    path: "/page/oidc",
    component: OidcSettingsPage,
    permissionChecks: [isEditSystemSettingsGranted("oidc")],
  },
  Favourites: {
    to: (favouritesType: FavouritesType) =>
      `${NEW_FAVOURITES_PATH}?${FAVOURITES_TYPE_PARAM}=${favouritesType}`,
    path: NEW_FAVOURITES_PATH,
    component: FavouritesPage,
  },
  MyResources: {
    path: NEW_MY_RESOURCES_PATH,
    component: MyResourcesPage,
  },
  NewAdvancedSearch: {
    to: (uuid: string) => `${NEW_ADVANCED_SEARCH_PATH}/${uuid}`,
    path: `${NEW_ADVANCED_SEARCH_PATH}/:advancedSearchId`,
    component: AdvancedSearchPage,
    permissionChecks: [isSearchPageACLGranted],
  },
  Notifications: {
    to: "/access/notifications.do",
  },
  OldAdvancedSearch: {
    to: (uuid: string) => `/advanced/searching.do?in=P${uuid}&editquery=true`,
  },
  OldHierarchy: {
    to: (topic: string) => `${OLD_HIERARCHY_PATH}?topic=${topic}`,
  },
  RemoteSearch: {
    // `uc` parameter comes from sections code (AbstractRootSearchSection.Model.java). Setting it to
    // true clears out the Session State for Remote Repository pages. This replicates the behaviour
    // for links inside the 'Within' dropdown in the legacy UI.
    // See com.tle.web.searching.section.SearchQuerySection.forwardToRemote
    to: (uuid: string) => `/access/z3950.do?.repository=${uuid}&uc=true`,
  },
  SearchFilterSettings: {
    path: "/page/searchfiltersettings",
    component: SearchFilterPage,
    permissionChecks: [isEditSystemSettingsGranted("searching")],
  },
  SearchPage: {
    path: NEW_SEARCH_PATH,
    component: SearchPage,
    permissionChecks: [isSearchPageACLGranted],
  },
  SearchSettings: {
    path: "/page/searchsettings",
    component: SearchPageSettings,
    permissionChecks: [isEditSystemSettingsGranted("searching")],
  },
  Settings: {
    path: "(/access/settings.do|/page/settings)",
    to: "/page/settings",
    component: SettingsPage,
  },
  TaskList: {
    to: "/access/tasklist.do",
  },
  ThemeConfig: {
    path: "/page/themeconfiguration",
    component: ThemePage,
    permissionChecks: [isEditSystemSettingsGranted("theme")],
  },
  UserPreferences: {
    to: "/access/user.do",
  },
  ViewItem: {
    to: (uuid: string, version: number) => `/items/${uuid}/${version}/`,
  },
};
