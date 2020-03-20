import Axios from "axios";
import { fromAxiosError } from "../../api/errors";
import { templateError, TemplateUpdate } from "../../mainui/Template";

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

export const SEARCH_SETTINGS_URL = "api/settings/search";
export const CLOUD_SETTINGS_URL = "api/settings/search/cloud";

export const getSearchSettingsFromServer = new Promise(
  (
    resolve: (settings: SearchSettings) => void,
    reject: (error: TemplateUpdate) => void
  ) => {
    Axios.get(SEARCH_SETTINGS_URL)
      .then(response => resolve(response.data))
      .catch(error => reject(templateError(fromAxiosError(error))));
  }
);

export const saveSearchSettingsToServer = (settings: SearchSettings) =>
  new Promise(
    (resolve: () => void, reject: (error: TemplateUpdate) => void) => {
      Axios.put(SEARCH_SETTINGS_URL, settings)
        .then(() => {
          resolve();
        })
        .catch(error => reject(templateError(fromAxiosError(error))));
    }
  );
export const saveCloudSettingsToServer = (settings: CloudSettings) =>
  new Promise(
    (resolve: () => void, reject: (error: TemplateUpdate) => void) => {
      Axios.put(CLOUD_SETTINGS_URL, settings)
        .then(() => {
          resolve();
        })
        .catch(error => reject(templateError(fromAxiosError(error))));
    }
  );

export const getCloudSettingsFromServer = new Promise(
  (
    resolve: (settings: CloudSettings) => void,
    reject: (error: TemplateUpdate) => void
  ) => {
    Axios.get(CLOUD_SETTINGS_URL)
      .then(response => resolve(response.data))
      .catch(error => reject(templateError(fromAxiosError(error))));
  }
);

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
