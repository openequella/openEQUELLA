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
import {
  Backdrop,
  Card,
  CardContent,
  Grid,
  IconButton,
  Theme,
  Toolbar,
  Typography,
} from "@material-ui/core";
import { makeStyles } from "@material-ui/core/styles";
import CloseIcon from "@material-ui/icons/Close";
import OpenInNewIcon from "@material-ui/icons/OpenInNew";
import * as React from "react";
import { ReactNode, SyntheticEvent, useEffect, useState } from "react";
import { Literal, match, Unknown } from "runtypes";
import {
  isBrowserSupportedAudio,
  isBrowserSupportedVideo,
  splitMimeType,
} from "../modules/MimeTypesModule";
import { languageStrings } from "../util/langstrings";
import NavigateBeforeIcon from "@material-ui/icons/NavigateBefore";
import NavigateNextIcon from "@material-ui/icons/NavigateNext";
import { TooltipIconButton } from "./TooltipIconButton";

const useStyles = makeStyles((theme: Theme) => ({
  lightboxBackdrop: {
    backgroundColor: "#000000cc",
    cursor: "default", // Replace the backdrop 'pointer' style
    zIndex: theme.zIndex.drawer + 1,
  },
  lightboxAudio: {
    minWidth: 200,
    width: "60vw",
  },
  lightboxContent: {
    maxWidth: "80vw",
    maxHeight: "80vh",
  },
  lightboxImage: {
    minWidth: 100,
    minHeight: 100,
  },
  menuButton: {
    color: "inherit",
    "&:hover": {
      background: "#505050",
    },
  },
  title: {
    flexGrow: 1,
  },
  toolbar: {
    backgroundColor: "#0a0a0a",
    color: "#fafafa",
    position: "absolute",
    top: 0,
    width: "100%",
  },
  arrowButton: {
    width: 48,
    height: 48,
    color: "#fafafa",
  },
}));

export interface LightboxAttachment {
  /** URL for the item to display in the Lightbox. */
  src: string;
  /** Title to display at the top of the Lightbox. */
  title?: string;
  /** MIME type of the items specified by `src` */
  mimeType: string;
}

export interface LightboxConfig {
  /**
   * The attachment displayed in Lightbox.
   */
  attachment: LightboxAttachment;
  /**
   * Function fired to view previous attachment.
   */
  onPrevious?: () => LightboxConfig;
  /**
   * Function fired to view next attachment.
   */
  onNext?: () => LightboxConfig;
}
export interface LightboxProps {
  /** Function to call when the Lightbox is closing. */
  onClose: () => void;
  /** Control whether to hide (`false`) or show (`true`) the Lightbox. */
  open: boolean;
  config: LightboxConfig;
}

const {
  viewNext: viewNextString,
  viewPrevious: viewPreviousString,
} = languageStrings.lightboxComponent;

