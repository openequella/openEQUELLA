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
import * as t from "io-ts";
import Axios from "axios";
import { API_BASE_URL } from "../AppConfig";
import type { ScrapbookType } from "./ScrapbookModule";

const legacyContentSubmitBaseUrl = `${API_BASE_URL}/content/submit`;
const legacyMyResourcesUrl = `${legacyContentSubmitBaseUrl}/access/myresources.do`;

export const ScrapbookLiteral = t.literal("scrapbook");
export const ModQueueLiteral = t.literal("modqueue");

export const LegacyBrowseHierarchyLiteral = t.literal("ALL");

export const LegacyMyResourcesCodec = t.union([
  t.literal("published"),
  t.literal("draft"),
  ScrapbookLiteral,
  ModQueueLiteral,
  t.literal("archived"),
  t.literal("all"),
  t.literal("defaultValue"),
]);

export type LegacyMyResourcesTypes = t.TypeOf<typeof LegacyMyResourcesCodec>;

export interface ExternalRedirect {
  href: string;
}

export interface ChangeRoute {
  route: string;
  userUpdated: boolean;
}

export interface StateData {
  [key: string]: string[];
}

export interface LegacyContentResponse {
  html: { [key: string]: string };
  state: StateData;
  css?: string[];
  js: string[];
  script: string;
  noForm: boolean;
  title: string;
  metaTags: string;
  fullscreenMode: string;
  menuMode: string;
  hideAppBar: boolean;
  preventUnload: boolean;
  userUpdated: boolean;
}

export type SubmitResponse =
  | ExternalRedirect
  | LegacyContentResponse
  | ChangeRoute;

export function isPageContent(
  response: SubmitResponse,
): response is LegacyContentResponse {
  return (response as LegacyContentResponse).html !== undefined;
}

export function isChangeRoute(
  response: SubmitResponse,
): response is ChangeRoute {
  return (response as ChangeRoute).route !== undefined;
}

export function isExternalRedirect(
  response: SubmitResponse,
): response is ExternalRedirect {
  return (response as ExternalRedirect).href !== undefined;
}

/**
 * Send a Legacy content request to trigger server side event 'contributeFromNewUI', which will
 * return a route for accessing the Legacy Scrapbook creating page. To support the requirement
 * of persisting SearchPageOptions in the communication between New UI and Legacy UI, the event
 * also requires the UUID representing a SearchPageOptions to be supplied.
 *
 * @param scrapbookType Type of the Scrapbook to be created.
 * @param searchPageOptionsID UUID generated on front-end to represent a SearchPageOptions.
 */
export const getLegacyScrapbookCreatingPageRoute = async (
  scrapbookType: ScrapbookType,
  searchPageOptionsID: string,
): Promise<string> =>
  Axios.post<ChangeRoute>(legacyMyResourcesUrl, {
    event__: [
      `${scrapbookType === "file" ? "cmca" : "cmca2"}.contributeFromNewUI`,
    ],
    eventp__0: [searchPageOptionsID],
  }).then(({ data: { route } }) => `/${route}`);

/**
 * Similar to {@link getLegacyScrapbookCreatingPageRoute}, return a route for accessing
 * the Legacy Scrapbook editing page.
 *
 * @param itemKey Unique key of the Scrapbook including the UUID and version.
 * @param searchOptionID UUID generated on front-end to represent a SearchPageOptions.
 */
export const getLegacyScrapbookEditingPageRoute = async (
  itemKey: string,
  searchOptionID: string,
): Promise<string> =>
  Axios.post<ChangeRoute>(legacyMyResourcesUrl, {
    event__: ["mcile.editFromNewUI"],
    eventp__0: [itemKey],
    eventp__1: [searchOptionID],
  }).then(({ data: { route } }) => `/${route}`);

/**
 * Encode a relative URL path and its query string, skipping re-encoding of the `token` param.
 *
 * Used to avoid double-encoding issues in the new UI where `token` is already encoded.
 *
 * @param path Relative URL with optional query string.
 * @returns Encoded path with safe query encoding.
 */
const encodeRelativeUrlSkipToken = (path: string): string => {
  const [pathname, query] = path.split("?", 2);
  const encodedPathname = encodeURI(pathname);

  if (!query) return encodedPathname;

  try {
    const params = new URLSearchParams(query);
    const encodedQuery = new URLSearchParams();

    params.forEach((value, key) => {
      encodedQuery.append(
        key,
        key === "token" ? value : encodeURIComponent(value),
      );
    });

    return `${encodedPathname}?${encodedQuery}`;
  } catch (e) {
    console.error(`Error in query string: "${query}"`, e);
    return encodedPathname;
  }
};

/**
 * Send a request to the legacy content submit API.
 *
 * @param path - A relative path that may include query parameters (e.g., tokens).
 * @param vals - StateData to be submitted.
 * @returns A Promise resolving to a SubmitResponse.
 */
export const submitRequest = (
  path: string,
  vals: StateData,
): Promise<SubmitResponse> =>
  Axios.post<SubmitResponse>(
    legacyContentSubmitBaseUrl + encodeRelativeUrlSkipToken(path),
    vals,
  ).then((res) => res.data);
