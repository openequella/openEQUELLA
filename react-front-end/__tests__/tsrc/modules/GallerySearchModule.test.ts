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
import * as A from "fp-ts/Array";
import * as O from "fp-ts/Option";
import {
  basicImageSearchResponse,
  basicVideoSearchResponse,
} from "../../../__mocks__/GallerySearchModule.mock";
import { ATYPE_LINK } from "../../../tsrc/modules/AttachmentsModule";
import {
  AttachmentFilter,
  buildGalleryEntry,
  buildGallerySearchResultItem,
  GalleryEntry,
  GallerySearchResultItem,
  imageGallerySearch,
  videoGallerySearch,
} from "../../../tsrc/modules/GallerySearchModule";
import { CustomMimeTypes } from "../../../tsrc/modules/MimeTypesModule";
import {
  defaultSearchOptions,
  searchItems,
} from "../../../tsrc/modules/SearchModule";
import { expectRight } from "../FpTsMatchers";

jest.mock("@openequella/rest-api-client");
jest.mock("../../../tsrc/modules/SearchModule");

const mockListMimeTypes = OEQ.MimeType.listMimeTypes as jest.Mock<
  Promise<OEQ.MimeType.MimeTypeEntry[]>
>;
const mockSearchItems = searchItems as jest.Mock<
  Promise<OEQ.Search.SearchResult<OEQ.Search.SearchResultItem>>
>;

describe("buildGalleryEntry", () => {
  const itemUuid = "1eeb3df5-3809-4655-925b-24d994e42ff6";
  const itemVersion = 1;
  // Although this example is specifically an image, the data structure is the same for local (i.e.
  // not YouTube etc) videos - just different `mimeType`s. And really, it's then the same for any
  // `file` `attachmentType`.
  const imageAttachment: OEQ.Search.Attachment = {
    attachmentType: "file",
    id: "7186d40d-6159-4d07-8eee-4f7ee0cfdc4e",
    description: "image.png",
    preview: false,
    mimeType: "image/png",
    hasGeneratedThumb: true,
    brokenAttachment: false,
    links: {
      view: "https://example.com/inst/items/1eeb3df5-3809-4655-925b-24d994e42ff6/1/?attachment.uuid=7186d40d-6159-4d07-8eee-4f7ee0cfdc4e",
      thumbnail:
        "https://example.com/inst/thumbs/1eeb3df5-3809-4655-925b-24d994e42ff6/1/7186d40d-6159-4d07-8eee-4f7ee0cfdc4e",
    },
    filePath: "image.png",
  };
  const youtubeAttachment: OEQ.Search.Attachment = {
    attachmentType: "custom/youtube",
    id: "b18ed9ab-1ddb-4961-8935-22bbf1095b24",
    description: "The Odyssey by Homer | Summary & Analysis",
    preview: false,
    brokenAttachment: false,
    links: {
      view: "https://example.com/inst/items/234b9bd6-b603-4e26-8214-b79b8aab0ed9/1/?attachment.uuid=b18ed9ab-1ddb-4961-8935-22bbf1095b24",
      thumbnail:
        "https://example.com/inst/thumbs/234b9bd6-b603-4e26-8214-b79b8aab0ed9/1/b18ed9ab-1ddb-4961-8935-22bbf1095b24",
      externalId: "z7i9AmmZE2o",
    },
  };
  const kalturaAttachment: OEQ.Search.Attachment = {
    attachmentType: "custom/kaltura",
    id: "0da6100e-332e-4116-bd56-059a114e2130",
    description: "TeamIT",
    brokenAttachment: false,
    preview: false,
    links: {
      view: "https://example.com/inst/items/a288b17b-4a6f-416f-9c29-aaad53cf7b23/1/?attachment.uuid=0da6100e-332e-4116-bd56-059a114e2130",
      thumbnail:
        "https://example.com/inst/thumbs/a288b17b-4a6f-416f-9c29-aaad53cf7b23/1/0da6100e-332e-4116-bd56-059a114e2130",
      externalId: "3466623/47988073/1_nd0xv0cp",
    },
  };

  it.each([
    ["Image", imageAttachment],
    ["YouTube", youtubeAttachment],
    ["Kaltura", kalturaAttachment],
  ])(
    "builds a gallery entry from a valid %s `Attachment`",
    (_: string, attachment: OEQ.Search.Attachment) => {
      const galleryEntry = buildGalleryEntry(itemUuid, itemVersion, attachment);
      expectRight(galleryEntry);
    },
  );

  it("fails if Attachment is an unsupported attachment type", () => {
    const galleryEntry = buildGalleryEntry(itemUuid, itemVersion, {
      ...imageAttachment,
      attachmentType: ATYPE_LINK,
    });
    expect(galleryEntry).toBeLeft();
  });

  it("fails if the Attachment has a missing MIME type", () => {
    const galleryEntry = buildGalleryEntry(itemUuid, itemVersion, {
      ...imageAttachment,
      mimeType: undefined,
    });
    expect(galleryEntry).toBeLeft();
  });

  it("fails if the Attachment has a missing attachmentFilePath", () => {
    const galleryEntry = buildGalleryEntry(itemUuid, itemVersion, {
      ...imageAttachment,
      filePath: undefined,
    });
    expect(galleryEntry).toBeLeft();
  });

  it.each([false, undefined])(
    "fails if the Attachment has a missing a thumbnail (value: %s)",
    (hasGeneratedThumb) => {
      const galleryEntry = buildGalleryEntry(itemUuid, itemVersion, {
        ...imageAttachment,
        hasGeneratedThumb,
      });
      expect(galleryEntry).toBeLeft();
    },
  );
});

