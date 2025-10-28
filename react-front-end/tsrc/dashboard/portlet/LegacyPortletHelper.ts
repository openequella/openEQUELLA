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
/**
 * Helper file dedicate to legacy portlet related functionalities.
 */
import * as A from "fp-ts/Array";
import { identity, pipe } from "fp-ts/function";
import * as M from "fp-ts/Map";
import * as O from "fp-ts/Option";
import * as S from "fp-ts/string";
import { OLD_DASHBOARD_PATH } from "../../mainui/routes";
import {
  LegacyContentResponse,
  resolveUrl,
  submitRequest,
  updateStylesheets,
} from "../../modules/LegacyContentModule";

/**
 * Submit a Legacy content API request to `home.do` to retrieve the legacy content of a portlet.
 * The endpoint is `psh.getPortletContent` where `psh` is the name of Section `ShowPortletsSection`
 * and `getPortletContent` is the event handler defined in this Section for this request.
 *
 * @param portletId UUID of a portlet which must be supplied to `getPortletContent` as the first
 * parameter.
 */
export const getPortletLegacyContent = async (
  portletId: string,
): Promise<LegacyContentResponse> =>
  await submitRequest<LegacyContentResponse>(OLD_DASHBOARD_PATH, {
    event__: ["psh.getPortletContent"],
    eventp__0: [portletId],
  });

/**
 * Registry to track whether legacy JS files have already been loaded.
 *
 * - Key: URL of the JS file.
 * - Value: Promise that resolves when the file has finished loading.
 *
 * Purpose:
 * - Prevents duplicate <script> tags from being inserted into the DOM.
 * - Ensures that multiple requests to load the same script will all wait for the same Promise,
 *   avoiding race conditions like "xxx is undefined".
 */
const scriptRegistry: Map<string, Promise<void>> = new Map();

/**
 * Loads a single JS file dynamically by creating a <script> element in <head>.
 *
 * If the script has already been requested before, the existing Promise from `scriptRegistry`
 * is returned, ensuring deduplication and proper ordering of script execution.
 *
 * @param url - The URL of the script to load.
 * @returns Promise<void> that resolves when the script is loaded or fails.
 */
const loadSingleScript = async (url: string): Promise<void> => {
  const load = (): Promise<void> =>
    new Promise((resolve, _) => {
      const script = document.createElement("script");
      script.src = url;
      script.async = false;
      script.onload = () => resolve();
      script.onerror = () => {
        console.error(new Error(`Failed to load script: ${url}`));
        resolve();
      };
      document.head.appendChild(script);
    });

  await pipe(
    scriptRegistry,
    M.lookup(S.Eq)(url),
    O.fold(async () => {
      const promise = load();
      scriptRegistry.set(url, promise);
      return promise;
    }, identity),
  );
};

/**
 * Loads multiple JS files by resolving their URLs and ensuring that each script is only loaded once.
 *
 * Note: the original implementation defined in `LegacyContentModule`  returns a Promise that is
 * resolved after only the last newly inserted script was loaded. While this works in `LegacyContent`
 * which always deals with only one Legacy content API request, it could cause race conditions in
 * the New Dashboard because multiple Legacy content API requests can occur concurrently.Therefore,
 * the new implementation instead waits for **all** scripts to be loaded, ensuring consistent script
 * execution order and availability of dependencies.
 *
 * @param scripts - Array of script URLs to load.
 * @returns Promise<void[]> resolving when all scripts are done.
 */
const loadMissingScripts = async (scripts: string[]): Promise<void[]> =>
  pipe(scripts, A.map(resolveUrl), A.map(loadSingleScript), (promises) =>
    Promise.all(promises),
  );

/**
 * Update extra JS and CSS files required to render a legacy portlet properly.
 *
 * Since the original CSS update implementation defined in `LegacyContentModule` can ensure all the
 * required CSS files are consistently loaded by waiting for all the CSS loading Promises, it can be
 * reused here. However, for JS files,  the new JS update implementation must be used to ensure proper
 * deduplication and ordering.
 */
export const updateExtraFiles = async (
  jsFiles: string[],
  cssFiles?: string[],
): Promise<void> => {
  await updateStylesheets(cssFiles);
  await loadMissingScripts(jsFiles);
};
