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

import { SearchResultItem } from "@openequella/rest-api-client/dist/Search";

const basicSearchObj: SearchResultItem = {
  uuid: "72558c1d-8788-4515-86c8-b24a28cc451e",
  name: "Little Larry",
  description: "A description of a bird",
  status: "live",
  createdDate: new Date("2020-05-26T13:24:00.889+10:00"),
  modifiedDate: new Date("2020-05-26T12:45:06.857+10:00"),
  collectionId: "b28f1ffe-2008-4f5e-d559-83c8acd79316",
  commentCount: 0,
  attachments: [],
  thumbnail: "default",
  displayFields: [],
  displayOptions: {
    attachmentType: "STRUCTURED",
    disableThumbnail: false,
    standardOpen: true,
    integrationOpen: true,
  },
  links: {
    view:
      "http://localhost:8080/rest/items/72558c1d-8788-4515-86c8-b24a28cc451e/1/",
    self:
      "http://localhost:8080/rest/api/item/72558c1d-8788-4515-86c8-b24a28cc451e/1/",
  },
};

const attachSearchObj: SearchResultItem = {
  uuid: "72558c1d-8788-4515-86c8-b24a28cc451e",
  name: "Little Larry",
  description: "A description of a bird",
  status: "live",
  createdDate: new Date("2020-05-26T13:24:00.889+10:00"),
  modifiedDate: new Date("2020-05-26T12:45:06.857+10:00"),
  collectionId: "b28f1ffe-2008-4f5e-d559-83c8acd79316",
  commentCount: 0,
  attachments: [
    {
      attachmentType: "file",
      id: "78b8af7e-f0f5-4b5c-9f44-16f212583fe8",
      description: "config.json",
      preview: false,
      links: {
        view:
          "http://localhost:8080/rest/items/72558c1d-8788-4515-86c8-b24a28cc451e/1/?attachment.uuid=78b8af7e-f0f5-4b5c-9f44-16f212583fe8",
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
  links: {
    view:
      "http://localhost:8080/rest/items/72558c1d-8788-4515-86c8-b24a28cc451e/1/",
    self:
      "http://localhost:8080/rest/api/item/72558c1d-8788-4515-86c8-b24a28cc451e/1/",
  },
};

const customMetaSearchObj: SearchResultItem = {
  uuid: "72558c1d-8788-4515-86c8-b24a28cc451e",
  name: "Little Larry",
  description: "A description of a bird",
  status: "live",
  createdDate: new Date("2020-05-26T13:24:00.889+10:00"),
  modifiedDate: new Date("2020-05-26T12:45:06.857+10:00"),
  collectionId: "b28f1ffe-2008-4f5e-d559-83c8acd79316",
  commentCount: 0,
  attachments: [
    {
      attachmentType: "file",
      id: "78b8af7e-f0f5-4b5c-9f44-16f212583fe8",
      description: "config.json",
      preview: false,
      links: {
        view:
          "http://localhost:8080/rest/items/72558c1d-8788-4515-86c8-b24a28cc451e/1/?attachment.uuid=78b8af7e-f0f5-4b5c-9f44-16f212583fe8",
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
      html:
        '<a href="http://www.abc.net.au/news">http://www.abc.net.au/news</a>',
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
  links: {
    view:
      "http://localhost:8080/rest/items/72558c1d-8788-4515-86c8-b24a28cc451e/1/",
    self:
      "http://localhost:8080/rest/api/item/72558c1d-8788-4515-86c8-b24a28cc451e/1/",
  },
};
export { basicSearchObj, attachSearchObj, customMetaSearchObj };
