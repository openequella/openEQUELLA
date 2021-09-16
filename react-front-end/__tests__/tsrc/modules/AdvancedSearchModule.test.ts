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
import {
  getAdvancedSearchDefinition,
  getAdvancedSearchesFromServerResult,
} from "../../../__mocks__/AdvancedSearchModule.mock";
import {
  getAdvancedSearchByUuid,
  getAdvancedSearchesFromServer,
  getAdvancedSearchIdFromLocation,
} from "../../../tsrc/modules/AdvancedSearchModule";

jest.mock("@openequella/rest-api-client");

(
  OEQ.AdvancedSearch.listAdvancedSearches as jest.Mock<
    Promise<OEQ.Common.BaseEntitySummary[]>
  >
).mockResolvedValue(getAdvancedSearchesFromServerResult);

const mockGetAdvancedSearchByUuid = (
  OEQ.AdvancedSearch.getAdvancedSearchByUuid as jest.Mock<
    Promise<OEQ.AdvancedSearch.AdvancedSearchDefinition>
  >
).mockResolvedValue(getAdvancedSearchDefinition);

describe("getAdvancedSearchesFromServer", () => {
  it("lists all Advanced searches", async () => {
    const advancedSearches = await getAdvancedSearchesFromServer();
    expect(advancedSearches).toHaveLength(3);
  });
});

describe("getAdvancedSearchByUuid", () => {
  it("retrieves Advanced search definition by UUID", async () => {
    const uuid = "e82207be-a9f2-442a-a17f-5c834d5b36cc";
    const definition = await getAdvancedSearchByUuid(uuid);

    expect(OEQ.AdvancedSearch.getAdvancedSearchByUuid).toHaveBeenCalledTimes(1);
    expect(mockGetAdvancedSearchByUuid.mock.calls[0][1]).toBe(uuid);
    expect(definition.controls).toHaveLength(2);
  });
});

describe("getAdvancedSearchIdFromLocation", function () {
  const uuid = "c9fd1ae8-0dc1-ab6f-e923-1f195a22d537";

  it.each([
    [
      "new",
      { ...window.location, pathname: `/fiveo/page/advancedsearch/${uuid}` },
    ],
    ["old", { ...window.location, search: `in=P${uuid}` }],
  ])(
    "supports getting the ID for %s Advanced search URL",
    (_: string, location: Location) => {
      expect(getAdvancedSearchIdFromLocation(location)).toBe(uuid);
    }
  );
});
