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
import { identity, pipe } from "fp-ts/function";
import { getBaseUrl } from "../AppConfig";
import { simpleMatchD } from "../util/match";
import { updateKalturaAttachment } from "./KalturaModule";
import { updateYoutubeAttachment } from "./YouTubeModule";

/** `attachmentType` for file attachments. */
export const ATYPE_FILE = "file";
/** `attachmentType` for attachments linking to Kaltura media. */
export const ATYPE_KALTURA = "custom/kaltura";
/** `attachmentType` for link/URL attachments. */
export const ATYPE_LINK = "link";
/** `attachmentType` for attachments linking to oEQ resources(e.g. Item). */
export const ATYPE_RESOURCE = "custom/resource";
/** `attachmentType` for attachments linking to YouTube videos. */
export const ATYPE_YOUTUBE = "custom/youtube";

/**
 * Build a direct URL to a file attachment.
 *
 * @param itemUuid The attachment item's UUID
 * @param itemVersion The attachment item's version
 * @param fileAttachmentPath The `filePath` provided by the server
 */
export const buildFileAttachmentUrl = (
  itemUuid: string,
  itemVersion: number,
  fileAttachmentPath: string,
) => `${getBaseUrl()}file/${itemUuid}/${itemVersion}/${fileAttachmentPath}`;

/**
 * Call this function to update attachments that require custom information
 * such as custom MIME type.
 *
 * @param attachment An attachment that might need custom information
 */
export const updateAttachmentForCustomInfo = (
  attachment: OEQ.Search.Attachment,
): OEQ.Search.Attachment => {
  const updateFn = pipe(
    attachment.attachmentType,
    simpleMatchD(
      [
        [ATYPE_KALTURA, () => updateKalturaAttachment],
        [ATYPE_YOUTUBE, () => updateYoutubeAttachment],
      ],
      () => identity,
    ),
  );

  return updateFn(attachment);
};
