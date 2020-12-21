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
  determineAttachmentViewUrl,
  determineViewer,
} from "../../../tsrc/modules/ViewerModule";

describe("determineViewer()", () => {
  const fileAttachmentType = "file";
  const fileViewUrl =
    "http://aninstitution.net/file/1c3f53d6-e618-47d2-8c3b-10f3b5d3005d/1/recording.mp3";
  const linkViewerId = "link";

  it("returns link viewer details for non-file attachment", () => {
    const testLink = "http://some.link/blah";
    expect(determineViewer("blah", testLink)).toEqual([linkViewerId, testLink]);
  });

  it("returns a 'link' viewer if full parameters aren't provided for 'file' attachments", () => {
    const [viewer] = determineViewer(fileAttachmentType, fileViewUrl);
    expect(viewer).toEqual(linkViewerId);
  });

  it("determines link viewer with modified link for 'save' MIME type viewer", () => {
    const [viewer, url] = determineViewer(
      fileAttachmentType,
      fileViewUrl,
      "not/used",
      "save"
    );
    expect(viewer).toEqual(linkViewerId);
    expect(url.endsWith("?.vi=save")).toEqual(true);
  });

  it.each<OEQ.MimeType.ViewerId>(["fancy", "file", "htmlFiveViewer"])(
    "determines lightbox for known supported MIME types and oEQ viewer %s",
    (mimeTypeViewerId) =>
      expect(
        determineViewer(
          fileAttachmentType,
          fileViewUrl,
          "audio/x-mp3", // rather than test every supported MIME type, just using one to keep things short
          mimeTypeViewerId
        )
      ).toEqual(["lightbox", fileViewUrl])
  );
});

describe("determineAttachmentViewUrl()", () => {
  const viewUrl = "http://blah/blah";
  const uuid = "uuid";
  const version = 1;

  it("returns the viewUrl for non 'file' attachmentType", () =>
    expect(
      determineAttachmentViewUrl(uuid, version, "link", viewUrl, undefined)
    ).toEqual(viewUrl));

  it("returns an oEQ 'file' URL for attachmentType 'file'", () =>
    expect(
      determineAttachmentViewUrl(
        uuid,
        version,
        "file",
        viewUrl,
        "directory/file.txt"
      )
    ).toEqual("file/uuid/1/directory/file.txt"));
});
