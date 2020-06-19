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
exports.getSearchResult = {
  start: 0,
  length: 10,
  available: 4,
  results: [
    {
      uuid: "9b9bf5a9-c5af-490b-88fe-7e330679fad2",
      name: "new title",
      status: "personal",
      createdDate: new Date("2014-06-11T10:28:58.190+10:00"),
      modifiedDate: new Date("2014-06-11T10:28:58.393+10:00"),
      collectionId: "6b356e2e-e6a0-235a-5730-15ad1d8ad630",
      commentCount: 0,
      attachments: [
        {
          attachmentType: "file",
          id: "29e0fe1b-dbd6-4c98-9e7a-d957d9c731f5",
          description: "B.txt",
          preview: false,
          links: {
            view:
              "http://localhost:8080/rest/items/9b9bf5a9-c5af-490b-88fe-7e330679fad2/1/?attachment.uuid=29e0fe1b-dbd6-4c98-9e7a-d957d9c731f5",
            thumbnail:
              "http://localhost:8080/rest/thumbs/9b9bf5a9-c5af-490b-88fe-7e330679fad2/1/29e0fe1b-dbd6-4c98-9e7a-d957d9c731f5",
          },
        },
      ],
      thumbnail: "initial",
      displayFields: [],
      links: {
        view:
          "http://localhost:8080/rest/items/9b9bf5a9-c5af-490b-88fe-7e330679fad2/1/",
        self:
          "http://localhost:8080/rest/api/item/9b9bf5a9-c5af-490b-88fe-7e330679fad2/1/",
      },
    },
    {
      uuid: "266bb0ff-a730-4658-aec0-c68bbefc227c",
      status: "live",
      createdDate: new Date("2014-06-11T09:31:08.557+10:00"),
      modifiedDate: new Date("2014-06-11T09:31:08.557+10:00"),
      collectionId: "9a1ddb24-6bf5-db3d-d8fe-4fca20ecf69c",
      commentCount: 0,
      attachments: [],
      thumbnail: "initial",
      displayFields: [],
      links: {
        view:
          "http://localhost:8080/rest/items/266bb0ff-a730-4658-aec0-c68bbefc227c/1/",
        self:
          "http://localhost:8080/rest/api/item/266bb0ff-a730-4658-aec0-c68bbefc227c/1/",
      },
    },
    {
      uuid: "2534e329-e37e-4851-896e-51d8b39104c4",
      status: "live",
      createdDate: new Date("2014-06-11T09:27:14.800+10:00"),
      modifiedDate: new Date("2014-06-11T09:27:14.803+10:00"),
      collectionId: "9a1ddb24-6bf5-db3d-d8fe-4fca20ecf69c",
      commentCount: 0,
      attachments: [],
      thumbnail: "initial",
      displayFields: [],
      links: {
        view:
          "http://localhost:8080/rest/items/2534e329-e37e-4851-896e-51d8b39104c4/1/",
        self:
          "http://localhost:8080/rest/api/item/2534e329-e37e-4851-896e-51d8b39104c4/1/",
      },
    },
    {
      uuid: "925f5dd2-66eb-4b68-85be-93837af785d0",
      name: "new title",
      status: "personal",
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
          links: {
            view:
              "http://localhost:8080/rest/items/925f5dd2-66eb-4b68-85be-93837af785d0/1/?attachment.uuid=0a89415c-73b6-4e9b-8372-197b6ba4946c",
            thumbnail:
              "http://localhost:8080/rest/thumbs/925f5dd2-66eb-4b68-85be-93837af785d0/1/0a89415c-73b6-4e9b-8372-197b6ba4946c",
          },
        },
      ],
      thumbnail: "initial",
      displayFields: [],
      links: {
        view:
          "http://localhost:8080/rest/items/925f5dd2-66eb-4b68-85be-93837af785d0/1/",
        self:
          "http://localhost:8080/rest/api/item/925f5dd2-66eb-4b68-85be-93837af785d0/1/",
      },
    },
  ],
};
