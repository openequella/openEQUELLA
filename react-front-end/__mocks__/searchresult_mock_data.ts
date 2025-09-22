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

const basicSearchObj: OEQ.Search.SearchResultItem = {
  uuid: "72558c1d-8788-4515-86c8-b24a28cc451e",
  version: 1,
  name: "Little Larry",
  description: "A description of a bird",
  status: "live",
  createdDate: new Date("2020-05-26T13:24:00.889+10:00"),
  modifiedDate: new Date("2020-05-26T12:45:06.857+10:00"),
  collectionId: "b28f1ffe-2008-4f5e-d559-83c8acd79316",
  commentCount: 1,
  starRatings: 1,
  attachmentCount: 0,
  attachments: [],
  thumbnail: "default",
  displayFields: [],
  displayOptions: {
    attachmentType: "STRUCTURED",
    disableThumbnail: false,
    standardOpen: true,
    integrationOpen: true,
  },
  keywordFoundInAttachment: false,
  links: {
    view: "http://localhost:8080/rest/items/72558c1d-8788-4515-86c8-b24a28cc451e/1/",
    self: "http://localhost:8080/rest/api/item/72558c1d-8788-4515-86c8-b24a28cc451e/1/",
  },
  bookmark: {
    id: 884874,
    addedAt: new Date("2020-05-27T13:24:00.889+10:00"),
    tags: [],
  },
  isLatestVersion: true,
};

const attachSearchObj: OEQ.Search.SearchResultItem = {
  uuid: "72558c1d-8788-4515-86c8-b24a28cc451e",
  version: 1,
  name: "Little Larry",
  description: "A description of a bird",
  status: "live",
  createdDate: new Date("2020-05-26T13:24:00.889+10:00"),
  modifiedDate: new Date("2020-05-26T12:45:06.857+10:00"),
  collectionId: "b28f1ffe-2008-4f5e-d559-83c8acd79316",
  commentCount: 2,
  starRatings: 1.5,
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
  thumbnail: "default",
  displayFields: [],
  displayOptions: {
    attachmentType: "STRUCTURED",
    disableThumbnail: false,
    standardOpen: true,
    integrationOpen: true,
  },
  keywordFoundInAttachment: false,
  links: {
    view: "http://localhost:8080/rest/items/72558c1d-8788-4515-86c8-b24a28cc451e/1/",
    self: "http://localhost:8080/rest/api/item/72558c1d-8788-4515-86c8-b24a28cc451e/1/",
  },
  isLatestVersion: true,
};

