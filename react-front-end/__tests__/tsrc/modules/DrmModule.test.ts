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
import { drmTerms } from "../../../__mocks__/Drm.mock";
import { acceptDrmTerms, listDrmTerms } from "../../../tsrc/modules/DrmModule";

const uuid = "9b9bf5a9-c5af-490b-88fe-7e330679fad2";
const version = 1;

jest.mock("@openequella/rest-api-client");

describe("listDrmTerms", () => {
  const mockListDrmTerms = OEQ.Drm.listDrmTerms as jest.Mock<
    Promise<OEQ.Drm.ItemDrmDetails>
  >;
  mockListDrmTerms.mockResolvedValue(drmTerms);

  it("supports listing an Item's DRM terms", async () => {
    await listDrmTerms(uuid, version);
    expect(mockListDrmTerms).toHaveBeenCalledTimes(1);
  });
});

describe("acceptDrmTerms", () => {
  const mockAcceptDrmTerms = OEQ.Drm.acceptDrmTerms as jest.Mock<Promise<void>>;

  it("supports accepting an Item's DRM terms", async () => {
    await acceptDrmTerms(uuid, version);
    expect(mockAcceptDrmTerms).toHaveBeenCalledTimes(1);
  });
});
