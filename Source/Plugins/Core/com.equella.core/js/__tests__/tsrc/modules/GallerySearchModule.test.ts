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
import * as E from "fp-ts/lib/Either";
import * as O from "fp-ts/Option";
import { basicImageSearchResponse } from "../../../__mocks__/GallerySearchModule.mock";
import {
  AttachmentFilter,
  buildGalleryEntry,
  buildGallerySearchResultItem,
  GalleryEntry,
  GallerySearchResultItem,
  imageGallerySearch,
} from "../../../tsrc/modules/GallerySearchModule";
import {
  defaultSearchOptions,
  searchItems,
} from "../../../tsrc/modules/SearchModule";

jest.mock("@openequella/rest-api-client");
jest.mock("../../../tsrc/modules/SearchModule");

const mockListMimeTypes = OEQ.MimeType.listMimeTypes as jest.Mock<
  Promise<OEQ.MimeType.MimeTypeEntry[]>
>;
const mockSearchItems = searchItems as jest.Mock<
  Promise<OEQ.Search.SearchResult<OEQ.Search.SearchResultItem>>
>;

/**
 * An expect matcher for Jest to confirm the response is a E.Left - typically to confirm an
 * error result.
 *
 * @param either The E.Either to validate
 */
const expectLeft = (either: E.Either<unknown, unknown>): void =>
  expect(E.isLeft(either)).toBeTruthy();

/**
 * An expect matcher for Jest to confirm the response is an E.Right - typically to confirm an
 * success result. The value is also returned, albeit (unfortunately) unioned with an undefined. It
 * is considered safe to access the value via a bang - .e.g `const value = expectRight(either)!;`
 *
 * @param either The E.Either to validate
 * @return The value contained in the E.Right
 */
const expectRight = <V>(either: E.Either<unknown, V>): V | undefined => {
  const value = E.isRight(either) ? either.right : undefined;
  expect(value).toBeTruthy();
  return value;
};

