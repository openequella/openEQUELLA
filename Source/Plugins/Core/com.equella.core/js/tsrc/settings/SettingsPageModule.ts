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
import Axios, { AxiosPromise, CancelToken } from "axios";
import { GeneralSetting, UISetting } from "./SettingsPageEntry";
import { Config } from "../config";

const GET_SETTINGS_URL = `${Config.baseUrl}api/settings`;
const GET_UI_SETTINGS_URL = `${GET_SETTINGS_URL}/ui`;

export const fetchSettings = (
  token: CancelToken
): Promise<GeneralSetting[]> => {
  return Axios.get<GeneralSetting[]>(GET_SETTINGS_URL, {
    cancelToken: token,
  }).then((res) => res.data);
};

export const fetchUISetting = (token: CancelToken): Promise<UISetting> => {
  return Axios.get<UISetting>(GET_UI_SETTINGS_URL, {
    cancelToken: token,
  }).then((res) => res.data);
};

export const saveUISetting = (
  newUIEnabled: boolean,
  newSearchEnabled: boolean
): AxiosPromise => {
  return Axios.put(GET_UI_SETTINGS_URL, {
    newUI: {
      enabled: newUIEnabled,
      newSearch: newSearchEnabled,
    },
  });
};
