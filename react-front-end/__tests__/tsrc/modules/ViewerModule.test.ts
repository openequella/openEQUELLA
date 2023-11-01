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
  ATYPE_KALTURA,
  ATYPE_YOUTUBE,
} from "../../../tsrc/modules/AttachmentsModule";
import {
  CustomMimeTypes,
  getMimeTypeDefaultViewerDetails,
} from "../../../tsrc/modules/MimeTypesModule";
import type { BasicSearchResultItem } from "../../../tsrc/modules/SearchModule";
import {
  buildAttachmentsAndViewerDefinitions,
  buildViewerConfigForAttachments,
  determineAttachmentViewUrl,
  determineViewer,
} from "../../../tsrc/modules/ViewerModule";
import "../FpTsMatchers";

const attachments = [
  {
    attachmentType: "file",
    id: "4fddbeb7-8d16-4417-be60-8709ce9d7b15",
    description: "Kelpie2.jpg",
    preview: false,
    mimeType: "image/jpeg",
    hasGeneratedThumb: true,
    brokenAttachment: false,
    links: {
      view: "http://localhost:8080/ian/items/40e879db-393b-4256-bfe2-9a78771d6937/1/?attachment.uuid=4fddbeb7-8d16-4417-be60-8709ce9d7b15",
      thumbnail:
        "http://localhost:8080/ian/thumbs/40e879db-393b-4256-bfe2-9a78771d6937/1/4fddbeb7-8d16-4417-be60-8709ce9d7b15",
    },
    filePath: "Kelpie2.jpg",
  },
  {
    attachmentType: "file",
    id: "df55f129-1bbb-427f-b8a0-46792559bea9",
    description: "Kelpie1.jpg",
    preview: false,
    mimeType: "image/png",
    hasGeneratedThumb: true,
    brokenAttachment: false,
    links: {
      view: "http://localhost:8080/ian/items/40e879db-393b-4256-bfe2-9a78771d6937/1/?attachment.uuid=df55f129-1bbb-427f-b8a0-46792559bea9",
      thumbnail:
        "http://localhost:8080/ian/thumbs/40e879db-393b-4256-bfe2-9a78771d6937/1/df55f129-1bbb-427f-b8a0-46792559bea9",
    },
    filePath: "Kelpie1.jpg",
  },
  {
    attachmentType: "file",
    id: "44528005-fb39-4461-bac7-12cd33ce4330",
    description: "Australian Kelpie Pet Profile | Bondi Vet",
    preview: false,
    mimeType: "image/gif",
    brokenAttachment: false,
    links: {
      view: "http://localhost:8080/ian/items/40e879db-393b-4256-bfe2-9a78771d6937/1/?attachment.uuid=44528005-fb39-4461-bac7-12cd33ce4330",
      thumbnail:
        "http://localhost:8080/ian/thumbs/40e879db-393b-4256-bfe2-9a78771d6937/1/44528005-fb39-4461-bac7-12cd33ce4330",
    },
  },
];

const testItem: BasicSearchResultItem = {
  uuid: "4fddbeb7-8d16-4417-be60-8709ce9d7b15",
  version: 1,
  status: "live",
};
const mockGetViewerDetails = jest.fn();

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
      "save",
    );
    expect(viewer).toEqual(linkViewerId);
    expect(url.endsWith("?.vi=save")).toBe(true);
  });

  it.each<OEQ.MimeType.ViewerId>(["fancy", "file", "htmlFiveViewer"])(
    "determines lightbox for known supported MIME types and oEQ viewer %s",
    (mimeTypeViewerId) =>
      expect(
        determineViewer(
          fileAttachmentType,
          fileViewUrl,
          "audio/x-mp3", // rather than test every supported MIME type, just using one to keep things short
          mimeTypeViewerId,
        ),
      ).toEqual(["lightbox", fileViewUrl]),
  );

  it.each([
    [ATYPE_KALTURA, CustomMimeTypes.KALTURA],
    [ATYPE_YOUTUBE, CustomMimeTypes.YOUTUBE],
  ])(
    "determines lightbox for known special attachment types [%s]",
    async (attachmentType: string, mimeType: string) => {
      const testUrl = "https://some.url/";
      expect(
        determineViewer(
          attachmentType,
          testUrl,
          mimeType,
          (await getMimeTypeDefaultViewerDetails(mimeType)).viewerId,
        ),
      ).toEqual(["lightbox", testUrl]);
    },
  );
});

describe("determineAttachmentViewUrl()", () => {
  const viewUrl = "http://blah/blah";
  const uuid = "uuid";
  const version = 1;

  it("returns the viewUrl for non 'file' attachmentType", () =>
    expect(
      determineAttachmentViewUrl(uuid, version, "link", viewUrl, undefined),
    ).toEqual(viewUrl));

  it("returns an oEQ 'file' URL for attachmentType 'file'", () =>
    expect(
      determineAttachmentViewUrl(
        uuid,
        version,
        "file",
        viewUrl,
        "directory/file.txt",
      ),
    ).toBe("file/uuid/1/directory/file.txt"));
});

describe("buildAttachmentsAndViewerDefinitions()", () => {
  it("returns a Left if any attachment fails to build viewer definition", async () => {
    mockGetViewerDetails.mockRejectedValue("Failure");
    const attachmentsAndViewerDefinitions =
      await buildAttachmentsAndViewerDefinitions(
        attachments,
        testItem.uuid,
        testItem.version,
        mockGetViewerDetails,
      );
    expect(attachmentsAndViewerDefinitions).toBeLeft();
  });

  it("returns a Right if viewer definitions are built for all attachments", async () => {
    mockGetViewerDetails.mockResolvedValue({ viewerId: "fancy" });
    const attachmentsAndViewerDefinitions =
      await buildAttachmentsAndViewerDefinitions(
        attachments,
        testItem.uuid,
        testItem.version,
        mockGetViewerDetails,
      );
    expect(attachmentsAndViewerDefinitions).toBeRight();
  });
});

describe("buildViewerConfigForAttachment", () => {
  it("returns a rejected promise when failed", async () => {
    mockGetViewerDetails.mockRejectedValue("Failure");
    await expect(
      buildViewerConfigForAttachments(
        testItem,
        attachments,
        mockGetViewerDetails,
      ),
    ).rejects.toBeTruthy();
  });

  it("returns a resolved promise when successful", async () => {
    mockGetViewerDetails.mockResolvedValue("Success");
    await expect(
      buildViewerConfigForAttachments(
        testItem,
        attachments,
        mockGetViewerDetails,
      ),
    ).resolves.toBeTruthy();
  });
});
