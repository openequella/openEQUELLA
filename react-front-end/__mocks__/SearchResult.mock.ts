// eslint-disable-next-line @typescript-eslint/ban-ts-comment
// @ts-nocheck
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
import { range } from "lodash";
import { DateTime } from "luxon";
import { v4 as uuidv4 } from "uuid";

export const getEmptySearchResult: OEQ.Search.SearchResult<OEQ.Search.SearchResultItem> =
  {
    start: 0,
    length: 0,
    available: 0,
    results: [],
    highlight: [],
  };

export const itemWithAttachment: OEQ.Search.SearchResultItem = {
  uuid: "9b9bf5a9-c5af-490b-88fe-7e330679fad2",
  version: 1,
  name: "new title",
  status: "personal",
  createdDate: new Date("2014-06-11T10:28:58.190+10:00"),
  modifiedDate: new Date("2014-06-11T10:28:58.393+10:00"),
  collectionId: "6b356e2e-e6a0-235a-5730-15ad1d8ad630",
  commentCount: 0,
  attachmentCount: 1,
  attachments: [
    {
      attachmentType: "file",
      id: "29e0fe1b-dbd6-4c98-9e7a-d957d9c731f5",
      description: "B.txt",
      preview: false,
      mimeType: "text/plain",
      hasGeneratedThumb: true,
      links: {
        view: "http://localhost:8080/rest/items/9b9bf5a9-c5af-490b-88fe-7e330679fad2/1/?attachment.uuid=29e0fe1b-dbd6-4c98-9e7a-d957d9c731f5",
        thumbnail: "./thumb.jpg",
      },
    },
  ],
  thumbnail: "initial",
  displayFields: [],
  keywordFoundInAttachment: false,
  links: {
    view: "http://localhost:8080/rest/items/9b9bf5a9-c5af-490b-88fe-7e330679fad2/1/",
    self: "http://localhost:8080/rest/api/item/9b9bf5a9-c5af-490b-88fe-7e330679fad2/1/",
  },
  isLatestVersion: true,
};

export const normalItemWithoutName: OEQ.Search.SearchResultItem = {
  uuid: "266bb0ff-a730-4658-aec0-c68bbefc227c",
  version: 1,
  status: "live",
  createdDate: new Date("2014-06-11T09:31:08.557+10:00"),
  modifiedDate: new Date("2014-06-11T09:31:08.557+10:00"),
  collectionId: "9a1ddb24-6bf5-db3d-d8fe-4fca20ecf69c",
  commentCount: 0,
  attachments: [],
  thumbnail: "initial",
  displayFields: [],
  keywordFoundInAttachment: false,
  links: {
    view: "http://localhost:8080/rest/items/266bb0ff-a730-4658-aec0-c68bbefc227c/1/",
    self: "http://localhost:8080/rest/api/item/266bb0ff-a730-4658-aec0-c68bbefc227c/1/",
  },
  isLatestVersion: true,
};

export const itemNotInKeyResource: OEQ.Search.SearchResultItem = {
  uuid: "2534e329-e37e-4851-896e-51d8b39104c4",
  version: 1,
  status: "live",
  createdDate: new Date("2014-06-11T09:27:14.800+10:00"),
  modifiedDate: new Date("2014-06-11T09:27:14.803+10:00"),
  collectionId: "9a1ddb24-6bf5-db3d-d8fe-4fca20ecf69c",
  commentCount: 0,
  attachments: [],
  thumbnail: "initial",
  displayFields: [],
  keywordFoundInAttachment: false,
  links: {
    view: "http://localhost:8080/rest/items/2534e329-e37e-4851-896e-51d8b39104c4/1/",
    self: "http://localhost:8080/rest/api/item/2534e329-e37e-4851-896e-51d8b39104c4/1/",
  },
  isLatestVersion: true,
};

