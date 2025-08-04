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
// MUST end in /
//export const INST_URL = 'http://localhost:8080/reports/';
import * as O from "fp-ts/Option";
import { pipe } from "fp-ts/function";
interface Config {
  baseUrl: string;
}

/**
 * Structure of Selection Session information which is used to help build
 * components(e.g. SearchResult) in the context of Selection Session.
 */
export interface SelectionSessionInfo {
  /**
   * The ID of a Selection Session
   */
  stateId: string;
  /**
   * The ID of an LMS Integration
   */
  integId?: string;
  /**
   * The UI layout used in Selection Session
   */
  layout: "coursesearch" | "search" | "skinnysearch";
  /**
   * True if the Select Summary button is disabled
   */
  isSelectSummaryButtonDisabled: boolean;
}

export interface RenderData {
  /**
   * Path to base resources for this institution.
   */
  baseResources: string;
  /**
   * True if in the new UI.
   */
  newUI: boolean;
  /**
   * True if server is running in autotestMode (VM argument -Dequella.autotest=true)
   */
  autotestMode: boolean;
  /**
   * True if using the new Search UI.
   */
  newSearch: boolean;
  /**
   * If in a Selection Session, this will be populated with info about the session. If not, this will be null.
   */
  selectionSessionInfo: SelectionSessionInfo | null;
  /**
   * True if this is an item viewed via integration (integ/gen item link)
   */
  viewedFromIntegration: boolean;
  /**
   * Google Analytics ID if it exists. If not, this will be null.
   */
  analyticsId: string | null;
  /**
   * `true` if the user is already authenticated before the initial rendering of New UI.
   * This typically happens when oEQ is open in a new tab where a session has been established.
   */
  hasAuthenticated: boolean;
}

declare const renderData: RenderData | undefined;

/**
 * Return the server-side prepared variable `renderData`.
 */
export const getRenderData = (): RenderData | undefined =>
  typeof renderData !== "undefined" ? renderData : undefined;

const AppConfig: Config = {
  baseUrl: document?.getElementsByTagName("base")[0]?.href ?? "",
};

/**
 * Return the base URL.
 */
export const getBaseUrl = () => AppConfig.baseUrl;

/**
 * Return the base route.
 */
export const getRouterBaseName = () => {
  const baseFullPath = new URL(getBaseUrl()).pathname;
  return baseFullPath.substr(0, baseFullPath.length - 1);
};

/**
 * Root URL of REST API calls.
 */
export const API_BASE_URL = `${AppConfig.baseUrl}api`;
/**
 * URL of 'legacy.css'
 */
export const LEGACY_CSS_URL = `${API_BASE_URL}/theme/legacy.css`;

/**
 * Returns a relative URL by stripping the base URL (if present) from the given absolute URL.
 *
 * @param url - Absolute url
 * @returns Relative URL starting with "/".
 */
export const getRelativeUrl = (url: string): string => {
  const baseUrl = getBaseUrl();
  return pipe(
    url,
    O.fromPredicate((u) => u.startsWith(baseUrl)),
    O.map((fullUrl) => fullUrl.slice(baseUrl.length)),
    O.getOrElse(() => url),
    (relativeUrl) =>
      relativeUrl.startsWith("/") ? relativeUrl : `/${relativeUrl}`,
  );
};
