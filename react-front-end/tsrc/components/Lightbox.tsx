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
import { Backdrop, Grid, Toolbar, Typography } from "@mui/material";
import { styled } from "@mui/material/styles";
import CloseIcon from "@mui/icons-material/Close";
import CodeIcon from "@mui/icons-material/Code";
import NavigateBeforeIcon from "@mui/icons-material/NavigateBefore";
import NavigateNextIcon from "@mui/icons-material/NavigateNext";
import OpenInNewIcon from "@mui/icons-material/OpenInNew";
import { pipe } from "fp-ts/function";
import * as O from "fp-ts/Option";
import * as React from "react";
import {
  ReactElement,
  SyntheticEvent,
  useEffect,
  useRef,
  useState,
} from "react";
import {
  CustomMimeTypes,
  isBrowserSupportedAudio,
  isBrowserSupportedVideo,
  splitMimeType,
} from "../modules/MimeTypesModule";
import { languageStrings } from "../util/langstrings";
import { simpleMatch } from "../util/match";
import { EmbedCodeDialog } from "./EmbedCodeDialog";
import { buildCustomEmbed } from "./LightboxHelper";
import LightboxMessage from "./LightboxMessage";
import { OEQItemSummaryPageButton } from "./OEQItemSummaryPageButton";
import { TooltipIconButton } from "./TooltipIconButton";

const {
  common: {
    action: { close: labelClose, openInNewTab: labelOpenInNewTab },
  },
  lightboxComponent: {
    viewNext: labelViewNext,
    viewPrevious: labelViewPrevious,
    openSummaryPage: labelOpenSummaryPage,
    unsupportedContent: labelUnsupportedContent,
  },
  embedCode: { copy: labelCopyEmbedCode },
} = languageStrings;

const PREFIX = "Lightbox";

const classes = {
  lightboxBackdrop: `${PREFIX}-lightboxBackdrop`,
  lightboxAudio: `${PREFIX}-lightboxAudio`,
  lightboxContent: `${PREFIX}-lightboxContent`,
  lightboxImage: `${PREFIX}-lightboxImage`,
  menuButton: `${PREFIX}-menuButton`,
  title: `${PREFIX}-title`,
  toolbar: `${PREFIX}-toolbar`,
  arrowButton: `${PREFIX}-arrowButton`,
};

