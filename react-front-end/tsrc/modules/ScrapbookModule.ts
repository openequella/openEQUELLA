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
import * as OEQ from "@openequella/rest-api-client";
import Axios from "axios";
import { History } from "history";
import { API_BASE_URL } from "../AppConfig";
import type { ChangeRoute } from "../legacycontent/LegacyContent";

/**
 * Delete one Scrapbook Item by UUID.
 * @param uuid UUID of the Scrapbook
 */
export const deleteScrapbook = (uuid: string): Promise<void> =>
  OEQ.Scrapbook.deleteScrapbook(API_BASE_URL, uuid);

const legacyPageUrl = `${API_BASE_URL}/content/submit/access/myresources.do`;

/**
 * Open the legacy Scrapbook creating pages.
 *
 * A LegacyContent request is firstly sent with different payload determined by
 * whether to create a file or a web page. The route to the Legacy creating page
 * will be provided in the response and be used to open the page.
 *
 * @param scrapbookType Type of Scrapbook which must be either 'file' or 'page'.
 * @param history History of browser in which the returning route will be pushed in.
 */
export const openLegacyFileCreatingPage = (
  scrapbookType: "file" | "page",
  history: History
): Promise<void> =>
  Axios.post<ChangeRoute>(legacyPageUrl, {
    event__: [`${scrapbookType === "file" ? "cmca" : "cmca2"}.contribute`],
  }).then(({ data: { route } }) => history.push(`/${route}`));
