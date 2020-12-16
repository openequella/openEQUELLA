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

import { ReactNode } from "react";
import * as React from "react";
import { BrowserRouter } from "react-router-dom";
import {
  ThemeProvider,
  StylesProvider,
  createGenerateClassName,
} from "@material-ui/core";
import { oeqTheme } from "../theme";
import { startHeartbeat } from "../util/heartbeat";
import { Literal, match } from "runtypes";
import type { EntryPage } from "./index";

const SettingsPage = React.lazy(() => import("../settings/SettingsPage"));
const SearchPage = React.lazy(() => import("../search/SearchPage"));
const IndexPage = React.lazy(() => import("./IndexPage"));

const baseFullPath = new URL(document.head.getElementsByTagName("base")[0].href)
  .pathname;
export const basePath = baseFullPath.substr(0, baseFullPath.length - 1);

interface NewPageProps {
  children: ReactNode;
  classPrefix: string;
  forceRefresh?: boolean;
}

/**
 * Build a single oEQ new UI page.
 * @param page A tsx page such as SearchPage.tsx
 * @param forceRefresh Whether to refresh the page when navigating to different route
 * @param classPrefix The prefix added in MUI styles
 */
function NewPage({
  children,
  forceRefresh = false,
  classPrefix,
}: NewPageProps) {
  const generateClassName = createGenerateClassName({
    productionPrefix: classPrefix,
  });

  return (
    <StylesProvider generateClassName={generateClassName}>
      <ThemeProvider theme={oeqTheme}>
        <BrowserRouter basename={basePath} forceRefresh={forceRefresh}>
          {children}
        </BrowserRouter>
      </ThemeProvider>
    </StylesProvider>
  );
}

interface AppProps {
  entryPage: EntryPage;
}

const App = ({ entryPage }: AppProps) => {
  const nop = () => {};
  const renderApp = match(
    [
      Literal("mainDiv"),
      () => {
        startHeartbeat();
        return (
          <ThemeProvider theme={oeqTheme}>
            <IndexPage />
          </ThemeProvider>
        );
      },
    ],
    [
      Literal("searchPage"),
      () => (
        <NewPage classPrefix="oeq-nsp">
          <SearchPage updateTemplate={nop} />
        </NewPage>
      ),
    ],
    [
      Literal("settingsPage"),
      () => (
        // When SettingsPage is used in old UI, each route change should trigger a refresh
        // for the whole page because there are no React component matching routes.
        <NewPage classPrefix="oeq-nst" forceRefresh>
          <SettingsPage
            refreshUser={nop}
            updateTemplate={nop}
            isReloadNeeded={false}
          />
        </NewPage>
      ),
    ]
  );
  return renderApp(entryPage);
};

export default App;