export const getSearchResult: OEQ.Search.SearchResult<OEQ.Search.SearchResultItem> =
  {
    start: 0,
    length: 10,
    available: 12,
    results: [
      itemWithAttachment,
      normalItemWithoutName,
      itemNotInKeyResource,
      {
        uuid: "925f5dd2-66eb-4b68-85be-93837af785d0",
        version: 1,
        name: "new title",
        status: "moderating",
        createdDate: new Date("2014-06-10T16:01:25.817+10:00"),
        modifiedDate: new Date("2014-06-10T16:01:25.967+10:00"),
        collectionId: "6b356e2e-e6a0-235a-5730-15ad1d8ad630",
        commentCount: 0,
        attachments: [
          {
            attachmentType: "file",
            id: "0a89415c-73b6-4e9b-8372-197b6ba4946c",
            description: "B.txt",
            preview: false,
            mimeType: "text/plain",
            hasGeneratedThumb: true,
            links: {
              view: "http://localhost:8080/rest/items/925f5dd2-66eb-4b68-85be-93837af785d0/1/?attachment.uuid=0a89415c-73b6-4e9b-8372-197b6ba4946c",
              thumbnail: "./thumb.jpg",
            },
          },
        ],
        thumbnail: "initial",
        displayFields: [],
        keywordFoundInAttachment: false,
        links: {
          view: "http://localhost:8080/rest/items/925f5dd2-66eb-4b68-85be-93837af785d0/1/",
          self: "http://localhost:8080/rest/api/item/925f5dd2-66eb-4b68-85be-93837af785d0/1/",
        },
        isLatestVersion: true,
        moderationDetails: {
          submittedDate: new Date("2022-08-30"),
          lastActionDate: Date.now(),
          rejectionMessage: "rejected",
        },
      },
      {
        uuid: "266bb0ff-a730-4658-aec0-c68bbefc2271",
        name: "a",
        version: 1,
        status: "live",
        createdDate: new Date("2014-06-11T09:31:08.557+10:00"),
        modifiedDate: new Date("2014-06-11T09:31:08.557+10:00"),
        collectionId: "9a1ddb24-6bf5-db3d-d8fe-4fca20ecf69c",
        commentCount: 0,
        attachments: [],
        thumbnail: "initial",
        displayFields: [],
        keywordFoundInAttachment: false,
        links: {
          view: "http://localhost:8080/rest/items/266bb0ff-a730-4658-aec0-c68bbefc2271/1/",
          self: "http://localhost:8080/rest/api/item/266bb0ff-a730-4658-aec0-c68bbefc2271/1/",
        },
        isLatestVersion: true,
      },
      {
        uuid: "266bb0ff-a730-4658-aec0-c68bbefc2272",
        name: "b",
        version: 1,
        status: "live",
        createdDate: new Date("2014-06-11T09:31:08.557+10:00"),
        modifiedDate: new Date("2014-06-11T09:31:08.557+10:00"),
        collectionId: "9a1ddb24-6bf5-db3d-d8fe-4fca20ecf69c",
        commentCount: 0,
        attachments: [],
        thumbnail: "initial",
        displayFields: [],
        keywordFoundInAttachment: false,
        links: {
          view: "http://localhost:8080/rest/items/266bb0ff-a730-4658-aec0-c68bbefc2272/1/",
          self: "http://localhost:8080/rest/api/item/266bb0ff-a730-4658-aec0-c68bbefc2272/1/",
        },
        isLatestVersion: true,
        bookmarkId: 123,
      },
      {
        uuid: "266bb0ff-a730-4658-aec0-c68bbefc2273",
        name: "c",
        version: 1,
        status: "live",
        createdDate: new Date("2014-06-11T09:31:08.557+10:00"),
        modifiedDate: new Date("2014-06-11T09:31:08.557+10:00"),
        collectionId: "9a1ddb24-6bf5-db3d-d8fe-4fca20ecf69c",
        commentCount: 0,
        attachments: [],
        thumbnail: "initial",
        displayFields: [],
        keywordFoundInAttachment: false,
        links: {
          view: "http://localhost:8080/rest/items/266bb0ff-a730-4658-aec0-c68bbefc2273/1/",
          self: "http://localhost:8080/rest/api/item/266bb0ff-a730-4658-aec0-c68bbefc2273/1/",
        },
      },
      {
        uuid: "266bb0ff-a730-4658-aec0-c68bbefc2274",
        name: "d",
        version: 1,
        status: "live",
        createdDate: new Date("2014-06-11T09:31:08.557+10:00"),
        modifiedDate: new Date("2014-06-11T09:31:08.557+10:00"),
        collectionId: "9a1ddb24-6bf5-db3d-d8fe-4fca20ecf69c",
        commentCount: 0,
        attachments: [],
        thumbnail: "initial",
        displayFields: [],
        keywordFoundInAttachment: false,
        links: {
          view: "http://localhost:8080/rest/items/266bb0ff-a730-4658-aec0-c68bbefc2274/1/",
          self: "http://localhost:8080/rest/api/item/266bb0ff-a730-4658-aec0-c68bbefc2274/1/",
        },
        isLatestVersion: true,
      },
      {
        uuid: "266bb0ff-a730-4658-aec0-c68bbefc2275",
        name: "e",
        version: 1,
        status: "live",
        createdDate: new Date("2014-06-11T09:31:08.557+10:00"),
        modifiedDate: new Date("2014-06-11T09:31:08.557+10:00"),
        collectionId: "9a1ddb24-6bf5-db3d-d8fe-4fca20ecf69c",
        commentCount: 0,
        attachments: [],
        thumbnail: "initial",
        displayFields: [],
        keywordFoundInAttachment: false,
        links: {
          view: "http://localhost:8080/rest/items/266bb0ff-a730-4658-aec0-c68bbefc2275/1/",
          self: "http://localhost:8080/rest/api/item/266bb0ff-a730-4658-aec0-c68bbefc2275/1/",
        },
        isLatestVersion: true,
      },
      {
        uuid: "266bb0ff-a730-4658-aec0-c68bbefc2276",
        name: "f",
        version: 1,
        status: "live",
        createdDate: new Date("2014-06-11T09:31:08.557+10:00"),
        modifiedDate: new Date("2014-06-11T09:31:08.557+10:00"),
        collectionId: "9a1ddb24-6bf5-db3d-d8fe-4fca20ecf69c",
        commentCount: 0,
        attachments: [],
        thumbnail: "initial",
        displayFields: [],
        keywordFoundInAttachment: false,
        links: {
          view: "http://localhost:8080/rest/items/266bb0ff-a730-4658-aec0-c68bbefc2276/1/",
          self: "http://localhost:8080/rest/api/item/266bb0ff-a730-4658-aec0-c68bbefc2276/1/",
        },
        isLatestVersion: true,
      },
      {
        uuid: "266bb0ff-a730-4658-aec0-c68bbefc2277",
        name: "g",
        version: 1,
        status: "live",
        createdDate: new Date("2014-06-11T09:31:08.557+10:00"),
        modifiedDate: new Date("2014-06-11T09:31:08.557+10:00"),
        collectionId: "9a1ddb24-6bf5-db3d-d8fe-4fca20ecf69c",
        commentCount: 0,
        attachments: [],
        thumbnail: "initial",
        displayFields: [],
        keywordFoundInAttachment: false,
        links: {
          view: "http://localhost:8080/rest/items/266bb0ff-a730-4658-aec0-c68bbefc2277/1/",
          self: "http://localhost:8080/rest/api/item/266bb0ff-a730-4658-aec0-c68bbefc2277/1/",
        },
        isLatestVersion: true,
      },
      {
        uuid: "266bb0ff-a730-4658-aec0-c68bbefc2278",
        name: "last modified item",
        version: 1,
        status: "live",
        createdDate: new Date("2020-07-10T09:31:08.557+10:00"),
        modifiedDate: new Date("2020-07-10T09:31:08.557+10:00"),
        collectionId: "9a1ddb24-6bf5-db3d-d8fe-4fca20ecf69c",
        commentCount: 0,
        attachments: [],
        thumbnail: "initial",
        displayFields: [],
        keywordFoundInAttachment: false,
        links: {
          view: "http://localhost:8080/rest/items/266bb0ff-a730-4658-aec0-c68bbefc2278/1/",
          self: "http://localhost:8080/rest/api/item/266bb0ff-a730-4658-aec0-c68bbefc2278/1/",
        },
        isLatestVersion: true,
      },
    ],
    highlight: [],
  };

