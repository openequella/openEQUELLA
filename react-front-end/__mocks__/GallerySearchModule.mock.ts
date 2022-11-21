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
import { GallerySearchResultItem } from "../tsrc/modules/GallerySearchModule";

export const basicImageSearchResponse: OEQ.Search.SearchResult<OEQ.Search.SearchResultItem> =
  {
    start: 0,
    length: 3,
    available: 24,
    results: [
      {
        uuid: "fe79c485-a6dd-4743-81e8-52de66494633",
        version: 1,
        name: "A history of cattle dogs",
        description: "An broad overview of the history of cattle dogs.",
        status: "live",
        createdDate: new Date("2017-08-15T08:52:38.954+10:00"),
        modifiedDate: new Date("2020-07-22T02:03:36.979+10:00"),
        collectionId: "b790e41a-577e-4d9a-92c4-6736c18b2ba6",
        commentCount: 0,
        starRatings: -1.0,
        attachmentCount: 1,
        attachments: [
          {
            attachmentType: "file",
            id: "dbd6dd98-d731-4a8f-907e-ceaf9608da3b",
            description: "Kelpie1.jpg",
            preview: false,
            mimeType: "image/jpeg",
            hasGeneratedThumb: true,
            brokenAttachment: false,
            links: {
              view: "http://localhost:8080/ian/items/fe79c485-a6dd-4743-81e8-52de66494633/1/?attachment.uuid=dbd6dd98-d731-4a8f-907e-ceaf9608da3b",
              thumbnail:
                "http://localhost:8080/ian/thumbs/fe79c485-a6dd-4743-81e8-52de66494633/1/dbd6dd98-d731-4a8f-907e-ceaf9608da3b",
            },
            filePath: "Kelpie1.jpg",
          },
        ],
        thumbnail: "default",
        displayFields: [
          { type: "node", name: "Resource Type", html: "Course material" },
        ],
        displayOptions: {
          attachmentType: "STRUCTURED",
          disableThumbnail: false,
          standardOpen: false,
          integrationOpen: true,
        },
        keywordFoundInAttachment: false,
        links: {
          view: "http://localhost:8080/ian/items/fe79c485-a6dd-4743-81e8-52de66494633/1/",
          self: "http://localhost:8080/ian/api/item/fe79c485-a6dd-4743-81e8-52de66494633/1/",
        },
        isLatestVersion: true,
      },
      {
        uuid: "40e879db-393b-4256-bfe2-9a78771d6937",
        version: 1,
        name: "Australian Kelpies",
        description: "The famous dogs of Australia",
        status: "live",
        createdDate: new Date("2017-08-22T10:35:45.460+10:00"),
        modifiedDate: new Date("2019-12-16T00:12:23.745+11:00"),
        collectionId: "b790e41a-577e-4d9a-92c4-6736c18b2ba6",
        commentCount: 0,
        starRatings: -1.0,
        attachmentCount: 3,
        attachments: [
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
            mimeType: "image/jpeg",
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
            attachmentType: "custom/youtube",
            id: "44528005-fb39-4461-bac7-12cd33ce4330",
            description: "Australian Kelpie Pet Profile | Bondi Vet",
            preview: false,
            brokenAttachment: false,
            links: {
              view: "http://localhost:8080/ian/items/40e879db-393b-4256-bfe2-9a78771d6937/1/?attachment.uuid=44528005-fb39-4461-bac7-12cd33ce4330",
              thumbnail:
                "http://localhost:8080/ian/thumbs/40e879db-393b-4256-bfe2-9a78771d6937/1/44528005-fb39-4461-bac7-12cd33ce4330",
            },
          },
        ],
        thumbnail: "default",
        displayFields: [
          {
            type: "node",
            name: "Resource Type",
            html: "Supplementary reading",
          },
        ],
        displayOptions: {
          attachmentType: "STRUCTURED",
          disableThumbnail: false,
          standardOpen: false,
          integrationOpen: true,
        },
        keywordFoundInAttachment: false,
        links: {
          view: "http://localhost:8080/ian/items/40e879db-393b-4256-bfe2-9a78771d6937/1/",
          self: "http://localhost:8080/ian/api/item/40e879db-393b-4256-bfe2-9a78771d6937/1/",
        },
        isLatestVersion: true,
      },
      {
        uuid: "8d25bfcc-f877-4cb6-84cd-391a79c7c67a",
        version: 1,
        name: "Authors of the 20th Century",
        description:
          "An overview of some of the most famous authors of the 20th Century.",
        status: "live",
        createdDate: new Date("2017-08-11T15:47:58.294+10:00"),
        modifiedDate: new Date("2019-03-27T02:35:41.012+11:00"),
        collectionId: "b790e41a-577e-4d9a-92c4-6736c18b2ba6",
        commentCount: 0,
        starRatings: -1.0,
        attachmentCount: 5,
        attachments: [
          {
            attachmentType: "file",
            id: "a09056f0-c867-40ea-80c0-a1433f487182",
            description: "Wilkie Collins.jpg",
            preview: false,
            mimeType: "image/jpeg",
            hasGeneratedThumb: true,
            brokenAttachment: false,
            links: {
              view: "http://localhost:8080/ian/items/8d25bfcc-f877-4cb6-84cd-391a79c7c67a/1/?attachment.uuid=a09056f0-c867-40ea-80c0-a1433f487182",
              thumbnail:
                "http://localhost:8080/ian/thumbs/8d25bfcc-f877-4cb6-84cd-391a79c7c67a/1/a09056f0-c867-40ea-80c0-a1433f487182",
            },
            filePath: "Wilkie Collins.jpg",
          },
          {
            attachmentType: "file",
            id: "6411799f-052a-4926-8c67-0c851447c762",
            description: "Joseph Conrad.pdf",
            preview: false,
            mimeType: "application/pdf",
            hasGeneratedThumb: true,
            brokenAttachment: false,
            links: {
              view: "http://localhost:8080/ian/items/8d25bfcc-f877-4cb6-84cd-391a79c7c67a/1/?attachment.uuid=6411799f-052a-4926-8c67-0c851447c762",
              thumbnail:
                "http://localhost:8080/ian/thumbs/8d25bfcc-f877-4cb6-84cd-391a79c7c67a/1/6411799f-052a-4926-8c67-0c851447c762",
            },
            filePath: "Joseph Conrad.pdf",
          },
          {
            attachmentType: "file",
            id: "e3f96e6b-a6aa-4c8e-975c-c2c3870daa34",
            description: "Dickens.jpg",
            preview: false,
            mimeType: "image/jpeg",
            hasGeneratedThumb: true,
            brokenAttachment: false,
            links: {
              view: "http://localhost:8080/ian/items/8d25bfcc-f877-4cb6-84cd-391a79c7c67a/1/?attachment.uuid=e3f96e6b-a6aa-4c8e-975c-c2c3870daa34",
              thumbnail:
                "http://localhost:8080/ian/thumbs/8d25bfcc-f877-4cb6-84cd-391a79c7c67a/1/e3f96e6b-a6aa-4c8e-975c-c2c3870daa34",
            },
            filePath: "Dickens.jpg",
          },
          {
            attachmentType: "file",
            id: "f226e79a-1d2c-4894-aaa1-032812351d29",
            description: "Conrad.jpg",
            preview: false,
            mimeType: "image/jpeg",
            hasGeneratedThumb: true,
            brokenAttachment: false,
            links: {
              view: "http://localhost:8080/ian/items/8d25bfcc-f877-4cb6-84cd-391a79c7c67a/1/?attachment.uuid=f226e79a-1d2c-4894-aaa1-032812351d29",
              thumbnail:
                "http://localhost:8080/ian/thumbs/8d25bfcc-f877-4cb6-84cd-391a79c7c67a/1/f226e79a-1d2c-4894-aaa1-032812351d29",
            },
            filePath: "Conrad.jpg",
          },
          {
            attachmentType: "file",
            id: "3afceca2-63d0-47ea-b921-f199b73194fc",
            description: "Eliot.jpg",
            preview: false,
            mimeType: "image/jpeg",
            hasGeneratedThumb: true,
            brokenAttachment: false,
            links: {
              view: "http://localhost:8080/ian/items/8d25bfcc-f877-4cb6-84cd-391a79c7c67a/1/?attachment.uuid=3afceca2-63d0-47ea-b921-f199b73194fc",
              thumbnail:
                "http://localhost:8080/ian/thumbs/8d25bfcc-f877-4cb6-84cd-391a79c7c67a/1/3afceca2-63d0-47ea-b921-f199b73194fc",
            },
            filePath: "Eliot(2).jpg",
          },
        ],
        thumbnail: "default",
        displayFields: [
          { type: "node", name: "Resource Type", html: "Course material" },
        ],
        displayOptions: {
          attachmentType: "STRUCTURED",
          disableThumbnail: false,
          standardOpen: false,
          integrationOpen: true,
        },
        keywordFoundInAttachment: false,
        links: {
          view: "http://localhost:8080/ian/items/8d25bfcc-f877-4cb6-84cd-391a79c7c67a/1/",
          self: "http://localhost:8080/ian/api/item/8d25bfcc-f877-4cb6-84cd-391a79c7c67a/1/",
        },
        isLatestVersion: true,
      },
    ],
    highlight: [],
  };

