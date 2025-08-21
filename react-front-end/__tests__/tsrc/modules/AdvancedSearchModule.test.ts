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
import { createMemoryHistory, Location } from "history";
import {
  getAdvancedSearchIdFromLocation,
  getAdvancedSearchIdFromUrl,
} from "../../../tsrc/modules/AdvancedSearchModule";

describe("getAdvancedSearchIdFromLocation", function () {
  const uuid = "c9fd1ae8-0dc1-ab6f-e923-1f195a22d537";
  const history = createMemoryHistory();

  it.each([
    [
      "new",
      { ...history.location, pathname: `/fiveo/page/advancedsearch/${uuid}` },
    ],
    ["old", { ...history.location, search: `in=P${uuid}` }],
  ])(
    "supports getting the ID for %s Advanced search URL",
    (_: string, location: Location) => {
      expect(getAdvancedSearchIdFromLocation(location)).toBe(uuid);
    },
  );
});

describe("getAdvancedSearchIdFromUrl", () => {
  const UUID = "f439c445-fc3e-40ac-b0cd-c733d9164835";

  it.each([
    [
      "return UUID from the new UI advanced search URL",
      `http://localhost:8080/demonstration/page/advancedsearch/${UUID}?searchOptions=test`,
      UUID,
    ],
    [
      "return UUID from the old UI advanced search URL",
      `http://localhost:8080/demonstration/searching.do?doc=%3Cxml%2F%3E&in=P${UUID}&q=&type=standard&sort=rank&dr=AFTER`,
      UUID,
    ],
    [
      "return undefined if the url is not advanced search URL",
      `http://localhost:8080/demonstration/page/notadvancedsearch/${UUID}?searchOptions=test`,
      undefined,
    ],
    [
      "return undefined if the url is not valid",
      `htt://localhost:8080/demonstration/page/notadvancedsearch/${UUID}`,
      undefined,
    ],
  ])("%s", async (_, url, expectedResult) => {
    const uuid = getAdvancedSearchIdFromUrl(url);

    expect(uuid).toBe(expectedResult);
  });
});
