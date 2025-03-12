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
  buildKalturaPlayerUrl,
  KalturaEmbedStyle,
  KalturaPlayerDetails,
  KalturaPlayerVersion,
  parseExternalId,
} from "../../../tsrc/modules/KalturaModule";

describe("parseExternalId", () => {
  it("extracts the player details for a properly formed externalId", () => {
    const playerDetails: KalturaPlayerDetails = {
      partnerId: 123,
      uiconfId: 456,
      entryId: "1_asdf1234",
      version: "V7",
    };
    const result = parseExternalId(
      `${playerDetails.partnerId}/${playerDetails.uiconfId}-${playerDetails.version}/${playerDetails.entryId}`,
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

  const PLAYER_ID = "kaltura_player_id";
  const V7_PLAYER_DETAILS: KalturaPlayerDetails = {
    partnerId: 123,
    uiconfId: 456,
    entryId: "abc",
    version: "V7",
  };
  it.each<[KalturaPlayerVersion, KalturaPlayerDetails, string]>([
    [
      "V2",
      { ...V7_PLAYER_DETAILS, version: "V2" },
      `https://cdnapisec.kaltura.com/p/123/sp/12300/embedIframeJs/uiconf_id/456/partner_id/123?autoembed=true&entry_id=abc&playerId=${PLAYER_ID}`,
    ],
    [
      "V7",
      V7_PLAYER_DETAILS,
      `https://cdnapisec.kaltura.com/p/123/embedPlaykitJs/uiconf_id/456?autoembed=true&entry_id=abc&targetId=${PLAYER_ID}`,
    ],
  ])(
    "builds a Kaltura player embed URL for %s",
    (
      _: KalturaPlayerVersion,
      details: KalturaPlayerDetails,
      expected: string,
    ) => {
      const playerUrl = buildKalturaPlayerUrl(details, PLAYER_ID);
      expect(playerUrl).toBe(expected);
    },
  );

  it.each<[KalturaEmbedStyle, string]>([
    [
      "AUTO",
      `https://cdnapisec.kaltura.com/p/123/embedPlaykitJs/uiconf_id/456?autoembed=true&entry_id=abc&targetId=${PLAYER_ID}`,
    ],
    [
      "IFRAME",
      `https://cdnapisec.kaltura.com/p/123/embedPlaykitJs/uiconf_id/456?iframeembed=true&entry_id=abc`,
    ],
  ])(
    "builds a Kaltura player embed URL for %s embed style",
    (style: KalturaEmbedStyle, expected: string) => {
      const playerUrl = buildKalturaPlayerUrl(
        V7_PLAYER_DETAILS,
        PLAYER_ID,
        style,
      );
      expect(playerUrl).toBe(expected);
    },
  );
});
