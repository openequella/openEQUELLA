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
import * as SearchModule from "../../../tsrc/search/SearchModule";
import { getSearchResult } from "../../../__mocks__/getSearchResult";

jest.mock("@openequella/rest-api-client");
(OEQ.Search.search as jest.Mock<
  Promise<OEQ.Common.PagedResult<OEQ.Search.SearchResultItem>>
>).mockResolvedValue(getSearchResult);

describe("SearchModule", () => {
  it("should provide an list of items", async () => {
    const searchResult = await SearchModule.searchItems(
      SearchModule.defaultSearchOptions
    );
    expect(searchResult.available).toBe(4);
    expect(searchResult.results).toHaveLength(4);
  });
});
