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
import Share from "@mui/icons-material/Share";
import { styled } from "@mui/material/styles";
import * as OEQ from "@openequella/rest-api-client";
import * as React from "react";
import { useEffect, useState } from "react";
import { OEQItemSummaryPageButton } from "../../components/OEQItemSummaryPageButton";
import { TooltipIconButton } from "../../components/TooltipIconButton";
import { createDrmDialog } from "../../drm/DrmHelper";
import { defaultDrmStatus } from "../../modules/DrmModule";
import {
  GalleryEntry,
  GallerySearchResultItem,
} from "../../modules/GallerySearchModule";
import type { BasicSearchResultItem } from "../../modules/SearchModule";
import { languageStrings } from "../../util/langstrings";

const { ariaLabel, viewItem } = languageStrings.searchpage.gallerySearchResult;

const GalleryThumbnail = styled("img")({
  cursor: "zoom-in",
  objectFit: "cover",
  objectPosition: "center",
  height: 250,
  width: "100%",
});

/**
 * Type for the handler of opening Lightbox from Gallery.
 */
export type LightboxHandler = (
  /**
   * @param item Details of the item to which the Lightbox content belongs.
   */
  item: BasicSearchResultItem,
  /**
   * @param entry A Gallery Entry to be viewed in the Lightbox.
   */
  entry: GalleryEntry,
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
  /**
   * Handler for sharing the viewable Attachment of a Gallery entry.
   */
  onShare: (entry: GalleryEntry) => void;
  /**
   * `true` to show an Info icon in the ImageListItem for accessing Item summary page.
   */
  enableItemSummaryButton?: boolean;
}

/**
 * Component which builds a list of 'ImageListItem' for all gallery entries of an Item.
 */
export const GallerySearchItemTiles = ({
  item,
  updateGalleryItemList,
  onShare,
  enableItemSummaryButton = true,
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
    initialDrmStatus,
  );
  const [drmCheckOnSuccessHandler, setDrmCheckOnSuccessHandler] = useState<
    (() => void) | undefined
  >();
  const [drmDialog, setDrmDialog] = useState<JSX.Element | undefined>(
    undefined,
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
            drmCheckOnSuccessHandler,
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
      updateGalleryItemList(updatedItem)(item, entry);
    });
  };

  const buildTile = (entry: GalleryEntry, key: string, altText: string) => (
    <ImageListItem
      onClick={buildOnClickHandler(entry)}
      aria-label={ariaLabel}
      key={key}
    >
      <GalleryThumbnail src={entry.thumbnailLarge} alt={altText} />
      {enableItemSummaryButton && (
        <ImageListItemBar
          sx={{
            background:
              "linear-gradient(to top, rgba(0,0,0,0.5) 0%, " +
              "rgba(0,0,0,0.1) 70%, rgba(0,0,0,0) 100%)",
          }}
          actionIcon={
            <>
              <OEQItemSummaryPageButton
                title={viewItem}
                color="secondary"
                item={{ uuid, version }}
                checkDrmPermission={
                  drmStatus?.isAllowSummary ? undefined : checkDrmPermission
                }
              />
              <TooltipIconButton
                title={languageStrings.common.action.share}
                color="secondary"
                onClick={(event) => {
                  event.stopPropagation();
                  onShare(entry);
                }}
              >
                <Share />
              </TooltipIconButton>
            </>
          }
        />
      )}
    </ImageListItem>
  );

  const tiles = [
    buildTile(
      mainEntry,
      `${uuid}-mainEntry`,
      `${itemName} - Main Entry (${mainEntry.name})`,
    ),
    additionalEntries.map((ae, idx) =>
      buildTile(
        ae,
        `${uuid}-additionalEntry-${idx}`,
        `${itemName} - Additional Entry ${idx + 1} (${ae.name})`,
      ),
    ),
  ];

  return (
    <>
      {tiles}
      {drmDialog}
    </>
  );
};
