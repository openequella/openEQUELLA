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
import * as E from "fp-ts/Either";
import { flow, pipe } from "fp-ts/function";
import * as O from "fp-ts/Option";
import { simpleMatchD } from "../util/match";
import {
  ATYPE_FILE,
  ATYPE_KALTURA,
  ATYPE_YOUTUBE,
  buildFileAttachmentUrl,
} from "./AttachmentsModule";
import * as kaltura from "./KalturaModule";
import { CustomMimeTypes, getImageMimeTypes } from "./MimeTypesModule";
import { Classification, listClassifications } from "./SearchFacetsModule";
import { searchItems, SearchOptions } from "./SearchModule";
import * as yt from "./YouTubeModule";

/**
 * The shape of data for entries to display in a Gallery, primarily consisting of links to access
 * the asset and the associated MIME Type.
 */
export interface GalleryEntry {
  /**
   * The ID of the attachment this points to.
   */
  id: string;
  /**
   * The MIME type for the attachment being represented. Note however that it really only matches
   * the MIME type for `imagePathFull` - as thumbnails generated for small and medium seem to mainly
   * be `image/jpeg` regardless of input type.
   */
  mimeType: string;
  /**
   * The name of the attachment this points to.
   */
  name: string;
  /**
   * The path to a small thumbnail - typically the thumbnail with parameter `gallery` as
   * `thumbnail`.
   */
  thumbnailSmall: string;
  /**
   * The path to a large thumbnail - typically the thumbnail with parameter `gallery` as
   * `preview`.
   */
  thumbnailLarge: string;
  /**
   * The URL to the actual raw asset, good for viewing in the lightbox (or perhaps downloading).
   */
  directUrl: string;
}

/**
 * A transformed `OEQ.Search.SearchResultItem` only containing what is needed for a gallery. With
 * specific attention been drawn to the absence of `attachments` but the presence of `mainEntry` and
 * `additionalEntries`.
 */
export interface GallerySearchResultItem
  extends Pick<
    OEQ.Search.SearchResultItem,
    "uuid" | "version" | "name" | "links" | "drmStatus" | "status"
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

const unsupportedAttachmentType = (id: string, attachmentType: string) =>
  E.left(`Attachment ${id} of type "${attachmentType}" is not supported`);

const warnOnLeft = E.mapLeft((left) => {
  console.warn(left);
  return left;
});

/**
 * Checks that the attachment is a type which we can currently support.
 *
 * @param attachment validation candidate
 */
const validateAttachmentType = (
  attachment: OEQ.Search.Attachment,
): E.Either<string, string> =>
  pipe(
    attachment.attachmentType,
    E.fromPredicate(
      (type) => [ATYPE_FILE, ATYPE_KALTURA, ATYPE_YOUTUBE].includes(type),
      (type) =>
        `Attachments of type ${type} are not supported. Attachment ID: ${attachment.id}`,
    ),
  );

/**
 * Based on `attachmentType` validates whether the provided `attachment` meets requirements
 * for thumbnails
 *
 * @param attachment validation candidate
 */
const validateThumbnailRequirements = (
  attachment: OEQ.Search.Attachment,
): E.Either<string, OEQ.Search.Attachment> =>
  pipe(
    attachment,
    E.fromPredicate(
      (a) =>
        a.attachmentType === ATYPE_FILE
          ? !!a.hasGeneratedThumb // result based on what the server tells us
          : true, // For non `file` attachments, we currently do thumbnails ourselves
      (a) => `Attachment ${a.id} does not meet thumbnail requirements.`,
    ),
  );

/**
 * For file based attachments, simply check and use the `mimeType` on the attachment. For supported
 * other types build a custom `openequella/*` MIME type.
 *
 * @param attachment target from which to derive the MIME type
 */
const mimeType = ({
  id,
  mimeType,
  attachmentType,
}: OEQ.Search.Attachment): E.Either<string, string> =>
  pipe(
    attachmentType,
    simpleMatchD(
      [
        [
          ATYPE_FILE,
          () =>
            pipe(
              mimeType,
              E.fromNullable(
                `File attachment ${id} is missing a defined MIME type`,
              ),
            ),
        ],
        [ATYPE_KALTURA, () => E.right(CustomMimeTypes.KALTURA)],
        [ATYPE_YOUTUBE, () => E.right(CustomMimeTypes.YOUTUBE)],
      ],
      () => unsupportedAttachmentType(id, attachmentType),
    ),
  );