export const getSearchResultsCustom = (
  numberOfResults: number,
): OEQ.Search.SearchResult<OEQ.Search.SearchResultItem> => ({
  start: 0,
  length: 10,
  available: numberOfResults,
  results: range(numberOfResults).map((i) => ({
    uuid: uuidv4(),
    name: `item ${i}`,
    version: 1,
    status: "live",
    createdDate: new Date("2020-07-10T09:31:08.557+10:00"),
    modifiedDate: new Date("2020-07-10T09:31:08.557+10:00"),
    collectionId: "9a1ddb24-6bf5-db3d-d8fe-4fca20ecf69c",
    commentCount: 0,
    attachments: [],
    thumbnail: "initial",
    displayFields: [],
    keywordFoundInAttachment: false,
    links: {
      view: "http://localhost:8080/rest/items/266bb0ff-a730-4658-aec0-c68bbefc227c/1/",
      self: "http://localhost:8080/rest/api/item/266bb0ff-a730-4658-aec0-c68bbefc227c/1/",
    },
    isLatestVersion: true,
  })),
  highlight: [],
});

export const getModerationItemsSearchResult =
  (): OEQ.Search.SearchResult<OEQ.Search.SearchResultItem> => {
    const oneHourAgo: Date = DateTime.now().minus({ hour: 1 }).toJSDate();
    const yesterday: Date = DateTime.now()
      .minus({ day: 1, hours: 2 })
      .toJSDate();

    return {
      start: 0,
      length: 3,
      available: 3,
      results: [
        {
          uuid: "724c8478-0203-4866-8fb4-463452fa348c",
          version: 3,
          name: "Testing workflows v2",
          status: "moderating",
          createdDate: oneHourAgo,
          modifiedDate: oneHourAgo,
          collectionId: "26679ec5-ac77-4124-8f5c-3a46674b1136",
          commentCount: 0,
          starRatings: -1.0,
          attachmentCount: 1,
          thumbnail: "default",
          displayFields: [],
          keywordFoundInAttachment: false,
          links: {
            view: "http://localhost:8080/ian/items/724c8478-0203-4866-8fb4-463452fa348c/3/",
            self: "http://localhost:8080/ian/api/item/724c8478-0203-4866-8fb4-463452fa348c/3/",
          },
          isLatestVersion: true,
          moderationDetails: {
            lastActionDate: oneHourAgo,
            submittedDate: oneHourAgo,
          },
        },
        {
          uuid: "724c8478-0203-4866-8fb4-463452fa348c",
          version: 2,
          name: "Testing workflows",
          status: "rejected",
          createdDate: yesterday,
          modifiedDate: yesterday,
          collectionId: "26679ec5-ac77-4124-8f5c-3a46674b1136",
          commentCount: 0,
          starRatings: -1.0,
          attachmentCount: 1,
          thumbnail: "default",
          displayFields: [],
          keywordFoundInAttachment: false,
          links: {
            view: "http://localhost:8080/ian/items/724c8478-0203-4866-8fb4-463452fa348c/2/",
            self: "http://localhost:8080/ian/api/item/724c8478-0203-4866-8fb4-463452fa348c/2/",
          },
          isLatestVersion: false,
          moderationDetails: {
            lastActionDate: oneHourAgo,
            submittedDate: yesterday,
            rejectionMessage: "I reject this item, just because I can.",
          },
        },
        {
          uuid: "1a65f60d-3d35-452b-881f-d01dc66155c3",
          version: 1,
          name: "An Error screenshot",
          description: "DB Issues when attempting to setupForTests",
          status: "review",
          createdDate: new Date("2022-09-02T12:26:45.428+10:00"),
          modifiedDate: new Date("2022-09-02T12:26:45.426+10:00"),
          collectionId: "241dfa27-5023-4b89-91ca-1f716cb2cf9b",
          commentCount: 0,
          starRatings: -1.0,
          attachmentCount: 1,
          thumbnail: "default",
          displayFields: [],
          keywordFoundInAttachment: false,
          links: {
            view: "http://localhost:8080/ian/items/1a65f60d-3d35-452b-881f-d01dc66155c3/1/",
            self: "http://localhost:8080/ian/api/item/1a65f60d-3d35-452b-881f-d01dc66155c3/1/",
          },
          isLatestVersion: true,
          moderationDetails: {
            lastActionDate: yesterday,
            submittedDate: new Date("2022-09-02T12:26:45.434+10:00"),
          },
        },
      ],
    };
  };

