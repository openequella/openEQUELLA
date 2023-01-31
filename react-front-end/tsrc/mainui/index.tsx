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
import * as ReactDOM from "react-dom/client";
import { initStrings } from "../util/langstrings";
import "../util/polyfill";
import "@fontsource/roboto/300.css";
import "@fontsource/roboto/400.css";
import "@fontsource/roboto/500.css";
import "@fontsource/material-icons";

export type EntryPage =
  | "advancedSearchPage"
  | "mainDiv"
  | "myResourcesPage"
  | "searchPage"
  | "settingsPage";

// Lazy import 'App' in order to initialise language strings (independent of imports)
// before loading of the full app.
const App = React.lazy(() => import("./App"));

export default function main(entry: EntryPage) {
  if (process.env.NODE_ENV === "production") {
    const nop = () => {};
    // eslint-disable-next-line no-native-reassign, no-global-assign
    console = {
      ...console,
      debug: nop,
      log: nop,
      time: nop,
      timeEnd: nop,
    };
  }

  initStrings();

  const rootElement = document.getElementById(entry);
  if (rootElement) {
    ReactDOM.createRoot(rootElement).render(
      <React.Suspense fallback={<>loading</>}>
        <App entryPage={entry} />
      </React.Suspense>
    );
  } else {
    throw new Error(
      `Failed to render the New UI: root element ${entry} is missing.`
    );
  }
}
