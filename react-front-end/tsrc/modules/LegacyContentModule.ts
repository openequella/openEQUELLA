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
import { pipe } from "fp-ts/function";
import * as O from "fp-ts/Option";
import * as t from "io-ts";
import { API_BASE_URL, LEGACY_CSS_URL } from "../AppConfig";
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

export interface FormUpdate {
  state: StateData;
  partial: boolean;
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
export const submitRequest = <T extends SubmitResponse = SubmitResponse>(
  relativeUrl: string,
  vals: StateData,
): Promise<T> =>
  Axios.post<T>(
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

export const resolveUrl = (url: string) =>
  new URL(url, $("base").attr("href")).href;

/**
 * Dynamically add script tags to the document head for legacy JS files, and return a promise
 * which resolves when the last new script is loaded.
 */
const loadMissingScripts = (_scripts: string[]) => {
  return new Promise((resolve) => {
    const scripts = _scripts.map(resolveUrl);
    const doc = window.document;
    const head = doc.getElementsByTagName("head")[0];
    const scriptTags = doc.getElementsByTagName("script");
    const scriptSrcs: { [index: string]: boolean } = {};
    for (let i = 0; i < scriptTags.length; i++) {
      const scriptTag = scriptTags[i];
      if (scriptTag.src) {
        scriptSrcs[scriptTag.src] = true;
      }
    }
    const lastScript = scripts.reduce(
      (lastScript: HTMLScriptElement | null, scriptUrl) => {
        if (scriptSrcs[scriptUrl]) {
          return lastScript;
        } else {
          const newScript = doc.createElement("script");
          newScript.src = scriptUrl;
          newScript.async = false;
          head.appendChild(newScript);
          return newScript;
        }
      },
      null,
    );
    if (!lastScript) resolve(undefined);
    else {
      lastScript.addEventListener("load", resolve, false);
      lastScript.addEventListener(
        "error",
        () => {
          console.error(`Failed to load script: ${lastScript.src}`);
          resolve(undefined);
        },
        false,
      );
    }
  });
};

/**
 * Update stylesheets by dynamically adding link tags to the document head for CSS files, and
 * return a promise which resolves when all the new CSS files are loaded.
 */
export const updateStylesheets = async (
  _sheets?: string[],
): Promise<{ [url: string]: HTMLLinkElement }> => {
  const sheets = _sheets
    ? _sheets.map(resolveUrl)
    : [resolveUrl(LEGACY_CSS_URL)];
  const doc = window.document;
  const insertPoint = doc.getElementById("_dynamicInsert");
  const head = doc.getElementsByTagName("head")[0];
  let current = insertPoint?.previousElementSibling ?? null;
  const existingSheets: { [index: string]: HTMLLinkElement } = {};

  while (
    current != null &&
    current.tagName === "LINK" &&
    current instanceof HTMLLinkElement
  ) {
    existingSheets[current.href] = current;
    current = current.previousElementSibling;
  }
  const cssPromises = sheets.reduce((lastLink, cssUrl) => {
    if (existingSheets[cssUrl]) {
      delete existingSheets[cssUrl];
      return lastLink;
    } else {
      const newCss = doc.createElement("link");
      newCss.rel = "stylesheet";
      newCss.href = cssUrl;
      head.insertBefore(newCss, insertPoint);
      const p = new Promise((resolve) => {
        newCss.addEventListener("load", resolve, false);
        newCss.addEventListener(
          "error",
          (_) => {
            console.error(`Failed to load css: ${newCss.href}`);
            resolve(undefined);
          },
          false,
        );
      });
      lastLink.push(p);
      return lastLink;
    }
  }, [] as Promise<unknown>[]);
  return Promise.all(cssPromises).then((_) => existingSheets);
};

/**
 * Update external resources (JS and CSS), typically called after a new LegacyContentResponse
 * is received.
 *
 * @param jsFiles A list of JS files to be loaded.
 * @param cssFiles A list of CSS files to be loaded.
 */
export const updateIncludes = async (
  jsFiles: string[],
  cssFiles?: string[],
): Promise<{ [url: string]: HTMLLinkElement }> => {
  const extraCss = await updateStylesheets(cssFiles);
  await loadMissingScripts(jsFiles);
  return extraCss;
};

/**
 * Collect a variety of values that should be sent to a Legacy event handler from a form
 * along with an optional command and arguments.
 *
 * @param form Typically a Legacy form where the id starts with "eqpageForm".
 * @param command Name of the Legacy event handler.
 * @param args Arguments to be passed to the Legacy event handler.
 */
export const collectParams = (
  form: HTMLFormElement,
  command: string | null,
  args: string[],
): StateData => {
  const vals: { [index: string]: string[] } = {};
  if (command) {
    vals["event__"] = [command];
  }
  args.forEach((c, i) => {
    let outval = c;
    switch (typeof c) {
      case "object":
        if (c != null) {
          outval = JSON.stringify(c);
        }
    }
    vals["eventp__" + i] = [outval];
  });
  form
    .querySelectorAll<HTMLInputElement>("input,textarea")
    .forEach((v: HTMLInputElement) => {
      if (v.type) {
        switch (v.type) {
          case "button":
            return;
          case "checkbox":
          case "radio":
            if (!v.checked || v.disabled) return;
        }
      }
      const ex = vals[v.name];
      if (ex) {
        ex.push(v.value);
      } else vals[v.name] = [v.value];
    });
  form.querySelectorAll("select").forEach((v: HTMLSelectElement) => {
    for (let i = 0; i < v.length; i++) {
      const o = v[i] as HTMLOptionElement;
      if (o.selected) {
        const ex = vals[v.name];
        if (ex) {
          ex.push(o.value);
        } else vals[v.name] = [o.value];
      }
    }
  });
  return vals;
};

/**
 * Remove a collection of elements from the DOM of Legacy content.
 */
export const deleteElements = (elements: { [url: string]: HTMLElement }) => {
  Object.values(elements).forEach((elem) => {
    elem.parentElement?.removeChild(elem);
  });
};
