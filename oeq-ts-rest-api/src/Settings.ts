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
import { is } from 'typescript-is';

export interface GeneralSetting {
  id: string;
  group: string;
  name: string;
  description: string;
  links: {
    href?: string;
    route?: string;
  };
}

export interface UISettings {
  newUI: {
    enabled: boolean;
    newSearch: boolean;
  };
}

const SETTINGS_ROOT_PATH = '/settings';
const UI_SETTINGS_PATH = `${SETTINGS_ROOT_PATH}/ui`;

/**
 * Retrieve the general oEQ settings.
 *
 * @param apiBasePath Base URI to the oEQ institution and API
 */
export const getGeneralSettings = (
  apiBasePath: string
): Promise<GeneralSetting[]> =>
  GET<GeneralSetting[]>(
    apiBasePath + SETTINGS_ROOT_PATH,
    (data): data is GeneralSetting[] => is<GeneralSetting[]>(data)
  );

/**
 * Retrieve the general UI settings for oEQ.
 *
 * @param apiBasePath Base URI to the oEQ institution and API
 */
export const getUiSettings = (apiBasePath: string): Promise<UISettings> =>
  GET<UISettings>(apiBasePath + UI_SETTINGS_PATH, (data): data is UISettings =>
    is<UISettings>(data)
  );

/**
 * Update the UI settings to those provided.
 *
 * @param apiBasePath Base URI to the oEQ institution and API
 * @param updatedSettings New UI Settings
 */
export const updateUiSettings = (
  apiBasePath: string,
  updatedSettings: UISettings
): Promise<void> =>
  PUT<UISettings, undefined>(apiBasePath + UI_SETTINGS_PATH, updatedSettings);
