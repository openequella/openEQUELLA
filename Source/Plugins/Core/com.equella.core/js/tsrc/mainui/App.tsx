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
import { BrowserRouter } from "react-router-dom";
import { ThemeProvider } from "@material-ui/core";
import { oeqTheme } from "../theme";
import { startHeartbeat } from "../util/heartbeat";

const SettingsPage = React.lazy(() => import("../settings/SettingsPage"));
const IndexPage = React.lazy(() => import("./IndexPage"));

const baseFullPath = new URL(document.head.getElementsByTagName("base")[0].href)
  .pathname;
export const basePath = baseFullPath.substr(0, baseFullPath.length - 1);

interface AppProps {
  legacySettingsMode: boolean;
}

export const App = ({ legacySettingsMode }: AppProps) => {
  if (legacySettingsMode) {
    return (
      <BrowserRouter basename={basePath} forceRefresh>
        <ThemeProvider theme={oeqTheme}>
          <SettingsPage
            refreshUser={() => {}}
            updateTemplate={() => {}}
            isReloadNeeded={false}
          />
        </ThemeProvider>
      </BrowserRouter>
    );
  } else {
    startHeartbeat();
    return (
      <ThemeProvider theme={oeqTheme}>
        <IndexPage />
      </ThemeProvider>
    );
  }
};

export default App;
