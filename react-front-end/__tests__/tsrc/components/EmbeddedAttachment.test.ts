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

import { render } from "@testing-library/react";
import * as O from "fp-ts/Option";
import {
  buildEmbedCode,
  buildEmbeddedComponent,
} from "../../../tsrc/components/embedattachment/EmbeddedAttachmentHelper";
import { CustomMimeTypes } from "../../../tsrc/modules/MimeTypesModule";
import { buildViewUrl } from "../../../tsrc/modules/YouTubeModule";
import { languageStrings } from "../../../tsrc/util/langstrings";
import "@testing-library/jest-dom";
import * as React from "react";
import "../FpTsMatchers";
import { HtmlValidate } from "html-validate";

const {
  kalturaExternalIdIssue,
  kalturaMissingId,
  contentNotSupported,
  youTubeVideoMissingId,
} = languageStrings.shareAttachment.error;

const RESOURCE_URL = "https://example.com/resource";
const RESOURCE_TITLE = "resource";

const YOUTUBE_URL = buildViewUrl("fakeId");
const BROKEN_YOUTUBE_URL = "https://www.youtube.com/watch";

const KALTURA_ATTACHMENT =
  "http://localhost:8080/ian/items/91406c5e-e2fe-4528-beac-ab22266e0f50/1/?attachment.uuid=5673f889-6f72-432d-ad50-29b505a28739";
const KALTURA_URL = `${KALTURA_ATTACHMENT}&externalId=4211723/48373143-V7/1_d1h8f1dx`;
const BROKEN_KALTURA_URL = `${KALTURA_ATTACHMENT}&externalId=blah/blah/blah`;

const MIME_TYPE_IMAGE = "image/jpeg";
const MIME_TYPE_VIDEO = "video/mp4";
const MIME_TYPE_AUDIO = "audio/ogg";

describe("buildEmbeddedComponent", () => {
  const getComponentName = (c: React.JSX.Element): string | undefined =>
    c.type.name ?? c.type;

  it.each([
    ["video", MIME_TYPE_VIDEO, RESOURCE_URL],
    ["img", MIME_TYPE_IMAGE, RESOURCE_URL],
    ["audio", MIME_TYPE_AUDIO, RESOURCE_URL],
    ["YouTubeEmbed", CustomMimeTypes.YOUTUBE, YOUTUBE_URL],
    ["KalturaPlayerEmbed", CustomMimeTypes.KALTURA, KALTURA_URL],
  ])(
    `correctly builds a %s for %s when correctly specified`,
    (componentType: string, mimeType: string, src: string) => {
      const kalturaEmbed = buildEmbeddedComponent(mimeType, src);
      expect(getComponentName(kalturaEmbed)).toBe(componentType);
    },
  );

  it.each([
    [
      "no YouTube videoId",
      CustomMimeTypes.YOUTUBE,
      BROKEN_YOUTUBE_URL,
      youTubeVideoMissingId,
    ],
    [
      "no Kaltura externalId",
      CustomMimeTypes.KALTURA,
      KALTURA_ATTACHMENT,
      kalturaMissingId,
    ],
    [
      "corrupt Kaltura externalId",
      CustomMimeTypes.KALTURA,
      BROKEN_KALTURA_URL,
      kalturaExternalIdIssue,
    ],
    ["unknown MIME types", "blah/blah", "irrelevant", contentNotSupported],
  ])(
    "returns a EmbedAttachmentFailure for %s",
    (_: string, mimeType: string, src: string, expectedError: string) => {
      const component = buildEmbeddedComponent(mimeType, src);
      expect(getComponentName(component)).toBe("EmbedAttachmentFailure");

      const { getByText } = render(component);
      expect(getByText(expectedError)).toBeInTheDocument();
    },
  );
});

describe("buildEmbedCode", () => {
  const getCode = (code: O.Option<string>): string | undefined =>
    O.toUndefined(code);

  const htmlValidate = new HtmlValidate();

  it.each([
    [
      MIME_TYPE_IMAGE,
      RESOURCE_URL,
      `<img alt="resource" src="https://example.com/resource">`,
    ],
    [
      MIME_TYPE_VIDEO,
      RESOURCE_URL,
      `<video controls src="https://example.com/resource" aria-label="resource"></video>`,
    ],
    [
      MIME_TYPE_AUDIO,
      RESOURCE_URL,
      `<audio controls src="https://example.com/resource" aria-label="resource"></audio>`,
    ],
    [
      CustomMimeTypes.YOUTUBE,
      YOUTUBE_URL,
      `<iframe width="560" height="315" src="https://www.youtube-nocookie.com/embed/fakeId" title="YouTube video player" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture" allowfullscreen></iframe>`,
    ],
    [
      CustomMimeTypes.KALTURA,
      KALTURA_URL,
      `<iframe width="560" height="395" src="https://cdnapisec.kaltura.com/p/4211723/embedPlaykitJs/uiconf_id/48373143?iframeembed=true&entry_id=1_d1h8f1dx" allowfullscreen allow="autoplay *; fullscreen *; encrypted-media *" title="Kaltura video player"></iframe>`,
    ],
  ])(
    "builds embed code for supported MIME type %s",
    async (mimeType: string, url: string, expectation: string) => {
      const code = buildEmbedCode(mimeType, url, RESOURCE_TITLE);
      expect(code).toBeSome();

      const embedCode = getCode(code);
      expect(embedCode).toBe(expectation);

      const result = await htmlValidate.validateString(embedCode!);
      expect(result.valid).toBe(true);
    },
  );

  it.each([
    ["video/x-ms-wmv"],
    ["audio/x-ms-wma"],
    ["application/pdf"],
    ["application/zip"],
    ["text/plain"],
  ])(
    "returns O.none for MIME type %s which is not viewable in browser",
    (mimeType: string) => {
      const code = buildEmbedCode(mimeType, RESOURCE_URL, RESOURCE_TITLE);
      expect(code).toBeNone();
    },
  );

  it.each([
    [CustomMimeTypes.YOUTUBE, BROKEN_YOUTUBE_URL],
    [CustomMimeTypes.KALTURA, BROKEN_KALTURA_URL],
  ])(
    "returns O.none for custom MIME type %s when the URL is bad",
    (mimeType: string, url: string) => {
      const code = buildEmbedCode(mimeType, url, RESOURCE_TITLE);
      expect(code).toBeNone();
    },
  );
});
