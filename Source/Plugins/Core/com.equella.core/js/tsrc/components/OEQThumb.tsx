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
import * as React from "react";
import { makeStyles } from "@material-ui/core/styles";
import { Theme } from "@material-ui/core";
import * as OEQ from "@openequella/rest-api-client";
import {
  Link as LinkIcon,
  Movie as VideoIcon,
  Panorama as ImageIcon,
  InsertDriveFile as DefaultFileIcon,
  Language as WebIcon,
  Subject as PlaceholderIcon,
} from "@material-ui/icons";

const useStyles = makeStyles((theme: Theme) => {
  return {
    thumbnail: {
      //if material UI changes such that the MuiPaper-elevation1 MuiPaper-rounded no longer map, add the rules here.
      marginRight: theme.spacing(2),
      width: "88px",
      height: "auto",
    },
    placeholderThumbnail: {
      color: theme.palette.text.secondary,
      opacity: 0.4,
      height: "66px",
    },
  };
});

interface ThumbProps {
  className: string;
  fontSize: "inherit" | "default" | "small" | "large";
}
interface OEQThumbProps {
  attachment?: OEQ.Search.Attachment;
  showPlaceholder: boolean;
}
/**
 * OEQThumb component
 * Takes an OEQ.Search.Attachment object as a parameter.
 * The thumbnail will be served up from oEQ if the attachment has had a thumbnail generated (Image, video, pdf based files, Youtube, Flickr, Google books)
 * Otherwise, an appropriate Icon is returned
 */
export default function OEQThumb({
  attachment,
  showPlaceholder,
}: OEQThumbProps) {
  const classes = useStyles();
  const generalThumbStyles: ThumbProps = {
    className: `MuiPaper-elevation1 MuiPaper-rounded ${classes.thumbnail} ${classes.placeholderThumbnail}`,
    fontSize: "large",
  };
  if (!attachment) {
    return <PlaceholderIcon {...generalThumbStyles} />;
  }

  const {
    description,
    mimeType,
    attachmentType,
    hasGeneratedThumb,
    links,
  } = attachment;

  const oeqProvidedThumb: React.ReactElement = (
    <img
      className={`MuiPaper-elevation1 MuiPaper-rounded ${classes.thumbnail}`}
      src={links.thumbnail}
      alt={description}
    />
  );

  /**
   * We need to check if a thumbnail has been generated, and return a generic icon if not
   * @param {string} mimeType - Attachment's Mimetype. eg. image/png application/pdf
   * @return {ReactElement} Image, video and pdf based mimetypes are thumbnailed by oEQ.
   */
  const handleMimeType = (mimeType?: string): React.ReactElement => {
    if (hasGeneratedThumb) {
      return oeqProvidedThumb;
    }
    let result = <DefaultFileIcon {...generalThumbStyles} />;
    if (mimeType?.startsWith("image")) {
      result = <ImageIcon {...generalThumbStyles} />;
    } else if (mimeType?.startsWith("video")) {
      result = <VideoIcon {...generalThumbStyles} />;
    }
    return result;
  };

  let oeqThumb: React.ReactElement = showPlaceholder ? (
    <PlaceholderIcon {...generalThumbStyles} />
  ) : (
    <DefaultFileIcon {...generalThumbStyles} />
  );

  switch (attachmentType) {
    case "file":
      oeqThumb = handleMimeType(mimeType);
      break;
    case "link":
      oeqThumb = <LinkIcon {...generalThumbStyles} />;
      break;
    case "html":
      oeqThumb = <WebIcon {...generalThumbStyles} />;
      break;
    case "custom/flickr":
    case "custom/youtube":
    case "custom/googlebook":
      oeqThumb = (
        <img
          className={`MuiPaper-elevation1 MuiPaper-rounded ${classes.thumbnail}`}
          src={links.thumbnail}
          alt={description}
        />
      );
      break;
  }
  return oeqThumb;
}