describe("buildGallerySearchResultItem", () => {
  const searchItem: OEQ.Search.SearchResultItem = {
    uuid: "535e4e9b-4836-4011-8857-eb29260bf155",
    version: 1,
    name: "A mix of files",
    description: "A collection of images and videos",
    status: "live",
    createdDate: new Date("2020-11-10T04:15:36.411Z"),
    modifiedDate: new Date("2020-11-10T04:15:36.403Z"),
    collectionId: "312be657-ae6a-4c60-b6fa-ced02c955915",
    commentCount: 0,
    starRatings: -1,
    attachmentCount: 3,
    attachments: [
      {
        attachmentType: "file",
        id: "e7e84411-7cc6-4516-9bc8-d60dab47fccb",
        description: "ima_3166d0f.jpeg",
        preview: false,
        mimeType: "image/jpeg",
        hasGeneratedThumb: true,
        brokenAttachment: false,
        links: {
          view: "https://example.com/inst/items/535e4e9b-4836-4011-8857-eb29260bf155/1/?attachment.uuid=e7e84411-7cc6-4516-9bc8-d60dab47fccb",
          thumbnail:
            "https://example.com/inst/thumbs/535e4e9b-4836-4011-8857-eb29260bf155/1/e7e84411-7cc6-4516-9bc8-d60dab47fccb",
        },
        filePath: "content.zip/images/ima_3166d0f.jpeg",
      },
      {
        attachmentType: "file",
        id: "17f3036e-a3c6-4e6d-85cb-aaa78ca2835b",
        description: "ima_b9c9780.jpeg",
        preview: false,
        mimeType: "image/jpeg",
        hasGeneratedThumb: true,
        brokenAttachment: false,
        links: {
          view: "https://example.com/inst/items/535e4e9b-4836-4011-8857-eb29260bf155/1/?attachment.uuid=17f3036e-a3c6-4e6d-85cb-aaa78ca2835b",
          thumbnail:
            "https://example.com/inst/thumbs/535e4e9b-4836-4011-8857-eb29260bf155/1/17f3036e-a3c6-4e6d-85cb-aaa78ca2835b",
        },
        filePath: "content.zip/images/ima_b9c9780.jpeg",
      },
      {
        attachmentType: "file",
        id: "16a3d193-430c-45f2-bd0c-6a3fc0a3bcaf",
        description: "VID_20200526_180011.mp4",
        preview: false,
        mimeType: "video/mp4",
        hasGeneratedThumb: true,
        brokenAttachment: false,
        links: {
          view: "https://example.com/inst/items/535e4e9b-4836-4011-8857-eb29260bf155/1/?attachment.uuid=16a3d193-430c-45f2-bd0c-6a3fc0a3bcaf",
          thumbnail:
            "https://example.com/inst/thumbs/535e4e9b-4836-4011-8857-eb29260bf155/1/16a3d193-430c-45f2-bd0c-6a3fc0a3bcaf",
        },
        filePath: "content.zip/videos/VID_20200526_180011.mp4",
      },
    ],
    thumbnail: "default",
    displayFields: [],
    displayOptions: {
      attachmentType: "STRUCTURED",
      disableThumbnail: false,
      standardOpen: false,
      integrationOpen: false,
    },
    keywordFoundInAttachment: false,
    links: {
      view: "https://example.com/inst/items/535e4e9b-4836-4011-8857-eb29260bf155/1/",
      self: "https://example.com/inst/api/item/535e4e9b-4836-4011-8857-eb29260bf155/1/",
    },
    isLatestVersion: true,
  };

  const passThroughAttachmentFilter: AttachmentFilter = (
    a: OEQ.Search.Attachment[],
  ) => (A.isNonEmpty(a) ? O.some(a) : O.none);

  it("builds a GallerySearchResultItem from a valid SearchResultItem", () => {
    const result = buildGallerySearchResultItem(passThroughAttachmentFilter)(
      searchItem,
    );
    const { mainEntry, additionalEntries } = expectRight(result)!;
    expect(mainEntry.directUrl).toBe(
      // Based on the fact there is no base URL set in Jest tests
      "file/535e4e9b-4836-4011-8857-eb29260bf155/1/content.zip/images/ima_3166d0f.jpeg",
    );
    expect(additionalEntries).toHaveLength(2);
  });

  it("supports a minimum of one attachment on the item", () => {
    const singleAttachment = searchItem.attachments!.slice(0, 1);
    const result = buildGallerySearchResultItem(passThroughAttachmentFilter)({
      ...searchItem,
      attachments: singleAttachment,
    });
    expectRight(result);
  });

  it("fails if there are no attachments", () => {
    const result = buildGallerySearchResultItem(passThroughAttachmentFilter)({
      ...searchItem,
      attachments: undefined,
    });
    expect(result).toBeLeft();
  });

  // It would've been good to merge this with the above via an `it.each`, but unfortunately
  // type issues were encountered so they're done long form.
  it("fails if the attachments array is empty", () => {
    const result = buildGallerySearchResultItem(passThroughAttachmentFilter)({
      ...searchItem,
      attachments: [],
    });
    expect(result).toBeLeft();
  });

  it("will log and prune any problematic 'additional' attachments", () => {
    const consoleSpy = jest.spyOn(global.console, "warn");
    const [a1, a2, a3] = searchItem.attachments!;
    // The second attachment would be the first 'additional' item - so let's mess with it
    a2.mimeType = undefined;
    const result = buildGallerySearchResultItem(passThroughAttachmentFilter)({
      ...searchItem,
      attachments: [a1, a2, a3],
    });
    const { additionalEntries } = expectRight(result)!;
    expect(consoleSpy).toHaveBeenCalledTimes(1);
    expect(additionalEntries).toHaveLength(1);
    expect(additionalEntries[0].directUrl.endsWith(a3.filePath!)).toBeTruthy();
  });
});

