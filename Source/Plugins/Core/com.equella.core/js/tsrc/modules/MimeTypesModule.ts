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
import { memoize } from "lodash";
import { API_BASE_URL } from "../AppConfig";

export const OEQ_MIMETYPE_TYPE = "openequella";
/**
 * A collection of custom internal MIME types for openEQUELLA
 */
export const CustomMimeTypes = {
  YOUTUBE: `${OEQ_MIMETYPE_TYPE}/youtube`,
};

export const getMIMETypesFromServer = (): Promise<
  OEQ.MimeType.MimeTypeEntry[]
> => OEQ.MimeType.listMimeTypes(API_BASE_URL);

/**
 * Retrieve the Viewer Configuration for the specified MIME type from the server - or from cache.
 * Note this function is cached (memoized) and so any server side changes will not take effect
 * until the app is reloaded.
 *
 * @param mimeType the MIME type to get the configuration for
 */
export const getMimeTypeViewerConfiguration: (
  mimeType: string
) => Promise<OEQ.MimeType.MimeTypeViewerConfiguration> = memoize(
  async (mimeType: string): Promise<OEQ.MimeType.MimeTypeViewerConfiguration> =>
    await OEQ.MimeType.getViewersForMimeType(API_BASE_URL, mimeType)
);

/**
 * Retrieve the default viewer details for the specified MIME type from the server - or from cache.
 * This function relies on `getMimeTypeViewerConfiguration` which is cached (memoized) and so any
 * server side changes will not take effect until the app is reloaded.
 *
 * @param mimeType the MIME type to get default viewer details for
 */
export const getMimeTypeDefaultViewerDetails = async (
  mimeType: string
): Promise<OEQ.MimeType.MimeTypeViewerDetail> => {
  const cfg = await getMimeTypeViewerConfiguration(mimeType);
  const viewerDetails = cfg.viewers.find(
    (v) => v.viewerId === cfg.defaultViewer
  );
  if (!viewerDetails) {
    throw new ReferenceError(
      `Missing viewer details for default viewer with id: "${cfg.defaultViewer}"`
    );
  }

  return viewerDetails;
};

/**
 * Produces a predicate function to filter `MimeTypeEntry` collections based on MIME type types -
 * i.e. the value before the first slash (e.g. `image` in `image/png`).
 *
 * @param type the MIME `type` to filter on - e.g. image, video, application, etc.
 */
const mimeTypeEntryTypePredicate = (type: string) => (
  mte: OEQ.MimeType.MimeTypeEntry
): boolean => {
  try {
    return splitMimeType(mte.mimeType)[0] === type;
  } catch (e) {
    if (e instanceof TypeError) {
      return false;
    } else {
      throw e;
    }
  }
};

/**
 * Provides a list of all the `image/` MIME types configured on the server. Results are memoized.
 */
export const getImageMimeTypes: () => Promise<string[]> = memoize(
  async (): Promise<string[]> =>
    (await getMIMETypesFromServer())
      .filter(mimeTypeEntryTypePredicate("image"))
      .map((mte) => mte.mimeType)
);

/**
 * Given a MIME Type of the form `<type>/<sub-type>`, validate correct form and then return
 * `type` and `sub-type` in a tuple. `sub-type` will actually also include any attributes, etc. as
 * that's all we need for now (i.e. we're not so interested in sub-types yet).
 *
 * @param mimeType a MIME type of the form `<type>/<sub-type>`
 */
export const splitMimeType = (mimeType: string): [string, string] => {
  // Regex break down:
  // ^\w+          Starting with 1 or more word characters
  // \/            Followed by a forward slash
  // [+.=; \w-]+$  Ending with 1 or more of the allowed characters: + . = ; <space> - or word characters
  // /i            And all done with case insensitivity
  const validMimeTypeRegex = /^\w+\/[+.=; \w-]+$/i;
  if (!validMimeTypeRegex.test(mimeType)) {
    throw new TypeError(
      `Provided string [${mimeType}] is NOT a validly formatted MIME type.`
    );
  }

  return mimeType.split("/", 2) as [string, string];
};

/**
 * Provides a quick check to see if the `mimeType` is a MIME type which can typically be
 * played by the built in browser audio player.
 *
 * @param mimeType A MIME type to be checked
 */
export const isBrowserSupportedAudio = (mimeType: string): boolean =>
  [
    // Standard
    "audio/mpeg",
    "audio/ogg",
    "audio/wav",
    // oEQ standard types
    "audio/x-mp3",
  ].some((supported) => mimeType.startsWith(supported));

/**
 * Provides a quick check to see if the `mimeType` is a MIME type which can typically be
 * played by the built in browser video player.
 *
 * @param mimeType A MIME type to be checked
 */
export const isBrowserSupportedVideo = (mimeType: string): boolean =>
  ["video/mp4", "video/webm", "video/ogg"].some((supported) =>
    mimeType.startsWith(supported)
  );
