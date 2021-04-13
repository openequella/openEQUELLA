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
import { is } from 'typescript-is';

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
  id: string;
  username: string;
  firstName: string;
  lastName: string;
  emailAddress?: string;
  accessibilityMode: boolean;
  autoLoggedIn: boolean;
  guest: boolean;
  prefsEditable: boolean;
  menuGroups: Array<Array<MenuItem>>;
  counts?: ItemCounts;
  canDownloadSearchResult: boolean;
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
    (data): data is CurrentUserDetails => is<CurrentUserDetails>(data)
  );
