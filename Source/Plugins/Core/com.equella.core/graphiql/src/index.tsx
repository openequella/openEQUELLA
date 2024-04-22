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
import { createGraphiQLFetcher } from "@graphiql/toolkit";
import { GraphiQL } from "graphiql";
import * as React from "react";
import { JSX } from "react";
import { createRoot } from "react-dom/client";
import "graphiql/graphiql.css";

const graphiQLUI = (baseURL: string): JSX.Element => {
  const fetcher = createGraphiQLFetcher({
    url: baseURL,
  });
  return <GraphiQL fetcher={fetcher} />;
};

/**
 * The institution URL is the base URL for the GraphiQL UI. The GraphiQL UI is immutable
 * as it's specified in the Plugin JPF configuration.
 */
const getInstitutionURL = (): string =>
  window.location.href.replace(/\/graphql\/ui\/?$/, "");

const baseURL = getInstitutionURL() + "/graphql";

const root = createRoot(document.getElementById("graphiql_ui") as HTMLElement);
root.render(graphiQLUI(baseURL));
