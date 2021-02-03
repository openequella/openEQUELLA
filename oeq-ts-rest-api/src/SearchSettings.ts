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
import { Literal, Static, Union } from 'runtypes';
import { is } from 'typescript-is';
import { GET, PUT } from './AxiosInstance';

export const ContentIndexLevelRunTypes = Union(
  Literal(0),
  Literal(1),
  Literal(2)
);

export type ContentIndexLevel = Static<typeof ContentIndexLevelRunTypes>;

export const SortOrderRunTypes = Union(
  Literal('RANK'),
  Literal('DATEMODIFIED'),
  Literal('DATECREATED'),
  Literal('NAME'),
  Literal('RATING')
);

export type SortOrder = Static<typeof SortOrderRunTypes>;

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

export interface CloudSettings {
  disabled: boolean;
}

export const SEARCH_SETTINGS_URL = '/settings/search';
export const CLOUD_SETTINGS_URL = `${SEARCH_SETTINGS_URL}/cloud`;

/**
 * Retrieve the general Search settings for OEQ.
 * @param apiBasePath Base URI to the oEQ institution and API
 */
export const getSearchSettings = (apiBasePath: string): Promise<Settings> =>
  GET<Settings>(apiBasePath + SEARCH_SETTINGS_URL, (data): data is Settings =>
    is<Settings>(data)
  );

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

/**
 * Retrieve the general Cloud settings for OEQ.
 * @param apiBasePath Base URI to the oEQ institution and API
 */
export const getCloudSettings = (apiBasePath: string): Promise<CloudSettings> =>
  GET<CloudSettings>(
    apiBasePath + CLOUD_SETTINGS_URL,
    (data): data is CloudSettings => is<CloudSettings>(data)
  );

/**
 * Update the Cloud settings.
 * @param apiBasePath Base URI to the oEQ institution and API
 * @param settings New Cloud settings
 */
export const updateCloudSettings = (
  apiBasePath: string,
  settings: CloudSettings
): Promise<void> =>
  PUT<CloudSettings, void>(apiBasePath + CLOUD_SETTINGS_URL, settings);
