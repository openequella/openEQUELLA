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
import * as ReactDOM from "react-dom";
import * as React from "react";
import "babel-polyfill";
import { CssBaseline } from "@material-ui/core";
import { ThemeProvider } from "@material-ui/core/styles";
import { theme, useStyles } from "./theme";

interface Props {
  attachment?: string;
  item?: string;
  version?: string;
}

declare const postValues: Props;

interface ViewItemProps {
  query: Props;
}

function ViewItem({ query: q }: ViewItemProps) {
  const classes = useStyles();

  return (
    <div id="testCloudProvider" className={classes.root}>
      {JSON.stringify(q)}
    </div>
  );
}

ReactDOM.render(
  <React.Fragment>
    <CssBaseline />
    <ThemeProvider theme={theme}>
      <ViewItem query={postValues} />
    </ThemeProvider>
  </React.Fragment>,
  document.getElementById("app")
);
