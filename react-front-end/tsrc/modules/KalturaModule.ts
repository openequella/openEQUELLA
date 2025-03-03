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
import * as E from "../util/Either.extended";
import * as t from "io-ts";
import { pipe } from "fp-ts/function";
import { ATYPE_KALTURA } from "./AttachmentsModule";
import { CustomMimeTypes } from "./MimeTypesModule";

const KalturaPlayerVersionCodec = t.union([t.literal("V2"), t.literal("V7")]);

/**
 * Currently supported Kaltura player versions. However, V2 will be not supported since 30th Sep 2024.
 */
export type KalturaPlayerVersion = t.TypeOf<typeof KalturaPlayerVersionCodec>;

const KalturaEmbedStyleCodec = t.union([
  t.literal("AUTO"),
  t.literal("IFRAME"),
]);

/**
 * Kaltura supports 3 methods to embed a resource: Iframe Embed, Auto Embed and Dynamic Embed, but only the first two
 * are used in OEQ. Each style will generate different embed code.
 */
export type KalturaEmbedStyle = t.TypeOf<typeof KalturaEmbedStyleCodec>;

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
   * Version of the selected player to use. Must be either "V2" or "V7".
   */
  version: KalturaPlayerVersion;
  /**
   * Kaltura Media Entry ID for the movie, audio, etc to be embedded.
   */
  entryId: string;
}

export const EXTERNAL_ID_PARAM = "externalId";

/**
 * Given an externalId from a Kaltura attachments in the format of `<partnerId>/<uiconfId-version>/<entryId>`
 * splits it into it its parts and returns a representative object. If there are issues with the
 * format, then a `TypeError` will be thrown.
 *
 * @param externalId an externalId property for Kaltura attachments from the oEQ server
 */
export const parseExternalId = (externalId: string): KalturaPlayerDetails => {
  const playerIdAndVersion = (uiconfId: string) =>
    pipe(uiconfId.split("-"), ([id, version]) => ({
      uiconfId: Number.parseInt(id),
      version: version as KalturaPlayerVersion, // We will validate this later so casting in here is OK.
    }));

  const result: E.Either<string, KalturaPlayerDetails> = pipe(
    externalId.split("/"),
    E.fromPredicate(
      (xs) => xs.length === 3,
      (xs) => `externalId should have three parts, but has ${xs.length}`,
    ),
    E.map(([partnerId, uiconfId, entryId]) => ({
      partnerId: Number.parseInt(partnerId),
      ...playerIdAndVersion(uiconfId),
      entryId,
    })),
    E.filterOrElse(
      ({ partnerId }) => Number.isInteger(partnerId),
      () => "partnerId should be a number",
    ),
    E.filterOrElse(
      ({ uiconfId }) => Number.isInteger(uiconfId),
      () => "uiconfId should be a number",
    ),
    E.filterOrElse(
      ({ version }) => KalturaPlayerVersionCodec.is(version),
      () => "Unknown Kaltura player version",
    ),
  );

  if (E.isLeft(result)) {
    throw new TypeError(result.left);
  }
  return result.right;
};

/**
 * Build a URL which embeds the externalId in a fashion commonly used by the Lightbox.
 */
export const buildViewerUrl = ({
  id,
  links,
  attachmentType,
}: OEQ.Search.Attachment): E.Either<string, string> =>
  pipe(
    attachmentType,
    E.fromPredicate(
      (aType) => aType === ATYPE_KALTURA,
      () => "The provided attachment is not a Kaltura attachment.",
    ),
    E.chain((_) =>
      E.fromNullable(`Kaltura attachment ${id} is missing a 'externalId'.`)(
        links.externalId,
      ),
    ),
    E.map((externalId) => {
      const u = new URL(links.view);
      u.searchParams.set(EXTERNAL_ID_PARAM, externalId);
      return u.toString();
    }),
  );

/**
 * Update Kaltura Media Attachment with Custom MIME type and View URL.
 *
 * @param attachment A Kaltura media attachment
 */
export const updateKalturaAttachment = (
  attachment: OEQ.Search.Attachment,
): OEQ.Search.Attachment => {
  const { links } = attachment;

  return pipe(
    buildViewerUrl(attachment),
    E.map((view) => ({
      ...attachment,
      mimeType: CustomMimeTypes.KALTURA,
      links: { ...links, view },
    })),
    E.getOrThrow,
  );
};

/**
 * Build a Kaltura player embed URL based on the provided external IDs and player version.
 */
export const buildPlayerUrl = (
  { partnerId, uiconfId, entryId, version }: KalturaPlayerDetails,
  playerId: string,
  kalturaEmbedStyle: KalturaEmbedStyle = "AUTO",
): string => {
  const src = new URL(
    version === "V7"
      ? `https://cdnapisec.kaltura.com/p/${partnerId}/embedPlaykitJs/uiconf_id/${uiconfId}`
      : `https://cdnapisec.kaltura.com/p/${partnerId}/sp/${partnerId}00/embedIframeJs/uiconf_id/${uiconfId}/partner_id/${partnerId}`,
  );
  (
    [
      [kalturaEmbedStyle === "AUTO" ? "autoembed" : "iframeembed", true],
      ["entry_id", entryId],
    ] as [string, string][]
  ).forEach(([name, value]) => src.searchParams.set(name, value));

  if (kalturaEmbedStyle === "AUTO") {
    src.searchParams.set(version === "V7" ? "targetId" : "playerId", playerId);
  }

  return src.toString();
};
