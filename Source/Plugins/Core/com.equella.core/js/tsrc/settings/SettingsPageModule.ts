import Axios, { AxiosPromise } from "axios";
import { GeneralSetting, UISetting } from "./SettingsPageEntry";
import { Config } from "../config";

const GET_SETTINGS_URL = `${Config.baseUrl}api/settings`;
const GET_UI_SETTINGS_URL = `${GET_SETTINGS_URL}/ui`;

export function fetchSettings(): AxiosPromise<GeneralSetting[]> {
  return Axios.get(GET_SETTINGS_URL);
}

export function fetchUISetting(): AxiosPromise<UISetting> {
  return Axios.get(GET_UI_SETTINGS_URL);
}

export function saveUISetting(
  newUIEnabled: boolean,
  newSearchEnabled: boolean
): AxiosPromise<UISetting> {
  return Axios.put(GET_UI_SETTINGS_URL, {
    newUI: {
      enabled: newUIEnabled,
      newSearch: newSearchEnabled
    }
  });
}
