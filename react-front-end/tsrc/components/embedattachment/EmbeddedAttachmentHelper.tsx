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

/**
 * This helper file provides functionality related to embedding OEQ Attachments such as
 * build a component for embedding the Attachment and generating embed code.
 */
import { Card, CardContent, Typography } from "@mui/material";
import * as E from "fp-ts/Either";
import { pipe } from "fp-ts/function";
import * as O from "fp-ts/Option";
import * as React from "react";
import {
  buildKalturaPlayerUrl,
  EXTERNAL_ID_PARAM,
  parseExternalId,
} from "../../modules/KalturaModule";
import {
  CustomMimeTypes,
  isBrowserSupportedAudio,
  isBrowserSupportedVideo,
  splitMimeType,
} from "../../modules/MimeTypesModule";
import { extractVideoId } from "../../modules/YouTubeModule";
import { languageStrings } from "../../util/langstrings";
import { simpleMatch, simpleMatchD } from "../../util/match";
import KalturaPlayerEmbed from "./KalturaPlayerEmbed";
import YouTubeEmbed from "./YouTubeEmbed";

const {
  kalturaExternalIdIssue,
  kalturaMissingId,
  contentNotSupported,
  youTubeVideoMissingId,
} = languageStrings.shareAttachment.error;

/**
 * Structure for the information of an Attachment to be shared.
 */
export interface ShareAttachment {
  /**
   * URL to view the Attachment.
   */
  src: string;
  /**
   * Optional HTML code for embedding the Attachment if browser supports embedding the attachment type.
   */
  embedCode: O.Option<string>;
}

// Component to display why embedding an attachment is failed.
const EmbedAttachmentFailure = ({
  reason,
}: {
  reason: string;
}): React.JSX.Element => (
  <Card>
    <CardContent>
      <Typography variant="h5" component="h2">
        {reason}
      </Typography>
    </CardContent>
  </Card>
);

const unsupportedContent = (
  <EmbedAttachmentFailure reason={contentNotSupported} />
);

// Build an HTML Element for embedding the Kaltura media.
const buildKaltura = (src: string): React.JSX.Element =>
  pipe(
    new URL(src).searchParams.get(EXTERNAL_ID_PARAM),
    E.fromNullable(kalturaMissingId),
    E.chain(
      E.tryCatchK(parseExternalId, (e) => {
        console.error("Failed to display Kaltura media: " + e);
        return kalturaExternalIdIssue;
      }),
    ),
    E.fold(
      (e) => <EmbedAttachmentFailure reason={e} />,
      (playerDetails) => <KalturaPlayerEmbed playerDetails={playerDetails} />,
    ),
  );

// Build an HTML Element for embedding the Youtube videos.
const buildYouTube = (src: string): React.JSX.Element =>
  pipe(
    extractVideoId(src),
    O.fromNullable,
    O.map((id) => <YouTubeEmbed videoId={id} />),
    O.getOrElse(() => (
      <EmbedAttachmentFailure reason={youTubeVideoMissingId} />
    )),
  );

/**
 * Builds the embedded components for oEQ custom mimetypes (i.e. those starting with `openequella/`).
 * It assumes that the mimeType has already been checked to start with `openequella`.
 *
 * @param mimeType The full MIME type pointing to a custom oEQ type - e.g. openequella/youtube.
 * @param src The src (typically a URL) for the item to embed.
 *
 * @return If a match for the `mimeType` is found, a `React.JSX.Element` of either `src` or containing
 *         processing information will be returned. However, if a MIME type match fails, then return a
 *         component showing that the provided content is unsupported.
 */
const customEmbeddedComponent = (
  mimeType: string,
  src: string,
): React.JSX.Element =>
  pipe(
    mimeType,
    simpleMatchD(
      [
        [CustomMimeTypes.KALTURA, () => buildKaltura(src)],
        [CustomMimeTypes.YOUTUBE, () => buildYouTube(src)],
      ],
      () => unsupportedContent,
    ),
  );

/**
 * Given an Attachment's MIME type and URL link, builds an embedded component for viewing the Attachment.
 * If browser doesn't support the MIME type or the build fails due to other reasons, return component to
 * display an error to the user.
 *
 * @param mimeType MIME type of the Attachment which determines what type of embedded attachment to build.
 * @param src Link to view the Attachment
 * @param title Optional title of the Attachment
 * @param classes Additional CSS classes to apply to the embedded attachment
 */
export const buildEmbeddedComponent = (
  mimeType: string,
  src: string,
  title?: string,
  classes?: string,
): React.JSX.Element =>
  pipe(
    splitMimeType(mimeType)[0],
    simpleMatch<React.JSX.Element>({
      image: () => <img className={classes} alt={title} src={src} />,
      video: () =>
        isBrowserSupportedVideo(mimeType) ? (
          <video className={classes} controls src={src} aria-label={title} />
        ) : (
          unsupportedContent
        ),
      audio: () =>
        isBrowserSupportedAudio(mimeType) ? (
          <audio className={classes} controls src={src} aria-label={title} />
        ) : (
          unsupportedContent
        ),
      openequella: () => customEmbeddedComponent(mimeType, src),
      _: () => unsupportedContent,
    }),
  );

// Kaltura media embed code which is constructed as per the "IFRAME" style.
const kalturaEmbedCode = (src: string): O.Option<string> =>
  pipe(
    new URL(src).searchParams.get(EXTERNAL_ID_PARAM),
    O.fromNullable,
    O.chainEitherK(
      E.tryCatchK(parseExternalId, (e) =>
        console.error("Failed to build Kaltura embed code: " + e),
      ),
    ),
    O.map((details) =>
      buildKalturaPlayerUrl(details, `kaltura_player_${Date.now()}`, "IFRAME"),
    ),
    O.map(
      (url) =>
        `<iframe width="560" height="395" src="${url}" allowfullscreen allow="autoplay *; fullscreen *; encrypted-media *" title="${languageStrings.kalturaPlayer.title}"></iframe>`,
    ),
  );

const youtubeEmbedCode = (src: string): O.Option<string> =>
  pipe(
    extractVideoId(src),
    O.fromNullable,
    O.map(
      (id) =>
        `<iframe width="560" height="315" src="https://www.youtube-nocookie.com/embed/${id}" title="${languageStrings.youTubePlayer.title}" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture" allowfullscreen></iframe>`,
    ),
  );

const customEmbedCode = (mimeType: string, src: string): O.Option<string> =>
  pipe(
    mimeType,
    simpleMatchD(
      [
        [CustomMimeTypes.KALTURA, () => kalturaEmbedCode(src)],
        [CustomMimeTypes.YOUTUBE, () => youtubeEmbedCode(src)],
      ],
      () => O.none,
    ),
  );

export const buildEmbedCode = (
  mimeType: string,
  src: string,
  title?: string,
): O.Option<string> =>
  pipe(
    splitMimeType(mimeType)[0],
    simpleMatch({
      image: () => O.of(`<img alt="${title}" src="${src}">`),
      video: () =>
        isBrowserSupportedVideo(mimeType)
          ? O.of(`<video controls src="${src}" aria-label="${title}"></video>`)
          : O.none,
      audio: () =>
        isBrowserSupportedAudio(mimeType)
          ? O.of(`<audio controls src="${src}" aria-label="${title}"></audio>`)
          : O.none,
      openequella: () => customEmbedCode(mimeType, src),
      _: () => O.none,
    }),
  );