/**
 * Builds a URL for displaying an attachment's thumbnail, optionally at the format as specified by
 * `type`.
 *
 * @param attachment The attachment whose thumbnail is to be displayed
 * @param type Optional alternate formats
 */
const thumbnailLink = (
  { attachmentType, id, links }: OEQ.Search.Attachment,
  type: "small" | "large",
): E.Either<string, string> => {
  const isSmall = type === "small";
  const fileThumbnailLink = (basePath: string): string =>
    basePath + "?gallery=" + (isSmall ? "gallery" : "preview");
  const youTubeLink = (videoId: string): string =>
    yt.buildThumbnailUrl(videoId, isSmall ? "default" : "high");

  return [ATYPE_FILE, ATYPE_KALTURA].includes(attachmentType)
    ? E.right(fileThumbnailLink(links.thumbnail))
    : pipe(
        links.externalId,
        E.fromNullable(
          `Expected 'links.externalId' missing for attachment ${id} with type ${attachmentType}`,
        ),
        E.map(youTubeLink),
      );
};

/**
 * Builds a URL for directly accessing the attachment. For file attachments (i.e. those residing in
 * openEQUELLA) it will lead to a direct download/view from the server. For external attachments
 * (e.g. YouTube) it will build a link (URL) using the `links.externalId` to access it via the
 * owning system.
 *
 * @param itemUuid The UUID of the item it belongs to
 * @param itemVersion The version of the item the attachment belongs to
 * @param attachment The attachment to build this URL for
 */
const directUrl = (
  itemUuid: string,
  itemVersion: number,
  attachment: OEQ.Search.Attachment,
): E.Either<string, string> => {
  const { attachmentType, filePath, id, links } = attachment;
  return pipe(
    attachmentType,
    simpleMatchD(
      [
        [
          ATYPE_FILE,
          () =>
            pipe(
              filePath,
              E.fromNullable(`File attachment ${id} is missing a 'filePath'.`),
              E.map((path) =>
                buildFileAttachmentUrl(itemUuid, itemVersion, path),
              ),
            ),
        ],
        // For Kaltura media we don't really have a direct URL, we can only view them via the
        // oEQ view mechanisms - AFAIK. But we add the externalId details onto the end so that
        // the lightbox can use them with the KalturaPlayerEmbed.
        [ATYPE_KALTURA, () => kaltura.buildViewerUrl(attachment)],
        [
          ATYPE_YOUTUBE,
          () =>
            pipe(
              links.externalId,
              E.fromNullable(
                `YouTube attachment ${id} is missing an 'externalId'.`,
              ),
              E.map(yt.buildViewUrl),
            ),
        ],
      ],
      () => unsupportedAttachmentType(id, attachmentType),
    ),
  );
};

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
  attachment: OEQ.Search.Attachment,
): E.Either<string, GalleryEntry> =>
  Do(E.Monad)
    .do(validateAttachmentType(attachment))
    .do(validateThumbnailRequirements(attachment))
    .bind("id", E.right(attachment.id))
    .bind("mimeType", mimeType(attachment))
    .bind("name", E.right(attachment.description ?? attachment.id))
    .bind("thumbnailSmall", thumbnailLink(attachment, "small"))
    .bind("thumbnailLarge", thumbnailLink(attachment, "large"))
    .bind("directUrl", directUrl(itemUuid, itemVersion, attachment))
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
  attachments: OEQ.Search.Attachment[],
): E.Either<string, GalleryEntry> =>
  pipe(
    attachments,
    A.head,
    E.fromOption(() => "Missing attachment for main entry"),
    E.chain((attachment) =>
      buildGalleryEntry(itemUuid, itemVersion, attachment),
    ),
  );