const Root = styled("div")(({ theme }) => ({
  [`& .${classes.lightboxBackdrop}`]: {
    backgroundColor: "#000000cc",
    cursor: "default", // Replace the backdrop 'pointer' style
    zIndex: theme.zIndex.drawer + 1,
  },

  [`& .${classes.lightboxAudio}`]: {
    minWidth: 200,
    width: "60vw",
  },

  [`& .${classes.lightboxContent}`]: {
    maxWidth: "80vw",
    maxHeight: "80vh",
  },

  [`& .${classes.lightboxImage}`]: {
    minWidth: 100,
    minHeight: 100,
  },

  [`& .${classes.menuButton}`]: {
    color: "#fafafa",
    "&:hover": {
      background: "#505050",
    },
  },

  [`& .${classes.title}`]: {
    flexGrow: 1,
  },

  [`& .${classes.toolbar}`]: {
    backgroundColor: "#0a0a0a",
    color: "#fafafa",
    position: "absolute",
    top: 0,
    width: "100%",
  },

  [`& .${classes.arrowButton}`]: {
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
  /**
   * Optional details for an item which (if present) are used to display a button which when
   * clicked with navigate the user to the item's summary page.
   */
  item?: {
    /**
     * UUID of the Item.
     */
    uuid: string;
    /**
     * Version of the Item.
     */
    version: number;
  };
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

const domParser = new DOMParser();

const Lightbox = ({ open, onClose, config }: LightboxProps) => {
  const [content, setContent] = useState<ReactElement | undefined>();
  const [lightBoxConfig, setLightBoxConfig] = useState<LightboxConfig>(config);
  const [openEmbedCodeDialog, setOpenEmbedCodeDialog] =
    useState<boolean>(false);

  const { src, title, mimeType, onPrevious, onNext, item } = lightBoxConfig;

  const handleNav = (getLightboxConfig: () => LightboxConfig) => {
    setContent(undefined);
    setLightBoxConfig(getLightboxConfig());
  };

  const contentEmbedCodeRef = useRef<HTMLDivElement>(null);

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
    const unsupportedContent = (
      <LightboxMessage message={labelUnsupportedContent} />
    );

    const buildContent = (): JSX.Element =>
      pipe(
        splitMimeType(mimeType)[0],
        simpleMatch<JSX.Element>({
          image: () => (
            <img
              className={`${classes.lightboxContent} ${classes.lightboxImage}`}
              alt={title}
              src={src}
            />
          ),
          video: () =>
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
          audio: () =>
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
          // same as OEQ_MIMETYPE_TYPE but unable to use with simpleMatch :'(
          openequella: () =>
            pipe(
              buildCustomEmbed(mimeType, src),
              O.getOrElse(() => unsupportedContent)
            ),
          _: () => unsupportedContent,
        })
      );

    setContent(buildContent());
  }, [lightBoxConfig, mimeType, src, title]);

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
  const generateEmbedCode = (): string => {
    const currentContent = contentEmbedCodeRef.current;
    if (!currentContent) {
      throw new Error("Failed to generate embed code for empty content");
    }

    const unneededAttributes = ["class"];
    const fullHtml: HTMLDocument = domParser.parseFromString(
      currentContent.innerHTML,
      "text/html"
    );
    const contentHtml = fullHtml.body.childNodes[0] as HTMLElement;
    unneededAttributes.forEach((a) => contentHtml.removeAttribute(a));

    return contentHtml.outerHTML;
  };

  return (
    <Root>
      <Backdrop
        className={classes.lightboxBackdrop}
        open={open}
        onClick={handleCloseLightbox}
      >
        <Toolbar className={classes.toolbar}>
          <Typography variant="h6" className={classes.title}>
            {title}
          </Typography>
          {item && (
            <OEQItemSummaryPageButton
              {...{ item, title: labelOpenSummaryPage, color: "inherit" }}
            />
          )}
          <TooltipIconButton
            title={labelCopyEmbedCode}
            color="inherit"
            className={classes.menuButton}
            aria-label={labelCopyEmbedCode}
            onClick={(event) => {
              event.stopPropagation();
              setOpenEmbedCodeDialog(true);
            }}
          >
            <CodeIcon />
          </TooltipIconButton>
          <TooltipIconButton
            title={labelOpenInNewTab}
            color="inherit"
            className={classes.menuButton}
            aria-label={labelOpenInNewTab}
            onClick={handleOpenInNewWindow}
          >
            <OpenInNewIcon />
          </TooltipIconButton>
          {
            // This following close button is really just added as a security blanket. A common thing
            // with most lightboxes which typically support clicking anywhere outside the content to
            // trigger a close.
          }
          <TooltipIconButton
            title={labelClose}
            color="inherit"
            className={classes.menuButton}
            aria-label={labelClose}
            onClick={handleCloseLightbox}
          >
            <CloseIcon />
          </TooltipIconButton>
        </Toolbar>
        <Grid container alignItems="center">
          <Grid item xs={1}>
            {onPrevious && (
              <TooltipIconButton
                title={labelViewPrevious}
                onClick={(e) => {
                  e.stopPropagation();
                  handleNav(onPrevious);
                }}
              >
                <NavigateBeforeIcon className={classes.arrowButton} />
              </TooltipIconButton>
            )}
          </Grid>
          <Grid item container justifyContent="center" xs={10}>
            <Grid item ref={contentEmbedCodeRef}>
              {content}
            </Grid>
          </Grid>
          <Grid item container justifyContent="flex-end" xs={1}>
            <Grid item>
              {onNext && (
                <TooltipIconButton
                  title={labelViewNext}
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
            onCloseDialog={() => setOpenEmbedCodeDialog(false)}
            embedCode={generateEmbedCode()}
          />
        )}
      </Backdrop>
    </Root>
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
  [CustomMimeTypes.KALTURA, CustomMimeTypes.YOUTUBE].includes(mimeType);

export default Lightbox;
