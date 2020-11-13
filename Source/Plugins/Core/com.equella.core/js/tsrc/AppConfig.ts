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
}

export interface RenderData {
  baseResources: string;
  newUI: boolean;
  autotestMode: boolean;
  newSearch: boolean;
  selectionSessionInfo: SelectionSessionInfo | null;
}

declare const renderData: RenderData | undefined;
export const getRenderData = () => {
  if (typeof renderData !== "undefined") {
    return renderData;
  }
  return undefined;
};

export const AppConfig: Config = {
  baseUrl: document?.getElementsByTagName("base")[0]?.href ?? "",
};

export const API_BASE_URL = `${AppConfig.baseUrl}api`;