const attachmentToGalleryEntry =
  (itemUuid: string, itemVersion: number) =>
  (attachment: OEQ.Search.Attachment): E.Either<string, GalleryEntry> =>
    pipe(
      buildGalleryEntry(itemUuid, itemVersion, attachment),
      // Let's log any issues - just so we're aware of any odd data
      warnOnLeft,
    ) as E.Either<string, GalleryEntry>;

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
  attachments: O.Option<OEQ.Search.Attachment[]>,
): O.Option<GalleryEntry[]> =>
  pipe(
    attachments,
    O.map(
      flow(
        A.map(attachmentToGalleryEntry(itemUuid, itemVersion)),
        A.filter(E.isRight),
        A.map((e) => e.right),
      ),
    ),
  );

/**
 * Type for functions used for filtering attachments.
 */
export type AttachmentFilter = (
  attachments: OEQ.Search.Attachment[],
) => O.Option<OEQ.Search.Attachment[]>;

/**
 * Type for functions acting as predicates over attachments.
 */
type AttachmentPredicate = (attachment: OEQ.Search.Attachment) => boolean;

/**
 * Creates an attachment predicate function which will predicate based on matching an attachment's
 * MIME type against the supplied `typeRegex`.
 *
 * @param typeRegex A regular expression to check against an attachment's MIME type/
 */
const attachmentMimeTypePredicate =
  (typeRegex: string): AttachmentPredicate =>
  (attachment: OEQ.Search.Attachment) =>
    pipe(
      attachment.mimeType,
      O.fromNullable,
      O.fold(
        () => false,
        (mt: string) => mt.match(typeRegex) !== null,
      ),
    );

/**
 * Creates an attachment filter using the supplied `AttachmentPredicate`. The resulting filter
 * will return all attachments which match the predicate (`O.Some`) otherwise it will return none
 * (`O.None`).
 *
 * @param predicate The predicate to specify which attachments to keep.
 */
const filterAttachments =
  (predicate: AttachmentPredicate): AttachmentFilter =>
  (attachments: OEQ.Search.Attachment[]): O.Option<OEQ.Search.Attachment[]> =>
    pipe(
      attachments,
      A.filter(predicate),
      O.fromPredicate<OEQ.Search.Attachment[]>(A.isNonEmpty),
    );

/**
 * Provides a means to generate a predicate function for filtering attachments. Needed due to the
 * search REST API returning all attachments for an item even if a MIME type filter is specified.
 *
 * @param typeRegex a regular expression to filter the MIME types by
 */
const filterAttachmentsByMimeType =
  (typeRegex: string): AttachmentFilter =>
  (attachments: OEQ.Search.Attachment[]): O.Option<OEQ.Search.Attachment[]> =>
    pipe(
      attachments,
      filterAttachments(attachmentMimeTypePredicate(typeRegex)),
    );

/**
 * Filters a list of attachments down to those which are either `file` attachments with a 'video'
 * mimeType; or are of a suitable attachmentType - such as `custom/youtube`.
 *
 * @param attachments the attachments to be filtered
 */
const filterAttachmentsByVideo: AttachmentFilter = (
  attachments: OEQ.Search.Attachment[],
): O.Option<OEQ.Search.Attachment[]> => {
  const videoMimeTypePredicate: AttachmentPredicate =
    attachmentMimeTypePredicate("video");
  const attachmentTypePredicate: AttachmentPredicate = (a) =>
    [ATYPE_KALTURA, ATYPE_YOUTUBE].includes(a.attachmentType);

  // The above predicates combined into one
  const combinedPredicate: AttachmentPredicate = (a) =>
    [videoMimeTypePredicate, attachmentTypePredicate].some((predicate) =>
      predicate(a),
    );

  // Do the filtering
  return pipe(attachments, filterAttachments(combinedPredicate));
};

/**
 * Expecting a `OEQ.Search.SearchResultItem` returned from a search call, will filter out attachments
 * with the provided `attachmentFilter` and then go about producing a GallerySearchResultItem. If
 * any error is encountered the first will be returned in E.Left.
 *
 * @param attachmentFilter The resultant function from `filterAttachmentsByMimeType`
 */
