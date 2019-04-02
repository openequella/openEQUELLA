import axios, { AxiosPromise } from "axios";
import { Config } from "../config";
import { prepLangStrings } from "../util/langstrings";

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
  startDate?: Date;
  endDate?: Date;
}

export const emptyTinyMCEString =
  "<!DOCTYPE html>\n" +
  "<html>\n" +
  "<head>\n" +
  "</head>\n" +
  "<body>\n" +
  "\n" +
  "</body>\n" +
  "</html>";

export const strings = prepLangStrings("loginnoticepage", {
  title: "Login notice editor",
  clear: {
    title: "Warning",
    confirm: "Are you sure you want to clear this login notice?"
  },
  prelogin: {
    label: "Before login notice"
  },
  postlogin: {
    label: "After login notice",
    description:
      "Write a plaintext message to be displayed after login as an alert..."
  },
  notifications: {
    saved: "Login notice saved successfully.",
    cleared: "Login notice cleared successfully.",
    cancelled: "Cancelled changes to login notice."
  },
  errors: {
    permissions: "You do not have permission to edit these settings."
  },
  scheduling: {
    title: "Schedule settings",
    start: "Start date:",
    end: "End date:",
    scheduled: "Scheduled",
    alwayson: "On",
    disabled: "Off",
    endbeforestart: "End date must be after start date."
  }
});

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

export function uploadPreLoginNoticeImage(file: any): AxiosPromise {
  let imageBlob: Blob = file.blob();
  let name: string = encodeURIComponent(file.filename());
  return axios.put(PRE_LOGIN_NOTICE_IMAGE_API_URL + name, imageBlob, {
    headers: { "content-type": imageBlob.type }
  });
}
