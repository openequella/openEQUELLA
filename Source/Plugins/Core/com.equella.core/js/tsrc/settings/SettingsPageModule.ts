import Axios, { AxiosPromise } from "axios";
import { GeneralSetting, UISetting } from "./SettingsPageEntry";
import { Config } from "../config";

const GET_SETTINGS_URL = `${Config.baseUrl}api/settings`;
const GET_UI_SETTINGS_URL = `${GET_SETTINGS_URL}/ui`;

export const fetchSettings = (): Promise<GeneralSetting[]> => {
  return Axios.get<GeneralSetting[]>(GET_SETTINGS_URL).then(res => res.data);
};

export const fetchUISetting = (): Promise<UISetting> => {
  return Axios.get<UISetting>(GET_UI_SETTINGS_URL).then(res => res.data);
};

export const saveUISetting = (
  newUIEnabled: boolean,
  newSearchEnabled: boolean
): AxiosPromise => {
  return Axios.put(GET_UI_SETTINGS_URL, {
    newUI: {
      enabled: newUIEnabled,
      newSearch: newSearchEnabled
    }
  });
};