export const buildGallerySearchResultItem =
  (attachmentFilter: AttachmentFilter) =>
  ({
    uuid,
    version,
    name,
    links,
    attachments,
    drmStatus,
    status,
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
                  `No attachments remain for item ${uuid}/${version} after MIME type filter`,
              ),
            ),
          ),
        ),
      )
      .bindL("mainEntry", ({ attachmentsNotEmpty }) =>
        createMainEntry(uuid, version, attachmentsNotEmpty),
      )
      .bindL("additionalEntries", ({ attachmentsNotEmpty }) =>
        pipe(
          attachmentsNotEmpty,
          A.tail,
          (attachments) => createAdditionalEntries(uuid, version, attachments),
          O.getOrElse(() => [] as GalleryEntry[]),
          E.right,
        ),
      )
      .return(({ mainEntry, additionalEntries }) => ({
        uuid,
        version,
        name,
        links,
        mainEntry,
        additionalEntries,
        drmStatus,
        status,
      }));

/**
 * Undertakes an `searchItems` based on the supplied `options` filtering out all attachments with
 * `attachmentFilter`. And most importantly, converts the output from `itemSearch` to
 * a collection of `GallerySearchResultItem`s.
 *
 * @param options Search options to be passed to `searchItems`.
 * @param attachmentFilter The filter to filter all items attachment's by.
 */
const gallerySearch = async (
  options: SearchOptions,
  attachmentFilter: AttachmentFilter,
): Promise<OEQ.Search.SearchResult<GallerySearchResultItem>> => {
  const processSearchResultItem = (
    sri: OEQ.Search.SearchResultItem,
  ): E.Either<string, GallerySearchResultItem> =>
    pipe(
      sri,
      buildGallerySearchResultItem(attachmentFilter),
      E.mapLeft((error) => {
        const msg = `Failed to create gallery item for item ${sri.uuid}: ${error}`;
        console.error(msg);
        return msg;
      }),
    );

  const searchResults = await searchItems(options);
  const items: GallerySearchResultItem[] = pipe(
    searchResults.results,
    A.map(processSearchResultItem),
    A.filter(E.isRight),
    A.map((a) => a.right),
  );

  return { ...searchResults, length: items.length, results: items };
};

/**
 * Perform a search as per `options` and also request filtering to all (institution) known image
 * MIME types. The output ideally used to display a gallery of all available images at an institution
 * with links to their related item.
 *
 * @param options Standard `SearchOptions` to refine the search by
 */
export const imageGallerySearch = async (
  options: SearchOptions,
): Promise<OEQ.Search.SearchResult<GallerySearchResultItem>> =>
  gallerySearch(
    { ...options, mimeTypes: await getImageMimeTypes() },
    filterAttachmentsByMimeType("image"),
  );

const videoGalleryMusts: OEQ.Search.Must[] = [["videothumb", ["true"]]];
/**
 * Perform a search as per `options` and also request filtering to any items which potentially are
 * have video attachments. The output ideally used to display a gallery of all available videos at
 * an institution with links to their related item.
 *
 * @param options Standard `SearchOptions` to refine the search by
 */
export const videoGallerySearch = async (
  options: SearchOptions,
): Promise<OEQ.Search.SearchResult<GallerySearchResultItem>> =>
  gallerySearch(
    { ...options, musts: videoGalleryMusts, mimeTypes: undefined },
    filterAttachmentsByVideo,
  );

/**
 * Perform a facet search as per `options` and also request facets to only be generated for items
 * which potentially have image attachments. The output ideally used to alongside the search
 * results for `imageGallerySearch`.
 *
 * @param options Standard `SearchOptions` to refine the faceting by
 */
export const listImageGalleryClassifications = async (
  options: SearchOptions,
): Promise<Classification[]> =>
  listClassifications({ ...options, mimeTypes: await getImageMimeTypes() });

/**
 * Perform a facet search as per `options` and also request facets to only be generated for items
 * which potentially have video attachments. The output ideally used to alongside the search
 * results for `videoGallerySearch`.
 *
 * @param options Standard `SearchOptions` to refine the faceting by
 */
export const listVideoGalleryClassifications = async (
  options: SearchOptions,
): Promise<Classification[]> =>
  listClassifications({
    ...options,
    musts: videoGalleryMusts,
    mimeTypes: undefined,
  });
