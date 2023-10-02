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
import { GET, PUT } from './AxiosInstance';
import { SettingsCodec } from './gen/SearchSettings';
import type { SortOrder } from './Search';
import { validate } from './Utils';

export type ContentIndexLevel = 0 | 1 | 2;

export interface Settings {
  searchingShowNonLiveCheckbox: boolean;
  searchingDisableGallery: boolean;
  searchingDisableVideos: boolean;
  searchingDisableOwnerFilter: boolean;
  searchingDisableDateModifiedFilter: boolean;
  fileCountDisabled: boolean;
  defaultSearchSort?: SortOrder;
  authenticateFeedsByDefault: boolean;
  urlLevel: ContentIndexLevel;
  titleBoost: number;
  descriptionBoost: number;
  attachmentBoost: number;
}

export const SEARCH_SETTINGS_URL = '/settings/search';
export const CLOUD_SETTINGS_URL = `${SEARCH_SETTINGS_URL}/cloud`;

/**
 * Retrieve the general Search settings for OEQ.
 * @param apiBasePath Base URI to the oEQ institution and API
 */
export const getSearchSettings = (apiBasePath: string): Promise<Settings> =>
  GET<Settings>(apiBasePath + SEARCH_SETTINGS_URL, validate(SettingsCodec));

/**
 * Update the Search settings.
 * @param apiBasePath Base URI to the oEQ institution and API
 * @param settings New Search settings
 */
export const updateSearchSettings = (
  apiBasePath: string,
  settings: Settings
): Promise<void> =>
  PUT<Settings, void>(apiBasePath + SEARCH_SETTINGS_URL, settings);
