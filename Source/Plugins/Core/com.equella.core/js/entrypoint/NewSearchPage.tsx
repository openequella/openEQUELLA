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
import { ThemeProvider } from "@material-ui/core";
import * as React from "react";
import * as ReactDOM from "react-dom";
import { BrowserRouter } from "react-router-dom";
import { basePath } from "../tsrc/mainui/App";
import SearchPage from "../tsrc/search/SearchPage";
import { oeqTheme } from "../tsrc/theme";
import { StylesProvider, createGenerateClassName } from "@material-ui/core";

const searchPageProps = { updateTemplate: () => {} };
const generateClassName = createGenerateClassName({
  productionPrefix: "new-search-page",
});

const renderSearchPage = () => {
  ReactDOM.render(
    <BrowserRouter basename={basePath}>
      <StylesProvider generateClassName={generateClassName}>
        <ThemeProvider theme={oeqTheme}>
          <SearchPage {...searchPageProps} />
        </ThemeProvider>
      </StylesProvider>
    </BrowserRouter>,
    document.getElementById("new-search-page")
  );
};

renderSearchPage();
