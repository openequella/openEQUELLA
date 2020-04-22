import axios, { AxiosPromise } from "axios";
import { Config } from "../config";
import { languageStrings } from "../util/langstrings";
import { BlobInfo } from "../components/RichTextEditor";

export const PRE_LOGIN_NOTICE_API_URL = `${Config.baseUrl}api/preloginnotice`;
export const POST_LOGIN_NOTICE_API_URL = `${Config.baseUrl}api/postloginnotice`;
export const PRE_LOGIN_NOTICE_IMAGE_API_URL = `${PRE_LOGIN_NOTICE_API_URL}/image/`;
export enum NotificationType {
  Save,
  Clear,
  Revert
}

export enum ScheduleTypeSelection {
  OFF = "OFF",
  ON = "ON",
  SCHEDULED = "SCHEDULED"
}
export interface PreLoginNotice {
  notice?: string;
  scheduleSettings: ScheduleTypeSelection;
  startDate: Date;
  endDate: Date;
}

export const strings = languageStrings.loginnoticepage;

export function submitPreLoginNotice(notice: PreLoginNotice): AxiosPromise {
  return axios.put(PRE_LOGIN_NOTICE_API_URL, notice);
}

export function getPreLoginNotice(): AxiosPromise<PreLoginNotice> {
  return axios.get(PRE_LOGIN_NOTICE_API_URL);
}

export function clearPreLoginNotice(): AxiosPromise {
  return axios.delete(PRE_LOGIN_NOTICE_API_URL);
}

export function submitPostLoginNotice(notice: string): AxiosPromise {
  return axios.put(POST_LOGIN_NOTICE_API_URL, notice);
}

export function getPostLoginNotice(): AxiosPromise {
  return axios.get(POST_LOGIN_NOTICE_API_URL);
}

export function clearPostLoginNotice(): AxiosPromise {
  return axios.delete(POST_LOGIN_NOTICE_API_URL);
}

export function unMarshallPreLoginNotice(
  marshalled: PreLoginNotice
): PreLoginNotice {
  return {
    notice: marshalled.notice,
    endDate: new Date(marshalled.endDate),
    startDate: new Date(marshalled.startDate),
    scheduleSettings: marshalled.scheduleSettings
  };
}

export function uploadPreLoginNoticeImage(file: BlobInfo): AxiosPromise {
  const imageBlob: Blob = file.blob();
  const name: string = encodeURIComponent(file.filename());
  return axios.put(PRE_LOGIN_NOTICE_IMAGE_API_URL + name, imageBlob, {
    headers: { "content-type": imageBlob.type }
  });
}
