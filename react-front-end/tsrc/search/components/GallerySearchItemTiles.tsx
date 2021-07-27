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
import { GridListTile, GridListTileBar } from "@material-ui/core";
import { makeStyles } from "@material-ui/core/styles";
import * as OEQ from "@openequella/rest-api-client";
import * as React from "react";
import { useEffect, useState } from "react";
import { LightboxProps } from "../../components/Lightbox";
import { OEQItemSummaryPageButton } from "../../components/OEQItemSummaryPageButton";
import { createDrmDialog } from "../../drm/DrmHelper";
import { defaultDrmStatus } from "../../modules/DrmModule";
import {
  GalleryEntry,
  GallerySearchResultItem,
} from "../../modules/GallerySearchModule";
import {
  buildLightboxNavigationHandler,
  LightboxEntry,
} from "../../modules/ViewerModule";
import { languageStrings } from "../../util/langstrings";

const { ariaLabel, viewItem } = languageStrings.searchpage.gallerySearchResult;

const useStyles = makeStyles({
  tile: {
    width: "25%",
    height: 250,
    padding: 2,
  },
  titleBar: {
    background:
      "linear-gradient(to top, rgba(0,0,0,0.5) 0%, " +
      "rgba(0,0,0,0.1) 70%, rgba(0,0,0,0) 100%)",
  },
  thumbnail: {
    cursor: "zoom-in",
  },
});

export interface GallerySearchTileProps {
  /**
   * The Item to be displayed in the gallery.
   */
  item: GallerySearchResultItem;
  /**
   * A list of Lightbox viewable resources.
   */
  lightboxEntries: LightboxEntry[];
  /**
   * Function to determine whether to open Lightbox or not.
   *
   * @param props Props that will be passed to Lightbox, or `undefined` to close Lightbox.
   */
  setLightboxProps: (props: LightboxProps | undefined) => void;
}

/**
 * Component which builds a list of 'GridListTile' for all gallery entries of an Item.
 */
export const GallerySearchItemTiles = ({
  item: {
    mainEntry,
    additionalEntries,
    name,
    uuid,
    version,
    drmStatus: initialDrmStatus = defaultDrmStatus,
  },
  lightboxEntries,
  setLightboxProps,
}: GallerySearchTileProps) => {
  const classes = useStyles();
  const itemName = name ?? uuid;

  const [drmStatus, setDrmStatus] =
    useState<OEQ.Search.DrmStatus>(initialDrmStatus);
  const [drmCheckOnSuccessHandler, setDrmCheckOnSuccessHandler] = useState<
    (() => void) | undefined
  >();
  const [drmDialog, setDrmDialog] = useState<JSX.Element | undefined>(
    undefined
  );

  const checkDrmPermission = (onSuccess: () => void) =>
    setDrmCheckOnSuccessHandler(() => onSuccess);

  useEffect(() => {
    setDrmDialog(
      createDrmDialog(
        uuid,
        version,
        drmStatus,
        setDrmStatus,
        () => setDrmCheckOnSuccessHandler(undefined),
        drmCheckOnSuccessHandler
      )
    );
  }, [drmCheckOnSuccessHandler, uuid, version, drmStatus]);

  // Used to build the onClick event handler for each tile.
  const lightboxHandler = ({
    mimeType,
    directUrl: src,
    name,
    id,
  }: GalleryEntry) => {
    const initialLightboxEntryIndex = lightboxEntries.findIndex(
      (entry) => entry.id === id
    );

    return setLightboxProps({
      onClose: () => setLightboxProps(undefined),
      open: true,
      item: {
        uuid,
        version,
      },
      config: {
        src,
        title: name,
        mimeType,
        onNext: buildLightboxNavigationHandler(
          lightboxEntries,
          initialLightboxEntryIndex + 1,
          true
        ),
        onPrevious: buildLightboxNavigationHandler(
          lightboxEntries,
          initialLightboxEntryIndex - 1,
          true
        ),
      },
    });
  };

  // Build a click event handler for each tile.
  const buildOnClickHandler = (entry: GalleryEntry) => () => {
    checkDrmPermission(() => lightboxHandler(entry));
  };

  const buildTile = (
    key: string,
    imgSrc: string,
    altText: string,
    onClick: () => void
  ) => (
    <GridListTile
      key={key}
      onClick={onClick}
      aria-label={ariaLabel}
      className={classes.tile}
    >
      <img className={classes.thumbnail} src={imgSrc} alt={altText} />
      <GridListTileBar
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
    </GridListTile>
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
    <>
      {tiles}
      {drmDialog}
    </>
  );
};
