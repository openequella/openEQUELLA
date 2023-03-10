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
import { ThemeSettingsCodec } from './gen/Theme';
import { validate } from './Utils';

const THEME_ROOT_PATH = '/theme';
const THEME_SETTINGS_PATH = `${THEME_ROOT_PATH}/settings`;
const THEME_LOGO_PATH = `${THEME_ROOT_PATH}/logo`;

export interface ThemeSettings {
  primaryColor: string;
  secondaryColor: string;
  backgroundColor: string;
  paperColor: string;
  menuItemColor: string;
  menuItemTextColor: string;
  menuItemIconColor: string;
  primaryTextColor: string;
  menuTextColor: string;
  fontSize: number;
}

/**
 * Retrieve the theme settings for oEQ.
 *
 * @param apiBasePath Base URI to the oEQ institution and API
 */
export const getThemeSettings = (apiBasePath: string): Promise<ThemeSettings> =>
  GET<ThemeSettings>(
    apiBasePath + THEME_SETTINGS_PATH,
    validate(ThemeSettingsCodec)
  );

/**
 * Update the theme settings to those provided.
 *
 * @param apiBasePath Base URI to the oEQ institution and API
 * @param updatedSettings New theme Settings
 */
export const updateThemeSettings = (
  apiBasePath: string,
  updatedSettings: ThemeSettings
): Promise<void> =>
  PUT<ThemeSettings, undefined>(
    apiBasePath + THEME_SETTINGS_PATH,
    updatedSettings
  );

/**
 * Retrieve theme logo url
 *
 * @param apiBasePath Base URI to the oEQ institution and API
 */
export const getThemeLogoUrl = (apiBasePath: string): string =>
  apiBasePath + THEME_ROOT_PATH + '/newLogo.png';

/**
 * Delete theme logo, reset logo to default
 *
 * @param apiBasePath Base URI to the oEQ institution and API
 */
export const deleteLogo = (apiBasePath: string): Promise<void> =>
  DELETE<void>(apiBasePath + THEME_LOGO_PATH);

/**
 * Update theme logo.
 *
 * @param apiBasePath Base URI to the oEQ institution and API
 * @param file New logo file
 */
export const updateThemeLogo = (
  apiBasePath: string,
  file: File
): Promise<void> => PUT<File, undefined>(apiBasePath + THEME_LOGO_PATH, file);
