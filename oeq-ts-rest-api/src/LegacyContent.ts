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
import { GET } from './AxiosInstance';
import { CurrentUserDetailsCodec } from './gen/LegacyContent';
import { validate } from './Utils';

export interface ItemCounts {
  tasks: number;
  notifications: number;
}

export interface MenuItem {
  title: string;
  href?: string;
  systemIcon?: string;
  route?: string;
  iconUrl?: string;
  newWindow: boolean;
}

export interface CurrentUserDetails {
  /** User ID */
  id: string;
  /** Username */
  username: string;
  /** First name */
  firstName: string;
  /** Last name */
  lastName: string;
  /** Email address */
  emailAddress?: string;
  /**
   * `true` if the user has enabled accessibility mode.
   */
  accessibilityMode: boolean;
  /**
   * `true` if the user was automatically logged in.
   */
  autoLoggedIn: boolean;
  /**
   * `true` if the user is a guest user.
   */
  guest: boolean;
  /**
   * `true` if the user can edit their preferences.
   */
  prefsEditable: boolean;
  /**
   * A list of menu groups the user has access to.
   */
  menuGroups: MenuItem[][];
  /**
   * Counts of items (tasks and notifications).
   */
  counts?: ItemCounts;
  /**
   * `true` if the user can download the results of a search.
   */
  canDownloadSearchResult: boolean;
  /**
   * UUIDs of the roles assigned to the user - as well as `TLE_LOGGED_IN_USER_ROLE` where applicable.
   */
  roles: string[];
  /**
   * `true` if access to Scrapbook is enabled.
   */
  scrapbookEnabled: boolean;
  /**
   * `true` if the user is a system user (mainly TLE_ADMINISTRATOR).
   */
  isSystem: boolean;
}

/**
 * Retrieve details of the current user (based on JSESSIONID) including details for the UI such
 * as menu structure and task and notification counts.
 *
 * @param apiBasePath Base URI to the oEQ institution and API
 */
export const getCurrentUserDetails = (
  apiBasePath: string
): Promise<CurrentUserDetails> =>
  GET<CurrentUserDetails>(
    apiBasePath + '/content/currentuser',
    validate(CurrentUserDetailsCodec)
  );
