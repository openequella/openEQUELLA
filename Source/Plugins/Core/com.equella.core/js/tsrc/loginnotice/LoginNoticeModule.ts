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
  title: "Login Notice Editor",
  currentnotice: "Current Notice: ",
  clear: {
    title: "Warning",
    confirm: "Are you sure you want to clear this login notice?"
  },
  prelogin: {
    label: "Before Login Notice",
    description:
      "Write a plaintext message to be displayed on the login screen..."
  },
  postlogin: {
    label: "After Login Notice",
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
  }
});

export function submitPreLoginNotice(notice: string): AxiosPromise {
  return axios.put(PRE_LOGIN_NOTICE_API_URL, notice, {
    headers: {
      "Content-Type": "text/html"
    }
  });
}

export function getPreLoginNotice(): AxiosPromise {
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
  return axios.put(
    PRE_LOGIN_NOTICE_IMAGE_API_URL + file.filename(),
    file.blob(),
    { headers: { "content-type": file.blob().type } }
  );
}
