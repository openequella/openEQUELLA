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
import * as O from "fp-ts/Option";
import * as R from "fp-ts/Record";
import * as t from "io-ts";
import { constVoid, pipe } from "fp-ts/function";
import { CustomMimeTypes } from "./MimeTypesModule";

/**
 * The bare minimum fields to create an embedded player and matches those found over in
 * `KalturaPlayerEmbedProps`.
 */
export interface KalturaExternalId {
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

export const KalturaPlayerVersionCodec = t.union([
  t.literal("V2"),
  t.literal("V7"),
]);

/**
 * Currently supported Kaltura player versions. However, V2 will be not supported since 30th Sep 2024.
 */
export type KalturaPlayerVersion = t.TypeOf<typeof KalturaPlayerVersionCodec>;

export const EXTERNAL_ID_PARAM = "externalId";
export const PLAYER_WIDTH_PARAM = "width";
export const PLAYER_HEIGHT_PARAM = "height";
export const PLAYER_VERSION_PARAM = "version";

/**
 * Given an externalId from a Kaltura attachments in the format of `<partnerId>/<uiconfId>/<entryId>`
 * splits it into it its parts and returns a representative object. If there are issues with the
 * format, then a `TypeError` will be thrown.
 *
 * @param externalId an externalId property for Kaltura attachments from the oEQ server
 */
export const parseExternalId = (externalId: string): KalturaExternalId => {
  const result: E.Either<string, KalturaExternalId> = pipe(
    externalId.split("/"),
    E.fromPredicate(
      (xs) => xs.length === 3,
      (xs) => `externalId should have three parts, but has ${xs.length}`,
    ),
    E.map(([partnerId, uiconfId, entryId]) => ({
      partnerId: Number.parseInt(partnerId),
      uiconfId: Number.parseInt(uiconfId),
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
  );

  if (E.isLeft(result)) {
    throw new TypeError(result.left);
  }
  return result.right;
};

/**
 * Build a Kaltura viewer URL which embeds the externalId and Kaltura player config in a fashion commonly used by the Lightbox.
 */
export const buildViewerUrl = ({
  id,
  links,
  viewerConfig,
}: OEQ.Search.Attachment): E.Either<string, string> => {
  const viewerUrl = (
    attachmentViewLink: string,
    externalId: string,
    viewerConfig: Record<string, string>,
  ): string => {
    const u = new URL(attachmentViewLink);
    u.searchParams.set(EXTERNAL_ID_PARAM, externalId);

    [PLAYER_WIDTH_PARAM, PLAYER_HEIGHT_PARAM, PLAYER_VERSION_PARAM].forEach(
      (key) =>
        pipe(
          viewerConfig,
          R.lookup(key),
          O.fold(constVoid, (config) => u.searchParams.set(key, config)),
        ),
    );

    return u.toString();
  };

  return pipe(
    E.Do,
    E.apS(
      "externalId",
      E.fromNullable(`Kaltura attachment ${id} is missing an 'externalId'.`)(
        links.externalId,
      ),
    ),
    E.apS(
      "viewerConfig",
      E.fromNullable(
        `Kaltura attachment ${id} is missing Player configuration.`,
      )(viewerConfig),
    ),
    E.map(({ externalId, viewerConfig }) =>
      viewerUrl(links.view, externalId, viewerConfig),
    ),
  );
};

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
