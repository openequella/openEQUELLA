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
import * as E from "fp-ts/Either";
import { pipe } from "fp-ts/function";
import * as O from "fp-ts/Option";
import * as React from "react";
import {
  EXTERNAL_ID_PARAM,
  KalturaExternalId,
  KalturaPlayerVersion,
  KalturaPlayerVersionCodec,
  parseExternalId,
  PLAYER_HEIGHT_PARAM,
  PLAYER_VERSION_PARAM,
  PLAYER_WIDTH_PARAM,
} from "../modules/KalturaModule";
import { CustomMimeTypes } from "../modules/MimeTypesModule";
import { extractVideoId } from "../modules/YouTubeModule";
import { languageStrings } from "../util/langstrings";
import { simpleMatchD } from "../util/match";
import KalturaPlayerEmbed from "./KalturaPlayerEmbed";
import LightboxMessage from "./LightboxMessage";
import YouTubeEmbed from "./YouTubeEmbed";

const { kalturaExternalIdIssue, kalturaMissingId, youTubeVideoMissingId } =
  languageStrings.lightboxComponent;

const buildKaltura = (src: string): O.Option<React.JSX.Element> => {
  const kalturaExternalId = (
    src: string,
  ): E.Either<string, KalturaExternalId> =>
    pipe(
      new URL(src).searchParams.get(EXTERNAL_ID_PARAM),
      E.fromNullable(kalturaMissingId),
      E.chain((externalId) =>
        E.tryCatch(
          () => parseExternalId(externalId),
          (e) => {
            console.error("Failed to display Kaltura media in Lightbox: " + e);
            return kalturaExternalIdIssue;
          },
        ),
      ),
    );

  const playerDimension = (
    src: string,
    param: string,
  ): E.Either<string, number> =>
    pipe(
      new URL(src).searchParams.get(param),
      E.fromNullable(`Missing Kaltura Player ${param}`),
      E.map(Number.parseInt),
      E.filterOrElse(
        Number.isInteger,
        () => `Invalid value for Kaltura Player ${param}`,
      ),
    );

  const playerVersion = (src: string): E.Either<string, KalturaPlayerVersion> =>
    pipe(
      new URL(src).searchParams.get(PLAYER_VERSION_PARAM),
      E.fromNullable("Missing Kaltura Player version"),
      E.filterOrElse(
        KalturaPlayerVersionCodec.is,
        () => "Unknown Kaltura Player version",
      ),
    );

  return pipe(
    E.Do,
    E.apS("externalId", kalturaExternalId(src)),
    E.apS("width", playerDimension(src, PLAYER_WIDTH_PARAM)),
    E.apS("height", playerDimension(src, PLAYER_HEIGHT_PARAM)),
    E.apS("version", playerVersion(src)),
    E.fold(
      (e) => <LightboxMessage message={e} />,
      ({ externalId, width, height, version }) => (
        <KalturaPlayerEmbed
          {...externalId}
          width={width}
          height={height}
          version={version}
        />
      ),
    ),
    O.some,
  );
};

const buildYouTube = (src: string): O.Option<JSX.Element> =>
  pipe(
    extractVideoId(src),
    O.fromNullable,
    O.map((id) => <YouTubeEmbed videoId={id} />),
    O.alt(() => O.some(<LightboxMessage message={youTubeVideoMissingId} />)),
  );

/**
 * Builds the embedded components used by the Lightbox for oEQ custom mimetypes (i.e. those starting
 * with `openequella/`). It assumes that the mimeType has already been checked to start with
 * `openequella`.
 *
 * @param mimeType The full MIME type pointing to a custom oEQ type - e.g. openequella/youtube.
 * @param src The src (typically a URL) for the item to embed.
 *
 * @return If a match for the `mimeType` is found a `JSX.Element` of either `src` or containing
 *         processing information will be returned. However if a MIME type match fails, then
 *         `O.none` is returned.
 */
export const buildCustomEmbed = (
  mimeType: string,
  src: string,
): O.Option<JSX.Element> =>
  pipe(
    mimeType,
    simpleMatchD<O.Option<JSX.Element>>(
      [
        [CustomMimeTypes.KALTURA, () => buildKaltura(src)],
        [CustomMimeTypes.YOUTUBE, () => buildYouTube(src)],
      ],
      () => O.none,
    ),
  );
