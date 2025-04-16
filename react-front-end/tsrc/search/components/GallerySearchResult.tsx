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
import { ImageList } from "@mui/material";
import { pipe } from "fp-ts/function";
import * as O from "fp-ts/Option";
import * as React from "react";
import { useEffect, useState } from "react";
import {
  buildEmbedCode,
  ShareAttachment,
} from "../../components/embedattachment/EmbeddedAttachmentHelper";
import Lightbox, { LightboxProps } from "../../components/Lightbox";
import { ShareAttachmentDialog } from "../../components/ShareAttachmentDialog";
import {
  GalleryEntry,
  GallerySearchResultItem,
} from "../../modules/GallerySearchModule";
import type { BasicSearchResultItem } from "../../modules/SearchModule";
import {
  buildLightboxNavigationHandler,
  LightboxEntry,
  maybeIncludeItemInLightbox,
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

  const [itemToShare, setItemToShare] = useState<O.Option<ShareAttachment>>(
    O.none,
  );

  // Ensure gallery entries are consistent with gallery mode.
  useEffect(() => {
    setGalleryItems(items);
  }, [items]);

  // Handler for opening the Lightbox
  const lightboxHandler = (
    item: BasicSearchResultItem,
    lightboxEntries: LightboxEntry[],
    { mimeType, directUrl: src, name, id }: GalleryEntry,
  ) => {
    const initialLightboxEntryIndex = lightboxEntries.findIndex(
      (entry) => entry.id === id,
    );

    return setLightboxProps({
      onClose: () => setLightboxProps(undefined),
      open: true,
      config: {
        src,
        title: name,
        mimeType,
        item: maybeIncludeItemInLightbox(item),
        onNext: buildLightboxNavigationHandler(
          lightboxEntries,
          initialLightboxEntryIndex + 1,
          true,
        ),
        onPrevious: buildLightboxNavigationHandler(
          lightboxEntries,
          initialLightboxEntryIndex - 1,
          true,
        ),
      },
    });
  };

  // Function to update the Item list, rebuild the Lightbox entry list and return a lightboxHandler.
  const updateGalleryItemList = (
    newItem: GallerySearchResultItem,
  ): LightboxHandler => {
    const updatedItems = galleryItems.map((i) =>
      i.uuid === newItem.uuid && i.version === newItem.version ? newItem : i,
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
      .flatMap(({ mainEntry, additionalEntries, status, uuid, version }) =>
        [mainEntry, ...additionalEntries].map(
          ({ id, name, mimeType, directUrl }) => ({
            src: directUrl,
            title: name,
            mimeType,
            id,
            item: {
              uuid,
              version,
              status,
            },
          }),
        ),
      );

    return (item: BasicSearchResultItem, entry: GalleryEntry) =>
      lightboxHandler(item, lightboxEntries, entry);
  };

  const shareGalleryItem = ({
    mimeType,
    directUrl: src,
    name,
  }: GalleryEntry) => {
    const embedCode = buildEmbedCode(mimeType, src, name);
    setItemToShare(O.of({ src, embedCode }));
  };

  const mapItemsToTiles = () =>
    items.map((item) => (
      <GallerySearchItemTiles
        item={item}
        updateGalleryItemList={updateGalleryItemList}
        onShare={shareGalleryItem}
        key={`${item.uuid}/${item.version}`}
        enableItemSummaryButton={item.status !== "personal"}
      />
    ));

  const shareAttachmentDialog = pipe(
    itemToShare,
    O.map(({ src, embedCode }) => (
      <ShareAttachmentDialog
        key={src}
        open
        onCloseDialog={() => setItemToShare(O.none)}
        src={src}
        embedCode={embedCode}
      />
    )),
    O.toUndefined,
  );

  return (
    <>
      <ImageList cols={4}>{mapItemsToTiles()}</ImageList>
      {lightboxProps && <Lightbox {...lightboxProps} />}
      {shareAttachmentDialog}
    </>
  );
};

export default GallerySearchResult;
