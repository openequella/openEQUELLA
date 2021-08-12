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
import { GridList } from "@material-ui/core";
import * as React from "react";
import { useEffect, useState } from "react";
import Lightbox, { LightboxProps } from "../../components/Lightbox";
import {
  GalleryEntry,
  GallerySearchResultItem,
} from "../../modules/GallerySearchModule";
import {
  buildLightboxNavigationHandler,
  LightboxEntry,
} from "../../modules/ViewerModule";
import {
  GallerySearchItemTiles,
  LightboxHandler,
} from "./GallerySearchItemTiles";

export interface GallerySearchResultProps {
  /**
   * The details of the items (and most importantly their thumbnails) to be displayed in the gallery.
   */
  items: GallerySearchResultItem[];
}

/**
 * Displays the results of a search in a Gallery format rather than a list format (as provided
 * by `SearchResult` with `mapSearchResultItems`). Intended for use with Images and Videos -
 * possibly later audio.
 */
const GallerySearchResult = ({ items }: GallerySearchResultProps) => {
  const [lightboxProps, setLightboxProps] = useState<
    LightboxProps | undefined
  >();

  const [galleryItems, setGalleryItems] =
    useState<GallerySearchResultItem[]>(items);

  // Ensure gallery entries are consistent with gallery mode.
  useEffect(() => {
    setGalleryItems(items);
  }, [items]);

  // Handler for opening the Lightbox
  const lightboxHandler = (
    lightboxEntries: LightboxEntry[],
    uuid: string,
    version: number,
    { mimeType, directUrl: src, name, id }: GalleryEntry
  ) => {
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

  // Function to update the Item list, rebuild the Lightbox entry list and return a lightboxHandler.
  const updateGalleryItemList = (
    newItem: GallerySearchResultItem
  ): LightboxHandler => {
    const updatedItems = galleryItems.map((i) =>
      i.uuid === newItem.uuid && i.version === newItem.version ? newItem : i
    );
    setGalleryItems(updatedItems);

    // A list of LightboxEntry which includes all main entries and additional entries.
    const lightboxEntries: LightboxEntry[] = updatedItems
      .filter(({ drmStatus }) => {
        if (drmStatus) {
          const { isAuthorised, termsAccepted } = drmStatus;
          return isAuthorised && termsAccepted;
        }
        // If not a DRM Item, keep it.
        return true;
      })
      .flatMap(({ mainEntry, additionalEntries }) =>
        [mainEntry, ...additionalEntries].map(
          ({ id, name, mimeType, directUrl }) => ({
            src: directUrl,
            title: name,
            mimeType: mimeType,
            id,
          })
        )
      );

    return (uuid: string, version: number, entry: GalleryEntry) =>
      lightboxHandler(lightboxEntries, uuid, version, entry);
  };

  const mapItemsToTiles = () =>
    items.map((item) => (
      <GallerySearchItemTiles
        item={item}
        updateGalleryItemList={updateGalleryItemList}
        key={`${item.uuid}/${item.version}`}
      />
    ));

  return (
    <>
      <GridList>{mapItemsToTiles()}</GridList>
      {lightboxProps && <Lightbox {...lightboxProps} />}
    </>
  );
};

export default GallerySearchResult;
