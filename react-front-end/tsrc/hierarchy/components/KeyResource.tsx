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
import { PushPin } from "@mui/icons-material";
import {
  Box,
  Card,
  CardContent,
  Chip,
  ListItem,
  ListItemText,
  Tooltip,
} from "@mui/material";
import { styled } from "@mui/material/styles";
import * as OEQ from "@openequella/rest-api-client";
import { pipe } from "fp-ts/function";
import * as O from "fp-ts/Option";
import * as React from "react";
import { useEffect, useState } from "react";
import { useHistory } from "react-router";
import { OEQLink } from "../../components/OEQLink";
import OEQThumb, { classes as thumbClasses } from "../../components/OEQThumb";
import { TooltipIconButton } from "../../components/TooltipIconButton";
import { createDrmDialog } from "../../drm/DrmHelper";
import { defaultDrmStatus } from "../../modules/DrmModule";
import { Root } from "../../search/components/SearchResult";
import { buildOpenSummaryPageHandler } from "../../search/SearchPageHelper";
import { languageStrings } from "../../util/langstrings";

const {
  removeKeyResource: removeKeyResourceText,
  attachmentCount: attachmentCountText,
} = languageStrings.hierarchy;

export interface KeyResourceProps {
  /**
   * The key resource to display.
   */
  keyResource: OEQ.BrowseHierarchy.KeyResource;
  /**
   * The handler for the pin icon click event. Hide the pin icon if it's undefined.
   */
  onPinIconClick?: (keyResource: OEQ.BrowseHierarchy.KeyResource) => void;
}

const PREFIX = "KeyResource";

export const classes = {
  container: `${PREFIX}-container`,
  thumbnail: `${PREFIX}-thumbnail`,
  info: `${PREFIX}-info`,
  action: `${PREFIX}-action`,
  attachmentCount: `${PREFIX}-attachmentCount`,
};
// The height of the card content before a thumbnail is added.
const thumbSize = 138;
export const StyledCard = styled(Card, {
  shouldForwardProp: (prop) => prop !== "hidePinIcon",
})<{ hidePinIcon?: boolean }>(({ hidePinIcon }) => {
  const baseActionCss = {
    display: "flex",
    alignItems: "center",
    flexDirection: "column",
  };

  const actionCss = hidePinIcon
    ? {
        ...baseActionCss,
        justifyContent: "flex-end",
        // Same as the margin of the pin icon.
        marginRight: "8px",
      }
    : {
        ...baseActionCss,
        justifyContent: "space-between",
      };

  return {
    [`&`]: {
      // Make sure every card has the same height in the row.
      height: "100%",
    },
    [`& .${classes.container}`]: {
      display: "flex",
      height: "100%",
    },
    // Override the default OEQ thumb root element style.
    [`& .${thumbClasses.root}`]: {
      width: thumbSize,
      height: thumbSize,
      marginRight: 0,
      display: "flex",
      alignItems: "center",
    },
    // Override the default thumbnail style.
    [`& .${thumbClasses.root} .${thumbClasses.thumbnail}`]: {
      width: thumbSize,
      objectFit: "cover",
      objectPosition: "center",
    },
    // Override the default OEQ thumb img style.
    [`& .${thumbClasses.root} img`]: {
      marginRight: 0,
      height: "100%",
    },
    [`& .${classes.info}`]: {
      flexGrow: "1",
      // Need a width property to make text ellipsis work.
      minWidth: thumbSize,
    },
    [`& .${classes.action}`]: actionCss,
    [`& .${classes.attachmentCount}`]: {
      // Ensure its style is consistent with the pin icon.
      marginBottom: "8px",
    },
  };
});

/**
 * A card view of a key resource for hierarchy.
 */
const KeyResource = ({ keyResource, onPinIconClick }: KeyResourceProps) => {
  const history = useHistory();

  const {
    uuid,
    version,
    thumbnailDetails,
    drmStatus: initialDrmStatus = defaultDrmStatus,
    attachmentCount,
    name,
    description,
    displayOptions,
  } = keyResource.item;

  const [showDrmDialog, setShowDrmDialog] = useState(false);
  const [drmDialog, setDrmDialog] = useState<React.JSX.Element | undefined>(
    undefined,
  );
  const [drmStatus, setDrmStatus] =
    useState<OEQ.Search.DrmStatus>(initialDrmStatus);

  useEffect(() => {
    (async () => {
      const { onClick } = buildOpenSummaryPageHandler(uuid, version, history);

      // If there is nothing requiring DRM permission check then return undefined.
      const dialog = showDrmDialog
        ? await createDrmDialog(
            uuid,
            version,
            drmStatus,
            setDrmStatus,
            () => setShowDrmDialog(false),
            onClick,
          )
        : undefined;
      setDrmDialog(dialog);
    })();
  }, [uuid, version, drmStatus, history, showDrmDialog]);

  const itemLink = () => {
    const itemTitle = name ?? uuid;
    const { url } = buildOpenSummaryPageHandler(uuid, version, history);

    return (
      <OEQLink
        routeLinkUrlProvider={() => url}
        muiLinkUrlProvider={() => url}
        onClick={(e: React.MouseEvent<HTMLAnchorElement>) =>
          pipe(
            drmStatus.isAllowSummary
              ? O.none
              : O.of(() => setShowDrmDialog(true)),
            O.map((handler) => {
              e.preventDefault();
              return handler();
            }),
          )
        }
      >
        {itemTitle}
      </OEQLink>
    );
  };

  const itemInfo = () => (
    <Root>
      <ListItem alignItems="flex-start">
        <ListItemText
          primary={itemLink()}
          secondary={description}
          primaryTypographyProps={{
            color: "primary",
            variant: "h6",
            sx: {
              overflow: "hidden",
              whiteSpace: "nowrap",
              textOverflow: "ellipsis",
            },
          }}
          secondaryTypographyProps={{
            sx: {
              fontSize: "1rem",
              overflow: "hidden",
              display: "-webkit-box",
              WebkitLineClamp: "2",
              // It's deprecated but is the only way to make `WebkitLineClamp` to work.
              WebkitBoxOrient: "vertical",
            },
          }}
        />
      </ListItem>
      {drmDialog}
    </Root>
  );

  return (
    <StyledCard hidePinIcon={onPinIconClick === undefined}>
      <Box className={classes.container}>
        <OEQThumb
          details={
            displayOptions?.disableThumbnail ? undefined : thumbnailDetails
          }
          large
        />

        <CardContent className={classes.info}>{itemInfo()}</CardContent>

        <Box className={classes.action}>
          {onPinIconClick ? (
            <TooltipIconButton
              id={`${uuid}-${version}-unpin`}
              title={removeKeyResourceText}
              onClick={() => onPinIconClick(keyResource)}
              aria-label={removeKeyResourceText}
            >
              <PushPin color="secondary" />
            </TooltipIconButton>
          ) : undefined}

          {attachmentCount > 0 && (
            <Tooltip
              className={classes.attachmentCount}
              title={attachmentCountText}
              aria-label={attachmentCountText}
            >
              <Chip label={attachmentCount} size="small" color="primary" />
            </Tooltip>
          )}
        </Box>
      </Box>
    </StyledCard>
  );
};

export default KeyResource;
