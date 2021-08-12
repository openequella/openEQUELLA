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
import * as E from "fp-ts/Either";
import { pipe } from "fp-ts/function";
import { CustomMimeTypes } from "./MimeTypesModule";

/**
 * The bare minimum fields to create an embedded player and matches those found over in
 * `KalturaPlayerEmbedProps`.
 */
export interface KalturaPlayerDetails {
  /**
   * A Kaltura Partner ID for the Kaltura account which holds the content identified by `entryId`.
   */
  partnerId: number;
  /**
   * The player `uiconf_id` for the player configuration to be used to create the embedded player.
   */
  uiconfId: number;
  /**
   * Kaltura Media Entry ID for the movie, audio, etc to be embedded.
   */
  entryId: string;
}

export const EXTERNAL_ID_PARAM = "externalId";

/**
 * Given an externalId from a Kaltura attachments in the format of `<partnerId>/<uiconfId>/<entryId>`
 * splits it into it its parts and returns a representative object. If there are issues with the
 * format, then a `TypeError` will be thrown.
 *
 * @param externalId an externalId property for Kaltura attachments from the oEQ server
 */
export const parseExternalId = (externalId: string): KalturaPlayerDetails => {
  const result: E.Either<string, KalturaPlayerDetails> = pipe(
    externalId.split("/"),
    E.fromPredicate(
      (xs) => xs.length === 3,
      (xs) => `externalId should have three parts, but has ${xs.length}`
    ),
    E.map(([partnerId, uiconfId, entryId]) => ({
      partnerId: Number.parseInt(partnerId),
      uiconfId: Number.parseInt(uiconfId),
      entryId,
    })),
    E.filterOrElse(
      ({ partnerId }) => Number.isInteger(partnerId),
      () => "partnerId should be a number"
    ),
    E.filterOrElse(
      ({ uiconfId }) => Number.isInteger(uiconfId),
      () => "uiconfId should be a number"
    )
  );

  if (E.isLeft(result)) {
    throw new TypeError(result.left);
  }
  return result.right;
};

/**
 * Build a URL which embeds the externalId in a fashion commonly used by the Lightbox.
 */
export const buildViewUrl = (
  attachmentViewLink: string,
  externalId: string
): string => {
  const u = new URL(attachmentViewLink);
  u.searchParams.set(EXTERNAL_ID_PARAM, externalId);
  return u.toString();
};

/**
 * Update Kaltura Media Attachment with Custom MIME type and View URL.
 *
 * @param attachment A Kaltura media attachment
 */
export const updateKalturaAttachment = (
  attachment: OEQ.Search.Attachment
): OEQ.Search.Attachment => {
  const { links } = attachment;
  const { externalId } = links;
  if (externalId) {
    return {
      ...attachment,
      mimeType: CustomMimeTypes.KALTURA,
      links: { ...links, view: buildViewUrl(links.view, externalId) },
    };
  }

  throw new Error("Missing Kaltura media attachment external ID.");
};
