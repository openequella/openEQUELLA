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

export const fileAttachment: OEQ.Search.Attachment = {
  attachmentType: "file",
  id: "9e751549-5cba-47dd-bccb-722c48072287",
  description: "broken.png",
  preview: false,
  mimeType: "image/png",
  hasGeneratedThumb: true,
  brokenAttachment: false,
  links: {
    view:
      "http://localhost:8080/rest/items/72558c1d-8788-4515-86c8-b24a28cc451e/1/?attachment.uuid=78b8af7e-f0f5-4b5c-9f44-16f212583fe8",
    thumbnail: "./thumb.jpg",
  },
};

export const brokenFileAttachment: OEQ.Search.Attachment = {
  attachmentType: "file",
  id: "9e751549-5cba-47dd-bccb-722c48072287",
  description: "broken.png",
  preview: false,
  mimeType: "image/png",
  hasGeneratedThumb: true,
  brokenAttachment: true,
  links: {
    view:
      "http://localhost:8080/rest/items/72558c1d-8788-4515-86c8-b24a28cc451e/1/?attachment.uuid=78b8af7e-f0f5-4b5c-9f44-16f212583fe8",
    thumbnail: "./thumb.jpg",
  },
};

export const resourceFileAttachment: OEQ.Search.Attachment = {
  attachmentType: "custom/resource",
  id: "2c663052-a472-4b3e-b4d1-a25a5cd45675",
  description: "miss-violet.jpg",
  brokenAttachment: false,
  preview: false,
  mimeType: "image/jpeg",
  links: {
    view:
      "http://localhost:8080/rest/items/9ba9a328-4697-4ae0-9dba-3f82f1876fb8/1/?attachment.uuid=2c663052-a472-4b3e-b4d1-a25a5cd45675",
    thumbnail: "./thumb.jpg",
  },
};
export const linkAttachment: OEQ.Search.Attachment = {
  attachmentType: "link",
  id: "7d84f75e-1756-4af0-b4e1-2553b52885f0",
  description: "https://www.google.com",
  brokenAttachment: false,
  preview: false,
  links: {
    view:
      "http://localhost:8080/rest/items/1dc04a21-9659-487f-b784-66726fa59bdc/1/?attachment.uuid=7d84f75e-1756-4af0-b4e1-2553b52885f0",
    thumbnail: "./thumb.jpg",
  },
};

export const resourceLinkAttachment: OEQ.Search.Attachment = {
  attachmentType: "custom/resource",
  id: "02f8f12e-8222-4c5b-b89a-888cbbbc402d",
  description: "https://www.google.com",
  brokenAttachment: false,
  preview: false,
  mimeType: "equella/link",
  links: {
    view:
      "http://localhost:8080/rest/items/a321a2f7-2228-4853-8292-54f390976049/1/?attachment.uuid=02f8f12e-8222-4c5b-b89a-888cbbbc402d",
    thumbnail: "./thumb.jpg",
  },
};

export const equellaItemAttachment: OEQ.Search.Attachment = {
  attachmentType: "custom/resource",
  id: "7140295f-7fe0-4b6b-b621-eb2adfbd386f",
  description: "a321a2f7-2228-4853-8292-54f390976049",
  brokenAttachment: false,
  preview: false,
  mimeType: "equella/item",
  links: {
    view:
      "http://localhost:8080/rest/items/475a5e1b-4558-43f9-aeb4-9d49408197be/1/?attachment.uuid=7140295f-7fe0-4b6b-b621-eb2adfbd386f",
    thumbnail:
      "http://localhost:8080/rest/thumbs/475a5e1b-4558-43f9-aeb4-9d49408197be/1/7140295f-7fe0-4b6b-b621-eb2adfbd386f",
  },
};

export const htmlAttachment: OEQ.Search.Attachment = {
  attachmentType: "html",
  id: "ef533d4c-15fc-45d4-91ee-0873e17fa7cb",
  description: "New Page",
  brokenAttachment: false,
  preview: false,
  mimeType: "text/html",
  links: {
    view:
      "http://localhost:8080/rest/items/eb099d2a-d1a8-4e4e-98ff-fec42587adb4/1/?attachment.uuid=ef533d4c-15fc-45d4-91ee-0873e17fa7cb",
    thumbnail:
      "http://localhost:8080/rest/thumbs/eb099d2a-d1a8-4e4e-98ff-fec42587adb4/1/ef533d4c-15fc-45d4-91ee-0873e17fa7cb",
  },
};

export const resourceHtmlAttachment: OEQ.Search.Attachment = {
  attachmentType: "custom/resource",
  id: "1fa39170-ad0a-4607-9e37-bac86a8dea32",
  description: "New Page",
  brokenAttachment: false,
  preview: false,
  mimeType: "text/html",
  links: {
    view:
      "http://localhost:8080/rest/items/550a3047-eb17-4db4-8b1e-6adbf6d63f3b/1/?attachment.uuid=1fa39170-ad0a-4607-9e37-bac86a8dea32",
    thumbnail:
      "http://localhost:8080/rest/thumbs/550a3047-eb17-4db4-8b1e-6adbf6d63f3b/1/1fa39170-ad0a-4607-9e37-bac86a8dea32",
  },
};
