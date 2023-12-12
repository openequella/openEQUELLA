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
import * as E from "fp-ts/Either";
import { identity, pipe } from "fp-ts/function";
import * as J from "fp-ts/Json";
import { MD5 } from "object-hash";
import { buildStorageKey } from "../../../tsrc/modules/BrowserStorageModule";
import { saveSearchPageOptions } from "../../../tsrc/modules/ScrapbookModule";
import { defaultSearchPageOptions } from "../../../tsrc/search/SearchPageHelper";

describe("saveSearchPageOptions", () => {
  it("saves a string containing a MD5 hash and a JSON string in browser session storage", () => {
    const jsonString = pipe(
      defaultSearchPageOptions,
      J.stringify,
      E.match((error) => {
        throw new Error(`Failed to parse 'defaultSearchPageOptions': ${error}`);
      }, identity),
    );
    const hash = MD5(jsonString);

    const uuid = saveSearchPageOptions(defaultSearchPageOptions);
    const value = window.sessionStorage.getItem(buildStorageKey(uuid));
    expect(value).toBe(`${hash}${jsonString}`);
  });
});
