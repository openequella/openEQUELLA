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
import { CssBaseline, Theme, ThemeProvider } from "@mui/material";
import * as React from "react";
import * as ReactDOM from "react-dom/client";
import { theme as defaultTheme } from "./theme";

/**
 * Renders a page to be used in an Intergation test.
 *
 * @param pageId Unique identifier for the page.
 * @param content Content of the page
 * @param theme MUI theme to be applied to the page.
 */
export const renderIntegTesterPage = (
  pageId: "cloud" | "update" | "viewitem",
  content: React.JSX.Element,
  theme: Theme = defaultTheme,
) => {
  const rootElement = document.getElementById("app");
  if (rootElement) {
    ReactDOM.createRoot(rootElement).render(
      <>
        <CssBaseline />
        <ThemeProvider theme={theme}>{content}</ThemeProvider>
      </>,
    );
  } else {
    throw new Error(
      `Failed to render IntegTester page ${pageId}: root element is missing.`,
    );
  }
};
