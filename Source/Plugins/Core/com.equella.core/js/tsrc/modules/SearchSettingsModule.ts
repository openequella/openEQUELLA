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
import Axios from "axios";
import { fromAxiosError } from "../api/errors";
import { templateError, TemplateUpdate } from "../mainui/Template";

export interface SearchSettings {
  searchingShowNonLiveCheckbox: boolean;
  searchingDisableGallery: boolean;
  searchingDisableVideos: boolean;
  searchingDisableOwnerFilter: boolean;
  searchingDisableDateModifiedFilter: boolean;
  fileCountDisabled: boolean;
  defaultSearchSort: SortOrder;
  authenticateFeedsByDefault: boolean;

  urlLevel: ContentIndex;
  titleBoost: number;
  descriptionBoost: number;
  attachmentBoost: number;
}

export interface CloudSettings {
  disabled: boolean;
}

export enum SortOrder {
  RANK = "RANK",
  DATEMODIFIED = "DATEMODIFIED",
  DATECREATED = "DATECREATED",
  NAME = "NAME",
  RATING = "RATING",
}

export enum ContentIndex {
  OPTION_NONE = 0,
  OPTION_WEBPAGE = 1,
  OPTION_SECONDARY = 2,
}

export const SEARCH_SETTINGS_URL = "api/settings/search";
export const CLOUD_SETTINGS_URL = "api/settings/search/cloud";

export const getSearchSettingsFromServer = () =>
  new Promise(
    (
      resolve: (settings: SearchSettings) => void,
      reject: (error: TemplateUpdate) => void
    ) => {
      Axios.get(SEARCH_SETTINGS_URL)
        .then((response) => resolve(response.data))
        .catch((error) => reject(templateError(fromAxiosError(error))));
    }
  );

export const saveSearchSettingsToServer = (settings: SearchSettings) =>
  new Promise(
    (resolve: () => void, reject: (error: TemplateUpdate) => void) => {
      Axios.put(SEARCH_SETTINGS_URL, settings)
        .then(() => {
          resolve();
        })
        .catch((error) => reject(templateError(fromAxiosError(error))));
    }
  );
export const saveCloudSettingsToServer = (settings: CloudSettings) =>
  new Promise(
    (resolve: () => void, reject: (error: TemplateUpdate) => void) => {
      Axios.put(CLOUD_SETTINGS_URL, settings)
        .then(() => {
          resolve();
        })
        .catch((error) => reject(templateError(fromAxiosError(error))));
    }
  );

export const getCloudSettingsFromServer = () =>
  new Promise(
    (
      resolve: (settings: CloudSettings) => void,
      reject: (error: TemplateUpdate) => void
    ) => {
      Axios.get(CLOUD_SETTINGS_URL)
        .then((response) => resolve(response.data))
        .catch((error) => reject(templateError(fromAxiosError(error))));
    }
  );

export const defaultSearchSettings: SearchSettings = {
  searchingShowNonLiveCheckbox: false,
  searchingDisableGallery: false,
  searchingDisableVideos: false,
  searchingDisableOwnerFilter: false,
  searchingDisableDateModifiedFilter: false,
  fileCountDisabled: false,
  defaultSearchSort: SortOrder.RANK,
  authenticateFeedsByDefault: false,

  urlLevel: 0,
  titleBoost: 0,
  descriptionBoost: 0,
  attachmentBoost: 0,
};
