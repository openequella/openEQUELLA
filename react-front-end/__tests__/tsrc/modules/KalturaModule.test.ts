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
import {
  KalturaExternalId,
  parseExternalId,
} from "../../../tsrc/modules/KalturaModule";

describe("parseExternalId", () => {
  it("extracts the player details for a properly formed externalId", () => {
    const playerDetails: KalturaExternalId = {
      partnerId: 123,
      uiconfId: 456,
      entryId: "1_asdf1234",
    };
    const result = parseExternalId(
      `${playerDetails.partnerId}/${playerDetails.uiconfId}/${playerDetails.entryId}`,
    );
    expect(result).toStrictEqual(playerDetails);
  });

  it.each([
    ["too few parts", "123/456"],
    ["too many parts", "123/456/789/012"],
    ["no parts", ""],
    ["non-numeric partnerId", "text/456/asdf"],
    ["non-numeric uiconfId", "123/text/asdf"],
  ])(
    "throws for malformed externalId inputs - %s",
    (_: string, testExternalId: string) => {
      expect(() => parseExternalId(testExternalId)).toThrow(TypeError);
    },
  );
});
