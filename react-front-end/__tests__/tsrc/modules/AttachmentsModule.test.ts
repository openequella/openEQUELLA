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
import { updateAttachmentForCustomInfo } from "../../../tsrc/modules/AttachmentsModule";
import { CustomMimeTypes } from "../../../tsrc/modules/MimeTypesModule";

describe("updateAttachmentForCustomInfo", () => {
  const fileAttachment: OEQ.Search.Attachment = {
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
  };

  const kalturaAttachment: OEQ.Search.Attachment = {
    attachmentType: "custom/kaltura",
    id: "5673f889-6f72-432d-ad50-29b505a28739",
    description: "From pexels: pexels-nadezhda-moryak-6530229.mp4",
    brokenAttachment: false,
    preview: false,
    links: {
      view: "http://localhost:8080/ian/items/91406c5e-e2fe-4528-beac-ab22266e0f50/1/?attachment.uuid=5673f889-6f72-432d-ad50-29b505a28739",
      thumbnail:
        "http://localhost:8080/ian/thumbs/91406c5e-e2fe-4528-beac-ab22266e0f50/1/5673f889-6f72-432d-ad50-29b505a28739",
      externalId: "4211234/48123443-V7/1_d1h8f1dx",
    },
  };

  const youTubeAttachment: OEQ.Search.Attachment = {
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
  };

  it("updates Kaltura attachments", () => {
    const updated = updateAttachmentForCustomInfo(kalturaAttachment);
    expect(updated.mimeType).toEqual(CustomMimeTypes.KALTURA);
    expect(updated.links.view).toBe(
      "http://localhost:8080/ian/items/91406c5e-e2fe-4528-beac-ab22266e0f50/1/?attachment.uuid=5673f889-6f72-432d-ad50-29b505a28739&externalId=4211234%2F48123443-V7%2F1_d1h8f1dx",
    );
  });

  it("updates YouTube attachments", () => {
    const updated = updateAttachmentForCustomInfo(youTubeAttachment);
    expect(updated.mimeType).toBe(CustomMimeTypes.YOUTUBE);
    expect(updated.links.view).toBe(
      "https://www.youtube.com/watch?v=9VCudo90K5I",
    );
  });

  it("leaves 'normal' attachments alone", () => {
    expect(updateAttachmentForCustomInfo(fileAttachment)).toStrictEqual(
      fileAttachment,
    );
  });
});
