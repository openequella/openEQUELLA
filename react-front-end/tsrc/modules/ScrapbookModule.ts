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
import { History } from "history";
import { API_BASE_URL } from "../AppConfig";
import {
  getLegacyScrapbookCreatingPageRoute,
  getLegacyScrapbookEditingPageRoute,
} from "./LegacyContentModule";

/**
 * Delete one Scrapbook Item by UUID.
 * @param uuid UUID of the Scrapbook
 */
export const deleteScrapbook = (uuid: string): Promise<void> =>
  OEQ.Scrapbook.deleteScrapbook(API_BASE_URL, uuid);

export type ScrapbookType = "file" | "page";
/**
 * Open Legacy Scrapbook creating pages by pushing the route of the page to the browser history.
 *
 * @param scrapbookType Type of Scrapbook which must be either 'file' or 'page'.
 * @param history History of browser in which the returning route will be pushed in.
 */
export const openLegacyFileCreatingPage = async (
  scrapbookType: ScrapbookType,
  history: History
): Promise<void> =>
  history.push(await getLegacyScrapbookCreatingPageRoute(scrapbookType));

/**
 * Open Legacy Scrapbook editing pages by pushing the route of the page to the browser history.
 *
 * @param key Unique key of the Scrapbook including the UUID and version.
 * @param history History of browser in which the returning route will be pushed in.
 */
export const openLegacyFileEditingPage = async (
  key: string,
  history: History
): Promise<void> => history.push(await getLegacyScrapbookEditingPageRoute(key));
