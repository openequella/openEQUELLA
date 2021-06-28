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
import { pipe } from "fp-ts/function";
import * as O from "fp-ts/Option";
import {
  isLightboxSupportedMimeType,
  LightboxConfig,
} from "../components/Lightbox";
import { buildFileAttachmentUrl } from "./AttachmentsModule";

export type Viewer = "lightbox" | "link";
export type ViewerDefinition = [Viewer, string];

export interface ViewerLinkConfig {
  /**
   * Indicating the viewer is 'link'.
   */
  viewerType: "link";
  /**
   * The URL of this viewer.
   */
  url: string;
}

export interface ViewerLightboxConfig {
  /**
   * Indicating the viewer is 'Lightbox'.
   */
  viewerType: "lightbox";
  /**
   * Config required by Lightbox.
   */
  config: LightboxConfig;
}

export type ViewerConfig = ViewerLinkConfig | ViewerLightboxConfig;

export const isViewerLightboxConfig = (
  config: ViewerConfig
): config is ViewerLightboxConfig => config.viewerType === "lightbox";

export interface AttachmentAndViewerDefinition {
  /**
   * The details of an attachment.
   */
  attachment: OEQ.Search.Attachment;
  /**
   * Viewer definition for the attachment, including which viewer to be used and the view URL.
   */
  viewerDefinition: ViewerDefinition;
}

export interface AttachmentAndViewerConfig {
  /**
   * The details of an attachment.
   */
  attachment: OEQ.Search.Attachment;
  /**
   * Viewer configuration for the attachment.
   */
  viewerConfig: ViewerConfig;
}

/**
 * Determine which viewer to use based on the provided `attachmentType` and `mimeType`. The result
 * being the viewer to use, as well as the link to use with that viewer. Currently only supporting
 * non-link type viewer for 'file' attachments.
 *
 * @param attachmentType on of the standard oEQ server attachmentTypes - e.g. 'file'
 * @param viewUrl the basic view URL returned in search results
 * @param mimeType the MIME type of the attachment to determine the viewer for
 * @param mimeTypeViewerId the server specified `ViewerId` for the attachment
 * @param broken whether or not this has been marked as a broken attachment by the server
 */
export const determineViewer = (
  attachmentType: string,
  viewUrl: string,
  broken: boolean,
  mimeType?: string,
  mimeTypeViewerId?: OEQ.MimeType.ViewerId
): ViewerDefinition => {
  const simpleLinkView: ViewerDefinition = ["link", viewUrl];
  if (broken) {
    return simpleLinkView;
  }
  if (attachmentType !== "file" && attachmentType !== "custom/resource") {
    // For non-file attachments, we currently just defer to the link provided by the server
    return simpleLinkView;
  }

  // When determining viewer for files we also need MIME type and the configured
  // MIME type viewer ID
  if (!mimeType || !mimeTypeViewerId) {
    console.warn(
      "When determining viewer for 'file' attachments, please supply both mimeType and mimeTypeViewerId. Falling back to simple link viewer."
    );

    return simpleLinkView;
  }

  if (
    isLightboxSupportedMimeType(mimeType) &&
    ["fancy", "file", "htmlFiveViewer"].includes(mimeTypeViewerId)
  ) {
    return ["lightbox", viewUrl];
  } else if (mimeTypeViewerId === "save") {
    return ["link", `${viewUrl}?.vi=save`];
  }

  return simpleLinkView;
};

/**
 * Based on attachment type, builds either a direct link to a file attachment URL or uses the
 * provided `viewUrl`.
 *
 * @param itemUuid The attachment item's UUID
 * @param itemVersion The attachment item's version
 * @param attachmentType The type of the attachment - as provided by the server
 * @param viewUrl The default 'view' link provided by the server for the attachment
 * @param fileAttachmentPath If attachment type of 'file' then the `filePath` provided by the server
 */
export const determineAttachmentViewUrl = (
  itemUuid: string,
  itemVersion: number,
  attachmentType: string,
  viewUrl: string,
  fileAttachmentPath?: string
): string =>
  attachmentType === "file" && fileAttachmentPath
    ? buildFileAttachmentUrl(itemUuid, itemVersion, fileAttachmentPath)
    : viewUrl;

/**
 * Return an attachment's viewer definition.
 *
 * @param itemUuid The attachment item's UUID
 * @param itemVersion The attachment item's version.
 * @param attachment The attachment for which
 * @param mimeTypeViewerId The attachment's viewer ID.
 */
export const getViewerDefinitionForAttachment = (
  itemUuid: string,
  itemVersion: number,
  attachment: OEQ.Search.Attachment,
  mimeTypeViewerId?: OEQ.MimeType.ViewerId
): ViewerDefinition => {
  const {
    attachmentType,
    mimeType,
    links: { view: defaultViewUrl },
    filePath,
    brokenAttachment,
  } = attachment;
  const viewUrl = determineAttachmentViewUrl(
    itemUuid,
    itemVersion,
    attachmentType,
    defaultViewUrl,
    filePath
  );

  return determineViewer(
    attachmentType,
    viewUrl,
    brokenAttachment,
    mimeType,
    mimeTypeViewerId
  );
};

/**
 * Build a function to handler navigation between Lightbox attachments.
 * @param lightboxAttachments All attachments that can be viewed in Lightbox and their Viewer definitions.
 * @param attachmentIndex Index of the attachment to be viewed
 */
export const buildLightboxNavigationHandler = (
  lightboxAttachments: AttachmentAndViewerDefinition[],
  attachmentIndex: number
): (() => LightboxConfig) | undefined =>
  pipe(
    lightboxAttachments,
    A.lookup(attachmentIndex),
    O.fold(
      () => undefined,
      ({
        attachment: { description, mimeType },
        viewerDefinition: [viewer, viewUrl],
      }) => {
        if (viewer === "lightbox") {
          return () => ({
            src: viewUrl,
            title: description,
            mimeType: mimeType ?? "",
            onNext: buildLightboxNavigationHandler(
              lightboxAttachments,
              attachmentIndex + 1
            ),
            onPrevious: buildLightboxNavigationHandler(
              lightboxAttachments,
              attachmentIndex - 1
            ),
          });
        }
        throw new TypeError(`Unexpected viewer configuration: ${viewer}`);
      }
    )
  );
