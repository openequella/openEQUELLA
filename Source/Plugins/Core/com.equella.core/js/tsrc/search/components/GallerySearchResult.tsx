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
import { GridList, GridListTile, GridListTileBar } from "@material-ui/core";
import { makeStyles } from "@material-ui/core/styles";
import InfoIcon from "@material-ui/icons/Info";
import * as React from "react";
import { useState } from "react";
import { useHistory } from "react-router";
import Lightbox, { LightboxProps } from "../../components/Lightbox";
import { TooltipIconButton } from "../../components/TooltipIconButton";
import { routes } from "../../mainui/routes";
import {
  GalleryEntry,
  GallerySearchResultItem,
} from "../../modules/GallerySearchModule";
import {
  buildSelectionSessionItemSummaryLink,
  isSelectionSessionOpen,
} from "../../modules/LegacySelectionSessionModule";
import { languageStrings } from "../../util/langstrings";

const { ariaLabel, viewItem } = languageStrings.searchpage.gallerySearchResult;

export interface GallerySearchResultProps {
  /**
   * The details of the items (and most importantly their thumbnails) to be displayed in the gallery.
   */
  items: GallerySearchResultItem[];
}

const useStyles = makeStyles({
  titleBar: {
    background:
      "linear-gradient(to top, rgba(0,0,0,0.5) 0%, " +
      "rgba(0,0,0,0.1) 70%, rgba(0,0,0,0) 100%)",
  },
});

/**
 * Displays the results of a search in a Gallery format rather than a list format (as provided
 * by `SearchResult` with `mapSearchResultItems`). Intended for use with Images and Videos -
 * possibly later audio.
 */
const GallerySearchResult = ({ items }: GallerySearchResultProps) => {
  const classes = useStyles();
  const history = useHistory();

  const [lightboxProps, setLightboxProps] = useState<
    LightboxProps | undefined
  >();

  // Done as a function, as when done as a component things don't work as expected.
  const buildTile = (
    itemUuid: string,
    itemVersion: number,
    key: string,
    imgSrc: string,
    altText: string,
    onClick: () => void
  ) => (
    <GridListTile key={key} onClick={onClick} aria-label={ariaLabel}>
      <img style={{ cursor: "zoom-in" }} src={imgSrc} alt={altText} />
      <GridListTileBar
        className={classes.titleBar}
        actionIcon={
          <TooltipIconButton
            color="secondary"
            title={viewItem}
            onClick={() =>
              history.push(
                isSelectionSessionOpen()
                  ? buildSelectionSessionItemSummaryLink(itemUuid, itemVersion)
                  : routes.ViewItem.to(itemUuid, itemVersion)
              )
            }
          >
            <InfoIcon />
          </TooltipIconButton>
        }
      />
    </GridListTile>
  );

  const mapItemsToTiles = () =>
    items.flatMap(
      ({
        additionalEntries,
        mainEntry,
        name,
        uuid,
        version,
      }: GallerySearchResultItem) => {
        const itemName = name ?? uuid;
        const buildOnClickHandler = ({
          mimeType,
          directUrl: src,
          name,
        }: GalleryEntry) => () =>
          setLightboxProps({
            onClose: () => setLightboxProps(undefined),
            open: true,
            config: {
              src,
              title: name,
              mimeType,
            },
          });

        return [
          buildTile(
            uuid,
            version,
            `${uuid}-mainEntry`,
            mainEntry.thumbnailLarge,
            `${itemName} - Main Entry (${mainEntry.name})`,
            buildOnClickHandler(mainEntry)
          ),
          additionalEntries.map((ae, idx) =>
            buildTile(
              uuid,
              version,
              `${uuid}-additionalEntry-${idx}`,
              ae.thumbnailLarge,
              `${itemName} - Additional Entry ${idx + 1} (${ae.name})`,
              buildOnClickHandler(ae)
            )
          ),
        ];
      }
    );

  return (
    <>
      <GridList cellHeight={250} cols={4}>
        {mapItemsToTiles()}
      </GridList>
      {lightboxProps && <Lightbox {...lightboxProps} />}
    </>
  );
};

export default GallerySearchResult;
