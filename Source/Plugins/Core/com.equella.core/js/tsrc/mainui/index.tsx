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
import * as React from "react";
import * as ReactDOM from "react-dom";
import { initStrings } from "../util/langstrings";
import "../util/polyfill";

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
  layout: string;
}

export interface RenderData {
  baseResources: string;
  newUI: boolean;
  autotestMode: boolean;
  newSearch: boolean;
  selectionSessionInfo: SelectionSessionInfo | null;
}
export type EntryPage = "mainDiv" | "searchPage" | "settingsPage";

// Lazy import 'App' in order to initialise language strings (independent of imports)
// before loading of the full app.
const App = React.lazy(() => import("./App"));

export default function (entry: EntryPage) {
  initStrings();
  ReactDOM.render(
    <React.Suspense fallback={<>loading</>}>
      <App entryPage={entry} />
    </React.Suspense>,
    document.getElementById(entry)
  );
}