const expectValidGalleryResult = (
  result: OEQ.Search.SearchResult<GallerySearchResultItem>,
  expectLength: number,
  mimeTypesValidator: (s: string) => boolean,
) => {
  // The below is validating a variable, not the length of an array
  // eslint-disable-next-line jest/prefer-to-have-length
  expect(result.length).toEqual(expectLength);
  expect(result.results).toHaveLength(result.length);

  const mimeTypes = result.results
    .reduce(
      (acc: GalleryEntry[], cur: GallerySearchResultItem) => [
        ...acc,
        cur.mainEntry,
        ...cur.additionalEntries,
      ],
      [] as GalleryEntry[],
    )
    .map((entry: GalleryEntry) => entry.mimeType);
  expect(mimeTypes.length).toBeGreaterThan(1);
  expect(mimeTypes.every(mimeTypesValidator)).toBeTruthy();
};

describe("imageGallerySearch", () => {
  beforeEach(() => mockListMimeTypes.mockResolvedValue([]));

  it("returns a list of GallerySearchItems containing only images", async () => {
    mockSearchItems.mockResolvedValue(basicImageSearchResponse);

    const result = await imageGallerySearch(
      defaultSearchOptions,
      mockSearchItems,
    );
    expectValidGalleryResult(result, basicImageSearchResponse.length, (s) =>
      s.startsWith("image"),
    );
  });

  it("logs any issues and skips corrupt data", async () => {
    const corruptResponse = { ...basicImageSearchResponse };
    corruptResponse.results[2].attachments = undefined;
    mockSearchItems.mockResolvedValue(corruptResponse);

    const consoleSpy = jest.spyOn(global.console, "error");

    const result = await imageGallerySearch(
      defaultSearchOptions,
      mockSearchItems,
    );
    expect(consoleSpy).toHaveBeenCalledTimes(1);
    expect(result.results).toHaveLength(2);
    expect(result).toHaveLength(2);
  });
});

describe("videoGallerySearch", () => {
  it("returns a list of GallerySearchItems containing only videos", async () => {
    mockSearchItems.mockResolvedValue(basicVideoSearchResponse);

    const result = await videoGallerySearch(
      defaultSearchOptions,
      mockSearchItems,
    );
    expectValidGalleryResult(
      result,
      basicVideoSearchResponse.length,
      (s) =>
        s.startsWith("video") ||
        [CustomMimeTypes.YOUTUBE, CustomMimeTypes.KALTURA].includes(s),
    );
  });
});
