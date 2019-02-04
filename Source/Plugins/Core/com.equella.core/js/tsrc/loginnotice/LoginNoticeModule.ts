import axios, {AxiosPromise} from "axios";
import {Config} from "../config";

export const PRE_LOGIN_NOTICE_API_URL = `${Config.baseUrl}api/preloginnotice`;
export const POST_LOGIN_NOTICE_API_URL = `${Config.baseUrl}api/postloginnotice`;
export enum NotificationType{Save, Clear, Revert, None}

export function submitPreLoginNotice(notice:string):AxiosPromise{
  return axios.put(PRE_LOGIN_NOTICE_API_URL, notice);
}

export function getPreLoginNotice():AxiosPromise{
  return axios.get(PRE_LOGIN_NOTICE_API_URL);
}

export function clearPreLoginNotice():AxiosPromise{
  return axios.delete(PRE_LOGIN_NOTICE_API_URL);
}

export function submitPostLoginNotice(notice:string):AxiosPromise{
  return axios.put(POST_LOGIN_NOTICE_API_URL, notice);
}

export function getPostLoginNotice():AxiosPromise{
  return axios.get(POST_LOGIN_NOTICE_API_URL);
}

export function clearPostLoginNotice():AxiosPromise{
  return axios.delete(POST_LOGIN_NOTICE_API_URL);
}