export const basicVideoSearchResponse: OEQ.Search.SearchResult<OEQ.Search.SearchResultItem> =
  {
    start: 0,
    length: 4,
    available: 15,
    results: [
      {
        uuid: "de8fcb0b-0b1c-4c34-9173-a83d1b0be6b5",
        version: 1,
        name: "How to grow African Violets",
        description:
          "YouTube resources to learn how to grow African Violets with 6 tips.",
        status: "live",
        createdDate: new Date("2021-03-29T16:21:41.801+11:00"),
        modifiedDate: new Date("2021-03-29T16:21:41.799+11:00"),
        collectionId: "b790e41a-577e-4d9a-92c4-6736c18b2ba6",
        commentCount: 0,
        starRatings: -1.0,
        attachmentCount: 1,
        attachments: [
          {
            attachmentType: "custom/youtube",
            id: "398dbef0-7d12-4b72-af3d-095dd70b019d",
            description: "6 Tips For Caring for African Violets",
            preview: false,
            brokenAttachment: false,
            links: {
              view: "http://localhost:8080/ian/items/de8fcb0b-0b1c-4c34-9173-a83d1b0be6b5/1/?attachment.uuid=398dbef0-7d12-4b72-af3d-095dd70b019d",
              thumbnail:
                "http://localhost:8080/ian/thumbs/de8fcb0b-0b1c-4c34-9173-a83d1b0be6b5/1/398dbef0-7d12-4b72-af3d-095dd70b019d",
              externalId: "9VCudo90K5I",
            },
          },
        ],
        thumbnail: "default",
        displayFields: [],
        displayOptions: {
          attachmentType: "STRUCTURED",
          disableThumbnail: false,
          standardOpen: false,
          integrationOpen: true,
        },
        keywordFoundInAttachment: false,
        links: {
          view: "http://localhost:8080/ian/items/de8fcb0b-0b1c-4c34-9173-a83d1b0be6b5/1/",
          self: "http://localhost:8080/ian/api/item/de8fcb0b-0b1c-4c34-9173-a83d1b0be6b5/1/",
        },
        isLatestVersion: true,
      },
      {
        uuid: "59139c45-788b-4200-a9cb-e4a39e76ad35",
        version: 1,
        name: "Quokka (JS) Example",
        description: "An example video of using Quokka JS in IntelliJ",
        status: "live",
        createdDate: new Date("2021-03-25T17:18:42.259+11:00"),
        modifiedDate: new Date("2021-03-25T17:18:42.258+11:00"),
        collectionId: "b790e41a-577e-4d9a-92c4-6736c18b2ba6",
        commentCount: 0,
        starRatings: -1.0,
        attachmentCount: 1,
        attachments: [
          {
            attachmentType: "file",
            id: "d81a7599-89a2-474e-b756-50bda202b349",
            description: "Quokka-2021-03-24_14.42.37.mp4",
            preview: false,
            mimeType: "video/mp4",
            hasGeneratedThumb: true,
            brokenAttachment: false,
            links: {
              view: "http://localhost:8080/ian/items/59139c45-788b-4200-a9cb-e4a39e76ad35/1/?attachment.uuid=d81a7599-89a2-474e-b756-50bda202b349",
              thumbnail:
                "http://localhost:8080/ian/thumbs/59139c45-788b-4200-a9cb-e4a39e76ad35/1/d81a7599-89a2-474e-b756-50bda202b349",
            },
            filePath: "Quokka-2021-03-24_14.42.37.mp4",
          },
        ],
        thumbnail: "default",
        displayFields: [],
        displayOptions: {
          attachmentType: "STRUCTURED",
          disableThumbnail: false,
          standardOpen: false,
          integrationOpen: true,
        },
        keywordFoundInAttachment: false,
        links: {
          view: "http://localhost:8080/ian/items/59139c45-788b-4200-a9cb-e4a39e76ad35/1/",
          self: "http://localhost:8080/ian/api/item/59139c45-788b-4200-a9cb-e4a39e76ad35/1/",
        },
        bookmarkId: 89567,
        isLatestVersion: true,
      },
      {
        uuid: "9d5112d4-87b6-4ac1-b773-ceaa4a6c5205",
        version: 1,
        name: "Daily Stoic Resources",
        description: "Resources provided by the Daily Stoic",
        status: "live",
        createdDate: new Date("2021-03-23T15:14:12.714+11:00"),
        modifiedDate: new Date("2021-03-23T15:14:12.713+11:00"),
        collectionId: "b790e41a-577e-4d9a-92c4-6736c18b2ba6",
        commentCount: 0,
        starRatings: -1.0,
        attachmentCount: 4,
        attachments: [
          {
            attachmentType: "file",
            id: "e82207be-a9f2-442a-a17f-5c834d5b36cc",
            description: "DailyStoicLogo.jpeg",
            preview: false,
            mimeType: "image/jpeg",
            hasGeneratedThumb: true,
            brokenAttachment: false,
            links: {
              view: "http://localhost:8080/ian/items/9d5112d4-87b6-4ac1-b773-ceaa4a6c5205/1/?attachment.uuid=e82207be-a9f2-442a-a17f-5c834d5b36cc",
              thumbnail:
                "http://localhost:8080/ian/thumbs/9d5112d4-87b6-4ac1-b773-ceaa4a6c5205/1/e82207be-a9f2-442a-a17f-5c834d5b36cc",
            },
            filePath: "DailyStoicLogo.jpeg",
          },
          {
            attachmentType: "custom/youtube",
            id: "33eb363d-77f4-4d40-84a3-d0ae1687b5f6",
            description:
              "These Simple Words Will Help You Through Life's Most Difficult Situations | Ryan Holiday",
            preview: false,
            brokenAttachment: false,
            links: {
              view: "http://localhost:8080/ian/items/9d5112d4-87b6-4ac1-b773-ceaa4a6c5205/1/?attachment.uuid=33eb363d-77f4-4d40-84a3-d0ae1687b5f6",
              thumbnail:
                "http://localhost:8080/ian/thumbs/9d5112d4-87b6-4ac1-b773-ceaa4a6c5205/1/33eb363d-77f4-4d40-84a3-d0ae1687b5f6",
              externalId: "qMNMyLm57VA",
            },
          },
          {
            attachmentType: "custom/youtube",
            id: "9fc093fa-f03a-45a5-98df-17381d63972f",
            description:
              "Stoicism and the Art of Resilience | Ryan Holiday | Epictetus",
            preview: false,
            brokenAttachment: false,
            links: {
              view: "http://localhost:8080/ian/items/9d5112d4-87b6-4ac1-b773-ceaa4a6c5205/1/?attachment.uuid=9fc093fa-f03a-45a5-98df-17381d63972f",
              thumbnail:
                "http://localhost:8080/ian/thumbs/9d5112d4-87b6-4ac1-b773-ceaa4a6c5205/1/9fc093fa-f03a-45a5-98df-17381d63972f",
              externalId: "6-UQYo1YabY",
            },
          },
          {
            attachmentType: "custom/youtube",
            id: "97bc82ed-fda4-40a9-91ea-e32da76e66a2",
            description:
              "Stoicism's Simple Secret To Being Happier | Ryan Holiday | Daily Stoic",
            preview: false,
            brokenAttachment: false,
            links: {
              view: "http://localhost:8080/ian/items/9d5112d4-87b6-4ac1-b773-ceaa4a6c5205/1/?attachment.uuid=97bc82ed-fda4-40a9-91ea-e32da76e66a2",
              thumbnail:
                "http://localhost:8080/ian/thumbs/9d5112d4-87b6-4ac1-b773-ceaa4a6c5205/1/97bc82ed-fda4-40a9-91ea-e32da76e66a2",
              externalId: "DtB1Uk_aJOw",
            },
          },
        ],
        thumbnail: "default",
        displayFields: [
          {
            type: "node",
            name: "Resource Type",
            html: "Supplementary reading",
          },
        ],
        displayOptions: {
          attachmentType: "STRUCTURED",
          disableThumbnail: false,
          standardOpen: false,
          integrationOpen: true,
        },
        keywordFoundInAttachment: false,
        links: {
          view: "http://localhost:8080/ian/items/9d5112d4-87b6-4ac1-b773-ceaa4a6c5205/1/",
          self: "http://localhost:8080/ian/api/item/9d5112d4-87b6-4ac1-b773-ceaa4a6c5205/1/",
        },
        bookmarkId: 89568,
        isLatestVersion: true,
      },
      {
        uuid: "91406c5e-e2fe-4528-beac-ab22266e0f50",
        version: 1,
        name: "[kaltura] snow drifting",
        description: "from pexels",
        status: "live",
        createdDate: new Date("2021-07-20T16:53:24.434+10:00"),
        modifiedDate: new Date("2021-07-20T16:53:24.430+10:00"),
        collectionId: "312be657-ae6a-4c60-b6fa-ced02c955915",
        starRatings: -1,
        attachmentCount: 1,
        attachments: [
          {
            attachmentType: "custom/kaltura",
            id: "5673f889-6f72-432d-ad50-29b505a28739",
            description: "From pexels: pexels-nadezhda-moryak-6530229.mp4",
            brokenAttachment: false,
            preview: false,
            links: {
              view: "http://localhost:8080/ian/items/91406c5e-e2fe-4528-beac-ab22266e0f50/1/?attachment.uuid=5673f889-6f72-432d-ad50-29b505a28739",
              thumbnail:
                "http://localhost:8080/ian/thumbs/91406c5e-e2fe-4528-beac-ab22266e0f50/1/5673f889-6f72-432d-ad50-29b505a28739",
              externalId: "4211234/48123443/1_d1h8f1dx",
            },
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
          view: "http://localhost:8080/ian/items/91406c5e-e2fe-4528-beac-ab22266e0f50/1/",
          self: "http://localhost:8080/ian/api/item/91406c5e-e2fe-4528-beac-ab22266e0f50/1/",
        },
        isLatestVersion: true,
      },
    ],
    highlight: [],
  };

export const transformedBasicImageSearchResponse: OEQ.Search.SearchResult<GallerySearchResultItem> =
  {
    start: 0,
    length: 3,
    available: 24,
    results: [
      {
        uuid: "fe79c485-a6dd-4743-81e8-52de66494633",
        version: 1,
        status: "live",
        name: "A history of cattle dogs",
        links: {
          view: "http://localhost:8080/ian/items/fe79c485-a6dd-4743-81e8-52de66494633/1/",
          self: "http://localhost:8080/ian/api/item/fe79c485-a6dd-4743-81e8-52de66494633/1/",
        },
        mainEntry: {
          id: "40e879db-393b-4256-bfe2-9a78771d6937",
          mimeType: "image/jpeg",
          name: "Kelpie1.jpg",
          thumbnailSmall:
            "http://localhost:8080/ian/thumbs/fe79c485-a6dd-4743-81e8-52de66494633/1/dbd6dd98-d731-4a8f-907e-ceaf9608da3b?gallery=thumbnail",
          thumbnailLarge:
            "http://localhost:8080/ian/thumbs/fe79c485-a6dd-4743-81e8-52de66494633/1/dbd6dd98-d731-4a8f-907e-ceaf9608da3b?gallery=preview",
          directUrl: "file/fe79c485-a6dd-4743-81e8-52de66494633/1/Kelpie1.jpg",
        },
        additionalEntries: [],
      },
      {
        uuid: "40e879db-393b-4256-bfe2-9a78771d6937",
        version: 1,
        status: "live",
        name: "Australian Kelpies",
        links: {
          view: "http://localhost:8080/ian/items/40e879db-393b-4256-bfe2-9a78771d6937/1/",
          self: "http://localhost:8080/ian/api/item/40e879db-393b-4256-bfe2-9a78771d6937/1/",
        },
        mainEntry: {
          id: "40e879db-393b-4256-bfe2-9a78771d6932",
          mimeType: "image/jpeg",
          name: "Kelpie2.jpg",
          thumbnailSmall:
            "http://localhost:8080/ian/thumbs/40e879db-393b-4256-bfe2-9a78771d6937/1/4fddbeb7-8d16-4417-be60-8709ce9d7b15?gallery=thumbnail",
          thumbnailLarge:
            "http://localhost:8080/ian/thumbs/40e879db-393b-4256-bfe2-9a78771d6937/1/4fddbeb7-8d16-4417-be60-8709ce9d7b15?gallery=preview",
          directUrl: "file/40e879db-393b-4256-bfe2-9a78771d6937/1/Kelpie2.jpg",
        },
        additionalEntries: [
          {
            id: "40e879db-393b-4256-bfe2-9a78771d6933",
            mimeType: "image/jpeg",
            name: "Kelpie1.jpg",
            thumbnailSmall:
              "http://localhost:8080/ian/thumbs/40e879db-393b-4256-bfe2-9a78771d6937/1/df55f129-1bbb-427f-b8a0-46792559bea9?gallery=thumbnail",
            thumbnailLarge:
              "http://localhost:8080/ian/thumbs/40e879db-393b-4256-bfe2-9a78771d6937/1/df55f129-1bbb-427f-b8a0-46792559bea9?gallery=preview",
            directUrl:
              "file/40e879db-393b-4256-bfe2-9a78771d6937/1/Kelpie1.jpg",
          },
        ],
      },
      {
        uuid: "8d25bfcc-f877-4cb6-84cd-391a79c7c67a",
        version: 1,
        status: "live",
        name: "Authors of the 20th Century",
        links: {
          view: "http://localhost:8080/ian/items/8d25bfcc-f877-4cb6-84cd-391a79c7c67a/1/",
          self: "http://localhost:8080/ian/api/item/8d25bfcc-f877-4cb6-84cd-391a79c7c67a/1/",
        },
        mainEntry: {
          id: "40e879db-393b-4256-bfe2-9a78771d6930",
          mimeType: "image/jpeg",
          name: "Wilkie Collins.jpg",
          thumbnailSmall:
            "http://localhost:8080/ian/thumbs/8d25bfcc-f877-4cb6-84cd-391a79c7c67a/1/a09056f0-c867-40ea-80c0-a1433f487182?gallery=thumbnail",
          thumbnailLarge:
            "http://localhost:8080/ian/thumbs/8d25bfcc-f877-4cb6-84cd-391a79c7c67a/1/a09056f0-c867-40ea-80c0-a1433f487182?gallery=preview",
          directUrl:
            "file/8d25bfcc-f877-4cb6-84cd-391a79c7c67a/1/Wilkie Collins.jpg",
        },
        additionalEntries: [
          {
            id: "40e879db-393b-4256-bfe2-9a78771d6939",
            mimeType: "image/jpeg",
            name: "Dickens.jpg",
            thumbnailSmall:
              "http://localhost:8080/ian/thumbs/8d25bfcc-f877-4cb6-84cd-391a79c7c67a/1/e3f96e6b-a6aa-4c8e-975c-c2c3870daa34?gallery=thumbnail",
            thumbnailLarge:
              "http://localhost:8080/ian/thumbs/8d25bfcc-f877-4cb6-84cd-391a79c7c67a/1/e3f96e6b-a6aa-4c8e-975c-c2c3870daa34?gallery=preview",
            directUrl:
              "file/8d25bfcc-f877-4cb6-84cd-391a79c7c67a/1/Dickens.jpg",
          },
          {
            id: "40e879db-393b-4256-bfe2-9a78771d6938",
            mimeType: "image/jpeg",
            name: "Conrad.jpg",
            thumbnailSmall:
              "http://localhost:8080/ian/thumbs/8d25bfcc-f877-4cb6-84cd-391a79c7c67a/1/f226e79a-1d2c-4894-aaa1-032812351d29?gallery=thumbnail",
            thumbnailLarge:
              "http://localhost:8080/ian/thumbs/8d25bfcc-f877-4cb6-84cd-391a79c7c67a/1/f226e79a-1d2c-4894-aaa1-032812351d29?gallery=preview",
            directUrl: "file/8d25bfcc-f877-4cb6-84cd-391a79c7c67a/1/Conrad.jpg",
          },
          {
            id: "40e879db-393b-4256-bfe2-9a78771d6922",
            mimeType: "image/jpeg",
            name: "Eliot.jpg",
            thumbnailSmall:
              "http://localhost:8080/ian/thumbs/8d25bfcc-f877-4cb6-84cd-391a79c7c67a/1/3afceca2-63d0-47ea-b921-f199b73194fc?gallery=thumbnail",
            thumbnailLarge:
              "http://localhost:8080/ian/thumbs/8d25bfcc-f877-4cb6-84cd-391a79c7c67a/1/3afceca2-63d0-47ea-b921-f199b73194fc?gallery=preview",
            directUrl:
              "file/8d25bfcc-f877-4cb6-84cd-391a79c7c67a/1/Eliot(2).jpg",
          },
        ],
      },
    ],
    highlight: [],
  };

export const transformedBasicVideoSearchResponse: OEQ.Search.SearchResult<GallerySearchResultItem> =
  {
    start: 0,
    length: 3,
    available: 15,
    results: [
      {
        uuid: "de8fcb0b-0b1c-4c34-9173-a83d1b0be6b5",
        version: 1,
        status: "live",
        name: "How to grow African Violets",
        links: {
          view: "http://localhost:8080/ian/items/de8fcb0b-0b1c-4c34-9173-a83d1b0be6b5/1/",
          self: "http://localhost:8080/ian/api/item/de8fcb0b-0b1c-4c34-9173-a83d1b0be6b5/1/",
        },
        mainEntry: {
          id: "40e879db-393b-4256-bfe2-9a78771d6117",
          mimeType: "openequella/youtube",
          name: "6 Tips For Caring for African Violets",
          thumbnailSmall: "https://i.ytimg.com/vi/9VCudo90K5I/default.jpg",
          thumbnailLarge: "https://i.ytimg.com/vi/9VCudo90K5I/hqdefault.jpg",
          directUrl: "https://www.youtube.com/watch?v=9VCudo90K5I",
        },
        additionalEntries: [],
      },
      {
        uuid: "59139c45-788b-4200-a9cb-e4a39e76ad35",
        version: 1,
        status: "live",
        name: "Quokka (JS) Example",
        links: {
          view: "http://localhost:8080/ian/items/59139c45-788b-4200-a9cb-e4a39e76ad35/1/",
          self: "http://localhost:8080/ian/api/item/59139c45-788b-4200-a9cb-e4a39e76ad35/1/",
        },
        mainEntry: {
          id: "40e879db-393b-4256-bfe2-9a78771d1937",
          mimeType: "video/mp4",
          name: "Quokka-2021-03-24_14.42.37.mp4",
          thumbnailSmall:
            "http://localhost:8080/ian/thumbs/59139c45-788b-4200-a9cb-e4a39e76ad35/1/d81a7599-89a2-474e-b756-50bda202b349?gallery=gallery",
          thumbnailLarge:
            "http://localhost:8080/ian/thumbs/59139c45-788b-4200-a9cb-e4a39e76ad35/1/d81a7599-89a2-474e-b756-50bda202b349?gallery=preview",
          directUrl:
            "file/59139c45-788b-4200-a9cb-e4a39e76ad35/1/Quokka-2021-03-24_14.42.37.mp4",
        },
        additionalEntries: [],
      },
      {
        uuid: "9d5112d4-87b6-4ac1-b773-ceaa4a6c5205",
        version: 1,
        status: "live",
        name: "Daily Stoic Resources",
        links: {
          view: "http://localhost:8080/ian/items/9d5112d4-87b6-4ac1-b773-ceaa4a6c5205/1/",
          self: "http://localhost:8080/ian/api/item/9d5112d4-87b6-4ac1-b773-ceaa4a6c5205/1/",
        },
        mainEntry: {
          id: "40e879db-393b-4256-bfe2-9a78773d6937",
          mimeType: "openequella/youtube",
          name: "These Simple Words Will Help You Through Life's Most Difficult Situations | Ryan Holiday",
          thumbnailSmall: "https://i.ytimg.com/vi/qMNMyLm57VA/default.jpg",
          thumbnailLarge: "https://i.ytimg.com/vi/qMNMyLm57VA/hqdefault.jpg",
          directUrl: "https://www.youtube.com/watch?v=qMNMyLm57VA",
        },
        additionalEntries: [
          {
            id: "40e879db-393b-4256-bfe2-9a78771d6237",
            mimeType: "openequella/youtube",
            name: "Stoicism and the Art of Resilience | Ryan Holiday | Epictetus",
            thumbnailSmall: "https://i.ytimg.com/vi/6-UQYo1YabY/default.jpg",
            thumbnailLarge: "https://i.ytimg.com/vi/6-UQYo1YabY/hqdefault.jpg",
            directUrl: "https://www.youtube.com/watch?v=6-UQYo1YabY",
          },
          {
            id: "40e879db-393b-4256-bfe2-9a78771d5937",
            mimeType: "openequella/youtube",
            name: "Stoicism's Simple Secret To Being Happier | Ryan Holiday | Daily Stoic",
            thumbnailSmall: "https://i.ytimg.com/vi/DtB1Uk_aJOw/default.jpg",
            thumbnailLarge: "https://i.ytimg.com/vi/DtB1Uk_aJOw/hqdefault.jpg",
            directUrl: "https://www.youtube.com/watch?v=DtB1Uk_aJOw",
          },
        ],
      },
    ],
    highlight: [],
  };
