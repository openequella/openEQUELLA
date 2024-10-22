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
import { flow } from "fp-ts/function";
import * as O from "fp-ts/Option";
import { buildCustomEmbed } from "../../../tsrc/components/LightboxHelper";
import { CustomMimeTypes } from "../../../tsrc/modules/MimeTypesModule";
import { buildViewUrl } from "../../../tsrc/modules/YouTubeModule";
import "../FpTsMatchers";

describe("buildCustomEmbed", () => {
  const kalturaAttachmentUrl =
    "http://localhost:8080/ian/items/91406c5e-e2fe-4528-beac-ab22266e0f50/1/?attachment.uuid=5673f889-6f72-432d-ad50-29b505a28739";

  const getOptionalComponentName: (
    c: O.Option<JSX.Element>,
  ) => string | undefined = flow(
    O.map((component) => component.type.name),
    O.toUndefined,
  );

  it("returns O.none if unknown MIME type provided", () => {
    expect(buildCustomEmbed("blah/blah", "irrelevant")).toBeNone();
  });

  it("correctly builds a YouTubeEmbed when correctly specified", () => {
    const youTubeEmbed = buildCustomEmbed(
      CustomMimeTypes.YOUTUBE,
      buildViewUrl("fakeId"),
    );
    expect(youTubeEmbed).toBeSome();
    expect(getOptionalComponentName(youTubeEmbed)).toBe("YouTubeEmbed");
  });

  it("correctly builds a KalturaPlayerEmbed when correctly specified", () => {
    const kalturaEmbed = buildCustomEmbed(
      CustomMimeTypes.KALTURA,
      `${kalturaAttachmentUrl}&externalId=4211723/48373143-V7/1_d1h8f1dx`,
    );
    expect(kalturaEmbed).toBeSome();
    expect(getOptionalComponentName(kalturaEmbed)).toBe("KalturaPlayerEmbed");
  });

  it.each([
    [
      "no YouTube videoId",
      CustomMimeTypes.YOUTUBE,
      "https://www.youtube.com/watch",
    ],
    ["no Kaltura externalId", CustomMimeTypes.KALTURA, kalturaAttachmentUrl],
    [
      "corrupt Kaltura externalId",
      CustomMimeTypes.KALTURA,
      `${kalturaAttachmentUrl}&externalId=blah/blah/blah`,
    ],
  ])(
    "returns a LightboxMessage for %s",
    (_: string, mimeType: string, src: string) => {
      const component = buildCustomEmbed(mimeType, src);
      expect(component).toBeSome();
      expect(getOptionalComponentName(component)).toBe("LightboxMessage");
    },
  );
});
