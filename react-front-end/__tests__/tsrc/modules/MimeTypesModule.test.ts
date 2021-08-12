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
  getImageMimeTypes,
  getMimeTypeDefaultViewerDetails,
  isBrowserSupportedAudio,
  isBrowserSupportedVideo,
  splitMimeType,
} from "../../../tsrc/modules/MimeTypesModule";

jest.mock("@openequella/rest-api-client");
const mockedGetViewersForMimeType = OEQ.MimeType
  .getViewersForMimeType as jest.Mock<
  Promise<OEQ.MimeType.MimeTypeViewerConfiguration>
>;
const mockListMimeTypes = OEQ.MimeType.listMimeTypes as jest.Mock<
  Promise<OEQ.MimeType.MimeTypeEntry[]>
>;

describe("splitMimeTypes()", () => {
  it.each<[string, string, string]>([
    ["image/jpeg", "image", "jpeg"],
    [
      "application/vnd.openxmlformats-officedocument.presentationml.presentation",
      "application",
      "vnd.openxmlformats-officedocument.presentationml.presentation",
    ],
    ["image/svg+xml", "image", "svg+xml"],
    ["text/html; charset=UTF-8", "text", "html; charset=UTF-8"],
  ])('can split: "%s"', (mimeType, expectedType, expectedSubType) => {
    const [actualType, actualSubtype] = splitMimeType(mimeType);
    expect(actualType).toEqual(expectedType);
    expect(actualSubtype).toEqual(expectedSubType);
  });

  it.each<string>(["", "blah", "/blah", "la/blah/fah"])(
    'invalidates: "%s"',
    (invalidMimeType) => {
      expect(() => splitMimeType(invalidMimeType)).toThrow(TypeError);
    }
  );
});

describe("isBrowserSupportedAudio()", () => {
  it("returns false for unsupported MIME types", () =>
    expect(isBrowserSupportedAudio("audio/aac")).toEqual(false));

  it("returns true for supported MIME types", () =>
    expect(isBrowserSupportedAudio("audio/ogg")).toEqual(true));

  it("disregards MIME type attributes", () =>
    expect(isBrowserSupportedAudio("audio/ogg; attribute1=one")).toEqual(true));
});

describe("isBrowserSupportedVideo()", () => {
  it("returns false for unsupported MIME types", () =>
    expect(isBrowserSupportedVideo("video/quicktime")).toEqual(false));

  it("returns true for supported MIME types", () =>
    expect(isBrowserSupportedVideo("video/ogg")).toEqual(true));

  it("disregards MIME type attributes", () =>
    expect(isBrowserSupportedVideo("video/ogg; attribute1=one")).toEqual(true));
});

describe("getMimeTypeDefaultViewerDetails()", () => {
  const defaultViewerId = "file";
  const simpleFileViewerConfig: OEQ.MimeType.MimeTypeViewerConfiguration = {
    defaultViewer: defaultViewerId,
    viewers: [
      {
        viewerId: "save",
      },
      {
        viewerId: defaultViewerId,
        config: {
          attr: {},
          height: "",
          openInNewWindow: true,
          thickbox: false,
          width: "",
        },
      },
    ],
  };

  it("gets the correct default viewer details", async () => {
    mockedGetViewersForMimeType.mockResolvedValue(simpleFileViewerConfig);
    const viewerDetails = await getMimeTypeDefaultViewerDetails(
      "application/pdf"
    );
    expect(viewerDetails.viewerId).toEqual(defaultViewerId);
    expect(viewerDetails.config).toBeTruthy();
  });

  it("throws an error if there is a data issue", async () => {
    const htmlFiveViewerId = "htmlFiveViewer";
    mockedGetViewersForMimeType.mockResolvedValue({
      ...simpleFileViewerConfig,
      defaultViewer: htmlFiveViewerId,
    });
    await expect(getMimeTypeDefaultViewerDetails("image/jpeg")).rejects.toThrow(
      ReferenceError
    );
  });
});

describe("getImageMimeTypes()", () => {
  const imagePrefix = "image/";
  const mimeTypeEntries: OEQ.MimeType.MimeTypeEntry[] = [
    "audio/ogg",
    "image/jpeg",
    "image/png",
    "video/mp4",
  ].map((s) => ({ mimeType: s, desc: `Testing MIME type ${s}` }));
  const imageMimeTypeCount = 2; // Based on number of image entries in `mimeTypeEntries`

  it("returns the list of image MIME types _only_", async () => {
    mockListMimeTypes.mockResolvedValue(mimeTypeEntries);

    const result = await getImageMimeTypes();

    expect(result).toHaveLength(imageMimeTypeCount);
    // Make sure only 'image' MIME types are in the list
    expect(result.find((s) => !s.startsWith(imagePrefix))).toBeFalsy();
  });
});
