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
import * as OEQ from "@openequella/rest-api-client";
import { Do } from "fp-ts-contrib/Do";
import * as A from "fp-ts/Array";
import { fold as boolFold } from "fp-ts/boolean";
import * as E from "fp-ts/Either";
import { flow, pipe } from "fp-ts/function";
import * as O from "fp-ts/Option";
import { buildFileAttachmentUrl } from "./AttachmentsModule";
import { getImageMimeTypes } from "./MimeTypesModule";
import { searchItems, SearchOptions } from "./SearchModule";

/**
 * The shape of data for entries to display in a Gallery, primarily consisting of links to access
 * the asset and the associated MIME Type.
 */
export interface GalleryEntry {
  mimeType: string;
  /**
   * The path to a small version of the image - typically the thumbnail with parameter `gallery` as
   * `thumbnail`.
   */
  imagePathSmall: string;
  /**
   * The path to a larger version of the image - typically the thumbnail with parameter `gallery` as
   * `preview`.
   */
  imagePathMedium: string;
  /**
   * The URL to the actual raw asset, good for viewing in the lightbox (or perhaps downloading).
   */
  imagePathFull: string;
}

/**
 * A transformed `OEQ.Search.SearchResultItem` only containing what is needed for a gallery. With
 * specific attention been drawn to the absence of `attachments` but the presence of `mainEntry` and
 * `additionalEntries`.
 */
export interface GallerySearchResultItem
  extends Pick<
    OEQ.Search.SearchResultItem,
    "uuid" | "version" | "name" | "links"
  > {
  /**
   * The primary asset to be shown to represent an item. (Typically the first attachment returned
   * for an item.
   */
  mainEntry: GalleryEntry;
  /**
   * All other assets for an item which would be suitable to show in the target gallery.
   */
  additionalEntries: GalleryEntry[];
}

const hasThumbnail = (thumbnail?: boolean): O.Option<boolean> =>
  pipe(
    thumbnail,
    O.fromNullable,
    O.chain(
      boolFold(
        () => O.none,
        () => O.some(true)
      )
    )
  );

/**
 * Builds a URL for displaying an attachment's thumbnail, optionally at the format as specified by
 * `type`.
 *
 * @param attachment The attachment whose thumbnail is to be displayed
 * @param type Optional alternate formats
 */
const thumbnailLink = (
  attachment: OEQ.Search.Attachment,
  type?: "thumbnail" | "preview"
): string =>
  pipe(
    type,
    O.fromNullable,
    O.fold(
      () => "",
      () => `?gallery=${type}`
    ),
    (suffix: string) => attachment.links.thumbnail.concat(suffix)
  );

/**
 * Transform an item's attachment into a `GalleryEntry`.
 *
 * @param itemUuid The UUID of the item the attachment belongs to
 * @param itemVersion The version of the item the attachment belongs to
 * @param attachment The attachment - should have MIME type specified, and `hasGeneratedThumb`
 */
export const buildGalleryEntry = (
  itemUuid: string,
  itemVersion: number,
  attachment: OEQ.Search.Attachment
): E.Either<string, GalleryEntry> =>
  Do(E.either)
    .do(
      pipe(
        attachment.hasGeneratedThumb,
        hasThumbnail,
        E.fromOption(
          () =>
            `Attachment ${attachment.id} on item ${itemUuid}/${itemVersion} does not have a thumbnail`
        )
      )
    )
    .bind(
      "mimeType",
      pipe(
        attachment.mimeType,
        E.fromNullable("Attachment is missing a defined MIME type")
      )
    )
    .bind("imagePathSmall", E.right(thumbnailLink(attachment, "thumbnail")))
    .bind("imagePathMedium", E.right(thumbnailLink(attachment, "preview")))
    .bind(
      "imagePathFull",
      pipe(
        attachment.filePath,
        E.fromNullable("Attachment is missing its `filePath`"),
        E.map((filePath) =>
          buildFileAttachmentUrl(itemUuid, itemVersion, filePath)
        )
      )
    )
    .done();

/**
 * Creates a `mainEntry` for a `GalleryEntry` based on an item's attachments. This specifically
 * entails using the very first attachment - or returning an error (E.Left) if there isn't one.
 *
 * @param itemUuid The UUID of the item for which this entry is for
 * @param itemVersion The version of the item for which this entry is for
 * @param attachments An item's attachments
 */
const createMainEntry = (
  itemUuid: string,
  itemVersion: number,
  attachments: OEQ.Search.Attachment[]
): E.Either<string, GalleryEntry> =>
  pipe(
    attachments,
    A.head,
    E.fromOption(() => "Missing attachment for main entry"),
    E.chain((attachment) =>
      buildGalleryEntry(itemUuid, itemVersion, attachment)
    )
  );