const keywordFoundInAttachmentObj: OEQ.Search.SearchResultItem = {
  uuid: "72558c1d-8788-4515-86c8-b24a28cc451e",
  version: 1,
  name: "Little Larry",
  description: "A description of a bird",
  status: "live",
  createdDate: new Date("2020-05-26T13:24:00.889+10:00"),
  modifiedDate: new Date("2020-05-26T12:45:06.857+10:00"),
  collectionId: "b28f1ffe-2008-4f5e-d559-83c8acd79316",
  commentCount: 0,
  starRatings: -1,
  attachmentCount: 1,
  attachments: [
    {
      attachmentType: "file",
      id: "78b8af7e-f0f5-4b5c-9f44-16f212583fe8",
      description: "config.json",
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
  thumbnail: "default",
  displayFields: [],
  displayOptions: {
    attachmentType: "STRUCTURED",
    disableThumbnail: false,
    standardOpen: true,
    integrationOpen: false,
  },
  keywordFoundInAttachment: true,
  links: {
    view: "http://localhost:8080/rest/items/72558c1d-8788-4515-86c8-b24a28cc451e/1/",
    self: "http://localhost:8080/rest/api/item/72558c1d-8788-4515-86c8-b24a28cc451e/1/",
  },
  isLatestVersion: false,
};

const customMetaSearchObj: OEQ.Search.SearchResultItem = {
  uuid: "72558c1d-8788-4515-86c8-b24a28cc451e",
  version: 1,
  name: "Little Larry",
  description: "A description of a bird",
  status: "live",
  createdDate: new Date("2020-05-26T13:24:00.889+10:00"),
  modifiedDate: new Date("2020-05-26T12:45:06.857+10:00"),
  collectionId: "b28f1ffe-2008-4f5e-d559-83c8acd79316",
  commentCount: 0,
  starRatings: -1,
  attachmentCount: 1,
  attachments: [
    {
      attachmentType: "file",
      id: "78b8af7e-f0f5-4b5c-9f44-16f212583fe8",
      description: "config.json",
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
  thumbnail: "default",
  displayFields: [
    {
      type: "node",
      name: "Collection",
      html: "cats, bobs, flerps",
    },
    {
      type: "node",
      name: "Url metadata",
      html: '<a href="http://www.abc.net.au/news">http://www.abc.net.au/news</a>',
    },
    {
      type: "node",
      name: "date metadata",
      html: "02 June 2020",
    },
  ],
  displayOptions: {
    attachmentType: "STRUCTURED",
    disableThumbnail: false,
    standardOpen: true,
    integrationOpen: true,
  },
  keywordFoundInAttachment: true,
  links: {
    view: "http://localhost:8080/rest/items/72558c1d-8788-4515-86c8-b24a28cc451e/1/",
    self: "http://localhost:8080/rest/api/item/72558c1d-8788-4515-86c8-b24a28cc451e/1/",
  },
  isLatestVersion: false,
};

const oneDeadAttachObj: OEQ.Search.SearchResultItem = {
  uuid: "72558c1d-8788-4515-86c8-b24a28cc451e",
  version: 1,
  name: "Little Larry",
  description: "A description of a bird",
  status: "live",
  createdDate: new Date("2020-05-26T13:24:00.889+10:00"),
  modifiedDate: new Date("2020-05-26T12:45:06.857+10:00"),
  collectionId: "b28f1ffe-2008-4f5e-d559-83c8acd79316",
  commentCount: 2,
  starRatings: 1.5,
  attachmentCount: 1,
  attachments: [
    {
      attachmentType: "file",
      id: "9e751549-5cba-47dd-bccb-722c48072287",
      description: "broken.png",
      preview: false,
      mimeType: "image/png",
      hasGeneratedThumb: true,
      brokenAttachment: true,
      links: {
        view: "http://localhost:8080/rest/items/72558c1d-8788-4515-86c8-b24a28cc451e/1/?attachment.uuid=78b8af7e-f0f5-4b5c-9f44-16f212583fe8",
        thumbnail: "./thumb.jpg",
      },
    },
  ],
  thumbnail: "default",
  displayFields: [],
  displayOptions: {
    attachmentType: "STRUCTURED",
    disableThumbnail: false,
    standardOpen: true,
    integrationOpen: true,
  },
  keywordFoundInAttachment: false,
  links: {
    view: "http://localhost:8080/rest/items/72558c1d-8788-4515-86c8-b24a28cc451e/1/",
    self: "http://localhost:8080/rest/api/item/72558c1d-8788-4515-86c8-b24a28cc451e/1/",
  },
  isLatestVersion: true,
};

const oneDeadOneAliveAttachObj: OEQ.Search.SearchResultItem = {
  uuid: "72558c1d-8788-4515-86c8-b24a28cc451e",
  version: 1,
  name: "Little Larry",
  description: "A description of a bird",
  status: "live",
  createdDate: new Date("2020-05-26T13:24:00.889+10:00"),
  modifiedDate: new Date("2020-05-26T12:45:06.857+10:00"),
  collectionId: "b28f1ffe-2008-4f5e-d559-83c8acd79316",
  commentCount: 2,
  starRatings: 1.5,
  attachmentCount: 2,
  attachments: [
    {
      attachmentType: "file",
      id: "9e751549-5cba-47dd-bccb-722c48072287",
      description: "broken.png",
      preview: false,
      mimeType: "image/png",
      hasGeneratedThumb: true,
      brokenAttachment: true,
      links: {
        view: "http://localhost:8080/rest/items/72558c1d-8788-4515-86c8-b24a28cc451e/1/?attachment.uuid=78b8af7e-f0f5-4b5c-9f44-16f212583fe8",
        thumbnail: "./thumb.jpg",
      },
    },
    {
      attachmentType: "file",
      id: "78883eff-7cf6-4b14-ab76-2b7f84dbe833",
      description: "notbroken.png",
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
  thumbnail: "default",
  displayFields: [],
  displayOptions: {
    attachmentType: "STRUCTURED",
    disableThumbnail: false,
    standardOpen: true,
    integrationOpen: true,
  },
  keywordFoundInAttachment: false,
  links: {
    view: "http://localhost:8080/rest/items/72558c1d-8788-4515-86c8-b24a28cc451e/1/",
    self: "http://localhost:8080/rest/api/item/72558c1d-8788-4515-86c8-b24a28cc451e/1/",
  },
  isLatestVersion: true,
};

export const DRM_ITEM_NAME = "DRM Item";
export const DRM_ATTACHMENT_NAME = "DRM Attachment Item";

const drmAttachObj: OEQ.Search.SearchResultItem = {
  uuid: "72558c1d-8788-4515-86c8-b24a28cc451e",
  version: 1,
  name: DRM_ITEM_NAME,
  description: "A description of a bird",
  status: "live",
  createdDate: new Date("2020-05-26T13:24:00.889+10:00"),
  modifiedDate: new Date("2020-05-26T12:45:06.857+10:00"),
  collectionId: "b28f1ffe-2008-4f5e-d559-83c8acd79316",
  commentCount: 2,
  starRatings: 1.5,
  attachmentCount: 2,
  attachments: [
    {
      attachmentType: "file",
      id: "9e751549-5cba-47dd-bccb-722c48072287",
      description: DRM_ATTACHMENT_NAME,
      preview: false,
      mimeType: "image/png",
      hasGeneratedThumb: true,
      brokenAttachment: false,
      links: {
        view: "http://localhost:8080/rest/items/72558c1d-8788-4515-86c8-b24a28cc451e/1/?attachment.uuid=78b8af7e-f0f5-4b5c-9f44-16f212583fe8",
        thumbnail: "./thumb.jpg",
      },
    },
    {
      attachmentType: "file",
      id: "78883eff-7cf6-4b14-ab76-2b7f84dbe833",
      description: "notbroken.png",
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
  thumbnail: "default",
  displayFields: [],
  displayOptions: {
    attachmentType: "STRUCTURED",
    disableThumbnail: false,
    standardOpen: true,
    integrationOpen: true,
  },
  keywordFoundInAttachment: false,
  links: {
    view: "http://localhost:8080/rest/items/72558c1d-8788-4515-86c8-b24a28cc451e/1/",
    self: "http://localhost:8080/rest/api/item/72558c1d-8788-4515-86c8-b24a28cc451e/1/",
  },
  isLatestVersion: true,
  drmStatus: {
    isAuthorised: true,
    termsAccepted: false,
    isAllowSummary: false,
  },
};

const drmUnauthorisedObj = {
  ...drmAttachObj,
  drmStatus: {
    isAuthorised: false,
    termsAccepted: false,
    isAllowSummary: false,
  },
};

const drmAllowSummaryObj = {
  ...drmAttachObj,
  drmStatus: {
    isAuthorised: false,
    termsAccepted: false,
    isAllowSummary: true,
  },
};

const nonLiveObj: OEQ.Search.SearchResultItem = {
  ...attachSearchObj,
  status: "draft",
};

export {
  basicSearchObj,
  attachSearchObj,
  customMetaSearchObj,
  keywordFoundInAttachmentObj,
  oneDeadAttachObj,
  oneDeadOneAliveAttachObj,
  drmAllowSummaryObj,
  drmAttachObj,
  drmUnauthorisedObj,
  nonLiveObj,
};
