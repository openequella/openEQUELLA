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
