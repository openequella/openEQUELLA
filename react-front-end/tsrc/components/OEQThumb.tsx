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
import { styled } from "@mui/material/styles";
import DefaultFileIcon from "@mui/icons-material/InsertDriveFile";
import WebIcon from "@mui/icons-material/Language";
import LinkIcon from "@mui/icons-material/Link";
import VideoIcon from "@mui/icons-material/Movie";
import ImageIcon from "@mui/icons-material/Panorama";
import PlaceholderIcon from "@mui/icons-material/TextFields";
import Web from "@mui/icons-material/Web";
import * as OEQ from "@openequella/rest-api-client";
import { flow, pipe } from "fp-ts/function";
import * as React from "react";
import { languageStrings } from "../util/langstrings";
import * as O from "fp-ts/Option";
import { simpleMatch } from "../util/match";
import * as S from "fp-ts/string";
import * as RNEA from "fp-ts/ReadonlyNonEmptyArray";

const PREFIX = "OEQThumb";

const classes = {
  thumbnail: `${PREFIX}-thumbnail`,
  placeholderThumbnail: `${PREFIX}-placeholderThumbnail`,
};

const Root = styled("div")(({ theme }) => {
  return {
    [`& .${classes.thumbnail}`]: {
      //if material UI changes such that the MuiPaper-elevation1 MuiPaper-rounded no longer map, add the rules here.
      marginRight: theme.spacing(2),
      width: 88,
      height: "auto",
    },
    [`& .${classes.placeholderThumbnail}`]: {
      color: theme.palette.text.secondary,
      opacity: 0.4,
      height: 66,
    },
  };
});

interface ThumbProps {
  className: string;
  fontSize: "inherit" | "medium" | "large" | "small" | undefined;
}

export interface OEQThumbProps {
  /**
   * Details to determine what and how the thumbnail should be displayed. Or if none are provided
   * a simple placeholder icon will be used instead.
   *
   * If `details` includes a `link` then that will have priority over any others and be used to
   * display an image based thumbnail.
   */
  details?: OEQ.Search.ThumbnailDetails;
}

/**
 * Displays a standard thumbnail using either the details provided to display an appropriate icon or
 * image from the server. Of if no details provided will display a generic placeholder.
 */
export default function OEQThumb({ details }: OEQThumbProps) {
  const thumbLabels = languageStrings.searchpage.thumbnails;
  const generalThumbStyles: ThumbProps = {
    className: `MuiPaper-elevation1 MuiPaper-rounded ${classes.thumbnail} ${classes.placeholderThumbnail}`,
    fontSize: "large",
  };

  const defaultThumb = () => (
    <DefaultFileIcon aria-label={thumbLabels.file} {...generalThumbStyles} />
  );

  /**
   * Build an `img` based thumb if link is available, otherwise use the `defaultThumb`.
   *
   * @param link a URL to an image thumbnail on the server
   */
  const buildServerProvidedThumb = (link?: string): JSX.Element =>
    link ? (
      <img
        aria-label={thumbLabels.provided}
        className={`MuiPaper-elevation1 MuiPaper-rounded ${classes.thumbnail}`}
        src={link}
        alt={thumbLabels.provided}
      />
    ) : (
      defaultThumb()
    );

  /**
   * Builds generic thumbs for files based on the supplied `mimeType`.
   *
   * @param mimeType expects values like `image/jpeg` or `video/mpeg` etc.
   */
  const buildGenericFileThumb = (mimeType?: string): JSX.Element => {
    const mimeTypeBasedThumb = (type: string): JSX.Element =>
      pipe(
        type,
        simpleMatch<JSX.Element>({
          image: () => (
            <ImageIcon aria-label={thumbLabels.image} {...generalThumbStyles} />
          ),
          video: () => (
            <VideoIcon aria-label={thumbLabels.video} {...generalThumbStyles} />
          ),
          _: defaultThumb,
        })
      );

    return pipe(
      mimeType,
      O.fromNullable,
      O.map(flow(S.split("/"), RNEA.head, mimeTypeBasedThumb)),
      O.getOrElse(defaultThumb)
    );
  };

  const buildGenericResourceAttachmentThumb = (
    mimeType?: string
  ): JSX.Element =>
    pipe(
      mimeType,
      O.fromNullable,
      O.map(
        simpleMatch<JSX.Element>({
          "equella/item": () => (
            <Web aria-label={thumbLabels.item} {...generalThumbStyles} />
          ),
          "equella/link": () => (
            <LinkIcon aria-label={thumbLabels.link} {...generalThumbStyles} />
          ),
          "text/html": () => (
            <WebIcon aria-label={thumbLabels.html} {...generalThumbStyles} />
          ),
          _: defaultThumb,
        })
      ),
      O.getOrElse(defaultThumb)
    );

  const buildGenericThumb = (
    attachmentType: string,
    mimeType?: string
  ): JSX.Element =>
    pipe(
      attachmentType,
      simpleMatch<JSX.Element>({
        link: () => (
          <LinkIcon aria-label={thumbLabels.link} {...generalThumbStyles} />
        ),
        html: () => (
          <WebIcon aria-label={thumbLabels.html} {...generalThumbStyles} />
        ),
        file: () => buildGenericFileThumb(mimeType),
        "custom/resource": () => buildGenericResourceAttachmentThumb(mimeType),
        _: defaultThumb,
      })
    );

  return (
    <Root>
      {pipe(
        details,
        O.fromNullable,
        O.map(({ attachmentType, mimeType, link }) =>
          link
            ? buildServerProvidedThumb(link)
            : buildGenericThumb(attachmentType, mimeType)
        ),
        O.getOrElse(() => (
          <PlaceholderIcon
            aria-label={thumbLabels.placeholder}
            {...generalThumbStyles}
          />
        ))
      )}
    </Root>
  );
}