/**
 * Similar to `createMainEntry` in intent, but operating on the ideally a provided `A.tail` of
 * attachments. Further, is gentler with errors (preferring to return an O.Option) as additional
 * entries are kind of nice to have.
 *
 * @param itemUuid The UUID of the item for which this entry is for
 * @param itemVersion The version of the item for which this entry is for
 * @param attachments Should be the `A.tail` of an item's attachments to potentially make up
 *                    additional entries.
 */
const createAdditionalEntries = (
  itemUuid: string,
  itemVersion: number,
  attachments: O.Option<OEQ.Search.Attachment[]>
): O.Option<GalleryEntry[]> =>
  pipe(
    attachments,
    O.map(
      flow(
        A.map((attachment) =>
          buildGalleryEntry(itemUuid, itemVersion, attachment)
        ),
        A.map(
          // Let's log any issues - just so we're aware of any odd data
          E.fold(
            (l) => {
              console.warn(l);
              return E.left(l);
            },
            (r) => E.right(r)
          )
        ),
        A.filter(E.isRight),
        A.map((e) => e.right)
      )
    )
  );

export type AttachmentFilter = (
  attachments: OEQ.Search.Attachment[]
) => O.Option<OEQ.Search.Attachment[]>;

/**
 * Provides a means to generate a predicate function for filtering attachments. Needed due to the
 * search REST API returning all attachments for an item even if a MIME type filter is specified.
 *
 * @param typeRegex a regular expression to filter the MIME types by
 */
const filterAttachmentsByMimeType = (typeRegex: string): AttachmentFilter => (
  attachments: OEQ.Search.Attachment[]
): O.Option<OEQ.Search.Attachment[]> =>
  pipe(
    attachments,
    A.filter((a) =>
      pipe(
        a.mimeType,
        O.fromNullable,
        O.fold(
          () => false,
          (mt: string) => mt.match(typeRegex) !== null
        )
      )
    ),
    (xs: OEQ.Search.Attachment[]) =>
      pipe(
        xs,
        A.isEmpty,
        boolFold(
          () => O.some(xs),
          () => O.none
        )
      )
  );

/**
 * Expecting a `OEQ.Search.SearchResultItem` returned from a search call, will filter out attachments
 * with the provided `attachmentFilter` and then go about producing a GallerySearchResultItem. If
 * any error is encountered the first will be returned in E.Left.
 *
 * @param attachmentFilter The resultant function from `filterAttachmentsByMimeType`
 */
export const buildGallerySearchResultItem = (
  attachmentFilter: AttachmentFilter
) => ({
  uuid,
  version,
  name,
  links,
  attachments,
}: OEQ.Search.SearchResultItem): E.Either<string, GallerySearchResultItem> =>
  Do(E.either)
    .bind(
      "attachmentsNotEmpty",
      pipe(
        attachments,
        E.fromNullable("No attachments available"),
        E.chain(
          flow(
            attachmentFilter,
            E.fromOption(
              () =>
                `No attachments remain for item ${uuid}/${version} after MIME type filter`
            )
          )
        )
      )
    )
    .bindL("mainEntry", ({ attachmentsNotEmpty }) =>
      createMainEntry(uuid, version, attachmentsNotEmpty)
    )
    .bindL("additionalEntries", ({ attachmentsNotEmpty }) =>
      pipe(
        attachmentsNotEmpty,
        A.tail,
        (attachments) => createAdditionalEntries(uuid, version, attachments),
        O.fold(
          () => [],
          (entries) => entries
        ),
        E.right
      )
    )
    .return(({ mainEntry, additionalEntries }) => ({
      uuid,
      version,
      name,
      links,
      mainEntry,
      additionalEntries,
    }));

/**
 * Perform a search as per `options` and also request filtering to all (institution) known image
 * MIME types. The output ideally used to display a gallery of all available images at an institution
 * with links to their related item.
 *
 * @param options Standard `SearchOptions` to refine the search by
 */
export const imageGallerySearch = async (
  options: SearchOptions
): Promise<OEQ.Search.SearchResult<GallerySearchResultItem>> => {
  const processSearchResultItem = (
    sri: OEQ.Search.SearchResultItem
  ): E.Either<string, GallerySearchResultItem> =>
    pipe(
      sri,
      buildGallerySearchResultItem(filterAttachmentsByMimeType("image")),
      E.fold(
        (error) => {
          const msg = `Failed to create gallery item for item ${sri.uuid}: ${error}`;
          console.error(msg);
          return E.left(msg);
        },
        (item) => E.right(item)
      )
    );

  const searchResults = await searchItems(options, await getImageMimeTypes());
  const items: GallerySearchResultItem[] = pipe(
    searchResults.results,
    A.map(processSearchResultItem),
    A.filter(E.isRight),
    A.map((a) => a.right)
  );

  return { ...searchResults, length: items.length, results: items };
};
