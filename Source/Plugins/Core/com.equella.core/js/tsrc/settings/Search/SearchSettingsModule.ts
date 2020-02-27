import Axios, { AxiosPromise } from "axios";

export interface SearchSettings {
  searchingShowNonLiveCheckbox: boolean;
  searchingDisableGallery: boolean;
  searchingDisableVideos: boolean;
  fileCountDisabled: boolean;
  defaultSearchSort: SortOrder;
  authenticateFeedsByDefault: boolean;

  urlLevel: number;
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
  RATING = "RATING"
}

export const SEARCH_SETTINGS_URL = "/api/settings/search";
export const CLOUD_SETTINGS_URL = "/api/settings/search/cloud";

export function getSearchSettingsFromServer(): AxiosPromise<SearchSettings> {
  return Axios.get(SEARCH_SETTINGS_URL);
}

export function saveSearchSettingsToServer(
  settings: SearchSettings
): AxiosPromise {
  return Axios.post(SEARCH_SETTINGS_URL, settings);
}

export function getCloudSettingsFromServer(): AxiosPromise<CloudSettings> {
  return Axios.get(CLOUD_SETTINGS_URL);
}

export function saveCloudSettingsToServer(
  settings: CloudSettings
): AxiosPromise {
  return Axios.post(CLOUD_SETTINGS_URL, settings);
}

export const defaultSearchSettings: SearchSettings = {
  searchingShowNonLiveCheckbox: false,
  searchingDisableGallery: false,
  searchingDisableVideos: false,
  fileCountDisabled: false,
  defaultSearchSort: SortOrder.RANK,
  authenticateFeedsByDefault: false,

  urlLevel: 0,
  titleBoost: 0,
  descriptionBoost: 0,
  attachmentBoost: 0
};
