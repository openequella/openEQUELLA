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
import { ImageListItem, ImageListItemBar } from "@mui/material";
import { styled } from "@mui/material/styles";
import * as OEQ from "@openequella/rest-api-client";
import * as React from "react";
import { useEffect, useState } from "react";
import { OEQItemSummaryPageButton } from "../../components/OEQItemSummaryPageButton";
import { createDrmDialog } from "../../drm/DrmHelper";
import { defaultDrmStatus } from "../../modules/DrmModule";
import {
  GalleryEntry,
  GallerySearchResultItem,
} from "../../modules/GallerySearchModule";
import { languageStrings } from "../../util/langstrings";

const PREFIX = "GallerySearchItemTiles";

const classes = {
  tile: `${PREFIX}-tile`,
  titleBar: `${PREFIX}-titleBar`,
  thumbnail: `${PREFIX}-thumbnail`,
};

// TODO jss-to-styled codemod: The Fragment root was replaced by div. Change the tag if needed.
const Root = styled("div")({
  [`& .${classes.tile}`]: {
    width: "25%",
    height: 250,
    padding: 2,
  },
  [`& .${classes.titleBar}`]: {
    background:
      "linear-gradient(to top, rgba(0,0,0,0.5) 0%, " +
      "rgba(0,0,0,0.1) 70%, rgba(0,0,0,0) 100%)",
  },
  [`& .${classes.thumbnail}`]: {
    cursor: "zoom-in",
  },
});

const { ariaLabel, viewItem } = languageStrings.searchpage.gallerySearchResult;

/**
 * Type for the handler of opening Lightbox from Gallery.
 */
export type LightboxHandler = (
  /**
   * @param uuid Item's UUID.
   */
  uuid: string,
  /**
   * @param version Item's Version.
   */
  version: number,
  /**
   * @param entry A Gallery Entry to be viewed in the Lightbox.
   */
  entry: GalleryEntry
) => void;

export interface GallerySearchTileProps {
  /**
   * The Item to be displayed in the gallery.
   */
  item: GallerySearchResultItem;
  /**
   * Function to update the list of Items in GallerySearchResult and return a LightboxHandler. Typically used
   * as a callback of successfully accepting DRM terms.
   *
   * @param item Updated Item which typically only has DRM status changed.
   */
  updateGalleryItemList: (item: GallerySearchResultItem) => LightboxHandler;
}

/**
 * Component which builds a list of 'ImageListItem' for all gallery entries of an Item.
 */
export const GallerySearchItemTiles = ({
  item,
  updateGalleryItemList,
}: GallerySearchTileProps) => {
  const {
    mainEntry,
    additionalEntries,
    name,
    uuid,
    version,
    drmStatus: initialDrmStatus,
  } = item;
  const itemName = name ?? uuid;

  const [drmStatus, setDrmStatus] = useState<OEQ.Search.DrmStatus | undefined>(
    initialDrmStatus
  );
  const [drmCheckOnSuccessHandler, setDrmCheckOnSuccessHandler] = useState<
    (() => void) | undefined
  >();
  const [drmDialog, setDrmDialog] = useState<JSX.Element | undefined>(
    undefined
  );

  const checkDrmPermission = (onSuccess: () => void) =>
    setDrmCheckOnSuccessHandler(() => onSuccess);

  useEffect(() => {
    (async () => {
      const dialog = drmCheckOnSuccessHandler
        ? await createDrmDialog(
            uuid,
            version,
            drmStatus,
            setDrmStatus,
            () => setDrmCheckOnSuccessHandler(undefined),
            drmCheckOnSuccessHandler
          )
        : undefined;

      setDrmDialog(dialog);
    })();
  }, [drmCheckOnSuccessHandler, uuid, version, drmStatus]);

  // Build a click event handler for each tile.
  const buildOnClickHandler = (entry: GalleryEntry) => () => {
    checkDrmPermission(() => {
      const updatedItem = drmStatus
        ? { ...item, drmStatus: defaultDrmStatus }
        : item;
      updateGalleryItemList(updatedItem)(uuid, version, entry);
    });
  };

  const buildTile = (
    key: string,
    imgSrc: string,
    altText: string,
    onClick: () => void
  ) => (
    <ImageListItem
      key={key}
      onClick={onClick}
      aria-label={ariaLabel}
      className={classes.tile}
    >
      <img className={classes.thumbnail} src={imgSrc} alt={altText} />
      <ImageListItemBar
        className={classes.titleBar}
        actionIcon={
          <OEQItemSummaryPageButton
            title={viewItem}
            color="secondary"
            item={{ uuid, version }}
            checkDrmPermission={checkDrmPermission}
          />
        }
      />
    </ImageListItem>
  );

  const tiles = [
    buildTile(
      `${uuid}-mainEntry`,
      mainEntry.thumbnailLarge,
      `${itemName} - Main Entry (${mainEntry.name})`,
      buildOnClickHandler(mainEntry)
    ),
    additionalEntries.map((ae, idx) =>
      buildTile(
        `${uuid}-additionalEntry-${idx}`,
        ae.thumbnailLarge,
        `${itemName} - Additional Entry ${idx + 1} (${ae.name})`,
        buildOnClickHandler(ae)
      )
    ),
  ];

  return (
    <Root>
      {tiles}
      {drmDialog}
    </Root>
  );
};