describe("buildGalleryEntry", () => {
  const itemUuid = "1eeb3df5-3809-4655-925b-24d994e42ff6";
  const itemVersion = 1;
  const attachment: OEQ.Search.Attachment = {
    attachmentType: "file",
    id: "7186d40d-6159-4d07-8eee-4f7ee0cfdc4e",
    description: "image.png",
    preview: false,
    mimeType: "image/png",
    hasGeneratedThumb: true,
    links: {
      view:
        "https://example.com/inst/items/1eeb3df5-3809-4655-925b-24d994e42ff6/1/?attachment.uuid=7186d40d-6159-4d07-8eee-4f7ee0cfdc4e",
      thumbnail:
        "https://example.com/inst/thumbs/1eeb3df5-3809-4655-925b-24d994e42ff6/1/7186d40d-6159-4d07-8eee-4f7ee0cfdc4e",
    },
    filePath: "image.png",
  };

  it("builds a gallery entry from a valid Attachment", () => {
    const galleryEntry = buildGalleryEntry(itemUuid, itemVersion, attachment);
    expectRight(galleryEntry);
  });

  it("fails if the Attachment has a missing MIME type", () => {
    const galleryEntry = buildGalleryEntry(itemUuid, itemVersion, {
      ...attachment,
      mimeType: undefined,
    });
    expectLeft(galleryEntry);
  });

  it("fails if the Attachment has a missing attachmentFilePath", () => {
    const galleryEntry = buildGalleryEntry(itemUuid, itemVersion, {
      ...attachment,
      filePath: undefined,
    });
    expectLeft(galleryEntry);
  });

  it.each([false, undefined])(
    "fails if the Attachment has a missing a thumbnail (value: %s)",
    (hasGeneratedThumb) => {
      const galleryEntry = buildGalleryEntry(itemUuid, itemVersion, {
        ...attachment,
        hasGeneratedThumb,
      });
      expectLeft(galleryEntry);
    }
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
    attachments: [
      {
        attachmentType: "file",
        id: "e7e84411-7cc6-4516-9bc8-d60dab47fccb",
        description: "ima_3166d0f.jpeg",
        preview: false,
        mimeType: "image/jpeg",
        hasGeneratedThumb: true,
        links: {
          view:
            "https://example.com/inst/items/535e4e9b-4836-4011-8857-eb29260bf155/1/?attachment.uuid=e7e84411-7cc6-4516-9bc8-d60dab47fccb",
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
        links: {
          view:
            "https://example.com/inst/items/535e4e9b-4836-4011-8857-eb29260bf155/1/?attachment.uuid=17f3036e-a3c6-4e6d-85cb-aaa78ca2835b",
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
        links: {
          view:
            "https://example.com/inst/items/535e4e9b-4836-4011-8857-eb29260bf155/1/?attachment.uuid=16a3d193-430c-45f2-bd0c-6a3fc0a3bcaf",
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
      view:
        "https://example.com/inst/items/535e4e9b-4836-4011-8857-eb29260bf155/1/",
      self:
        "https://example.com/inst/api/item/535e4e9b-4836-4011-8857-eb29260bf155/1/",
    },
    isLatestVersion: true,
  };

  const passThroughAttachmentFilter: AttachmentFilter = (
    a: OEQ.Search.Attachment[]
  ) => (A.isNonEmpty(a) ? O.some(a) : O.none);

  it("builds a GallerySearchResultItem from a valid SearchResultItem", () => {
    const result = buildGallerySearchResultItem(passThroughAttachmentFilter)(
      searchItem
    );
    const { mainEntry, additionalEntries } = expectRight(result)!;
    expect(mainEntry.directUrl).toBe(
      // Based on the fact there is no base URL set in Jest tests
      "file/535e4e9b-4836-4011-8857-eb29260bf155/1/content.zip/images/ima_3166d0f.jpeg"
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
    expectLeft(result);
  });

  // It would've been good to merge this with the above via an `it.each`, but unfortunately
  // type issues were encountered so they're done long form.
  it("fails if the attachments array is empty", () => {
    const result = buildGallerySearchResultItem(passThroughAttachmentFilter)({
      ...searchItem,
      attachments: [],
    });
    expectLeft(result);
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

describe("imageGallerySearch", () => {
  beforeEach(() => mockListMimeTypes.mockResolvedValue([]));

  it("returns a list of GallerySearchItems containing only images", async () => {
    mockSearchItems.mockResolvedValue(basicImageSearchResponse);

    const result = await imageGallerySearch(defaultSearchOptions);
    // The below is validating a variable, not the length of an array
    // eslint-disable-next-line jest/prefer-to-have-length
    expect(result.length).toEqual(basicImageSearchResponse.length);
    expect(result.results).toHaveLength(result.length);

    const mimeTypes = result.results
      .reduce(
        (acc: GalleryEntry[], cur: GallerySearchResultItem) => [
          ...acc,
          cur.mainEntry,
          ...cur.additionalEntries,
        ],
        [] as GalleryEntry[]
      )
      .map((entry: GalleryEntry) => entry.mimeType);
    expect(mimeTypes.length).toBeGreaterThan(1);
    expect(mimeTypes.filter((s) => s.startsWith("image"))).toHaveLength(
      mimeTypes.length
    );
  });

  it("logs any issues and skips corrupt data", async () => {
    const corruptResponse = { ...basicImageSearchResponse };
    corruptResponse.results[2].attachments = undefined;
    mockSearchItems.mockResolvedValue(corruptResponse);

    const consoleSpy = jest.spyOn(global.console, "error");

    const result = await imageGallerySearch(defaultSearchOptions);
    expect(consoleSpy).toHaveBeenCalledTimes(1);
    expect(result.results).toHaveLength(2);
    expect(result).toHaveLength(2);
  });
});
