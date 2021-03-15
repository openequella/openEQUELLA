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

export const basicImageSearchResponse: OEQ.Search.SearchResult<OEQ.Search.SearchResultItem> = {
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
      attachments: [
        {
          attachmentType: "file",
          id: "dbd6dd98-d731-4a8f-907e-ceaf9608da3b",
          description: "Kelpie1.jpg",
          preview: false,
          mimeType: "image/jpeg",
          hasGeneratedThumb: true,
          links: {
            view:
              "http://localhost:8080/ian/items/fe79c485-a6dd-4743-81e8-52de66494633/1/?attachment.uuid=dbd6dd98-d731-4a8f-907e-ceaf9608da3b",
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
        view:
          "http://localhost:8080/ian/items/fe79c485-a6dd-4743-81e8-52de66494633/1/",
        self:
          "http://localhost:8080/ian/api/item/fe79c485-a6dd-4743-81e8-52de66494633/1/",
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
      attachments: [
        {
          attachmentType: "file",
          id: "4fddbeb7-8d16-4417-be60-8709ce9d7b15",
          description: "Kelpie2.jpg",
          preview: false,
          mimeType: "image/jpeg",
          hasGeneratedThumb: true,
          links: {
            view:
              "http://localhost:8080/ian/items/40e879db-393b-4256-bfe2-9a78771d6937/1/?attachment.uuid=4fddbeb7-8d16-4417-be60-8709ce9d7b15",
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
          links: {
            view:
              "http://localhost:8080/ian/items/40e879db-393b-4256-bfe2-9a78771d6937/1/?attachment.uuid=df55f129-1bbb-427f-b8a0-46792559bea9",
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
          links: {
            view:
              "http://localhost:8080/ian/items/40e879db-393b-4256-bfe2-9a78771d6937/1/?attachment.uuid=44528005-fb39-4461-bac7-12cd33ce4330",
            thumbnail:
              "http://localhost:8080/ian/thumbs/40e879db-393b-4256-bfe2-9a78771d6937/1/44528005-fb39-4461-bac7-12cd33ce4330",
          },
        },
      ],
      thumbnail: "default",
      displayFields: [
        { type: "node", name: "Resource Type", html: "Supplementary reading" },
      ],
      displayOptions: {
        attachmentType: "STRUCTURED",
        disableThumbnail: false,
        standardOpen: false,
        integrationOpen: true,
      },
      keywordFoundInAttachment: false,
      links: {
        view:
          "http://localhost:8080/ian/items/40e879db-393b-4256-bfe2-9a78771d6937/1/",
        self:
          "http://localhost:8080/ian/api/item/40e879db-393b-4256-bfe2-9a78771d6937/1/",
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
      attachments: [
        {
          attachmentType: "file",
          id: "a09056f0-c867-40ea-80c0-a1433f487182",
          description: "Wilkie Collins.jpg",
          preview: false,
          mimeType: "image/jpeg",
          hasGeneratedThumb: true,
          links: {
            view:
              "http://localhost:8080/ian/items/8d25bfcc-f877-4cb6-84cd-391a79c7c67a/1/?attachment.uuid=a09056f0-c867-40ea-80c0-a1433f487182",
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
          links: {
            view:
              "http://localhost:8080/ian/items/8d25bfcc-f877-4cb6-84cd-391a79c7c67a/1/?attachment.uuid=6411799f-052a-4926-8c67-0c851447c762",
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
          links: {
            view:
              "http://localhost:8080/ian/items/8d25bfcc-f877-4cb6-84cd-391a79c7c67a/1/?attachment.uuid=e3f96e6b-a6aa-4c8e-975c-c2c3870daa34",
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
          links: {
            view:
              "http://localhost:8080/ian/items/8d25bfcc-f877-4cb6-84cd-391a79c7c67a/1/?attachment.uuid=f226e79a-1d2c-4894-aaa1-032812351d29",
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
          links: {
            view:
              "http://localhost:8080/ian/items/8d25bfcc-f877-4cb6-84cd-391a79c7c67a/1/?attachment.uuid=3afceca2-63d0-47ea-b921-f199b73194fc",
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
        view:
          "http://localhost:8080/ian/items/8d25bfcc-f877-4cb6-84cd-391a79c7c67a/1/",
        self:
          "http://localhost:8080/ian/api/item/8d25bfcc-f877-4cb6-84cd-391a79c7c67a/1/",
      },
      isLatestVersion: true,
    },
  ],
  highlight: [],
};

export const transformedBasicImageSearchResponse: OEQ.Search.SearchResult<GallerySearchResultItem> = {
  start: 0,
  length: 3,
  available: 24,
  results: [
    {
      uuid: "fe79c485-a6dd-4743-81e8-52de66494633",
      version: 1,
      name: "A history of cattle dogs",
      links: {
        view:
          "http://localhost:8080/ian/items/fe79c485-a6dd-4743-81e8-52de66494633/1/",
        self:
          "http://localhost:8080/ian/api/item/fe79c485-a6dd-4743-81e8-52de66494633/1/",
      },
      mainEntry: {
        mimeType: "image/jpeg",
        name: "Kelpie1.jpg",
        imagePathSmall:
          "http://localhost:8080/ian/thumbs/fe79c485-a6dd-4743-81e8-52de66494633/1/dbd6dd98-d731-4a8f-907e-ceaf9608da3b?gallery=thumbnail",
        imagePathMedium:
          "http://localhost:8080/ian/thumbs/fe79c485-a6dd-4743-81e8-52de66494633/1/dbd6dd98-d731-4a8f-907e-ceaf9608da3b?gallery=preview",
        imagePathFull:
          "file/fe79c485-a6dd-4743-81e8-52de66494633/1/Kelpie1.jpg",
      },
      additionalEntries: [],
    },
    {
      uuid: "40e879db-393b-4256-bfe2-9a78771d6937",
      version: 1,
      name: "Australian Kelpies",
      links: {
        view:
          "http://localhost:8080/ian/items/40e879db-393b-4256-bfe2-9a78771d6937/1/",
        self:
          "http://localhost:8080/ian/api/item/40e879db-393b-4256-bfe2-9a78771d6937/1/",
      },
      mainEntry: {
        mimeType: "image/jpeg",
        name: "Kelpie2.jpg",
        imagePathSmall:
          "http://localhost:8080/ian/thumbs/40e879db-393b-4256-bfe2-9a78771d6937/1/4fddbeb7-8d16-4417-be60-8709ce9d7b15?gallery=thumbnail",
        imagePathMedium:
          "http://localhost:8080/ian/thumbs/40e879db-393b-4256-bfe2-9a78771d6937/1/4fddbeb7-8d16-4417-be60-8709ce9d7b15?gallery=preview",
        imagePathFull:
          "file/40e879db-393b-4256-bfe2-9a78771d6937/1/Kelpie2.jpg",
      },
      additionalEntries: [
        {
          mimeType: "image/jpeg",
          name: "Kelpie1.jpg",
          imagePathSmall:
            "http://localhost:8080/ian/thumbs/40e879db-393b-4256-bfe2-9a78771d6937/1/df55f129-1bbb-427f-b8a0-46792559bea9?gallery=thumbnail",
          imagePathMedium:
            "http://localhost:8080/ian/thumbs/40e879db-393b-4256-bfe2-9a78771d6937/1/df55f129-1bbb-427f-b8a0-46792559bea9?gallery=preview",
          imagePathFull:
            "file/40e879db-393b-4256-bfe2-9a78771d6937/1/Kelpie1.jpg",
        },
      ],
    },
    {
      uuid: "8d25bfcc-f877-4cb6-84cd-391a79c7c67a",
      version: 1,
      name: "Authors of the 20th Century",
      links: {
        view:
          "http://localhost:8080/ian/items/8d25bfcc-f877-4cb6-84cd-391a79c7c67a/1/",
        self:
          "http://localhost:8080/ian/api/item/8d25bfcc-f877-4cb6-84cd-391a79c7c67a/1/",
      },
      mainEntry: {
        mimeType: "image/jpeg",
        name: "Wilkie Collins.jpg",
        imagePathSmall:
          "http://localhost:8080/ian/thumbs/8d25bfcc-f877-4cb6-84cd-391a79c7c67a/1/a09056f0-c867-40ea-80c0-a1433f487182?gallery=thumbnail",
        imagePathMedium:
          "http://localhost:8080/ian/thumbs/8d25bfcc-f877-4cb6-84cd-391a79c7c67a/1/a09056f0-c867-40ea-80c0-a1433f487182?gallery=preview",
        imagePathFull:
          "file/8d25bfcc-f877-4cb6-84cd-391a79c7c67a/1/Wilkie Collins.jpg",
      },
      additionalEntries: [
        {
          mimeType: "image/jpeg",
          name: "Dickens.jpg",
          imagePathSmall:
            "http://localhost:8080/ian/thumbs/8d25bfcc-f877-4cb6-84cd-391a79c7c67a/1/e3f96e6b-a6aa-4c8e-975c-c2c3870daa34?gallery=thumbnail",
          imagePathMedium:
            "http://localhost:8080/ian/thumbs/8d25bfcc-f877-4cb6-84cd-391a79c7c67a/1/e3f96e6b-a6aa-4c8e-975c-c2c3870daa34?gallery=preview",
          imagePathFull:
            "file/8d25bfcc-f877-4cb6-84cd-391a79c7c67a/1/Dickens.jpg",
        },
        {
          mimeType: "image/jpeg",
          name: "Conrad.jpg",
          imagePathSmall:
            "http://localhost:8080/ian/thumbs/8d25bfcc-f877-4cb6-84cd-391a79c7c67a/1/f226e79a-1d2c-4894-aaa1-032812351d29?gallery=thumbnail",
          imagePathMedium:
            "http://localhost:8080/ian/thumbs/8d25bfcc-f877-4cb6-84cd-391a79c7c67a/1/f226e79a-1d2c-4894-aaa1-032812351d29?gallery=preview",
          imagePathFull:
            "file/8d25bfcc-f877-4cb6-84cd-391a79c7c67a/1/Conrad.jpg",
        },
        {
          mimeType: "image/jpeg",
          name: "Eliot.jpg",
          imagePathSmall:
            "http://localhost:8080/ian/thumbs/8d25bfcc-f877-4cb6-84cd-391a79c7c67a/1/3afceca2-63d0-47ea-b921-f199b73194fc?gallery=thumbnail",
          imagePathMedium:
            "http://localhost:8080/ian/thumbs/8d25bfcc-f877-4cb6-84cd-391a79c7c67a/1/3afceca2-63d0-47ea-b921-f199b73194fc?gallery=preview",
          imagePathFull:
            "file/8d25bfcc-f877-4cb6-84cd-391a79c7c67a/1/Eliot(2).jpg",
        },
      ],
    },
  ],
  highlight: [],
};
