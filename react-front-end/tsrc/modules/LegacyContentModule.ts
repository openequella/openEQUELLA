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
import { pipe } from "fp-ts/function";
import * as O from "fp-ts/Option";
import * as t from "io-ts";
import Axios from "axios";
import { API_BASE_URL } from "../AppConfig";
import type { ScrapbookType } from "./ScrapbookModule";

export const legacyContentSubmitBaseUrl = `${API_BASE_URL}/content/submit`;
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
 * Send a request to the legacy content submit API.
 *
 * @param relativeUrl - A relative URL which may include query parameters (e.g., tokens).
 * @param vals - StateData to be submitted.
 * @returns A Promise resolving to a SubmitResponse.
 */
export const submitRequest = (
  relativeUrl: string,
  vals: StateData,
): Promise<SubmitResponse> =>
  Axios.post<SubmitResponse>(
    legacyContentSubmitBaseUrl + encodeRelativeUrl(relativeUrl),
    vals,
  ).then((res) => res.data);

/**
 * Splits a relative URL into pathname and optional query string.
 * @param url - The relative URL string (e.g., "/path?param=value").
 * @returns A tuple: [pathname, queryString | undefined]
 */
const splitRelativeUrl = (url: string): [string, string | undefined] =>
  url.split("?", 2) as [string, string | undefined];

/**
 * Encodes all query parameter values except the "token" parameter.
 * @param query - A raw query string
 * @returns Encoded query string
 */
const encodeQueryValuesExceptToken = (query: string): O.Option<string> =>
  O.tryCatch(() => {
    const params = new URLSearchParams(query);
    const encodedQuery = new URLSearchParams();

    params.forEach((value, key) => {
      encodedQuery.append(
        key,
        key === "token" ? value : encodeURIComponent(value),
      );
    });

    return encodedQuery.toString();
  });

/**
 * Combines encoded pathname with encoded query string (if present and valid).
 * Falls back to just the encoded pathname if query is missing or malformed.
 * @param parts - A tuple containing [pathname, query].
 * @returns Encoded URL string.
 */
const encodePathWithOptionalQuery = ([pathname, query]: [
  string,
  string | undefined,
]): string => {
  const encodedPathname = encodeURI(pathname);
  return pipe(
    query,
    O.fromNullable,
    O.flatMap(encodeQueryValuesExceptToken),
    O.match(
      () => encodedPathname,
      (qs) => `${encodedPathname}?${qs}`,
    ),
  );
};

/**
 * Encodes a relative URL by URI-encoding the path and query string,
 * skipping encoding for the "token" parameter.
 * @param url - A relative URL.
 * @returns A safe, encoded URL string.
 */
const encodeRelativeUrl = (url: string): string =>
  pipe(url, splitRelativeUrl, encodePathWithOptionalQuery);
