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
import CodeIcon from "@material-ui/icons/Code";
import NavigateBeforeIcon from "@material-ui/icons/NavigateBefore";
import NavigateNextIcon from "@material-ui/icons/NavigateNext";
import OpenInNewIcon from "@material-ui/icons/OpenInNew";
import { pipe } from "fp-ts/function";
import * as O from "fp-ts/Option";
import * as React from "react";
import { ReactElement, SyntheticEvent, useEffect, useState } from "react";
import { renderToStaticMarkup } from "react-dom/server";
import { Literal, match, Unknown } from "runtypes";
import {
  CustomMimeTypes,
  isBrowserSupportedAudio,
  isBrowserSupportedVideo,
  OEQ_MIMETYPE_TYPE,
  splitMimeType,
} from "../modules/MimeTypesModule";
import { extractVideoId } from "../modules/YouTubeModule";
import { EmbedCodeDialog } from "../search/components/EmbedCodeDialog";
import { languageStrings } from "../util/langstrings";
import { TooltipIconButton } from "./TooltipIconButton";
import YouTubeEmbed from "./YouTubeEmbed";

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

export interface LightboxConfig {
  /** URL for the item to display in the Lightbox. */
  src: string;
  /** Title to display at the top of the Lightbox. */
  title?: string;
  /** MIME type of the items specified by `src` */
  mimeType: string;
  /** Function fired to view previous attachment. */
  onPrevious?: () => LightboxConfig;
  /** Function fired to view next attachment. */
  onNext?: () => LightboxConfig;
}

export interface LightboxProps {
  /** Function to call when the Lightbox is closing. */
  onClose: () => void;
  /** Control whether to hide (`false`) or show (`true`) the Lightbox. */
  open: boolean;
  /** Configuration specifying the Lightbox's content. */
  config: LightboxConfig;
}

const {
  viewNext: viewNextString,
  viewPrevious: viewPreviousString,
} = languageStrings.lightboxComponent;

const {
  close: labelClose,
  openInNewWindow: labelOpenInNewWindow,
} = languageStrings.common.action;

const {
  unsupportedContent: labelUnsupportedContent,
  youTubeVideoMissingId,
} = languageStrings.lightboxComponent;

const { copy: copyEmbedCodeString } = languageStrings.embedCode;

const domParser = new DOMParser();

const Lightbox = ({ open, onClose, config }: LightboxProps) => {
  const classes = useStyles();

  const [content, setContent] = useState<ReactElement | undefined>();
  const [lightBoxConfig, setLightBoxConfig] = useState<LightboxConfig>(config);
  const [openEmbedCodeDialog, setOpenEmbedCodeDialog] = useState<boolean>(
    false
  );
  const { src, title, mimeType, onPrevious, onNext } = lightBoxConfig;

  const handleNav = (getLightboxConfig: () => LightboxConfig) => {
    setContent(undefined);
    setLightBoxConfig(getLightboxConfig());
  };

  useEffect(() => {
    const keyDownHandler = (e: KeyboardEvent) => {
      if (onPrevious && e.key === "ArrowLeft") {
        handleNav(onPrevious);
      } else if (onNext && e.key === "ArrowRight") {
        handleNav(onNext);
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
    const lightBoxMessage = (msg: string) => (
      <Card>
        <CardContent>
          <Typography variant="h5" component="h2">
            {msg}
          </Typography>
        </CardContent>
      </Card>
    );

    const unsupportedContent = lightBoxMessage(labelUnsupportedContent);

    const buildContent = () =>
      pipe(
        splitMimeType(mimeType)[0],
        match(
          [
            Literal("image"),
            () => (
              <img
                className={`${classes.lightboxContent} ${classes.lightboxImage}`}
                alt={title}
                src={src}
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
          [
            Literal(OEQ_MIMETYPE_TYPE),
            () =>
              mimeType === CustomMimeTypes.YOUTUBE
                ? pipe(
                    extractVideoId(src),
                    O.fromNullable,
                    O.fold(
                      () => lightBoxMessage(youTubeVideoMissingId),
                      (id) => <YouTubeEmbed videoId={id} />
                    )
                  )
                : unsupportedContent,
          ],
          [Unknown, () => unsupportedContent]
        )
      );

    setContent(buildContent());
  }, [lightBoxConfig, classes, mimeType, src, title]);

  const handleOpenInNewWindow = (event: SyntheticEvent) => {
    event.stopPropagation();
    window.open(src, "_blank")?.focus();
  };

  const handleCloseLightbox = (event: SyntheticEvent) => {
    event.stopPropagation();
    onClose();
  };

  // Generate a HTML string from current Lightbox content.
  // Also use DOMParser to help remove unneeded attributes such as 'class'.
  const generateEmbedCode = (content: ReactElement): string => {
    const unneededAttributes = ["class"];
    const doc = domParser.parseFromString(
      renderToStaticMarkup(content),
      "text/xml"
    );
    unneededAttributes.forEach((a) => doc.documentElement.removeAttribute(a));

    return doc.documentElement.outerHTML;
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
          aria-label={copyEmbedCodeString}
          onClick={(event) => {
            event.stopPropagation();
            setOpenEmbedCodeDialog(true);
          }}
        >
          <CodeIcon />
        </IconButton>
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
                handleNav(onPrevious);
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
                  handleNav(onNext);
                }}
              >
                <NavigateNextIcon className={classes.arrowButton} />
              </TooltipIconButton>
            )}
          </Grid>
        </Grid>
      </Grid>
      {openEmbedCodeDialog && content && (
        <EmbedCodeDialog
          open={openEmbedCodeDialog}
          closeDialog={() => setOpenEmbedCodeDialog(false)}
          embedCode={generateEmbedCode(content)}
        />
      )}
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
  isBrowserSupportedVideo(mimeType) ||
  [CustomMimeTypes.YOUTUBE].includes(mimeType);

export default Lightbox;
