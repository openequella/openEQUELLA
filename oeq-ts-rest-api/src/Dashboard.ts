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
import { DELETE, GET, PUT } from './AxiosInstance';
import type { UuidString, ItemStatus } from './Common';
import type { Trend } from './Task';
import {
  DashboardDetailsCodec,
  PortletClosedCodec,
  PortletCreatableCodec,
} from './gen/Dashboard';
import { validate } from './Utils';
import * as t from 'io-ts';

/**
 * Supported Portlet types, excluding those deprecated.
 */
export type PortletType =
  | 'browse'
  | 'favourites'
  | 'freemarker'
  | 'html'
  | 'myresources'
  | 'recent'
  | 'search'
  | 'tasks'
  | 'taskstatistics';

/**
 * Supported Portlet column positions where 'left' is represented by 0 and 'right' is represented by 1.
 */
export type PortletColumn = 0 | 1;

/**
 * Common details shared by all portlet types.
 */
interface PortletBase {
  /**
   * UUID of the portlet
   */
  uuid: UuidString;
  /**
   * Display name of the portlet
   */
  name: string;
  /**
   * Whether the portlet is institution-wide
   */
  isInstitutionWide: boolean;
  /**
   * Whether the portlet is closed
   */
  isClosed: boolean;
  /**
   * Whether the portlet is minimised
   */
  isMinimised: boolean;
  /**
   * Whether the portlet can be closed
   */
  canClose: boolean;
  /**
   * Whether the portlet can be deleted
   */
  canDelete: boolean;
  /**
   * Whether the portlet can be edited
   */
  canEdit: boolean;
  /**
   * Whether the portlet can be minimised
   */
  canMinimise: boolean;
  /**
   * The column the portlet is in
   */
  column: PortletColumn;
  /**
   * The order of the portlet in the column (starting from 0)
   */
  order: number;
}

/**
 * Basic structure for portlet details, typically used for portlets that do not
 * have any additional configurations.
 */
export interface BasicPortlet {
  commonDetails: PortletBase;
  portletType: PortletType;
}

/**
 * Structure for Formatted Text portlets, including the raw HTML content.
 */
export interface FormattedTextPortlet extends BasicPortlet {
  /**
   * String representation of the raw HTML content to be displayed in the portlet.
   */
  rawHtml: string;
}

/**
 * Structure for Recent Contributions portlets, including all the configured Item search criteria.
 */
export interface RecentContributionsPortlet extends BasicPortlet {
  /**
   * Optional list of Collection UUIDs used to filter the Items.
   */
  collectionUuids?: string[];
  /**
   * Optional query string used to filter Items.
   */
  query?: string;
  /**
   * Optional maximum age (in days) used to restrict Items by their last modified date.
   */
  maxAge?: number;
  /**
   * Optional Item status value used to filter Items.
   */
  itemStatus?: ItemStatus;
  /**
   * Whether to show title only or both title and description for each Item in the portlet.
   */
  isShowTitleOnly: boolean;
}

/**
 * Structure for Task Statistics portlets, including the configured trend period.
 */
export interface TaskStatisticsPortlet extends BasicPortlet {
  /**
   * Trend period used to calculate task statistics.
   */
  trend: Trend;
}

/**
 * Supported Dashboard layout types.
 */
export type DashboardLayout =
  | 'SingleColumn'
  | 'TwoEqualColumns'
  | 'TwoColumnsRatio1to2'
  | 'TwoColumnsRatio2to1';

/**
 * Details of the Dashboard, including the list of viewable Portlets and the layout.
 */
export interface DashboardDetails {
  portlets: BasicPortlet[];
  layout?: DashboardLayout;
}

/**
 * Structure for a portlet type that can be created by the user.
 */
export interface PortletCreatable {
  /**
   * One of portlet types defined in {@link PortletType}
   */
  portletType: PortletType;
  /**
   * Display name of the portlet type
   */
  name: string;
  /**
   * Short description explaining the portlet's purpose
   */
  desc: string;
}

/**
 * Structure for a portlet that has been closed by the user
 */
export interface PortletClosed {
  /**
   * UUID of the portlet
   */
  uuid: UuidString;
  /**
   * Display name of the portlet
   */
  name: string;
}

/**
 * Structure for a variety of UI preferences a user can configure for a portlet.
 */
export interface PortletPreference {
  /**
   * Whether the portlet is closed
   */
  isClosed?: boolean;
  /**
   * Whether the portlet is minimised
   */
  isMinimised?: boolean;
  /**
   * The column the portlet is in
   */
  column?: number;
  /**
   * The order of the portlet in the column
   */
  order?: number;
}

const DASHBOARD_PATH = '/dashboard';
const PORTLET_PATH = `${DASHBOARD_PATH}/portlet`;

/**
 * Retrieve Dashboard details, including the list of viewable Portlets and the layout.
 *
 * @param apiBasePath Base URI to the oEQ institution and API
 */
export const getDashboardDetails = (
  apiBasePath: string
): Promise<DashboardDetails> =>
  GET(`${apiBasePath}${DASHBOARD_PATH}`, validate(DashboardDetailsCodec));

/**
 * Update the Dashboard layout.
 *
 * @param apiBasePath Base URI to the oEQ institution and API
 * @param layout the new layout of the dashboard
 */
export const updateDashboardLayout = (
  apiBasePath: string,
  layout: DashboardLayout
): Promise<void> => PUT(`${apiBasePath}${DASHBOARD_PATH}/layout`, { layout });

/**
 * Retrieve a list of portlet types that can be created by the user.
 *
 * @param apiBasePath Base URI to the oEQ institution and API
 */
export const getCreatablePortlets = (
  apiBasePath: string
): Promise<PortletCreatable[]> =>
  GET(
    `${apiBasePath}${PORTLET_PATH}/creatable`,
    validate(t.array(PortletCreatableCodec))
  );

/**
 * Retrieve a list of portlets that have been closed by the user.
 *
 * @param apiBasePath Base URI to the oEQ institution and API
 */
export const getClosedPortlets = (
  apiBasePath: string
): Promise<PortletClosed[]> =>
  GET(
    `${apiBasePath}${PORTLET_PATH}/closed`,
    validate(t.array(PortletClosedCodec))
  );

/**
 * Update the user's UI preferences of a specific portlet.
 *
 * @param apiBasePath Base URI to the oEQ institution and API
 * @param uuid UUID of the target portlet
 * @param preference The new preferences to be applied to the portlet
 */
export const updatePortletPreferences = (
  apiBasePath: string,
  uuid: UuidString,
  preference: PortletPreference
): Promise<void> =>
  PUT(`${apiBasePath}${PORTLET_PATH}/${uuid}/preference`, preference);

/**
 * Delete a portlet by UUID.
 *
 * @param apiBasePath Base URI to the oEQ institution and API
 * @param uuid UUID of the target portlet
 */
export const deletePortlet = (
  apiBasePath: string,
  uuid: UuidString
): Promise<void> => DELETE(`${apiBasePath}${PORTLET_PATH}/${uuid}`);
