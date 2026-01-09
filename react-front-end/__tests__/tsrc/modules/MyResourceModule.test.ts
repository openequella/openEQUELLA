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
  getMyResourceCategoriesTransformedResp,
  mockGetMyResourceCategoriesResp,
} from "../../../__mocks__/myResourcesCategories_mock_data";
import {
  getMyResourceCategories,
  transformMyResourcesCategories,
} from "../../../tsrc/modules/MyResourceModule";
import { updateMockGetBaseUrl } from "../BaseUrlHelper";

jest.mock("@openequella/rest-api-client", () => {
  const restModule: typeof OEQ = jest.requireActual(
    "@openequella/rest-api-client",
  );
  return {
    ...restModule,
    MyResource: {
      getMyResourceCategories: jest.fn(),
    },
  };
});
(
  OEQ.MyResource.getMyResourceCategories as jest.Mock<
    Promise<OEQ.MyResource.MyResourcesCategory[]>
  >
).mockResolvedValue(mockGetMyResourceCategoriesResp);

// Mock getBaseUrl() function
updateMockGetBaseUrl();

describe("getMyResourceCategories", () => {
  it("should call the API and return a transformed list of search types", async () => {
    const result = await getMyResourceCategories(true);

    expect(OEQ.MyResource.getMyResourceCategories).toHaveBeenCalled();
    // 'all' resource type is filtered out
    expect(result).toHaveLength(mockGetMyResourceCategoriesResp.length - 1);
    expect(result).toEqual(getMyResourceCategoriesTransformedResp);
  });
});

describe("transformMyResourcesCategories", () => {
  it("filters out 'all' and keeps 'scrapbook' when scrapbook is enabled for the current user", () => {
    const result = transformMyResourcesCategories(true)(
      mockGetMyResourceCategoriesResp,
    );
    const ids = result.map((type) => type.id);

    expect(ids).not.toContain("all");
    expect(ids).toContain("scrapbook");
    expect(result).toHaveLength(mockGetMyResourceCategoriesResp.length - 1);
  });

  it("filters out both 'all' and 'scrapbook' when scrapbook is disabled for the current user", () => {
    const result = transformMyResourcesCategories(false)(
      mockGetMyResourceCategoriesResp,
    );
    const ids = result.map((type) => type.id);

    expect(ids).not.toContain("all");
    expect(ids).not.toContain("scrapbook");
    expect(result).toHaveLength(mockGetMyResourceCategoriesResp.length - 2);
  });
});