export const IMAGE_SCRAPBOOK = "Image Scrapbook";
export const ZIP_SCRAPBOOK = "ZIP Scrapbook";
export const WEBPAGE_SCRAPBOOK = "Webpage Scrapbook";

export const imageScrapbook: OEQ.Search.SearchResultItem = {
  uuid: "0a89415c-73b6-4e9b-8372-197b6ba49400",
  name: IMAGE_SCRAPBOOK,
  version: 1,
  status: "personal",
  createdDate: new Date("2020-07-10T09:31:08.557+10:00"),
  modifiedDate: new Date("2020-07-10T09:31:08.557+10:00"),
  collectionId: "9a1ddb24-6bf5-db3d-d8fe-4fca20ecf69c",
  commentCount: 0,
  thumbnail: "initial",
  thumbnailDetails: {
    attachmentType: "file",
  },
  attachmentCount: 1,
  attachments: [
    {
      attachmentType: "file",
      id: "78b8af7e-f0f5-4b5c-9f44-16f212583fe8",
      description: "image.png",
      preview: false,
      mimeType: "image/png",
      hasGeneratedThumb: true,
      brokenAttachment: false,
      links: {
        view: "http://localhost:8080/rest/items/72558c1d-8788-4515-86c8-b24a28cc451e/1/?attachment.uuid=78b8af7e-f0f5-4b5c-9f44-16f212583fe8",
        thumbnail: "./thumb.jpg",
      },
    },
  ],
  displayFields: [],
  displayOptions: {
    attachmentType: "STRUCTURED",
    disableThumbnail: false,
    standardOpen: true,
    integrationOpen: true,
  },
  keywordFoundInAttachment: false,
  links: {
    view: "http://localhost:8080/rest/items/0a89415c-73b6-4e9b-8372-197b6ba49400/1/",
    self: "http://localhost:8080/rest/api/item/0a89415c-73b6-4e9b-8372-197b6ba49400/1/",
  },
  isLatestVersion: true,
};

const zipScrapbook: OEQ.Search.SearchResultItem = {
  ...imageScrapbook,
  uuid: "2289415c-73b6-4e9b-8372-197b6ba49465",
  name: ZIP_SCRAPBOOK,
};

export const webpageScrapbook: OEQ.Search.SearchResultItem = {
  ...imageScrapbook,
  uuid: "0a89415c-73b6-4e9b-8372-197b6ba4946c",
  name: WEBPAGE_SCRAPBOOK,
  thumbnailDetails: {
    attachmentType: "html",
  },
};

export const getScrapbookItemSearchResult =
  (): OEQ.Search.SearchResult<OEQ.Search.SearchResultItem> => ({
    start: 0,
    length: 10,
    available: 3,
    results: [imageScrapbook, zipScrapbook, webpageScrapbook],
    highlight: [],
  });
