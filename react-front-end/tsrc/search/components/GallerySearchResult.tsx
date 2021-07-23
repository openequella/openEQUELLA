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
import { useState } from "react";
import Lightbox, { LightboxProps } from "../../components/Lightbox";
import { GallerySearchResultItem } from "../../modules/GallerySearchModule";
import { LightboxEntry } from "../../modules/ViewerModule";
import { GallerySearchItemTiles } from "./GallerySearchItemTiles";

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

  // A list of LightboxEntry which includes all main entries and additional entries.
  const lightboxEntries: LightboxEntry[] = items
    .filter(({ drmStatus }) => {
      if (drmStatus) {
        const { isAuthorised, termsAccepted } = drmStatus;
        return isAuthorised && termsAccepted;
      }
      // If no a DRM Item, keep it.
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

  const mapItemsToTiles = () =>
    items.map((item) => (
      <GallerySearchItemTiles
        item={item}
        lightboxEntries={lightboxEntries}
        setLightboxProps={setLightboxProps}
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
