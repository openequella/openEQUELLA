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
/**
 * OEQThumb component
 * Takes an OEQ.Search.Attachment object as a parameter.
 * The thumbnail will be served up from oEQ if the attachment has had a thumbnail generated (Image, video, pdf based files, Youtube, Flickr, Goodle books)
 * Otherwise, an appropriate Icon is returned
 */
export default function OEQThumb({
  mimeType,
  attachmentType,
  hasGeneratedThumb,
  description,
  links,
}: OEQ.Search.Attachment) {
  const classes = useStyles();
  const generalThumbStyles: ThumbProps = {
    className: `MuiPaper-elevation1 MuiPaper-rounded ${classes.thumbnail} ${classes.placeholderThumbnail}`,
    fontSize: "large",
  };
  let oeqThumb: React.ReactElement = (
    <DefaultFileIcon {...generalThumbStyles} />
  );

  const oeqProvidedThumb: React.ReactElement = (
    <img
      className={`MuiPaper-elevation1 MuiPaper-rounded ${classes.thumbnail}`}
      src={links.thumbnail}
      alt={description}
    />
  );
  /**
   * @param {string} mimeType - Attachment's Mimetype. eg. image/png application/pdf
   * @return {ReactElement} Image, video and pdf based mimetypes are thumbnailed by oEQ.
   * We need to check if a thumbnail has been generated, and return a generic icon if not
   */
  const handleMimeType = (mimeType?: string): React.ReactElement => {
    let result = <DefaultFileIcon {...generalThumbStyles} />;
    if (
      mimeType?.startsWith("image") ||
      mimeType?.startsWith("video") ||
      mimeType?.endsWith("/pdf")
    ) {
      if (hasGeneratedThumb) {
        result = oeqProvidedThumb;
      } else {
        if (mimeType?.startsWith("image")) {
          result = <ImageIcon {...generalThumbStyles} />;
        } else if (mimeType?.startsWith("video")) {
          result = <VideoIcon {...generalThumbStyles} />;
        }
      }
    }
    return result;
  };

  switch (attachmentType) {
    case "file": {
      oeqThumb = handleMimeType(mimeType);
      break;
    }
    case "link": {
      oeqThumb = <LinkIcon {...generalThumbStyles} />;
      break;
    }
    case "custom/flickr": {
      oeqThumb = oeqProvidedThumb;
      break;
    }
    case "custom/youtube": {
      oeqThumb = oeqProvidedThumb;
      break;
    }
    case "custom/googlebook": {
      oeqThumb = oeqProvidedThumb;
      break;
    }
    case "html": {
      oeqThumb = <WebIcon {...generalThumbStyles} />;
      break;
    }
  }
  return oeqThumb;
}
