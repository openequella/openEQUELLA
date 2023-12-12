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
import { MD5 } from "object-hash";
import { v4 } from "uuid";
import { API_BASE_URL } from "../AppConfig";
import type {
  SearchPageNavigationConfig,
  SearchPageOptions,
} from "../search/SearchPageHelper";
import { navigateTo } from "../search/SearchPageHelper";
import { saveDataToStorage } from "./BrowserStorageModule";
import {
  getLegacyScrapbookCreatingPageRoute,
  getLegacyScrapbookEditingPageRoute,
} from "./LegacyContentModule";
import { buildSelectionSessionScrapbookLink } from "./LegacySelectionSessionModule";

/**
 * Delete one Scrapbook Item by UUID.
 * @param uuid UUID of the Scrapbook
 */
export const deleteScrapbook = (uuid: string): Promise<void> =>
  OEQ.Scrapbook.deleteScrapbook(API_BASE_URL, uuid);

export type ScrapbookType = "file" | "page";

/**
 * Stores the provided SearchPageOptions in Session Storage with an MD5 hash used for lightweight data validation when read back in.
 *
 * @param searchPageOptions
 */
export const saveSearchPageOptions = (searchPageOptions: SearchPageOptions) => {
  const uuid = v4();

  saveDataToStorage(
    uuid,
    searchPageOptions,
    (json: string) => `${MD5(json)}${json}`,
    window.sessionStorage,
  );

  return uuid;
};

const openScrapbookLegacyPage = (path: string, history: History) => {
  const navConfig: SearchPageNavigationConfig = {
    path,
    selectionSessionPathBuilder: () => buildSelectionSessionScrapbookLink(path),
  };

  navigateTo(navConfig, history);
};

/**
 * Open Legacy Scrapbook creating pages by pushing the route of the page to the browser history.
 *
 * Due to the communication between New UI and Legacy UI, after the creation is completed or cancelled,
 * the browser will return to the new UI, and we should apply the last SearchPageOptions that was used
 * before we went to the Legacy UI to the initial search. To achieve this, we save that SearchPageOptions
 * in the session storage so that we can retrieve it again.
 *
 * @param scrapbookType Type of Scrapbook which must be either 'file' or 'page'.
 * @param history History of browser in which the returning route will be pushed in.
 * @param searchPageOptions Current SearchPageOptions to be saved in the session storage.
 */
export const openLegacyFileCreatingPage = async (
  scrapbookType: ScrapbookType,
  history: History,
  searchPageOptions: SearchPageOptions,
): Promise<void> => {
  const uuid = saveSearchPageOptions(searchPageOptions);
  const path = await getLegacyScrapbookCreatingPageRoute(scrapbookType, uuid);

  openScrapbookLegacyPage(path, history);
};

/**
 * Similar to {@link openLegacyFileCreatingPage}, returns the route for Scrapbook editing pages.
 *
 * @param key Unique key of the Scrapbook including the UUID and version.
 * @param history History of browser in which the returning route will be pushed in.
 * @param searchPageOptions Current SearchPageOptions to be saved in the session storage.
 */
export const openLegacyFileEditingPage = async (
  key: string,
  history: History,
  searchPageOptions: SearchPageOptions,
): Promise<void> => {
  const uuid = saveSearchPageOptions(searchPageOptions);
  const path = await getLegacyScrapbookEditingPageRoute(key, uuid);

  openScrapbookLegacyPage(path, history);
};
