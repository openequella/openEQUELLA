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
import "@fontsource/material-icons";
import "@fontsource/roboto/300.css";
import "@fontsource/roboto/400.css";
import "@fontsource/roboto/500.css";
import GlobalStyles from "@mui/material/GlobalStyles";
import * as React from "react";
import * as ReactDOM from "react-dom/client";
import { initStrings } from "../util/langstrings";
import "../util/polyfill";

/**
 * Global CSS styles required for the self-hosting Material Icons. More details can be found
 * from the {@link https://fontsource.org/docs/getting-started/migrate-v5} migration guide
 * of fontsource v5.
 */
const muiIconStyles = (
  <GlobalStyles
    styles={{
      ".material-icons": {
        fontFamily: "'Material Icons'",
        fontWeight: "normal",
        fontStyle: "normal",
        fontSize: 24,
        display: "inline-block",
        lineHeight: 1,
        textTransform: "none",
        letterSpacing: "normal",
        wordWrap: "normal",
        whiteSpace: "nowrap",
        direction: "ltr",
        WebkitFontSmoothing: "antialiased",
        textRendering: "optimizeLegibility",
        MozOsxFontSmoothing: "grayscale",
        fontFeatureSettings: "'liga'",
      },
    }}
  />
);

// Extends global interface `Window` with property `oEQRender` which provides the support for
// rendering New UI pages in the context of Legacy content.
declare global {
  interface Window {
    oEQRender: {
      searchPage: () => void;
      myResourcesPage: () => void;
      hierarchyBrowsePage: () => void;
      hierarchyPage: () => void;
    };
  }
}

window["oEQRender"] = {
  searchPage: () => main("searchPage"),
  myResourcesPage: () => main("myResourcesPage"),
  hierarchyBrowsePage: () => main("hierarchyBrowsePage"),
  hierarchyPage: () => main("hierarchyPage"),
};

export type EntryPage =
  | "advancedSearchPage"
  | "hierarchyBrowsePage"
  | "hierarchyPage"
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
    // eslint-disable-next-line no-global-assign
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
        {muiIconStyles}
        <App entryPage={entry} />
      </React.Suspense>,
    );
  } else {
    throw new Error(
      `Failed to render the New UI: root element ${entry} is missing.`,
    );
  }
}
