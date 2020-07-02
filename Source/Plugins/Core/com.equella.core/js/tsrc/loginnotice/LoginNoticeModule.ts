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
import axios, { AxiosPromise } from "axios";
import { API_BASE_URL } from "../config";
import { languageStrings } from "../util/langstrings";
import { BlobInfo } from "../components/RichTextEditor";

export const PRE_LOGIN_NOTICE_API_URL = `${API_BASE_URL}/preloginnotice`;
export const POST_LOGIN_NOTICE_API_URL = `${API_BASE_URL}/postloginnotice`;
export const PRE_LOGIN_NOTICE_IMAGE_API_URL = `${PRE_LOGIN_NOTICE_API_URL}/image/`;

export enum ScheduleTypeSelection {
  OFF = "OFF",
  ON = "ON",
  SCHEDULED = "SCHEDULED",
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
    scheduleSettings: marshalled.scheduleSettings,
  };
}

export function uploadPreLoginNoticeImage(file: BlobInfo): AxiosPromise {
  const imageBlob: Blob = file.blob();
  const name: string = encodeURIComponent(file.filename());
  return axios.put(PRE_LOGIN_NOTICE_IMAGE_API_URL + name, imageBlob, {
    headers: { "content-type": imageBlob.type },
  });
}