const Lightbox = ({ open, onClose, config }: LightboxProps) => {
  const classes = useStyles();
  const {
    close: labelClose,
    openInNewWindow: labelOpenInNewWindow,
  } = languageStrings.common.action;
  const {
    unsupportedContent: labelUnsupportedContent,
  } = languageStrings.lightboxComponent;

  const [content, setContent] = useState<ReactNode | undefined>();
  const [lightBoxConfig, setLightBoxConfig] = useState<LightboxConfig>(config);
  const { attachment, onPrevious, onNext } = lightBoxConfig;
  const { src, title, mimeType } = attachment;

  const handleOnPrevious = () => {
    setContent(undefined);
    onPrevious && setLightBoxConfig(onPrevious());
  };

  const handleOnNext = () => {
    setContent(undefined);
    onNext && setLightBoxConfig(onNext());
  };

  useEffect(() => {
    const keyDownHandler = (e: KeyboardEvent) => {
      if (e.key === "ArrowLeft") {
        handleOnPrevious();
      } else if (e.key === "ArrowRight") {
        handleOnNext();
      } else if (e.key === "Escape") {
        onClose();
      }
    };
    window.addEventListener("keydown", keyDownHandler);
    return () => {
      window.removeEventListener("keydown", keyDownHandler);
    };
  }, [onPrevious, onNext, onClose]);

  // Update content when config is updated.
  useEffect(() => {
    setContent(buildContent());
  }, [lightBoxConfig]);

  const unsupportedContent = (
    <Card>
      <CardContent>
        <Typography variant="h5" component="h2">
          {labelUnsupportedContent}
        </Typography>
      </CardContent>
    </Card>
  );

  const buildContent = () =>
    match(
      [
        Literal("image"),
        () => (
          <img
            className={`${classes.lightboxContent} ${classes.lightboxImage}`}
            alt={title}
            src={src}
            onLoad={() => console.log("ok")}
          />
        ),
      ],
      [
        Literal("video"),
        () =>
          isBrowserSupportedVideo(mimeType) ? (
            <video
              className={classes.lightboxContent}
              controls
              src={src}
              aria-label={title}
            />
          ) : (
            unsupportedContent
          ),
      ],
      [
        Literal("audio"),
        () =>
          isBrowserSupportedAudio(mimeType) ? (
            <audio
              className={classes.lightboxAudio}
              controls
              src={src}
              aria-label={title}
            />
          ) : (
            unsupportedContent
          ),
      ],
      [Unknown, () => unsupportedContent]
    )(splitMimeType(mimeType)[0]);

  const handleOpenInNewWindow = (event: SyntheticEvent) => {
    event.stopPropagation();
    window.open(src, "_blank")?.focus();
  };

  const handleCloseLightbox = (event: SyntheticEvent) => {
    event.stopPropagation();
    onClose();
  };

  return (
    <Backdrop
      className={classes.lightboxBackdrop}
      open={open}
      onClick={handleCloseLightbox}
    >
      <Toolbar className={classes.toolbar}>
        <Typography variant="h6" className={classes.title}>
          {title}
        </Typography>
        <IconButton
          className={classes.menuButton}
          aria-label={labelOpenInNewWindow}
          onClick={handleOpenInNewWindow}
        >
          <OpenInNewIcon />
        </IconButton>
        {
          // This following close button is really just added as a security blanket. A common thing
          // with most lightboxes which typically support clicking anywhere outside the content to
          // trigger a close.
        }
        <IconButton
          className={classes.menuButton}
          aria-label={labelClose}
          onClick={handleCloseLightbox}
        >
          <CloseIcon />
        </IconButton>
      </Toolbar>
      <Grid container alignItems="center">
        <Grid item xs={1}>
          {onPrevious && (
            <TooltipIconButton
              title={viewPreviousString}
              onClick={(e) => {
                e.stopPropagation();
                handleOnPrevious();
              }}
            >
              <NavigateBeforeIcon className={classes.arrowButton} />
            </TooltipIconButton>
          )}
        </Grid>
        <Grid item container justify="center" xs={10}>
          <Grid item>{content}</Grid>
        </Grid>
        <Grid item container justify="flex-end" xs={1}>
          <Grid item>
            {onNext && (
              <TooltipIconButton
                title={viewNextString}
                onClick={(e) => {
                  e.stopPropagation();
                  handleOnNext();
                }}
              >
                <NavigateNextIcon className={classes.arrowButton} />
              </TooltipIconButton>
            )}
          </Grid>
        </Grid>
      </Grid>
    </Backdrop>
  );
};

/**
 * Validate if the supplied `mimeType` is one which the `<Lightbox>` component
 * can display. This can be used to minimise the need to add the `<Lightbox>`
 * to the component tree if it's not even going to work.
 *
 * @param mimeType a string of format `<type>/<subtype>` to check
 */
export const isLightboxSupportedMimeType = (mimeType: string): boolean =>
  splitMimeType(mimeType)[0] === "image" ||
  isBrowserSupportedAudio(mimeType) ||
  isBrowserSupportedVideo(mimeType);

export default Lightbox;
